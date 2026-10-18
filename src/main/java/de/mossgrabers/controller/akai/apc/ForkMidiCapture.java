// Fork addition (Iuri) - not part of upstream DrivenByMoss.
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.akai.apc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mossgrabers.bitwig.framework.daw.ForkClipWriter;
import de.mossgrabers.controller.akai.apc.controller.APCControlSurface;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.ITransport;
import de.mossgrabers.framework.daw.data.ISlot;
import de.mossgrabers.framework.daw.data.ITrack;
import de.mossgrabers.framework.daw.data.bank.ITrackBank;


/**
 * Retroactive MIDI capture: remembers the notes played on the tracks of the session page and
 * writes the most recent ones into a new clip.
 *
 * With a running transport the clip ends at the bar which is the closest to the moment of the
 * capture. With a stopped transport the clip starts with the first note of the captured notes.
 * The length of the clip is the 'New Clip Length' setting.
 *
 * @author Iuri
 */
public class ForkMidiCapture
{
    private static final long   NANOS_PER_SECOND = 1_000_000_000L;
    /** Notes older than this are dropped. */
    private static final long   MAX_AGE          = 10 * 60 * NANOS_PER_SECOND;
    /** Single taps are only delayed if there were notes in this time. */
    private static final long   RECENT           = 60 * NANOS_PER_SECOND;
    private static final int    MAX_NOTES        = 4096;
    /** Notes which start up to this many beats before the clip are moved to its start. */
    private static final double EARLY_TOLERANCE  = 1.0 / 8.0;
    private static final int    WRITE_ATTEMPTS   = 40;
    private static final int    WRITE_RETRY_MS   = 50;

    private static class CapturedNote
    {
        final int  pitch;
        final int  velocity;
        final long on;
        long       off = -1;


        CapturedNote (final int pitch, final int velocity, final long on)
        {
            this.pitch = pitch;
            this.velocity = velocity;
            this.on = on;
        }
    }

    private static class NoteToWrite
    {
        final double start;
        final int    pitch;
        final int    velocity;
        final double duration;


        NoteToWrite (final double start, final int pitch, final int velocity, final double duration)
        {
            this.start = start;
            this.pitch = pitch;
            this.velocity = velocity;
            this.duration = duration;
        }
    }

    private final IModel                            model;
    private final APCControlSurface                 surface;
    private final ForkClipWriter                    writer;
    private final Map<Integer, List<CapturedNote>>  notes     = new HashMap<> ();
    private final Map<Integer, CapturedNote []>     openNotes = new HashMap<> ();


    /**
     * Constructor.
     *
     * @param model The model
     * @param surface The surface
     * @param writer The clip writer
     */
    public ForkMidiCapture (final IModel model, final APCControlSurface surface, final ForkClipWriter writer)
    {
        this.model = model;
        this.surface = surface;
        this.writer = writer;

        final ITrackBank trackBank = model.getTrackBank ();
        trackBank.addNoteObserver ( (trackIndex, note, velocity) -> this.handleNote (trackBank.getItem (trackIndex), note, velocity));
    }


    /**
     * Check if notes were played on the track recently.
     *
     * @param track The track
     * @return True if there are notes which could be captured
     */
    public boolean hasRecentNotes (final ITrack track)
    {
        if (!track.doesExist ())
            return false;
        final List<CapturedNote> list = this.notes.get (Integer.valueOf (track.getPosition ()));
        return list != null && !list.isEmpty () && System.nanoTime () - list.get (list.size () - 1).on < RECENT;
    }


    /**
     * Capture the recent notes of the track into a new clip in the given (empty) slot and start
     * it.
     *
     * @param track The track
     * @param slot The slot
     */
    public void capture (final ITrack track, final ISlot slot)
    {
        final int trackPosition = track.getPosition ();
        final List<CapturedNote> list = this.notes.get (Integer.valueOf (trackPosition));
        if (list == null || list.isEmpty ())
        {
            this.surface.getDisplay ().notify ("Nothing to capture");
            return;
        }

        final ITransport transport = this.model.getTransport ();
        final int quartersPerMeasure = transport.getQuartersPerMeasure ();
        final int length = this.surface.getConfiguration ().getNewClipLenghthInBeats (quartersPerMeasure);
        final double beatsPerNano = transport.getTempo () / 60.0 / NANOS_PER_SECOND;
        final long now = System.nanoTime ();

        // The time of the clip start in nanoseconds
        final double clipStart;
        if (transport.isPlaying ())
        {
            final double nowBeats = transport.getPosition ();
            final double end = Math.round (nowBeats / quartersPerMeasure) * (double) quartersPerMeasure;
            clipStart = now - (nowBeats - (end - length)) / beatsPerNano;
        }
        else
        {
            final long lastOn = list.get (list.size () - 1).on;
            final double windowStart = lastOn - length / beatsPerNano;
            long first = lastOn;
            for (final CapturedNote note: list)
            {
                if (note.on >= windowStart && note.on < first)
                    first = note.on;
            }
            clipStart = first;
        }

        final List<NoteToWrite> toWrite = new ArrayList<> ();
        for (final CapturedNote note: list)
        {
            double start = (note.on - clipStart) * beatsPerNano;
            if (start < -EARLY_TOLERANCE || start >= length - EARLY_TOLERANCE)
                continue;
            start = Math.max (0, start);
            final long off = note.off < 0 ? now : note.off;
            final double duration = Math.min ((off - note.on) * beatsPerNano, length - start);
            toWrite.add (new NoteToWrite (start, note.pitch, note.velocity, Math.max (ForkClipWriter.STEP_LENGTH, duration)));
        }

        if (toWrite.isEmpty ())
        {
            this.surface.getDisplay ().notify ("Nothing to capture");
            return;
        }

        track.createClip (slot.getIndex (), length);
        slot.select ();
        this.writeWhenReady (trackPosition, slot, toWrite, 0);
    }


    /**
     * Wait until the cursor clip points to the new clip, then write the notes and start the clip.
     *
     * @param trackPosition The position of the track
     * @param slot The slot
     * @param toWrite The notes to write
     * @param attempt The number of the attempt
     */
    private void writeWhenReady (final int trackPosition, final ISlot slot, final List<NoteToWrite> toWrite, final int attempt)
    {
        if (!this.writer.isClip (trackPosition, slot.getPosition ()))
        {
            if (attempt < WRITE_ATTEMPTS)
                this.surface.scheduleTask ( () -> this.writeWhenReady (trackPosition, slot, toWrite, attempt + 1), WRITE_RETRY_MS);
            else
                this.surface.getDisplay ().notify ("Capture failed: the new clip could not be selected");
            return;
        }

        for (final NoteToWrite note: toWrite)
            this.writer.setNote (note.start, note.pitch, note.velocity, note.duration);
        this.writer.finish ();

        slot.launch (true, false);
        slot.launch (false, false);
        this.surface.getDisplay ().notify ("Captured " + toWrite.size () + " notes");
    }


    /**
     * Remember a played note.
     *
     * @param track The track on which the note was played
     * @param pitch The MIDI note
     * @param velocity The velocity, 0 for note off
     */
    private void handleNote (final ITrack track, final int pitch, final int velocity)
    {
        if (!track.doesExist () || pitch < 0 || pitch > 127)
            return;

        final Integer position = Integer.valueOf (track.getPosition ());
        final CapturedNote [] open = this.openNotes.computeIfAbsent (position, k -> new CapturedNote [128]);
        final long now = System.nanoTime ();

        if (velocity == 0)
        {
            if (open[pitch] != null)
            {
                open[pitch].off = now;
                open[pitch] = null;
            }
            return;
        }

        if (open[pitch] != null)
            return;

        final CapturedNote note = new CapturedNote (pitch, velocity, now);
        open[pitch] = note;
        final List<CapturedNote> list = this.notes.computeIfAbsent (position, k -> new ArrayList<> ());
        list.add (note);
        while (!list.isEmpty () && (list.size () > MAX_NOTES || now - list.get (0).on > MAX_AGE))
            list.remove (0);
    }
}

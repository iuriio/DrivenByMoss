// Fork addition (Iuri) - not part of upstream DrivenByMoss.
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.bitwig.framework.daw;

import com.bitwig.extension.controller.api.Clip;
import com.bitwig.extension.controller.api.CursorTrack;

import de.mossgrabers.framework.daw.IHost;


/**
 * Writes notes at arbitrary positions into the clip which is selected in the clip launcher. Uses
 * its own cursor clip with a fine step resolution and checks that it points to the expected slot
 * before writing.
 *
 * @author Iuri
 */
public class ForkClipWriter
{
    /** The length of one step in beats. */
    public static final double STEP_LENGTH = 1.0 / 32.0;

    /** The number of steps of the cursor clip grid (32 beats). */
    private static final int   PAGE_STEPS  = 1024;

    private final CursorTrack  cursorTrack;
    private final Clip         clip;
    private int                scrollStep;


    /**
     * Constructor. Must be called during the initialization of the extension.
     *
     * @param host The host, must be the Bitwig host implementation
     */
    public ForkClipWriter (final IHost host)
    {
        // The cursor clip of a cursor track follows the slot selection made via the API
        this.cursorTrack = ((HostImpl) host).getControllerHost ().createCursorTrack ("ForkCaptureCursorTrack", "Fork capture", 0, 0, true);
        this.cursorTrack.position ().markInterested ();
        this.clip = this.cursorTrack.createLauncherCursorClip (PAGE_STEPS, 128);
        this.clip.exists ().markInterested ();
        this.clip.clipLauncherSlot ().sceneIndex ().markInterested ();
        this.clip.setStepSize (STEP_LENGTH);
        this.clip.scrollToKey (0);
        this.clip.scrollToStep (0);
    }


    /**
     * Check if the cursor clip points to the clip in the given slot.
     *
     * @param trackPosition The position of the track
     * @param sceneIndex The index of the scene (absolute)
     * @return True if the clip exists and is in that slot
     */
    public boolean isClip (final int trackPosition, final int sceneIndex)
    {
        return this.clip.exists ().get () && this.cursorTrack.position ().get () == trackPosition && this.clip.clipLauncherSlot ().sceneIndex ().get () == sceneIndex;
    }


    /**
     * Describe where the cursor clip points to, for error messages.
     *
     * @return The description
     */
    public String describe ()
    {
        return (this.clip.exists ().get () ? "clip" : "no clip") + " at track " + this.cursorTrack.position ().get () + " scene " + this.clip.clipLauncherSlot ().sceneIndex ().get ();
    }


    /**
     * Add a note to the clip.
     *
     * @param start The start of the note in beats, relative to the clip start
     * @param pitch The MIDI note
     * @param velocity The velocity 1-127
     * @param duration The length of the note in beats
     */
    public void setNote (final double start, final int pitch, final int velocity, final double duration)
    {
        final int step = (int) Math.round (start / STEP_LENGTH);
        final int page = step / PAGE_STEPS * PAGE_STEPS;
        if (page != this.scrollStep)
        {
            this.clip.scrollToStep (page);
            this.scrollStep = page;
        }
        this.clip.setStep (0, step - page, pitch, Math.max (1, Math.min (127, velocity)), duration);
    }


    /**
     * Scroll back to the start after all notes are written.
     */
    public void finish ()
    {
        if (this.scrollStep == 0)
            return;
        this.clip.scrollToStep (0);
        this.scrollStep = 0;
    }
}

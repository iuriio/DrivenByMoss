// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.akai.apc.controller;

import de.mossgrabers.framework.controller.color.ColorManager;
import de.mossgrabers.framework.controller.grid.PadGridImpl;
import de.mossgrabers.framework.daw.midi.IMidiOutput;


/**
 * Implementation of the APC grid of pads.
 *
 * @author Jürgen Moßgraber
 */
public class APCPadGrid extends PadGridImpl
{
    private final boolean isMkII;
    // [fork] Pads which pulse with the rates for groups
    private final boolean [] forkPulse = new boolean [NUM_NOTES];


    /**
     * Constructor.
     *
     * @param colorManager The color manager for accessing specific colors to use
     * @param output The MIDI output which can address the pad states
     * @param isMkII True if it is the MkII
     */
    public APCPadGrid (final ColorManager colorManager, final IMidiOutput output, final boolean isMkII)
    {
        super (colorManager, output, 5, 8, 36);

        this.isMkII = isMkII;
    }


    /** {@inheritDoc} */
    @Override
    protected void sendBlinkState (final int channel, final int note, final int blinkColor, final boolean fast)
    {
        // [fork] Group pulse: 1/4 (channel 9) for fast, 1/2 (channel 10) for slow
        if (this.isMkII && this.forkPulse[note + this.startNote])
            this.output.sendNoteEx (fast ? 9 : 10, note, blinkColor);
        else if (this.isMkII)
            this.output.sendNoteEx (fast ? 12 : 10, note, blinkColor);
        else
            this.output.sendNoteEx (channel, note, blinkColor);
    }


    /**
     * [fork] Let a pad pulse softly with the color (MkII only).
     *
     * @param x The x position of the pad
     * @param y The y position of the pad
     * @param color The color index
     * @param fast Pulse with 1/4 if true, otherwise with 1/2
     */
    public void lightPulse (final int x, final int y, final int color, final boolean fast)
    {
        final int note = (this.rows - 1) * this.columns + this.startNote + x - this.columns * y;
        this.setLight (note, color, color, fast);
        this.forkPulse[note] = true;
    }


    /** [fork] Reset the group pulse when a pad is set. */
    @Override
    protected void setLight (final int note, final int color, final int blinkColor, final boolean fast)
    {
        this.forkPulse[note] = false;
        super.setLight (note, color, blinkColor, fast);
    }


    /** {@inheritDoc} */
    @Override
    public int translateToGrid (final int note)
    {
        // Note: This is not implemented for MkI since it is not used

        return note + 36;
    }


    /** {@inheritDoc} */
    @Override
    public int [] translateToController (final int note)
    {
        final int n = note - 36;

        if (this.isMkII)
            return new int []
            {
                0,
                n
            };

        return new int []
        {
            n % 8,
            0x39 - n / 8
        };
    }
}
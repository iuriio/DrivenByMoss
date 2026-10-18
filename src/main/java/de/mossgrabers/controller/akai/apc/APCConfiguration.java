// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2026
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.akai.apc;

import java.util.List;

import de.mossgrabers.framework.configuration.AbstractConfiguration;
import de.mossgrabers.framework.configuration.IEnumSetting;
import de.mossgrabers.framework.configuration.ISettingsUI;
import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.midi.ArpeggiatorMode;


/**
 * The configuration settings for APC.
 *
 * @author Jürgen Moßgraber
 */
public class APCConfiguration extends AbstractConfiguration
{
    private final boolean         isMkII;

    // [fork] Device knob setting
    /** Setting ID for the device knob behaviour. */
    public static final Integer   DEVICE_KNOBS_FIXED          = Integer.valueOf (1000);
    private static final String [] DEVICE_KNOBS_OPTIONS       =
    {
        "Fixed (map with Learn)",
        "Follow selected device"
    };
    private boolean               deviceKnobsFixed            = true;


    /**
     * Constructor.
     *
     * @param host The DAW host
     * @param valueChanger The value changer
     * @param arpeggiatorModes The available arpeggiator modes
     * @param isMkII True if is MkII
     */
    public APCConfiguration (final IHost host, final IValueChanger valueChanger, final List<ArpeggiatorMode> arpeggiatorModes, final boolean isMkII)
    {
        super (host, valueChanger, arpeggiatorModes);

        this.isMkII = isMkII;
    }


    /** {@inheritDoc} */
    @Override
    public void init (final ISettingsUI globalSettings, final ISettingsUI documentSettings)
    {
        // [fork] Session only: no Scale and Note Repeat settings

        // ----------------------------------------------------------------
        // Session

        this.activateSelectClipOnLaunchSetting (globalSettings);
        this.activateDrawRecordStripeSetting (globalSettings);
        this.activateActionForRecArmedPad (globalSettings);

        // ----------------------------------------------------------------
        // [fork] Device knobs

        final IEnumSetting deviceKnobsSetting = globalSettings.getEnumSetting ("Device knobs", "Device Control", DEVICE_KNOBS_OPTIONS, DEVICE_KNOBS_OPTIONS[0]);
        deviceKnobsSetting.addValueObserver (value -> {
            this.deviceKnobsFixed = DEVICE_KNOBS_OPTIONS[0].equals (value);
            this.notifyObservers (DEVICE_KNOBS_FIXED);
        });
        this.isSettingActive.add (DEVICE_KNOBS_FIXED);

        // ----------------------------------------------------------------
        // Play and Sequence

        // [fork] Session only: only the quantize amount (used by the Quantize button) is kept
        this.activateQuantizeAmountSetting (globalSettings);

        // ----------------------------------------------------------------
        // Transport

        this.activateBehaviourOnPauseSetting (globalSettings);

        // ----------------------------------------------------------------
        // Workflow

        this.activateExcludeDeactivatedItemsSetting (globalSettings);
        this.activateNewClipLengthSetting (globalSettings);
        this.activateFootswitchSetting (globalSettings, 0, "Footswitch 1");
        if (!this.isMkII)
            this.activateFootswitchSetting (globalSettings, 1, "Footswitch 2");
    }


    /**
     * [fork] Should the device knobs be fixed user controls?
     *
     * @return True if fixed, false if they follow the remote controls of the selected device
     */
    public boolean areDeviceKnobsFixed ()
    {
        return this.deviceKnobsFixed;
    }
}

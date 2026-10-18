// Fork addition (Iuri) - not part of upstream DrivenByMoss.
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.bitwig.framework.daw;

import de.mossgrabers.framework.daw.IHost;


/**
 * Access to Bitwig host functions which are not part of the IHost abstraction.
 *
 * @author Iuri
 */
public final class ForkHostHelper
{
    private ForkHostHelper ()
    {
        // Intentionally empty
    }


    /**
     * Ask Bitwig to call flush() soon, e.g. to update blinking LEDs.
     *
     * @param host The host, must be the Bitwig host implementation
     */
    public static void requestFlush (final IHost host)
    {
        ((HostImpl) host).getControllerHost ().requestFlush ();
    }
}

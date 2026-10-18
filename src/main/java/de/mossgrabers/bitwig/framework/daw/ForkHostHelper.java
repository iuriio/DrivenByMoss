// Fork addition (Iuri) - not part of upstream DrivenByMoss.
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.bitwig.framework.daw;

import de.mossgrabers.framework.daw.IHost;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


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


    /**
     * Get the name of the build, which is written to fork-build.txt by the GitHub build.
     *
     * @return The commit and date of the build or "local build"
     */
    public static String getBuildName ()
    {
        try (final InputStream in = ForkHostHelper.class.getResourceAsStream ("/fork-build.txt"))
        {
            if (in != null)
                return new String (in.readAllBytes (), StandardCharsets.UTF_8).trim ();
        }
        catch (final IOException ex)
        {
            // Fall through
        }
        return "local build";
    }
}

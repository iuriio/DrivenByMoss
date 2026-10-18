// Fork addition (Iuri) - not part of upstream DrivenByMoss.
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.bitwig.framework.daw;

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

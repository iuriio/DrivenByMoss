// Fork addition (Iuri) - not part of upstream DrivenByMoss.
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.bitwig.framework.daw;

import com.bitwig.extension.controller.api.Parameter;
import com.bitwig.extension.controller.api.UserControlBank;

import de.mossgrabers.bitwig.framework.daw.data.ParameterImpl;
import de.mossgrabers.framework.controller.valuechanger.IValueChanger;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.parameter.IParameter;


/**
 * Creates Bitwig "user controls": fixed controls which are not bound to any device. They show up
 * in the Mappings browser and are assigned with right click on a parameter > Learn Controller
 * Assignment. The assignments are stored in the project. Must be called during the extension
 * initialization.
 *
 * @author Iuri
 */
public final class UserControlsFactory
{
    private UserControlsFactory ()
    {
        // Intentionally empty
    }


    /**
     * Create user controls.
     *
     * @param host The host, must be the Bitwig host implementation
     * @param valueChanger The value changer
     * @param count The number of controls to create
     * @param labelPrefix The prefix for the label of each control, the index (1-based) is added
     * @return The parameters wrapping the user controls
     */
    public static IParameter [] create (final IHost host, final IValueChanger valueChanger, final int count, final String labelPrefix)
    {
        final UserControlBank bank = ((HostImpl) host).getControllerHost ().createUserControls (count);
        final IParameter [] parameters = new IParameter [count];
        for (int i = 0; i < count; i++)
        {
            final Parameter control = bank.getControl (i);
            control.setLabel (labelPrefix + (i + 1));
            final ParameterImpl parameter = new ParameterImpl (valueChanger, control, i, true);
            parameter.enableObservers (true);
            parameters[i] = parameter;
        }
        return parameters;
    }
}

/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.pid;

import com.google.common.base.Preconditions;
import model.MacroII;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> A static method that takes a MacroII object and a class of controller and builds  it
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-12-21
 * @see
 */
public class ControllerFactory
{


    private ControllerFactory() {
    }

    /**
     * The factory creates the controller of specified type by drawing parameters at random from the model class
     * @param controllerType the type of controller you want
     * @return the controller object you get
     */
    public static <C extends Controller> C buildController(@Nonnull Class<C> controllerType, @Nonnull MacroII model)
    {
        Preconditions.checkArgument(controllerType != null && model != null,"Don't pass nulls!");

        //if it's a PID controller
        if(controllerType.equals(PIDController.class))
        {
            return (C) new PIDController(
                    model.drawProportionalGain(), model.drawIntegrativeGain(), model.drawDerivativeGain(),
            model.drawPIDSpeed(), model.random);
        }
        else if(controllerType.equals(CascadePIDController.class))
        {
            return (C) new CascadePIDController( model.drawProportionalGain(), model.drawIntegrativeGain(), model.drawDerivativeGain(),
                    model.drawProportionalGain(), 0, 0,model.getRandom());
        }
        else if(controllerType.equals(FlowAndStockController.class))
        {
            return (C) new FlowAndStockController( model.drawProportionalGain(), model.drawIntegrativeGain(), model.drawDerivativeGain(),
                    model.drawProportionalGain(), model.drawIntegrativeGain(), model.drawDerivativeGain()
                    ,model.getRandom());
        }
        else
            throw new IllegalArgumentException("The Controller factory doesn't recognize: " + controllerType);

    }

}

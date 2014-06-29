/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.pid;

/**
 * <h4>Description</h4>
 * <p/>  This is a consequence of bad design: children stay in school.
 * <p/> Unfortunately different controllers need different sets of input/output. PID need 1 target 1 input, Cascade need
 * 1 target 2 inputs, Flow/Stock need 2 and 2. But they are all controllers, they should be interchangeable even though
 * they command different interface. The idea then is that rather than having a different interface for each PID I create
 * this object as a common input to put in.
 * <p/> It changed a bit from the earlier version because now I try to "name" the inputs/targets on whether they are flows/stocks.
 * <p/> It is immutable. Filters need to create their own.
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-12-19
 * @see
 */
final public class ControllerInput {

    /**
     * The target flow, if any
     */
    final private float flowTarget;


    /**
     * The target stock, if any
     */
    final private float stockTarget;

    /**
     * The input flow, if any
     */
    final private float flowInput;


    /**
     * The input stock, if any
     */
    final private float stockInput;

    public ControllerInput(float flowTarget, float flowInput) {
        this(flowTarget, Float.NaN,flowInput,Float.NaN);
    }

    public ControllerInput(float flowTarget, float stockTarget, float flowInput, float stockInput) {
        this.flowTarget = flowTarget;
        this.stockTarget = stockTarget;
        this.flowInput = flowInput;
        this.stockInput = stockInput;
    }


    public float getFlowTarget() {
        return flowTarget;
    }

    public float getStockTarget() {
        return stockTarget;
    }

    public float getFlowInput() {
        return flowInput;
    }

    public float getStockInput() {
        return stockInput;
    }

    public static enum Position{
        STOCK,
        FLOW
    }
}

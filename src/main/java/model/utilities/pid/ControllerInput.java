/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.pid;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;

/**
 * <h4>Description</h4>
 * <p/>  This is a consequence of bad design: children stay in school.
 * <p/> Unfortunately different controllers need different sets of input/output. PID need 1 target 1 input, Cascade need
 * 1 target 2 inputs, Flow/Stock need 2 and 2. But they are all controllers, they should be interchangeable even though
 * they command different interface. The idea then is that rather than having a different interface for each PID I create
 * this object as a common input to put in.
 * <p/> It is mutable because I might need to intercept and change inputs (filtering)
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
     * an array holding all the set points
     */
    private Float[] targets;

    /**
     * an array holding all the state of the inputs
     */
    private Float[] inputs;


    /**
     * Creates a simple one input, one target controller input
     * @param target the set point, the y*, the value the input should be
     * @param input the current value of the variable to control
     * @return a new controller input
     */
    public static ControllerInput simplePIDTarget(float target, float input)
    {

        return new ControllerInput(new Float[]{target}, new Float[]{input});


    }

    /**
     * The private constructor for the controller. Caution must be exercised so that the reference to the arrays
     * is created by the static constructors/builder rather than passed from outside to keep this truly immutable
     * @param targets an array holding all the targets
     * @param inputs an array holding all the inputs
     */
    private ControllerInput(Float[] targets, Float[] inputs)
    {
        this.targets = targets;

        this.inputs = inputs;

        Preconditions.checkArgument(targets.length > 0, "this input has no targets!");
        Preconditions.checkArgument(inputs.length > 0, "this input has no inputs!");


    }


    /**
     * Get an input, the position distinguish between multiples. So for example if there are two inputs, you have an input
     * at position 0 and an input at position 1.
     * Throws an exception if there doesn't exist an input at that position
     * @param position the position of the input in the array
     * @return the input at that position
     */
    public float getInput(int position) {
        return readArrayAtThisPosition(inputs, position);
    }

    /**
     * Get an target, the position distinguish between multiples. So for example if there are two targets, you have a target
     * at position 0 and a target at position 1.
     * Throws an exception if there doesn't exist a target at that position
     * @param position the position of the target in the array
     * @return the target at that position
     */
    public float getTarget(int position) {
        return readArrayAtThisPosition(targets, position);
    }

    /**
     * Set a new value for specific target, the position distinguish between multiples. So for example if there are two targets, you have a target
     * at position 0 and a target at position 1.
     * Throws an exception if there doesn't exist a target at that position
     * @param position the position of the target in the array
     * @param newValue the new value
     */
    public void setTarget(int position, float newValue) {
        writeArrayAtThisPosition(targets, position,newValue);
    }

    /**
     * Set a new value for specific input, the position distinguish between multiples. So for example if there are two inputs,
     * you have an input
     * at position 0 and an input at position 1.
     * Throws an exception if there doesn't exist an input at that position
     * @param position the position of the input in the array
     * @param newValue the new value
     */
    public void setInput(int position, float newValue) {
        writeArrayAtThisPosition(inputs, position,newValue);
    }


    /**
     * utility to wrap array[position] with a try catch that converts array out of bounds into illegal argument exception
     * @param array the array to look into
     * @param position the position in the array
     * @return the element of array at position
     */
    private float readArrayAtThisPosition(Float[] array, int position) {
        try{
        return array[position];
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new IllegalArgumentException("No input at that position!");
        }
    }

    /**
     * utility to wrap array[position] with a try catch that converts array out of bounds into illegal argument exception
     * @param array the array to write into
     * @param position the position in the array
     * @param newValue the new value
     * @return the element of array at position
     */
    private void writeArrayAtThisPosition(Float[] array, int position, float newValue) {
        try{
            array[position] = newValue;
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new IllegalArgumentException("No input at that position!");
        }
    }




    public int howManyInputs() {
        return inputs.length;

    }

    public int howManyTargets() {
        return targets.length;


    }

    /**
     * cascade pid requires two inputs. Usually the first is a stock and the second is a flow. The first input is compared
     * with the target, the second input with the MV of the master PID
     * @param target the target for the master PID
     * @param input1 the input of the master PID
     * @param input2 the input of the slave PID
     * @return the controllerInput object that helps in this situation
     */
    public static ControllerInput cascadeInputCreation(float target, float input1, float input2)
    {
        return new ControllerInput(new Float[]{target},new Float[]{input1,input2});


    }

    /**
     * A builder class for controller inputs that are weird
     */
    public static class ControllerInputBuilder {
        
        private final ArrayList<Float> inputs;

        private final ArrayList<Float> targets;

        /**
         * initialize the builder
         */
        public ControllerInputBuilder() {
            inputs = new ArrayList<>();
            targets = new ArrayList<>();
        }


        /**
         * Add as many inputs as you want
         * @param inputs the inputs to add
         * @return the builder
         */
        public ControllerInputBuilder inputs(Float... inputs){
            Collections.addAll(this.inputs, inputs);
            return this;
        }


        /**
         * Add as many targets as you want
         * @param targets the targets to add
         * @return the builder
         */
        public ControllerInputBuilder targets(Float... targets){
            Collections.addAll(this.targets, targets);
            return this;
        }

        /**
         * finally build the controller input
         * @return the input to return
         */
        public ControllerInput build(){

            return new ControllerInput(targets.toArray(new Float[targets.size()]),
                    inputs.toArray(new Float[inputs.size()]));
        }

    }




}

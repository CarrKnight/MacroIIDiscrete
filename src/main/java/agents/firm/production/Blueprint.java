/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.production;

import goods.GoodType;

import java.util.EnumMap;

/**
 * <h4>Description</h4>
 * <p/> This class specifies a list of inputs and their proportion to completeProductionRunNow a list of outputs
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * It has a simple factory method and a more complicated builder class to make
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version %I%, %G%
 * @see
 */
public class Blueprint {

    EnumMap<GoodType,Integer> inputs;

    EnumMap<GoodType,Integer> outputs;

    /**
     * Private constructor so I am forced to use the builder/factory methods
     */
    private Blueprint(){


    }

    /**
     * Factory method to completeProductionRunNow a blueprint with one input and one output
     * @param input the good needed as an input
     * @param inputQuantity how much input needed
     * @param output the good that will be produced
     * @param outputQuantity how much of it will be produced
     * @return a blueprint object
     */

    public static Blueprint simpleBlueprint( GoodType input,int inputQuantity, GoodType output,int outputQuantity){
        Blueprint b = new Blueprint();
        b.inputs = new EnumMap<GoodType, Integer>(GoodType.class);
        b.inputs.put(input,inputQuantity);
        b.outputs = new EnumMap<GoodType, Integer>(GoodType.class);
        b.outputs.put(output,outputQuantity);

        return  b;


    }

    /**
     * Effective java trick.
     */
    public static class Builder{

        Blueprint b;

        public Builder(){
            b = new Blueprint();
            b.inputs = new EnumMap<>(GoodType.class);
            b.outputs = new EnumMap<>(GoodType.class);
        }

        public  Builder input( GoodType input, int inputQuantity){
            assert !b.inputs.containsKey(input);
            b.inputs.put(input,inputQuantity);

            return this;

        }

        public  Builder output( GoodType output, int outputQuantity){
            assert !b.outputs.containsKey(output);
            b.outputs.put(output,outputQuantity);

            return this;
        }

        public Blueprint build(){
            assert !b.outputs.isEmpty();
            return b;

        }




    }


    public EnumMap<GoodType, Integer> getInputs() {
        return inputs;
    }

    public EnumMap<GoodType, Integer> getOutputs() {
        return outputs;
    }
}

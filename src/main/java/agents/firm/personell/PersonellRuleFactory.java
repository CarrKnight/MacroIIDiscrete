/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.personell;

import agents.firm.production.control.HillClimberThroughPredictionControl;
import agents.firm.production.control.ParticleControl;
import agents.firm.production.control.PlantControl;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.purchases.pricing.BidPricingStrategy;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import model.utilities.NonDrawable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;



/**
 * <h4>Description</h4>
 * <p/>  This is a utility class that purchases department can call in order to generate pricing bidPricingRules, inventory controls or both
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-13
 * @see
 */
public class PersonellRuleFactory {
    //private constructor.
    private PersonellRuleFactory(){}

    /**
     * This holds a list of all the subclasses of InventoryControl. It's an arraylist to make randomization easier, but it's generated as a set first so there won't be duplicates
     */
    final private static ArrayList<Class<? extends PlantControl>> plantControlRules;



    //static clause to fill the set names
    static {
        plantControlRules = new ArrayList<>(); //read all the inventoryControlRules
        plantControlRules.add(HillClimberThroughPredictionControl.class);
        plantControlRules.add(ParticleControl .class);
        plantControlRules.add(TargetAndMaximizePlantControl .class);


        assert plantControlRules.size() > 0; // there should be at least one!!
        //do not let the decorators in the rule
        plantControlRules.removeIf(aClass -> aClass.isAnnotationPresent(NonDrawable.class));


    }



    /**
     * Returns a new specific InventoryControl (but not a rule that is BOTH a pricing and inventory control rule)
     *
     * @param rule the simpleName of the class!
     * @param hr  the human resources owning the rule
     * @return the new rule to follow
     */
    public static PlantControl newPlantControl( String rule,  HumanResources hr) {
        for (Class<? extends PlantControl> c : plantControlRules) {
            if (c.getSimpleName().equals(rule)) //if the name matches
            {
                try {

                    return c.getConstructor(HumanResources.class).newInstance(hr); //return it!
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                        IllegalArgumentException | InvocationTargetException ex) {
                    throw new RuntimeException("failed to instantiate InventoryControl" + ex.getMessage());
                }


            }
        }

        //if you are, nothing was found!
        throw new RuntimeException("failed to instantiate a market algorithm with name" + rule);


    }

    public static PlantControl randomPlantControl( HumanResources hr)
    {
        Class<? extends PlantControl > plantControl = null;
        MersenneTwisterFast randomizer = hr.getFirm().getRandom(); //get the randomizer
        //now you are going to pick at random, but keep doing it as long as you draw abstract classes or interfaces
        while(plantControl == null || Modifier.isAbstract(plantControl.getModifiers()) || plantControl.isInterface()
                || plantControl.isAnnotationPresent(NonDrawable.class))
        {
            assert randomizer != null;
            assert !plantControlRules.isEmpty();
            //get a new rule
            plantControl = plantControlRules.get(randomizer.nextInt(plantControlRules.size()));
        }

        //now just instantiate it!
        return newPlantControl(plantControl, hr);


    }

    public static <PC extends  PlantControl & BidPricingStrategy>
    PC newPlantControl( Class<PC> rule,  HumanResources hr)
    {
        Preconditions.checkNotNull(rule);

        if(!plantControlRules.contains(rule) || Modifier.isAbstract(rule.getModifiers()) || rule.isInterface() )
            throw new IllegalArgumentException("The rule given is either abstract or just not recognized");


        try {
            return rule.getConstructor(HumanResources.class).newInstance(hr);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException ex)
        {
            ex.printStackTrace();
            throw new RuntimeException(" failed to instantiate InventoryControl ----- " + rule  + ex.getMessage());
        }



    }






}

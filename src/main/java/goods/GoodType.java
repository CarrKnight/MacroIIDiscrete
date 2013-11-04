/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package goods;

/**
 *
 *
 */
public enum GoodType {

    /**
     * Generic good, useful
     */
    GENERIC(false,false),

    /**
     * Generic capital
     */
    CAPITAL(true,false),

    /**
     * Toil and sweat.
     */
    LABOR(false,true),

    /**
     * Yummie
     */
    BEEF(false,false),

    /**
     * swanky
     */
    LEATHER(false,false),

    /**
     * processed
     */
    FOOD(false,false),

    /**
     * where it all starts
     */
    CATTLE(false,false),

    /**
     * Toil and sweat.
     */
    LABOR_FOOD(false,true),


    /**
     * Toil and sweat.
     */
    LABOR_BEEF(false,true),

    /**
     * runs cars
     */
    OIL(false,false);


    /**
     * Is this type of good a machinery?
     */
    private final boolean isMachinery;

    /**
     * Is this type of good a kind of labor?
     */
    private final boolean isLabor;


    private GoodType(boolean machinery,boolean  labor) {
        isMachinery = machinery;
        isLabor = labor;
    }


    public boolean isMachinery() {
        return isMachinery;
    }

    public boolean isLabor() {
        return isLabor;
    }
}

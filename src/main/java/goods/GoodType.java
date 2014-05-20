/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package goods;

import agents.firm.Firm;

/**
 *
 *
 */
public abstract class GoodType implements Comparable<GoodType> {

    /**
     * something like the NAICS code or some other made-up id. Goodtypes are "equal" if they have the same code, so this ought to be unique!
     * It's a string rather than a number because NAICS like to add letters for no reason whatsoever.
     *
     */
    private final String code;

    /**
     * the name of the string
     */
    private final String name;


    /**
     * Is this type of good a machinery? (production plants can be made of this)
     */
    private final boolean isMachinery;

    /**
     * Is this type of good a kind of labor?
     */
    private final boolean isLabor;

    /**
     * create a good-type specifying whether it's a capital-good or a labor-good (or neither)
     * @param code the industry code
     * @param name the full name of the goodtype
     * @param isMachinery whether it can be used as capital
     * @param isLabor whether it can be used as labor
     */
    protected GoodType(String code, String name, boolean isMachinery, boolean isLabor) {
        this.code = code;
        this.name = name;
        this.isMachinery = isMachinery;
        this.isLabor = isLabor;
    }



    /**
     * create a good-type assuming it's a consumption good
     * @param code the industry code
     * @param name the full name of the goodtype
     */
    protected GoodType(String code,String name ) {
        this(code, name,false,false);
    }

    public boolean isMachinery() {
        return isMachinery;
    }

    public boolean isLabor() {
        return isLabor;

    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    /**
     * Goodtypes are equal if their industry code is equal!
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GoodType goodType = (GoodType) o;

        return code.equals(goodType.code);

    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }


    @Override
    public String toString() {
        return code + ": " + name ;
    }

    /**
     * Compares the industry code
     */
    @Override
    public int compareTo(GoodType o) {
        return code.compareTo(o.getCode());
    }


    public abstract boolean isDifferentiated();

    /**
     * method to quickly produce a number of units of this goodtype
     * @param amount the amount
     * @param receiver who built it/receives it
     * @return the goods produced if differentiated or null otherwise
     */
    public abstract Good[] produceAndDeliver(int amount, Firm receiver, int unitCost);
}

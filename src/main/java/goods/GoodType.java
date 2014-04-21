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
public class GoodType implements Comparable<GoodType> {

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
     * a "test"/madeup goodtype, representing generic homogeneous goods. Imagine macro's "c" (consumption good)
     */
    public static final GoodType GENERIC = new GoodType("testGeneric","Test good",false,false);


    /**
     * a "test"/madeup goodtype, representing generic capital. Imagine micro's "k"
     */
    public static final GoodType CAPITAL = new GoodType("testCapital","Test Capital",true,false);

    /**
     * a "test"/madeup goodtype, representing generic labor. Imagine micro's "L"
     */
    public static final GoodType LABOR = new GoodType("testLabor","Test Labor",false,true);



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
    public GoodType(String code, String name, boolean isMachinery, boolean isLabor) {
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
    public GoodType(String code,String name ) {
        this.name = name;
        this.code = code;
        isLabor=false;
        isMachinery=false;
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
}

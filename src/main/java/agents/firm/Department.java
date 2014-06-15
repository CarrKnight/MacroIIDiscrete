/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm;

/**
 * <h4>Description</h4>
 * <p/> Department refers to any subcomponent for a firm. So far we have PurchaseDepartments, SalesDepartment and HumanResources.
 * They don't really share an interface or any commonality, so this interface is basically an annotation. It's useful in registering quotes when I want to specify which
 * department generated such quotes.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-21
 * @see
 */
public interface Department {

    /**
     * what is the most recent day this department has memory of
     */
    public int getLastObservedDay();

    /**
     * what is the first day this department has memory of
     */
    public int getStartingDay();

    /**
     * flag that is set to true whenever a department has managed to fill a quote/make a trade at least once
     * @return true if at least once the department has bought/sold/hired
     */
    public boolean hasTradedAtLeastOnce();


    /**
     * this is today outflow for sales department and today inflow for purchases
     * @return the correct flow
     */
    public int getTodayTrades();


    /**
     * Last price that resulted in a trade. -1 if there has been no trade ever
     */
    public int getLastClosingPrice();

}

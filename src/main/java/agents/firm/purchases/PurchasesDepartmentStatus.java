/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases;

/**
 * <h4>Description</h4>
 * <p/> This is a very simple enum describing what a PurchasesDepartment is doing right now
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-03
 * @see
 */
public enum PurchasesDepartmentStatus {

    /**
     * The department is in the middle of placing a quote in the market
     */
    PLACING_QUOTE,

    /**
     * The department is in the middle of placing of shopping around
     */
    SHOPPING,

    /**
     * The department is waiting for a quote to be filled
     */
    WAITING,

    /**
     * The department is waiting to have an authorization to buy
     */
    IDLE





}

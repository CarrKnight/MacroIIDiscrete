/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.collectors.enums;

/**
 * <h4>Description</h4>
 * <p/>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-08-19
 * @see
 */
public enum PurchasesDataType {

    INFLOW,

    OUTFLOW,

    FAILURES_TO_CONSUME,

    CLOSING_PRICES,

    LAST_OFFERED_PRICE,

    INVENTORY,

    DEMAND_GAP,

    WORKERS_CONSUMING_THIS_GOOD,
    AVERAGE_CLOSING_PRICES, /**
     * this is 0 for the purchase department, it is only valid for HR
     */
    WORKERS_TARGETED



}

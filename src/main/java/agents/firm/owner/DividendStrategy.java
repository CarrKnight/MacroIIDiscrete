/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.owner;

import agents.EconomicAgent;
import agents.firm.Firm;

/**
 * <h4>Description</h4>
 * <p/> This interface relates to what the firm should do at the end of the week with the profits it has.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-20
 * @see
 */
public interface DividendStrategy {

    /**
     * Tell the firm to pay dividends on this week profits
     * @param aggregateProfits the total profits of the firm
     * @param firm the firm paying the dividends
     * @param owner the owner that should be paid
     */
    public void payDividends(int aggregateProfits, Firm firm, EconomicAgent owner);

}

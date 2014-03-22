/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.sales;

import agents.EconomicAgent;
import agents.firm.Firm;
import goods.Good;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/>  A very simple listener that records whenever the firm tasks the sales department to sell a new good
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-27
 * @see
 */
public interface SalesDepartmentListener {

    /**
     * Tell the listener the firm just tasked the salesdepartment to sell a new good
     * @param owner the owner of the sales department
     * @param dept the sales department asked
     * @param good the good being sold
     */
    public void sellThisEvent(@Nonnull final Firm owner,@Nonnull final SalesDepartment dept,@Nonnull final Good good);

    /**
     * This logEvent is fired whenever the sales department managed to sell a good!
     * @param dept The department
     * @param good the good being sold
     * @param price the price sold for
     */
    public void goodSoldEvent(@Nonnull final SalesDepartment dept, Good good, Long price);

    /**
     * Tell the listener a peddler just came by and we couldn't service him because we have no goods
     * @param owner the owner of the sales department
     * @param dept the sales department asked
     */
    public void stockOutEvent(@Nonnull final Firm owner,@Nonnull final SalesDepartment dept, @Nonnull final EconomicAgent buyer);


}

/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing.pid;

import agents.firm.sales.SalesDepartmentListener;
import financial.BidListener;
import financial.market.Market;
import financial.TradeListener;

/**
 * <h4>Description</h4>
 * <p/> The real problem with seller PID is that we need a decent estimator for how many stockouts occur. As of now a stockout when there is an order book is defined as:
 * <ul>
 *     <li>
 *         Our inventory is empty. A bid is placed, it is not cleared immediately AND we could have filled that bid for that price had we had any good
 *     </li>
 *     <li>
 *         Our inventory is empty. A bid is placed and cleared, but the best bid is lower than our usual price: we could have outcompeted!
 *     </li>
 * </ul>
 * <p/>
 * When there isn't an order book instead a stockout is:
 * <ul>
 *     <li>
 *         Our inventory is empty. Guy comes to the store, with reservation price higher than our last closing price.
 *     </li>
 *     <li>
 *         Our inventory is empty. A trade occurs somewhere with a price we could have accepted ourselves!
 *     </li>
 * </ul>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-28
 * @see
 */
public interface StockoutEstimator extends BidListener, TradeListener, SalesDepartmentListener{





    public void newPIDStep(Market market);


    public int getStockouts();



}

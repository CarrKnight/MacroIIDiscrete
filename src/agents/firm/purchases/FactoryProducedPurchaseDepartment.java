/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.purchases;

/**
 * <h4>Description</h4>
 * <p/>  This is similar to FactoryProducedSalesDepartment. A struct holding the purchase department and its strategies/algorithm
 * <p/> The reason this exists is that the strategies are all hidden away once the object is built and can never be accessed; this is usually fine
 * except that the one who constructed the purchase department (the scenario for example) might want to tune in some parameters.
 * Now that factories return this object, the scenario that build them can touch and extend the purchase departments as it sees fit
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-03-07
 * @see
 */

import agents.firm.purchases.inventoryControl.InventoryControl;
import agents.firm.purchases.pricing.BidPricingStrategy;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;

public class FactoryProducedPurchaseDepartment<IC extends InventoryControl, BP extends BidPricingStrategy,
        BS extends BuyerSearchAlgorithm, SS extends SellerSearchAlgorithm> {


    /**
     * the department built
     */
    final private PurchasesDepartment department;

    /**
     * the inventory control
     */
    final private IC inventoryControl;

    /**
     * the bid pricing strategy
     */
    final private BP bidPricing;

    /**
     * the buyer search strategy
     */
    final private BS buyerSearch;

    /**
     * the seller search strategy
     */
    final private SS sellerSearch;

    /**
     * Create a simple container for all the things instantiated by the firm
     * @param department the department built
     * @param inventoryControl the inventory control
     * @param bidPricing the bid pricing strategy
     * @param buyerSearch the buyer search strategy
     * @param sellerSearch the seller search strategy
     */
    public FactoryProducedPurchaseDepartment(PurchasesDepartment department, IC inventoryControl, BP bidPricing,
                                             BS buyerSearch, SS sellerSearch) {
        this.department = department;
        this.inventoryControl = inventoryControl;
        this.bidPricing = bidPricing;
        this.buyerSearch = buyerSearch;
        this.sellerSearch = sellerSearch;
    }


    /**
     * Gets the department built.
     *
     * @return Value of the department built.
     */
    public PurchasesDepartment getDepartment() {
        return department;
    }

    /**
     * Gets the inventory control.
     *
     * @return Value of the inventory control.
     */
    public IC getInventoryControl() {
        return inventoryControl;
    }

    /**
     * Gets the seller search strategy.
     *
     * @return Value of the seller search strategy.
     */
    public SS getSellerSearch() {
        return sellerSearch;
    }

    /**
     * Gets the bid pricing strategy.
     *
     * @return Value of the bid pricing strategy.
     */
    public BP getBidPricing() {
        return bidPricing;
    }

    /**
     * Gets the buyer search strategy.
     *
     * @return Value of the buyer search strategy.
     */
    public BS getBuyerSearch() {
        return buyerSearch;
    }
}

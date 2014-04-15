/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.personell;

import agents.firm.production.control.PlantControl;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;

/**
 * <h4>Description</h4>
 * <p/> Much like the other "factory produced" classes, this is really just a struct holding on the result of creating a new human resources
 * object through a factory method. It holds all the strategies instantiated AND the human resources.
 * Ordinarily the strategies are private and can't be accessed directly, but the one constructing the human resources might want to tune them
 * initially.
 * <p/> The only other difference is that human resources can be factory produced in two different ways and so there are two different classes for this
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


/*
                                                              Class<? extends PlantControl> integratedControl,
                                                              Class<? extends BuyerSearchAlgorithm> buyerSearchAlgorithmType,
                                                              Class<? extends SellerSearchAlgorithm> sellerSearchAlgorithmType
 */

public class FactoryProducedHumanResources<PC extends PlantControl, BS extends BuyerSearchAlgorithm, SS extends SellerSearchAlgorithm> {


    /**
     * the department built
     */
    final private HumanResources department;

    /**
     * the plant control
     */
    final private PC plantControl;

    /**
     * the buyer search strategy
     */
    final private BS buyerSearch;

    /**
     * the seller search strategy
     */
    final private SS sellerSearch;

    /**
     * A container for factory produced human resources
     * @param department the human resources department
     * @param plantControl the plant control
     * @param buyerSearch the buyer search algorithm
     * @param sellerSearch the seller search algorithm
     */
    public FactoryProducedHumanResources(HumanResources department, PC plantControl, BS buyerSearch, SS sellerSearch) {
        this.department = department;
        this.plantControl = plantControl;
        this.buyerSearch = buyerSearch;
        this.sellerSearch = sellerSearch;
    }


    /**
     * Gets the department built.
     *
     * @return Value of the department built.
     */
    public HumanResources getDepartment() {
        return department;
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
     * Gets the plant control.
     *
     * @return Value of the plant control.
     */
    public PC getPlantControl() {
        return plantControl;
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

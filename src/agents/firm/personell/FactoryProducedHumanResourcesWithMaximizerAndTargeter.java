/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.firm.personell;

import agents.firm.production.control.PlantControl;
import agents.firm.production.control.maximizer.WorkforceMaximizer;
import agents.firm.production.control.maximizer.algorithms.WorkerMaximizationAlgorithm;
import agents.firm.production.control.targeter.WorkforceTargeter;
import agents.firm.sales.exploration.BuyerSearchAlgorithm;
import agents.firm.sales.exploration.SellerSearchAlgorithm;

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
 * @version 2013-03-07
 * @see
 */
/*
@Nullable Class<? extends WorkforceTargeter> targeter,
@Nullable Class<? extends WorkforceMaximizer> maximizer,
 */

public class FactoryProducedHumanResourcesWithMaximizerAndTargeter<PC extends PlantControl,
        BS extends BuyerSearchAlgorithm, SS extends SellerSearchAlgorithm, WT extends WorkforceTargeter,
        WM extends WorkforceMaximizer<ALG>, ALG extends WorkerMaximizationAlgorithm>
        extends FactoryProducedHumanResources<PC, BS, SS> {

    /**
     * The component that tries to achieve the targeted workforce
     */
    private final WT workforceTargeter;

    /**
     * the component that tries to maximize the workforce
     */
    private final WM workforceMaximizer;

    public FactoryProducedHumanResourcesWithMaximizerAndTargeter(HumanResources department,
                                                                 PC plantControl, BS buyerSearch, SS sellerSearch,
                                                                 WT workforceTargeter, WM workforceMaximizer) {
        super(department, plantControl, buyerSearch, sellerSearch);
        this.workforceTargeter = workforceTargeter;
        this.workforceMaximizer = workforceMaximizer;
    }


    /**
     * Gets The component that tries to achieve the targeted workforce.
     *
     * @return Value of The component that tries to achieve the targeted workforce.
     */
    public WT getWorkforceTargeter() {
        return workforceTargeter;
    }

    /**
     * Gets the component that tries to maximize the workforce.
     *
     * @return Value of the component that tries to maximize the workforce.
     */
    public WM getWorkforceMaximizer() {
        return workforceMaximizer;
    }
}


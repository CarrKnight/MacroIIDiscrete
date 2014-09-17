/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing.pid;

import agents.firm.sales.SalesDepartment;
import financial.market.Market;
import model.MacroII;
import model.utilities.pid.decorator.PIDAutotuner;
import model.utilities.stats.regression.AutoRegressiveWithInputRegression;

/**
 * Just a facade to simple flow seller PID which forces it to be adaptive from the start
 * Created by carrknight on 8/22/14.
 */
public class AdaptiveFlowSellerPID extends SimpleFlowSellerPID{


    /**
     * Constructor that generates at random the seller PID from the model randomizer
     *
     * @param sales
     */
    public AdaptiveFlowSellerPID(SalesDepartment sales) {
        super(sales);
        decorateController(pid -> new PIDAutotuner(pid, getSales()));
    }

    public AdaptiveFlowSellerPID(SalesDepartment sales, float proportionalGain, float integralGain, float derivativeGain,
                                 int speed, Market market, int initialPrice, MacroII model) {
        super(sales, proportionalGain, integralGain, derivativeGain, speed, market, initialPrice, model);
        decorateController( pid -> new PIDAutotuner(pid,integer -> new AutoRegressiveWithInputRegression(integer, integer),getSales()));

    }
}

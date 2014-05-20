/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm.sales.pricing;

import agents.firm.sales.SalesDepartment;
import goods.Good;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/> This strategy starts with default markup and then keeps adjusting downward whenever it fails to sell ALL its goods and upwards otherwise.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-07-24
 * @see
 */
public class EverythingMustGoAdaptive extends  BaseAskPricingStrategy {

    float markup;

    SalesDepartment department;


    public EverythingMustGoAdaptive(SalesDepartment department) {
        this.department = department;
        markup = department.getFirm().getModel().getCluelessDefaultMarkup(); //set initial markup!
    }

    /**
     * The sales department is asked at what should be the sale price for a specific good; this, I guess, is the fundamental
     * part of the sales department
     *
     * @param g the good to price
     * @return
     */
    @Override
    public int price(Good g) {
        return (int)(g.getLastValidPrice() * (1+ markup));
    }



    /**
     * if the firm managed to sell 95% or more of its merchandise, then raise markup. Otherwise decrease it
     */
    @Override
    public void weekEnd() {
        MacroII model = department.getFirm().getModel();
        float oldMarkup = markup; //memorize old markup
        if(department.getHowManyToSell() > 0)
            markup = markup + model.getMarkupIncreases();
        else
            markup = Math.max(0,markup - model.getMarkupIncreases());
        //if markup has changed, adjust your sales department to change prices soon
        if(Math.abs(markup - oldMarkup) > .001)
            model.scheduleSoon(ActionOrder.TRADE,new Steppable() { //I am not doing this IMMEDIATELY because I don't want to screw up other people's weekend routine
                @Override
                public void step(SimState simState) {
                    department.updateQuotes(); //update quotes (becaue the price is going to be different!)


                }
            });

    }

    /**
     * tries to sell all
     *
     * @param inventorySize
     * @return
     */
    @Override
    public boolean isInventoryAcceptable(int inventorySize) {
        return department.getHowManyToSell() > 0;

    }

    /**
     * This is somewhat similar to rate current level. It estimates the excess (or shortage)of goods sold. It is basically
     * getCurrentInventory-AcceptableInventory
     *
     * @return positive if there is an excess of goods bought, negative if there is a shortage, 0 if you are right on target.
     */
    @Override
    public float estimateSupplyGap() {
        int inventorySize = department.getHowManyToSell();
        if(isInventoryAcceptable(inventorySize))
            return 0;
        else
            return inventorySize;

    }
}

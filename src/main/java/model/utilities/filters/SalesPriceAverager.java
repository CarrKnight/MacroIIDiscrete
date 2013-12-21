/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.filters;

import agents.firm.sales.SalesDepartment;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * <h4>Description</h4>
 * <p/> This is basically a self-updating price average estimator that does:
 *    Weighted Average Price / Total Inflows
 *    (or outflows if it's a purchase department)
 * <p/> The idea is to try and have the correct
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-12-20
 * @see
 */
public class SalesPriceAverager implements Steppable, Deactivatable{


    final private MovingSum<Integer> denominator;

    final private MovingSum<Long> numerator;

    final private SalesDepartment department;

    private boolean active= true;




    public SalesPriceAverager(final MacroII model, final SalesDepartment department, int observationSize)
    {

        denominator = new MovingSum<>(observationSize);
        numerator = new MovingSum<>(observationSize);
        this.department = department;

        //collects data at CLEANUP
        model.scheduleSoon(ActionOrder.CLEANUP_DATA_GATHERING,this);



    }

    @Override
    public void step(SimState simState) {
        if(!active)
            return;

        denominator.addObservation(department.getTodayInflow());
        numerator.addObservation(department.getTodayOutflow() * department.getLastClosingPrice());
        assert numerator.numberOfObservations() == denominator.numberOfObservations();

        ((MacroII)simState).scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING,this);
    }


    public float getAveragedPrice()
    {
        assert numerator.numberOfObservations() == denominator.numberOfObservations();
        if(denominator.numberOfObservations() == 0 || denominator.getSmoothedObservation() == 0)
            return -1;
        else
            return numerator.getSmoothedObservation()/denominator.getSmoothedObservation();
    }


    @Override
    public void turnOff() {
        active = false;

    }
}

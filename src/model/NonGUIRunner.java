package model;

import financial.Market;
import model.scenario.SupplyChainScenario;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;

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
 * @version 2013-01-11
 * @see
 */
public class NonGUIRunner {

    public static void main(String[] args){

        final MacroII macroII = new MacroII(System.currentTimeMillis());
        SupplyChainScenario scenario1 = new SupplyChainScenario(macroII);
        //    scenario1.setAlwaysMoving(true);
        //   MonopolistScenario scenario1 = new MonopolistScenario(macroII);
        macroII.setScenario(scenario1);
        scenario1.setNumberOfBeefProducers(1); scenario1.setNumberOfCattleProducers(1);
        scenario1.setNumberOfFoodProducers(1);

        //schedule a print to screen stat collector
        macroII.scheduleSoon(ActionOrder.CLEANUP, new Steppable() {
            @Override
            public void step(SimState state)
            {
              for(Market market : macroII.getMarkets())
                  System.out.println(market.getGoodType().name() + ", price: " + market.getLastPrice() + ", " +
                          "quantity: " + market.getLastWeekVolume());

                System.out.println("-----------------------------------------------");
                macroII.scheduleTomorrow(ActionOrder.CLEANUP,this);


            }
        });


        macroII.start();
        while(macroII.schedule.getTime()<10000)
            macroII.schedule.step(macroII);




    }
}

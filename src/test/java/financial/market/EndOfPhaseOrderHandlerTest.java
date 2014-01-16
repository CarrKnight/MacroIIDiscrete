/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial.market;

import goods.GoodType;
import model.MacroII;
import model.scenario.Scenario;
import model.utilities.dummies.Customer;
import model.utilities.dummies.DailyGoodTree;
import model.utilities.scheduler.Priority;
import org.junit.Assert;
import org.junit.Test;

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
 * @version 2014-01-16
 * @see
 */
public class EndOfPhaseOrderHandlerTest {





    @Test
    public void marketClearsCorrectly() throws IllegalAccessException {

        MacroII model = new MacroII(10l);
        model.setScenario(new Scenario(model) {
            @Override
            public void start() {
                OrderBookMarket market= new OrderBookMarket(GoodType.GENERIC);
                market.setOrderHandler(new EndOfPhaseOrderHandler(),model);
                getMarkets().put(GoodType.GENERIC,market);

                //20 buyers, from 100 to 120
                for(int i=0; i < 20; i++)
                {
                    Customer customer = new Customer(model,100+i,market);
                    getAgents().add(customer);
                    customer.setTradePriority(Priority.STANDARD);
                }
                //1 seller, pricing 100, trying to sell 10 things!
                DailyGoodTree tree = new DailyGoodTree(model,10,100,market);
                tree.setTradePriority(Priority.STANDARD);
                getAgents().add(tree);
            }
        });
        model.start();

        for(int i=0; i< 100; i++)
        {
            model.schedule.step(model);
            Assert.assertEquals(10, model.getMarket(GoodType.GENERIC).getTodayVolume());
            //this should always be true!
            Assert.assertEquals(109,model.getMarket(GoodType.GENERIC).getBestBuyPrice());
        }









    }

}

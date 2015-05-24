package model.gui.paper2;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizer;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.prediction.SalesPredictor;
import agents.firm.sales.pricing.pid.InventoryBufferSalesControl;
import financial.market.Market;
import model.MacroII;
import model.scenario.OneLinkSupplyChainScenario;
import model.scenario.OneLinkSupplyChainScenarioWithCheatingBuyingPrice;

/**
 * Competitive Supply chain factory
 * Created by carrknight on 5/23/15.
 */
public class CompetitiveChainFactory extends SupplyChainForm {
    /**
     * Applies this function to the given argument.
     *
     * @param macroII the function argument
     * @return the function result
     */
    @Override
    public OneLinkSupplyChainScenarioWithCheatingBuyingPrice apply(MacroII macroII) {


        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII){

            @Override
            protected void buildBeefSalesPredictor(SalesDepartment dept) {
                if(!isUpstreamLearning()) {
                    FixedDecreaseSalesPredictor predictor = SalesPredictor.Factory.newSalesPredictor(
                            FixedDecreaseSalesPredictor.class, dept);
                    predictor.setDecrementDelta(0);
                    dept.setPredictorStrategy(predictor);
                }
            }



            @Override
            public void buildFoodPurchasesPredictor(PurchasesDepartment department) {
                if(!isDownstreamLearning())
                    department.setPredictor(new FixedIncreasePurchasesPredictor(0));
                else
                    super.buildFoodPurchasesPredictor(department);

            }

            @Override
            protected SalesDepartment createSalesDepartment(Firm firm, Market goodmarket) {
                SalesDepartment department = super.createSalesDepartment(firm, goodmarket);
                if(!isDownstreamLearning() && goodmarket.getGoodType().equals(OneLinkSupplyChainScenario.OUTPUT_GOOD))
                    department.setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
                //set different pricing
                if(goodmarket.getGoodType().equals(OneLinkSupplyChainScenario.INPUT_GOOD))
                {
                    final InventoryBufferSalesControl askPricingStrategy =
                            new InventoryBufferSalesControl(department,10,200,macroII,
                                                            getProportionalGain()/getTimidity(),
                                                            getIntegralGain()/getTimidity(),
                                                            0, macroII.getRandom());
                    askPricingStrategy.setSpeed(getStickiness());
                }
                return department;
            }

            @Override
            protected HumanResources createPlant(Blueprint blueprint, Firm firm, Market laborMarket) {
                HumanResources hr = super.createPlant(blueprint, firm, laborMarket);
                if(isUpstreamLearning())
                    hr.setPredictor(new FixedIncreasePurchasesPredictor(0));
                return hr;
            }
        };



        scenario.setControlType(MarginalMaximizer.class);        scenario.setSalesDepartmentType(
                SalesDepartmentOneAtATime.class);
        scenario.setBeefPriceFilterer(null);


        //competition!
        scenario.setNumberOfBeefProducers(5);
        scenario.setNumberOfFoodProducers(5);

        scenario.setDivideProportionalGainByThis(getTimidity());
        scenario.setDivideIntegrativeGainByThis(getTimidity());
        //no delay
        scenario.setBeefPricingSpeed(getStickiness());


        return scenario;
    }



}

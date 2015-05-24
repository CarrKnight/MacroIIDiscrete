package model.gui.paper2;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizer;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.ErrorCorrectingPurchasePredictor;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.prediction.SalesPredictor;
import financial.market.Market;
import model.MacroII;
import model.scenario.OneLinkSupplyChainScenario;
import model.scenario.OneLinkSupplyChainScenarioWithCheatingBuyingPrice;

import java.io.IOException;

/**
 * Created by carrknight on 5/23/15.
 */
public class DownstreamMonopolistFactory extends SupplyChainForm {
    /**
     * Applies this function to the given argument.
     *
     * @param macroII the function argument
     * @return the function result
     */
    @Override
    public OneLinkSupplyChainScenarioWithCheatingBuyingPrice apply(MacroII macroII) {
        final OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario1 = new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII){

            @Override
            protected void buildBeefSalesPredictor(SalesDepartment dept) {
                if(!isUpstreamLearning()){
                    FixedDecreaseSalesPredictor predictor  = SalesPredictor.Factory.
                            newSalesPredictor(FixedDecreaseSalesPredictor.class, dept);
                    predictor.setDecrementDelta(0);
                    dept.setPredictorStrategy(predictor);
                }
                else{

                }
            }



            @Override
            public void buildFoodPurchasesPredictor(PurchasesDepartment department) {
                if(!isDownstreamLearning())
                    department.setPredictor(new FixedIncreasePurchasesPredictor(1));
                else{
                    final ErrorCorrectingPurchasePredictor predictor = new ErrorCorrectingPurchasePredictor(macroII, department);

                    department.setPredictor(predictor);

                }

            }

            @Override
            protected SalesDepartment createSalesDepartment(Firm firm, Market goodmarket) {
                SalesDepartment department = super.createSalesDepartment(firm, goodmarket);
                if(goodmarket.getGoodType().equals(OneLinkSupplyChainScenario.OUTPUT_GOOD))  {
                    if(!isDownstreamLearning())
                        department.setPredictorStrategy(new FixedDecreaseSalesPredictor(1));
                }
                return department;
            }

            @Override
            protected HumanResources createPlant(Blueprint blueprint, Firm firm, Market laborMarket) {
                HumanResources hr = super.createPlant(blueprint, firm, laborMarket);
                if(blueprint.getOutputs().containsKey(OneLinkSupplyChainScenario.INPUT_GOOD))
                {
                    if(!isUpstreamLearning()){
                        hr.setPredictor(new FixedIncreasePurchasesPredictor(0));
                    }
                }
                if(blueprint.getOutputs().containsKey(OneLinkSupplyChainScenario.OUTPUT_GOOD))
                {
                    if(!isDownstreamLearning())
                        hr.setPredictor(new FixedIncreasePurchasesPredictor(1));


                }
                return hr;
            }
        };
        scenario1.setControlType(MarginalMaximizer.class);
        scenario1.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario1.setBeefPriceFilterer(null);

        //competition!
        scenario1.setNumberOfBeefProducers(5);
        scenario1.setBeefTargetInventory(100);
        scenario1.setNumberOfFoodProducers(1);
        scenario1.setFoodTargetInventory(100);

        scenario1.setDivideProportionalGainByThis(getTimidity());
        scenario1.setDivideIntegrativeGainByThis(getTimidity());
        //no delay
        scenario1.setBeefPricingSpeed(getStickiness());
        return scenario1;
    }
}

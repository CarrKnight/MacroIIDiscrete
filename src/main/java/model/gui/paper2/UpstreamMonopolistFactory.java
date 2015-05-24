package model.gui.paper2;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.control.maximizer.algorithms.marginalMaximizers.MarginalMaximizer;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.prediction.FixedIncreasePurchasesPredictor;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentOneAtATime;
import agents.firm.sales.prediction.ErrorCorrectingSalesPredictor;
import agents.firm.sales.prediction.FixedDecreaseSalesPredictor;
import agents.firm.sales.prediction.SalesPredictor;
import agents.firm.sales.pricing.pid.InventoryBufferSalesControl;
import financial.market.Market;
import model.MacroII;
import model.scenario.OneLinkSupplyChainScenario;
import model.scenario.OneLinkSupplyChainScenarioWithCheatingBuyingPrice;

import java.util.function.Function;

/**'
 * The food monopolist factory
 * Created by carrknight on 5/23/15.
 */
public class UpstreamMonopolistFactory extends  SupplyChainForm
{


    /**
     * Applies this function to the given argument.
     *
     * @param macroII the function argument
     * @return the function result
     */
    @Override
    public OneLinkSupplyChainScenarioWithCheatingBuyingPrice apply(MacroII macroII) {


        final int timidity = getTimidity();
        final int stickiness = getStickiness();


        OneLinkSupplyChainScenarioWithCheatingBuyingPrice scenario =
                new OneLinkSupplyChainScenarioWithCheatingBuyingPrice(macroII){

                    @Override
                    protected void buildBeefSalesPredictor(SalesDepartment dept) {
                        if(!isUpstreamLearning()){
                            FixedDecreaseSalesPredictor predictor  = SalesPredictor.Factory.
                                    newSalesPredictor(FixedDecreaseSalesPredictor.class, dept);
                            predictor.setDecrementDelta(2);
                            dept.setPredictorStrategy(predictor);
                        }
                        else{
                            assert dept.getPredictorStrategy() instanceof ErrorCorrectingSalesPredictor;
                            //assuming here nothing has been changed and we are still dealing with recursive sale predictors
                        }
                    }



                    @Override
                    public void buildFoodPurchasesPredictor(PurchasesDepartment department) {
                        if(!isDownstreamLearning())
                            department.setPredictor(new FixedIncreasePurchasesPredictor(0));

                    }

                    @Override
                    protected SalesDepartment createSalesDepartment(Firm firm, Market goodmarket) {
                        SalesDepartment department = super.createSalesDepartment(firm, goodmarket);
                        if(goodmarket.getGoodType().equals(OneLinkSupplyChainScenario.OUTPUT_GOOD))  {
                            if(!isDownstreamLearning())
                                department.setPredictorStrategy(new FixedDecreaseSalesPredictor(0));
                        }
                        else
                        {
                            final InventoryBufferSalesControl askPricingStrategy =
                                    new InventoryBufferSalesControl(department,10,200,macroII,
                                                                    getProportionalGain()/timidity,
                                                                    getIntegralGain()/timidity,
                                                                    0, macroII.getRandom());
                            askPricingStrategy.setSpeed(stickiness);
                        }
                        return department;
                    }

                    @Override
                    protected HumanResources createPlant(Blueprint blueprint, Firm firm, Market laborMarket) {
                        HumanResources hr = super.createPlant(blueprint, firm, laborMarket);
                        if(blueprint.getOutputs().containsKey(OneLinkSupplyChainScenario.INPUT_GOOD))
                        {
                            if(!isUpstreamLearning()){
                                hr.setPredictor(new FixedIncreasePurchasesPredictor(1));
                            }
                        }
                        if(blueprint.getOutputs().containsKey(OneLinkSupplyChainScenario.OUTPUT_GOOD))
                        {
                            if(!isDownstreamLearning())
                                hr.setPredictor(new FixedIncreasePurchasesPredictor(0));
                        }
                        return hr;
                    }
                };
        scenario.setControlType(MarginalMaximizer.class);
        scenario.setSalesDepartmentType(SalesDepartmentOneAtATime.class);
        scenario.setBeefPriceFilterer(null);


        //competition!
        scenario.setNumberOfBeefProducers(1);
        scenario.setBeefTargetInventory(100);
        scenario.setFoodTargetInventory(100);
        scenario.setNumberOfFoodProducers(5);

        scenario.setDivideProportionalGainByThis(timidity);
        scenario.setDivideIntegrativeGainByThis(timidity);
        //no delay
        scenario.setBeefPricingSpeed(stickiness);

        return scenario;

    }





}

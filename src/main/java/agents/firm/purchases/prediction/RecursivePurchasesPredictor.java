package agents.firm.purchases.prediction;

import agents.firm.personell.HumanResources;
import agents.firm.purchases.PurchasesDepartment;
import agents.firm.purchases.pricing.CheaterPricing;
import agents.firm.sales.prediction.AbstractRecursivePredictor;
import model.MacroII;
import model.utilities.stats.collectors.DataStorage;
import model.utilities.stats.collectors.enums.PurchasesDataType;

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
 * @version 2013-11-13
 * @see
 */
public class RecursivePurchasesPredictor extends AbstractRecursivePredictor implements PurchasesPredictor {

    boolean regressingOnWorkers = false;


    private final PurchasesDepartment department;

    /**
     * use delegate if it's a simple linear regression
     */
    private final FixedIncreasePurchasesPredictor delegate = new FixedIncreasePurchasesPredictor();


    public RecursivePurchasesPredictor(MacroII model,PurchasesDepartment department) {
        super(model);
        this.department = department;
        if(department instanceof HumanResources)
            this.setUsingWeights(false);

    }

    public RecursivePurchasesPredictor(MacroII model, int priceLags, int independentLags, PurchasesDepartment department) {
        super(model, priceLags, independentLags);
        this.department = department;
        if(department instanceof HumanResources)
            this.setUsingWeights(false);

    }

    public RecursivePurchasesPredictor(int priceLags, int independentLags, MacroII model, int timeDelay,
                                       int howFarIntoTheFutureToPredict,PurchasesDepartment department) {
        super(priceLags, independentLags, model, timeDelay, howFarIntoTheFutureToPredict);
        this.department = department;
        if(department instanceof HumanResources)
            this.setUsingWeights(false);

    }

    public RecursivePurchasesPredictor(MacroII model, double[] initialCoefficients, int priceLags,
                                       int independentLags, PurchasesDepartment department) {
        super(model, initialCoefficients, priceLags, independentLags,defaultMovingAverageSize);
        this.department = department;
        if(department instanceof HumanResources)
            this.setUsingWeights(false);
    }

    @Override
    public Enum getXVariableType() {
        if(regressingOnWorkers)
                return PurchasesDataType.WORKERS_CONSUMING_THIS_GOOD;
        else
            return PurchasesDataType.INFLOW;

    }

    @Override
    public Enum getYVariableType() {
        if(department.getPricingStrategyClass().equals(CheaterPricing.class))
            return PurchasesDataType.CLOSING_PRICES;
        else
            return PurchasesDataType.LAST_OFFERED_PRICE;

    }

    @Override
    public DataStorage getData() {
        return department.getPurchasesData();
    }

    @Override
    public int modifyStepIfNeeded(int step) {
        if(regressingOnWorkers)
            return Integer.signum(step);
        else
            return step;
    }

    @Override
    public double defaultPriceWithNoObservations() {
        return department.getLastClosingPrice();
    }

    public boolean isRegressingOnWorkers() {
        return regressingOnWorkers;
    }

    public void setRegressingOnWorkers(boolean regressingOnWorkers) {
        this.regressingOnWorkers = regressingOnWorkers;
    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public long predictPurchasePriceWhenIncreasingProduction(PurchasesDepartment dept) {
    //    System.err.println("slope " + (predictPrice(1)-predictPrice(0)));
        if(getIndependentLags() > 1 || getPriceLags() > 0)
            return Math.round(Math.max(predictPrice(1),predictPrice(0)));
        else{
            delegate.setIncrementDelta((float)(predictPrice(1)-predictPrice(0)));
            return delegate.predictPurchasePriceWhenIncreasingProduction(dept);
        }


    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public long predictPurchasePriceWhenNoChangeInProduction(PurchasesDepartment dept) {

        if(getIndependentLags() > 1 || getPriceLags() > 0)
            return Math.round(predictPrice(0));
        else{
            delegate.setIncrementDelta((float)(predictPrice(1)-predictPrice(0)));
            return delegate.predictPurchasePriceWhenNoChangeInProduction(dept);
        }

    }

    /**
     * Predicts the future price of the next good to buy
     *
     * @param dept the department that needs to buy it
     * @return the predicted price or -1 if there are no predictions.
     */
    @Override
    public long predictPurchasePriceWhenDecreasingProduction(PurchasesDepartment dept) {
        if(getIndependentLags() > 1 || getPriceLags() > 0)
            return Math.round(Math.min(predictPrice(-1), predictPrice(0)));
        else{
            delegate.setIncrementDelta((float)(predictPrice(0)-predictPrice(-1)));
            return delegate.predictPurchasePriceWhenDecreasingProduction(dept);
        }
    }

    @Override
    public PurchasesDataType getDisturbanceType() {
        return PurchasesDataType.DEMAND_GAP;


    }

}

package agents.firm.owner;

import agents.EconomicAgent;
import agents.firm.Firm;

/**
 * <h4>Description</h4>
 * <p/> This dividend strategy returns all the profits to the owner
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-20
 * @see
 */
public class TotalDividendStrategy implements DividendStrategy {
    private static TotalDividendStrategy ourInstance = new TotalDividendStrategy();

    public static TotalDividendStrategy getInstance() {
        return ourInstance;
    }

    private TotalDividendStrategy() {
    }

    /**
     * Tell the firm to pay dividends on this week profits
     *
     * @param aggregateProfits the total profits of the firm
     * @param firm             the firm paying the dividends
     * @param owner            the owner that should be paid
     */
    @Override
    public void payDividends(long aggregateProfits, Firm firm, EconomicAgent owner) {

        //don't bother if you are making losses
        if(aggregateProfits>0)
        {
            assert firm.hasEnoughCash(aggregateProfits);
            //pay!!
            firm.pay(aggregateProfits,owner,null);
        }
    }
}

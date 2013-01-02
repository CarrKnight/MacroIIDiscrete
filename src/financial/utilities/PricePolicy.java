package financial.utilities;

/**
 * <h4>Description</h4>
 * <p/> Just a method that is called by the market to adjudicate the price of two matching offers
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-01
 * @see
 */
public interface PricePolicy {

    public long price(long sellerPrice, long buyerPrice);

}


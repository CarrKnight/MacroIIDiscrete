package financial.utilities;

/**
 * <h4>Description</h4>
 * <p/> With this policy the price of trade is always the buyer quoted price
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-10-09
 * @see
 */
public class BuyerSetPricePolicy implements PricePolicy {
    @Override
    public long price(long sellerPrice, long buyerPrice) {
        assert buyerPrice>=sellerPrice;
        return buyerPrice;
    }
}

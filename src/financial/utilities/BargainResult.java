package financial.utilities;

/**
 * <h4>Description</h4>
 * <p/> This is a simple struct object containing
 * <ul>
 *     <li> Whether the agents agreed </li>
 *     <li> Buyer offer</li>
 *     <li> Seller offer</li>
 *     <li> Agreed price or -1 if they didn't agree</li>
 * </ul>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author
 * @version %I%, %G%
 * @see
 */
public class BargainResult {



    private final boolean success;

    private final long buyerOffer;

    private final long sellerOffer;

    private final long agreedPrice;

    public BargainResult(boolean success, long buyerOffer, long sellerOffer, long agreedPrice) {
        this.success = success;
        this.buyerOffer = buyerOffer;
        this.sellerOffer = sellerOffer;
        this.agreedPrice = agreedPrice;
        assert success || agreedPrice<0;

    }


    public boolean isSuccess() {
        return success;
    }

    public long getBuyerOffer() {
        return buyerOffer;
    }

    public long getSellerOffer() {
        return sellerOffer;
    }

    public long getAgreedPrice() {
        return agreedPrice;
    }
}

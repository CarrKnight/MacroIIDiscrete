package financial.utilities.priceLooker;

/**
 * <h4>Description</h4>
 * <p/> A very simple method that reports a price when asked.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-25
 * @see
 */
public interface PriceLookup {

    /**
     * Get the price you are supposed to look up to.
     * @return The price or -1 if there is no price
     */
    public long getPrice();

}

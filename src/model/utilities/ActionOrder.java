package model.utilities;

/**
 * <h4>Description</h4>
 * <p/> In the synchronized model we let everybody act each day. This enum holds the ordering for various actions so that everything still makes sense
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2012-11-19
 * @see
 */
public enum ActionOrder
{

    /**
     * things are started here!
     */
    DAWN,


    /**
     * Stuff gets produced here
     */
    PRODUCTION,

    /**
     * Set up prices and what can/can't be sold
     */
    PREPARE_TO_TRADE,

    /**
     * Match, peddle, shop
     */
    TRADE,

    /**
     * After trading has occurred, think of the consequences
     */
    THINK,


    /**
     * Final Phase, just maintenance
     */
    CLEANUP



}

package financial;

/**
 * <h4>Description</h4>
 * <p/> This is a long list of events that could be visualized by Lifelines
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-09-06
 * @see
 */
public enum MarketEvents {


    /************************************
     * MARKET EVENTS
     ***********************************/

    REGISTERED_AS_BUYER(true,false),

    DEREGISTERED_AS_BUYER(true,false),


    REGISTERED_AS_SELLER(false,false),

    DEREGISTERED_AS_SELLER(true,false),

    BOUGHT(true,true),

    SOLD(true,true),

    SUBMIT_BUY_QUOTE(true,false),

    SUBMIT_SELL_QUOTE(true,false),

    REMOVE_BUY_QUOTE(true,false),

    REMOVE_SELL_QUOTE(true,false),

    /************************************
     * FIRM EVENTS
     ***********************************/
    /**
     * A target has been changed, this is usually due to control
     */
    CHANGE_IN_TARGET(false,true),

    /**
     * 1 or more workers have been hired
     */
    HIRED_WORKER(false,true),

    /**
     * 1 or more workers have been fired/quit
     */
    LOST_WORKER(false,true),

    /**
     * A plant has started production
     */
    PRODUCTION_STARTED(false,true),

    /**
     * A plant has finished production
     */
    PRODUCTION_COMPLETE(false,true),

    /**
     * A plant production is halted because of lack of input/labor
     */
    PRODUCTION_HALTED(false,true),

    /**
     * A plant has changed machinery
     */
    MACHINERY_CHANGE(false,true),

    /**
     * The firm told a sales department to sell a good
     */
    TASKED_TO_SELL(false,true),

    /**
     * The purchase department is told to buy a new good
     */
    TASKED_TO_BUY(false,true),

    /**
     * A component changed its policy/price. This is classic PID situation
     */
    CHANGE_IN_POLICY(false,true),

    /**
     * Any exogenous event
     */
    EXOGENOUS(false,false);


    private final boolean marketVisible;

    private final boolean plantVisible;

    private MarketEvents(boolean marketVisible, boolean plantVisible) {
        this.marketVisible = marketVisible;
        this.plantVisible = plantVisible;
    }


    public boolean isMarketVisible() {
        return marketVisible;
    }

    public boolean isPlantVisible() {
        return plantVisible;
    }


    static{

        //record the logEvent names for visualization later
   //     GlobalEventData.getInstance().reset();
   //     for(MarketEvents logEvent : MarketEvents.values()){
   //        GlobalEventData.getInstance().registerEventName(logEvent.name());
   //     }
    }
}

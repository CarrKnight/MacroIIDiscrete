package model.scenario.oil;

import agents.EconomicAgent;
import agents.firm.GeographicalFirm;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.sun.istack.internal.Nullable;
import ec.util.MersenneTwisterFast;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.geography.HasLocation;
import model.utilities.geography.Location;
import sim.engine.SimState;
import sim.engine.Steppable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Comparator;

/**
 * <h4>Description</h4>
 * <p/> This is supposed to be the final demand of good in the oil pump scenario;
 * Every day it puts sale quotes it doesn't expect to be filled, at the end of the day it always
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-10-27
 * @see
 */
public class OilCustomer extends EconomicAgent implements HasLocation{

    /**
     * how many units do you want to buy every day?
     */
    private int dailyDemand = 1;

    /**
     * the maximum price the customer is willing to pay for its stuff, that is
     * oilPrice + distanceCost * distance <= maxPrice to buy.
     */
    private long maxPrice;


    private final Location location;

    /**
     * every day, after consuming the customer gets back the total cash to this number
     */
    private long resetCashTo = 100000;


    /**
     * the firm that supplied you the last unit of oil; as a JavaFX property so that I don't have to implement listeners
     */
    private final SimpleObjectProperty<GeographicalFirm> lastSupplier = new SimpleObjectProperty<>();
    private GoodType goodTypeBought = GoodType.OIL;


    public OilCustomer(@Nonnull MacroII model, long maxPrice, double x, double y) {
        super(model, 100000);
        Preconditions.checkArgument(maxPrice>0);
        this.maxPrice = maxPrice;
        this.location = new Location(x,y);
        startSteppables();
    }


    private void startSteppables()
    {
        //every day, at "production" eat all the oil and start over (also make sure your cash remains the same)
        model.scheduleSoon(ActionOrder.PRODUCTION, new Steppable() {
            @Override
            public void step(SimState state) {
                eatEverythingAndResetCash();
                //reschedule yourself
                model.scheduleTomorrow(ActionOrder.PRODUCTION,this);
            }
        });

        //schedule yourself to try to buy every day
        model.scheduleSoon(ActionOrder.TRADE, new Steppable() {
            @Override
            public void step(SimState state) {
                buyIfNeeded();
                //reschedule yourself
                model.scheduleTomorrow(ActionOrder.TRADE,this);

            }
        });

    }

    private void eatEverythingAndResetCash() {
        //eat all!
        consumeAll();
        long cashDifference = resetCashTo-getCash();
        if(cashDifference > 0)
            earn(cashDifference);
        else if(cashDifference < 0)
            burnMoney(-cashDifference);

        assert getCash() == resetCashTo;
    }


    /**
     * Chooses which of this firms the customer wants to choose, if any.
     * @param firmsToChooseFrom The list of available firms
     * @return The firm chosen, or null if none is chosen
     */
    @Nullable
    public GeographicalFirm chooseSupplier(@Nonnull final Multimap<GeographicalFirm,Quote> firmsToChooseFrom)
    {
        Preconditions.checkArgument(!firmsToChooseFrom.isEmpty());



        //basically we want to find the minimum price+distance
        GeographicalFirm best = Collections.min(firmsToChooseFrom.keySet(),new Comparator<GeographicalFirm>() {
            @Override
            public int compare(GeographicalFirm o1, GeographicalFirm o2) {
                //price + distance
                double pricePlusDistance1 =
                        firmsToChooseFrom.get(o1).iterator().next().getPriceQuoted() + distance(OilCustomer.this,o1);
                assert pricePlusDistance1 >=0;

                double pricePlusDistance2 =
                        firmsToChooseFrom.get(o2).iterator().next().getPriceQuoted() + distance(OilCustomer.this,o2);
                assert pricePlusDistance2 >=0;

                return Double.compare(pricePlusDistance1,pricePlusDistance2);
            }
        });

        assert best != null;
        //is the minimum price distance okay?
        double bestPricePlusDistance = firmsToChooseFrom.get(best).iterator().next().getPriceQuoted() + distance(OilCustomer.this,best);
        if(bestPricePlusDistance <= maxPrice)
            return best;
        else
            return null;




    }

    /**
     * just an easy way to compute the monetary costs of distance
     * @param customer
     * @param seller
     * @return
     */
    private double distance(HasLocation customer, HasLocation seller )
    {
        //for now just pitagorean distance
        double xDistance = Math.pow(customer.getxLocation() - seller.getxLocation(),2);
        double yDistance = Math.pow(customer.getxLocation() - seller.getxLocation(),2);

        double distance =  Math.sqrt(xDistance + yDistance);
        assert distance >= 0;
        return distance;
    }


    private void buyIfNeeded() {



    }

    @Override
    public double getxLocation() {
        return location.getxLocation();
    }

    @Override
    public DoubleProperty xLocationProperty() {
        return location.xLocationProperty();
    }

    @Override
    public void setxLocation(double xLocation) {
        location.setxLocation(xLocation);
    }

    @Override
    public double getyLocation() {
        return location.getyLocation();
    }

    @Override
    public DoubleProperty yLocationProperty() {
        return location.yLocationProperty();
    }

    @Override
    public void setyLocation(double yLocation) {
        location.setyLocation(yLocation);
    }


    @Override
    public MersenneTwisterFast getRandom() {
        throw new RuntimeException("not implemented yet!");
    }

    @Override
    public void reactToFilledAskedQuote(Good g, long price, EconomicAgent buyer) {
        throw new RuntimeException("not implemented yet!");
    }

    @Override
    public void reactToFilledBidQuote(Good g, long price, EconomicAgent seller) {
        throw new RuntimeException("not implemented yet!");
    }

    /**
     * This is called by a peddler/survey to the agent and asks what would be the maximum he's willing to pay to buy a new good.
     *
     * @param g the good the peddler/survey is pushing
     * @return the maximum price willing to pay or -1 if no price is good.
     */
    @Override
    public long maximumOffer(Good g) {
        throw new RuntimeException("not implemented yet!");
    }

    /**
     * This is called by a peddler/survey to the agent and asks what would be the maximum he's willing to pay to buy a new good.
     *
     * @param t the type good the peddler/survey is pushing
     * @return the maximum price willing to pay or -1 if no price is good.
     */
    @Override
    public long askedForABuyOffer(GoodType t) {
        throw new RuntimeException("not implemented yet!");
    }

    /**
     * A buyer asks the sales department for the price they are willing to sell one of their good.
     *
     * @param buyer the economic agent that asks you that
     * @return a price quoted or -1 if there are no quotes
     */
    @Override
    public Quote askedForASaleQuote(EconomicAgent buyer, GoodType type) {
        throw new RuntimeException("not implemented yet!");
    }

    /**
     * Called by a buyer that wants to buy directly from this agent, not going through the market.
     *
     * @param buyerQuote  the quote (including the offer price) of the buyer
     * @param sellerQuote the quote (including the offer price) of the seller; I expect the buyer to have achieved this through asked for an offer function
     * @return a purchaseResult including whether the trade was succesful and if so the final price
     */
    @Nonnull
    @Override
    public PurchaseResult shopHere(@Nonnull Quote buyerQuote, @Nonnull Quote sellerQuote) {
        throw new RuntimeException("not implemented yet!");
    }

    /**
     * how "far" purchases inventory are from target.
     */
    @Override
    public int estimateDemandGap(GoodType type) {
        return hasHowMany(goodTypeBought) - dailyDemand;
    }

    /**
     * how "far" sales inventory are from target.
     */
    @Override
    public int estimateSupplyGap(GoodType type) {
            return 0;
    }


    /**
     * Sets how many units you want to buy every day.
     *
     * @param dailyDemand needs to be positive
     */
    public void setDailyDemand(int dailyDemand) {
        Preconditions.checkArgument(dailyDemand > 0);
        this.dailyDemand = dailyDemand;
    }

    /**
     * Gets how many units you want to buy every day.
     *
     */
    public int getDailyDemand() {
        return dailyDemand;
    }


    /**
     * Sets new "every day, after consuming the customer gets back the total cash to this number".
     *
     * @param resetCashTo New value of every day, after consuming the customer gets back the total cash to this number.
     */
    public void setResetCashTo(long resetCashTo) {
        Preconditions.checkArgument(resetCashTo >= 0);
        this.resetCashTo = resetCashTo;
    }

    /**
     * Gets "every day, after consuming the customer gets back the total cash to this number".
     *
     * @return Value of every day, after consuming the customer gets back the total cash to this number.
     */
    public long getResetCashTo() {
        return resetCashTo;
    }


}

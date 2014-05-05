package model.utilities.dummies;

import agents.HasInventory;
import agents.firm.GeographicalFirm;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import financial.market.GeographicalMarket;
import financial.market.Market;
import financial.utilities.Quote;
import goods.Good;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.geography.HasLocation;
import model.utilities.geography.Location;
import model.utilities.logs.LogEvent;
import model.utilities.logs.LogLevel;
import sim.engine.SimState;
import sim.engine.Steppable;

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
public class GeographicalCustomer extends Customer implements HasLocation{



    private  Location location;

    /**


    /**
     * the firm that supplied you the last unit of oil; as a JavaFX property so that I don't have to implement listeners
     */
    private SimpleObjectProperty<GeographicalFirm> lastSupplier;

    private double distanceExponent =1;


    public GeographicalCustomer( MacroII model, long maxPrice, double x, double y, GeographicalMarket market) {
        super(model, maxPrice,market);
        location.setxLocation(x);
        location.setyLocation(y);

        model.scheduleSoon(ActionOrder.DAWN, new Steppable() {
            @Override
            public void step(SimState simState) {
                if (!GeographicalCustomer.this.isActive())
                    return;
                lastSupplier.setValue(null);
                model.scheduleTomorrow(ActionOrder.DAWN, this);
            }
        });

    }


    @Override
    protected void init() {
        this.location = new Location(0,0);
        lastSupplier = new SimpleObjectProperty<>();


    }



    /**
     * Chooses which of this firms the customer wants to choose, if any.
     * @param firmsToChooseFrom The list of available firms
     * @return The firm chosen, or null if none is chosen
     */

    public GeographicalFirm chooseSupplier( final Multimap<GeographicalFirm,Quote> firmsToChooseFrom)
    {
        Preconditions.checkArgument(!firmsToChooseFrom.isEmpty());
        handleNewEvent(new LogEvent(this, LogLevel.TRACE,"was given these firms to choose from: {}",firmsToChooseFrom));



        //basically we want to find the minimum price+distance
        GeographicalFirm best = Collections.min(firmsToChooseFrom.keySet(),new Comparator<GeographicalFirm>() {
            @Override
            public int compare(GeographicalFirm o1, GeographicalFirm o2) {
                //price + distance
                double pricePlusDistance1 =
                        firmsToChooseFrom.get(o1).iterator().next().getPriceQuoted() + distance(GeographicalCustomer.this,o1);
                assert pricePlusDistance1 >=0;

                double pricePlusDistance2 =
                        firmsToChooseFrom.get(o2).iterator().next().getPriceQuoted() + distance(GeographicalCustomer.this,o2);
                assert pricePlusDistance2 >=0;

                return Double.compare(pricePlusDistance1,pricePlusDistance2);
            }
        });

        assert best != null;
        //is the minimum price distance okay?

        final long bestPriceAtSource = firmsToChooseFrom.get(best).iterator().next().getPriceQuoted();
        double bestPricePlusDistance = bestPriceAtSource + distance(GeographicalCustomer.this,best);
        //log it!
        handleNewEvent(new LogEvent(this, LogLevel.TRACE,"the best firm found was {}, pricing {}, total personal cost {}",
                best,bestPriceAtSource,bestPricePlusDistance));

        if(bestPricePlusDistance <= getMaxPrice())
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

        double distance =  Math.pow(Math.sqrt(xDistance + yDistance), distanceExponent);
        assert distance >= 0;
        return distance;
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
    /**
     * This method is called when inventory has to increase by 1. The reference to the sender is for accounting purpose only
     *
     * @param g      what good is delivered?
     * @param sender who sent it?
     */
    @Override
    public void receive(Good g,  HasInventory sender) {
        super.receive(g, sender);
        if(sender!=null && g.getType().equals(getMarket().getGoodType()) && sender instanceof GeographicalFirm)
            lastSupplier.setValue((GeographicalFirm) sender);
    }


    //i override this to call removeAllBuyQuoteByBuyer because it's faster for geographical markets
    @Override
    protected void removeAllQuotes(Market market) {
        market.removeAllBuyQuoteByBuyer(this);
        bidsMade.clear();
    }

    public GeographicalFirm getLastSupplier() {
        return lastSupplier.get();
    }

    public SimpleObjectProperty<GeographicalFirm> lastSupplierProperty() {
        return lastSupplier;
    }

    public double getDistanceExponent() {
        return distanceExponent;
    }

    public void setDistanceExponent(double distanceExponent) {
        System.out.println(distanceExponent);
        this.distanceExponent = distanceExponent;
    }
}

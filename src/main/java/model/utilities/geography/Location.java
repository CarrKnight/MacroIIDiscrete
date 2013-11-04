package model.utilities.geography;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * <h4>Description</h4>
 * <p/> To be used for any agent who has a location we want to keep track of.
 * It uses JavaFX properties to avoid having to deal with listeners and so on.
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
public class Location
{
    /**
     * the x of the agent/object
     */
    private final DoubleProperty xLocation;

    /**
     * the y of the agent/object
     */
    private final DoubleProperty yLocation;

    /**
     * creates the initial location
     * @param x ordinate
     * @param y abscissa
     */
    public Location( double x, double y)
    {
        xLocation = new SimpleDoubleProperty(x);
        yLocation = new SimpleDoubleProperty(y);

    }

    public double getxLocation() {
        return xLocation.get();
    }

    public DoubleProperty xLocationProperty() {
        return xLocation;
    }

    public void setxLocation(double xLocation) {
        this.xLocation.set(xLocation);
    }

    public double getyLocation() {
        return yLocation.get();
    }

    public DoubleProperty yLocationProperty() {
        return yLocation;
    }

    public void setyLocation(double yLocation) {
        this.yLocation.set(yLocation);
    }
}

package model.utilities.geography;

import javafx.beans.property.DoubleProperty;

/**
 * <h4>Description</h4>
 * <p/>
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
public interface HasLocation
{



    public double getxLocation();

    public DoubleProperty xLocationProperty();

    public void setxLocation(double xLocation);

    public double getyLocation();

    public DoubleProperty yLocationProperty();

    public void setyLocation(double yLocation);
}

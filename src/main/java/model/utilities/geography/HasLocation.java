/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

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

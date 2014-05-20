/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.firm;

import javafx.beans.property.DoubleProperty;
import model.MacroII;
import model.utilities.geography.HasLocation;
import model.utilities.geography.Location;


/**
 * <h4>Description</h4>
 * <p/> Like a Firm, but HasLocation
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
public class GeographicalFirm extends Firm implements HasLocation {


    private final Location location;

    /**
     * the usual constructor, checks for gui to build panel
     *
     * @param model
     */
    public GeographicalFirm(MacroII model, double xLocation, double yLocation) {
        super(model);
        location = new Location(xLocation,yLocation);
    }

    /**
     * alternative constructor, NEVER builds panel/inspector
     *
     * @param model
     */
    public GeographicalFirm(MacroII model, boolean ignored, double xLocation, double yLocation) {
        super(model, ignored);
        location = new Location(xLocation,yLocation);

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

}

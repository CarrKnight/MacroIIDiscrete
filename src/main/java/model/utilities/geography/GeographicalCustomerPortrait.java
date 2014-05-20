/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.geography;

import agents.EconomicAgent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import model.utilities.dummies.GeographicalCustomer;

import java.io.InputStream;


/**
 * <h4>Description</h4>
 * <p/> A simple home picture that gets colored
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-11-07
 * @see
 */
public class GeographicalCustomerPortrait extends HasLocationPortrait
{

    //load the image once statically and be done with it
    public final static Image CUSTOMER_IMAGE;
    static {
        InputStream input = GeographicalCustomerPortrait.class.getClassLoader().
                getResourceAsStream("images/homeIcon.png");
        CUSTOMER_IMAGE = new Image(input);
    }

    @Override
    protected Image initImage(EconomicAgent agent) {
        return CUSTOMER_IMAGE;
    }


    public GeographicalCustomerPortrait(GeographicalCustomer agent) {
        super(agent,agent.xLocationProperty(),agent.yLocationProperty());


        priceText.setText(Long.toString(agent.getMaxPrice())+ "$");
        StackPane.setAlignment(priceText,Pos.BOTTOM_CENTER);
        StackPane.setMargin(icon, new Insets(8,8,8,8));
    }




}

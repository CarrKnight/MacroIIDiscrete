/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.geography;

import javafx.geometry.Pos;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.InputStream;

/**
 * I might turn this into an anonymous class at some point since it's just used once. But basically it's a "oil-pump" with a +
 * on it to signify "click here to add a new one"
 * Created by carrknight on 4/24/14.
 */
public class NewFirmPortrait extends StackPane {



    public final static Image plusImage;
    static {
        InputStream input = GeographicalCustomerPortrait.class.getClassLoader().
                getResourceAsStream("images/plus.png");
        plusImage = new Image(input);
    }

    /**
     * take the oil pump image and adds a plus to it
     */
    public NewFirmPortrait() {

        //set big enough
        this.prefWidth(100);
        this.prefHeight(100);

        ImageView icon = new ImageView(GeographicalFirmPortrait.oilImage);
        icon.fitWidthProperty().bind(prefWidthProperty());
        icon.fitHeightProperty().bind(prefHeightProperty());
        ImageView plus = new ImageView(plusImage);
        plus.setEffect(new Glow(2));
        plus.fitWidthProperty().bind(prefWidthProperty().divide(3));
        plus.fitHeightProperty().bind(prefHeightProperty().divide(3));


        //we are going to "merge" them using the stackpane trick
        getChildren().addAll(icon,plus);
        StackPane.setAlignment(icon, Pos.TOP_LEFT);
        StackPane.setAlignment(plus, Pos.TOP_LEFT);
        this.setBlendMode(BlendMode.SRC_OVER);

    }


}

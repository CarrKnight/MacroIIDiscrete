/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.geography;

import javafx.scene.Group;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

/**
 * I might turn this into an anonymous class at some point since it's just used once. But basically it's a "oil-pump" with a +
 * on it to signify "click here to add a new one"
 * Created by carrknight on 4/24/14.
 */
public class NewFirmPortrait extends Group {



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
        super();


        ImageView firm = new ImageView(GeographicalFirmPortrait.oilImage);
        firm.setFitWidth(100);
        firm.setPreserveRatio(true);
        firm.setSmooth(true);
        firm.setCache(true);

        ImageView addPlus = new ImageView(plusImage);
        addPlus.fitWidthProperty().bind(firm.fitWidthProperty().divide(2));
        addPlus.setPreserveRatio(true);
        addPlus.setSmooth(true);
        addPlus.setCache(true);
        addPlus.setBlendMode(BlendMode.SRC_OVER);

        this.getChildren().addAll(firm,addPlus);





    }


}

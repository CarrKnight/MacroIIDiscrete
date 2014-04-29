/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.geography;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

/**
 * I might turn this into an anonymous class at some point since it's just used once. But basically it's a "oil-pump" with a +
 * on it to signify "click here to add a new one"
 * Created by carrknight on 4/24/14.
 */
public class NewAgentPortrait extends Group {



    public final static Image PLUS_IMAGE;
    static {
        InputStream input = GeographicalCustomerPortrait.class.getClassLoader().
                getResourceAsStream("images/plus.png");
        PLUS_IMAGE = new Image(input);
    }

    private final DoubleProperty size;

    /**
     * takes the oil pump image and adds a plus to it
     */
    public NewAgentPortrait(HasLocationPortrait originalPortrait) {

        this(originalPortrait.getImage());


    }

    /**
     * takes the oil pump image and adds a plus to it
     */
    public NewAgentPortrait(Image originalImage) {
        super();



        ImageView firm = new ImageView(originalImage);
        size = firm.fitWidthProperty();
        size.setValue(100);//just so that it has a value
        firm.setPreserveRatio(true);
        firm.setSmooth(true);
        firm.setCache(true);

        ImageView addPlus = new ImageView(PLUS_IMAGE);
        addPlus.setEffect(new Glow(2));
        addPlus.fitWidthProperty().bind(firm.fitWidthProperty().divide(2));
        addPlus.setPreserveRatio(true);
        addPlus.setSmooth(true);
        addPlus.setCache(true);
        addPlus.setBlendMode(BlendMode.SRC_OVER);

        this.getChildren().addAll(firm,addPlus);


    }




    public double getSize() {
        return size.get();
    }

    public DoubleProperty sizeProperty() {
        return size;
    }

    public void setSize(double size) {
        this.size.set(size);
    }
}

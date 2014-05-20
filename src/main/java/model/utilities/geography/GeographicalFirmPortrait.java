/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.geography;

import agents.EconomicAgent;
import agents.firm.GeographicalFirm;
import goods.GoodType;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.InputStream;

/**
 * <h4>Description</h4>
 * <p/> The little image we give to the oil pump when we draw it!
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-11-08
 * @see
 */
public class GeographicalFirmPortrait extends HasLocationPortrait implements Deactivatable {


    private boolean active = true;

    //load the image once statically and be done with it
    public final static Image OIL_IMAGE;
    static {
        InputStream input = GeographicalCustomerPortrait.class.getClassLoader().
                getResourceAsStream("images/gas-pump.png");
        OIL_IMAGE = new Image(input);
    }

    /**
     * the firm you are linked to
     */
    final private GeographicalFirm firm;

    @Override
    protected Image initImage(EconomicAgent agent) {
        return OIL_IMAGE;

    }

    public GeographicalFirmPortrait(final GeographicalFirm agent,
                                      Color firmColor, GoodType goodSold, MacroII model) {
        super(agent,agent.xLocationProperty(),agent.yLocationProperty());
        color.setValue(firmColor);
        this.firm = agent;
        //add a glow effect
        Glow glow = new Glow(3);
        Blend blend = new Blend(BlendMode.SRC_OVER, glow,icon.effectProperty().getValue());

        icon.setEffect(blend);



        //schedule yourself to update your text
        StackPane.setAlignment(priceText, Pos.CENTER);
        priceText.setFont(Font.font("Verdana", FontWeight.BOLD,10));
        model.scheduleSoon(ActionOrder.GUI_PHASE,new Steppable() {
            @Override
            public void step(SimState state) {
                if(!active)
                    return;
                long price = agent.getSalesDepartment(goodSold).getLastAskedPrice();
                Platform.runLater(() -> priceText.setText(String.valueOf(price)));
                //reschedule
                model.scheduleTomorrow(ActionOrder.GUI_PHASE,this);
            }
        });
        //register as deactivable with the model
        model.registerDeactivable(this);

    }

    public GeographicalFirm getFirm() {
        return firm;
    }

    @Override
    public void turnOff() {
        active = false;
    }
}

package model.utilities.geography;

import javafx.beans.value.ObservableObjectValue;
import javafx.scene.CacheHint;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

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
 * @version 2013-11-08
 * @see
 */
public abstract class HasLocationPortrait extends ImageView {

    abstract protected Image initImage(HasLocation agent, GeographicalClearLastMarketView marketView);

    abstract protected ObservableObjectValue<Color> initColor(HasLocation agent, GeographicalClearLastMarketView marketView);


    protected HasLocationPortrait(HasLocation agent, GeographicalClearLastMarketView marketView)
    {
        //load the image
        setImage(initImage(agent, marketView));

        //set layout properly
        //force the layout
        layoutXProperty().bind(agent.xLocationProperty());
        layoutYProperty().bind(agent.yLocationProperty());

        //resize image
        setFitHeight(32);
        setFitWidth(32);

        //this is to keep it with the right color
        ColorAdjust monochrome = new ColorAdjust(); //this is the first effect
        //monochrome.setBrightness(-1.0);
        //keep the color
        ObservableObjectValue<Color> color= initColor(agent, marketView);

        ColorInput input = new ColorInput(); //this is the second effect, the coloring proper
        input.setX(0); input.setY(0);
        input.setWidth(getImage().getWidth()); input.setHeight(getImage().getHeight());
        //todo might want to bind the x,y,width and height
        input.paintProperty().bind(color);

        //bind the color adjust
        Blend coloring = new Blend(BlendMode.SRC_ATOP,monochrome,input);
        effectProperty().setValue(coloring);

        setCache(true);
        setCacheHint(CacheHint.SPEED);

    }
}

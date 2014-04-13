package model.utilities.geography;

import com.google.common.base.Preconditions;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.CacheHint;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * <h4>Description</h4>
 * <p/> A "stackpane" made of an icon and a tex on top of it
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
public abstract class HasLocationPortrait extends StackPane {

    protected final ImageView icon;

    protected final Text priceText;

    abstract protected Image initImage(HasLocation agent);

    protected final SimpleObjectProperty<Color> color;

    protected final HasLocation agent;

    protected HasLocationPortrait(HasLocation agent)
    {
        this.agent =agent;
        //load the image
        icon = new ImageView();
        icon.setImage(initImage(agent));
        this.getChildren().add(icon);
        //bind icon height to pref-height. The idea here is that when they resize this region, we resize the location portrait as well
        icon.fitWidthProperty().bind(prefWidthProperty());
        icon.fitHeightProperty().bind(prefHeightProperty());


        //this is to keep it with the right color
        ColorAdjust monochrome = new ColorAdjust(); //this is the first effect
        //monochrome.setBrightness(-1.0);
        //keep the color
        color= new SimpleObjectProperty<>(Color.BLACK);


        ColorInput input = new ColorInput(); //this is the second effect, the coloring proper
        input.setX(0); input.setY(0);
        input.widthProperty().bind(prefWidthProperty());
        input.heightProperty().bind(prefHeightProperty());

        input.paintProperty().bind(color);

        //bind the color adjust
        Blend coloring = new Blend(BlendMode.SRC_ATOP,monochrome,input);
        icon.effectProperty().setValue(coloring);

        setCache(true);
        setCacheHint(CacheHint.SPEED);


        //now set the text
        priceText = new Text();
        this.getChildren().add(priceText );
        priceText.setFont(Font.font("Verdana", 10));

        priceText.setFill(Color.BLACK);

    }


    /**
     * the x location of the agent (in model coordinates)
     * @return
     */
    public DoubleProperty agentXLocationProperty() {
        return agent.xLocationProperty();
    }

    /**
     * the y location of the agent (in model coordinates)
     * @return
     */
    public DoubleProperty agentYLocationProperty() {
        return agent.yLocationProperty();
    }

    public HasLocation getAgent() {
        return agent;
    }

    public Color getColor() {
        return color.get();
    }

    public SimpleObjectProperty<Color> colorProperty() {
        return color;
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    public Text getPriceText() {
        return priceText;
    }
}

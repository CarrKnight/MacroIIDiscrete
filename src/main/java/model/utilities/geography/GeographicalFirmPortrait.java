package model.utilities.geography;

import agents.firm.GeographicalFirm;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.nio.file.Path;
import java.nio.file.Paths;

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
public class GeographicalFirmPortrait extends HasLocationPortrait {



    //load the image once statically and be done with it
    private final static Image oilImage;
    static {
        Path imageLink = Paths.get("resources", "gas-pump.png");
        oilImage = new Image(imageLink.toUri().toString());
    }

    /**
     * the firm you are linked to
     */
    final private GeographicalFirm firm;

    @Override
    protected Image initImage(HasLocation agent) {
        return oilImage;

    }

    public GeographicalFirmPortrait(HasLocation agent,
                                      Color firmColor) {
        super(agent);
        assert agent instanceof GeographicalFirm;
        color.setValue(firmColor);
        this.firm = (GeographicalFirm) agent;
        //add a glow effect
        Blend blend = new Blend(BlendMode.SRC_OVER,new Glow(3),this.effectProperty().getValue());
        this.setEffect(blend);
    }

    public GeographicalFirm getFirm() {
        return firm;
    }
}

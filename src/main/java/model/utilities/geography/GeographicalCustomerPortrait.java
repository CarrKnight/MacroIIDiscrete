package model.utilities.geography;

import javafx.application.Application;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.scenario.oil.GeographicalCustomer;

import java.nio.file.Path;
import java.nio.file.Paths;


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
    private final static Image customerImage;
    static {
        Path imageLink = Paths.get("resources", "homeIcon.png");
        customerImage = new Image(imageLink.toUri().toString());
    }

    @Override
    protected Image initImage(HasLocation agent) {
        return customerImage;
    }


    public GeographicalCustomerPortrait(GeographicalCustomer agent) {
        super(agent);

        //add a label describing its price
        Label price = new Label("lol",this);
        price.setContentDisplay(ContentDisplay.TOP);

        Text priceText = new Text(Long.toString(agent.getMaxPrice()));
        this.getChildren().add(priceText);

        this.setAlignment(Pos.CENTER);
    }




}

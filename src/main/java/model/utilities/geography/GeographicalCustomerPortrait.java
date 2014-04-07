package model.utilities.geography;

import javafx.application.Application;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.scenario.oil.GeographicalCustomer;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.*;

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
    }




}
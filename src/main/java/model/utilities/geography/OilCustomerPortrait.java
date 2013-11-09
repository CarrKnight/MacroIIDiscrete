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
import model.scenario.oil.OilCustomer;

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
public class OilCustomerPortrait extends HasLocationPortrait
{

    //load the image once statically and be done with it
    private final static Image customerImage;
    static {
        Path imageLink = Paths.get("resources", "homeIcon.png");
        customerImage = new Image(imageLink.toUri().toString());
    }

    @Override
    protected Image initImage(HasLocation agent, GeographicalClearLastMarketView marketView) {
        return customerImage;
    }


    @Override
    protected ObservableObjectValue<Color> initColor(final HasLocation agent, GeographicalClearLastMarketView marketView) {
        final OilCustomer agentCast = (OilCustomer) agent;

       return new ObjectBinding<Color>() {
            {
                this.bind(agentCast.lastSupplierProperty());
            }
            @Override
            protected Color computeValue() {

                if(agentCast.lastSupplierProperty().get()==null)
                    return Color.BLACK;
                else
                    return agentCast.lastSupplierProperty().get().colorProperty().get();
            }
        };    }

    /**
     * the oil customer we are trying to paint!
     */
    private final OilCustomer oilCustomer;


    public OilCustomerPortrait(OilCustomer agent, GeographicalClearLastMarketView marketView) {
        super(agent, marketView);
        this.oilCustomer = agent;
    }

    public DoubleProperty xLocationProperty() {
        return oilCustomer.xLocationProperty();
    }

    public DoubleProperty yLocationProperty() {
        return oilCustomer.yLocationProperty();
    }


    public static void main(String[] args) throws Exception {
        Application.launch(TestApplication.class);

    }


    public static class TestApplication extends Application
    {
        @Override
        public void start(Stage stage) throws Exception {
            StackPane panel = new StackPane();
            OilCustomer fakeCustomer = mock(OilCustomer.class);


            final SimpleObjectProperty<Color> color = new SimpleObjectProperty<>(Color.BLUE);
         //   when(fakeCustomer.colorProperty()).thenReturn(color);
            panel.getChildren().addAll(new OilCustomerPortrait(fakeCustomer,mock(GeographicalClearLastMarketView.class)));


            Scene scene = new Scene(panel);

            stage.setScene(scene);

            stage.show();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println(color.get());
                        Thread.sleep(2000);
                        color.setValue(Color.RED);
                        System.out.println(color.get());
                        Thread.sleep(2000);
                        color.setValue(Color.BLACK);

                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                }
            });
            thread.start();


        }
    }
}

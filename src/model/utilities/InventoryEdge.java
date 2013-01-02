package model.utilities;

import goods.GoodType;
import org.apache.commons.collections15.Transformer;

import java.awt.*;

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
 * @author Ernesto
 * @version 2012-09-08
 * @see
 */
public class InventoryEdge {

    int quantity = 0;


    final private GoodType type;

    private Integer howMuch;

    public InventoryEdge(GoodType type, Integer howMuch)
    {
        this.type = type;
        this.howMuch = howMuch;
    }


    public GoodType getType() {
        return type;
    }

    public Integer getHowMuch() {
        return howMuch;
    }

    public void setHowMuch(Integer howMuch) {
        this.howMuch = howMuch;
    }


    @Override
    public String toString() {
        return howMuch + " " + type.toString();
    }

    /**
     * This is used by the graph to draw the edge
     */
    public static Transformer<InventoryEdge,Stroke> edgeStrokeTransformer = new Transformer<InventoryEdge, Stroke>() {
        @Override
        public Stroke transform(InventoryEdge inventoryEdge) {
            return new BasicStroke((float) Math.sqrt(inventoryEdge.getHowMuch()),
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        }
    };


}

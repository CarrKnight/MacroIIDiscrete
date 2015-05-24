package model.gui.paper2;

import model.MacroII;
import sim.display.Console;

/**
 * Created by carrknight on 5/23/15.
 */
public class DownstreamMonopolistGUI extends UpstreamMonopolistGUI {
    public DownstreamMonopolistGUI(MacroII state) {
        super(state);
    }


    @Override
    protected int getEquilibriumQuantity() {
        return 17;
    }

    @Override
    protected int getEquilibriumPriceDownstream() {
        return 85;
    }

    @Override
    protected int getEquilibriumPriceUpstream() {
        return 17;
    }

    @Override
    protected SupplyChainForm getScenarioFactory() {
        return new DownstreamMonopolistFactory();
    }

    public static void main(String[] args){
        DownstreamMonopolistGUI gui = new DownstreamMonopolistGUI(new MacroII(System.currentTimeMillis()));
        Console c = new Console(gui);
        c.setVisible(true);
    }
}

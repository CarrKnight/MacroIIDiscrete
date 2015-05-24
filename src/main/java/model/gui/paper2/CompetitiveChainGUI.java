package model.gui.paper2;

import model.MacroII;
import sim.display.Console;

/**
 * Created by carrknight on 5/23/15.
 */
public class CompetitiveChainGUI extends UpstreamMonopolistGUI {
    public CompetitiveChainGUI(MacroII state) {
        super(state);
    }

    @Override
    protected int getEquilibriumQuantity() {
        return 34;
    }

    @Override
    protected int getEquilibriumPriceDownstream() {
        return 68;
    }

    @Override
    protected int getEquilibriumPriceUpstream() {
        return 34;
    }

    @Override
    protected SupplyChainForm getScenarioFactory() {
        return new CompetitiveChainFactory();
    }


    public static void main(String[] args){
        CompetitiveChainGUI gui = new CompetitiveChainGUI(new MacroII(System.currentTimeMillis()));
        Console c = new Console(gui);
        c.setVisible(true);
    }
}

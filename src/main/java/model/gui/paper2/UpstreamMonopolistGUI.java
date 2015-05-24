package model.gui.paper2;

import financial.market.Market;
import model.MacroII;
import model.scenario.OneLinkSupplyChainScenario;
import model.utilities.ActionOrder;
import model.utilities.stats.collectors.enums.MarketDataType;
import org.jfree.data.xy.XYSeries;
import sim.display.Console;
import sim.display.Controller;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.media.chart.TimeSeriesChartGenerator;

import javax.swing.*;

/**
 * Onelink supply chain
 * Created by carrknight on 5/22/15.
 */
public class UpstreamMonopolistGUI extends GUIState
{

    public static final int EQUILIBRIUM_PRICE_WOOD = 68;
    public static final int EQUILIBRIUM_PRICE_FURNITURE = 85;
    public static final int EQUILIBRIUM_QUANTITY = 17;
    SupplyChainForm factory;
    private JFrame settingPanel;

    private JFrame upstreamPriceFrame;
    TimeSeriesChartGenerator upstreamPriceChart;
    private JFrame downstreamPriceFrame;
    TimeSeriesChartGenerator downstreamPriceChart;
    private JFrame inputProductionFrame;
    TimeSeriesChartGenerator inputProductionChart;


    public UpstreamMonopolistGUI(MacroII state) {
        super(state);
    }

    @Override
    public void init(Controller controller) {
        super.init(controller);

        //wood chart
        upstreamPriceChart = new sim.util.media.chart.TimeSeriesChartGenerator();
        upstreamPriceChart.setTitle("Wood Prices");
        upstreamPriceChart.setXAxisLabel("Time");
        upstreamPriceChart.setYAxisLabel("Price");
        upstreamPriceFrame = upstreamPriceChart.createFrame();
        upstreamPriceFrame.setLocationByPlatform(true);
        upstreamPriceFrame.setVisible(true);
        upstreamPriceFrame.pack();
        upstreamPriceFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        controller.registerFrame(upstreamPriceFrame);


        //furniture chart
        downstreamPriceChart = new sim.util.media.chart.TimeSeriesChartGenerator();
        downstreamPriceChart.setTitle("Furniture Prices");
        downstreamPriceChart.setXAxisLabel("Time");
        downstreamPriceChart.setYAxisLabel("Price");
        downstreamPriceFrame = downstreamPriceChart.createFrame();
        downstreamPriceFrame.setLocationByPlatform(true);
        downstreamPriceFrame.setVisible(true);
        downstreamPriceFrame.pack();
        downstreamPriceFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        controller.registerFrame(downstreamPriceFrame);



        inputProductionChart = new sim.util.media.chart.TimeSeriesChartGenerator();
        inputProductionChart.setTitle("Wood Quantity");
        inputProductionChart.setXAxisLabel("Time");
        inputProductionChart.setYAxisLabel("Quantity");
        inputProductionFrame = inputProductionChart.createFrame();
        inputProductionFrame.setLocationByPlatform(true);
        inputProductionFrame.setVisible(true);
        inputProductionFrame.pack();
        inputProductionFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        controller.registerFrame(inputProductionFrame);

        //factory!
        factory = getScenarioFactory();
        settingPanel = new JFrame();
        settingPanel.setContentPane(factory.getControlPanel());
        settingPanel.setSize(400, 800);
        settingPanel.setName("Settings Panel");
  //      controller.registerFrame(settingPanel);
        settingPanel.setLocationByPlatform(true);
        settingPanel.setVisible(true);
        settingPanel.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);


    }



    @Override
    public void finish() {
        super.finish();
        settingPanel.setVisible(true);
        inputProductionFrame.setVisible(false);
        downstreamPriceFrame.setVisible(false);
        upstreamPriceFrame.setVisible(false);
    }



    @Override
    public void start() {

        final MacroII model = (MacroII) this.state;
        final OneLinkSupplyChainScenario scenario = factory.apply(model);
        model.setScenario(scenario);



        super.start();
        //forget the settings panel
        settingPanel.setVisible(false);

        Market inputMarket = model.getMarket(OneLinkSupplyChainScenario.INPUT_GOOD);
        Market outputMarket = model.getMarket(OneLinkSupplyChainScenario.OUTPUT_GOOD);


        model.scheduleSoon(ActionOrder.DAWN, new Steppable() {
            @Override
            public void step(SimState simState) {

                model.scheduleTomorrow(ActionOrder.DAWN,this);
            }
        });



        upstreamPriceChart.removeAllSeries();
        final XYSeries price = new XYSeries("Wood Price", false);
        upstreamPriceChart.addSeries(price, null);
        final XYSeries upstreamEquilibriumPrice = new XYSeries("Wood Equilibrium Price");
        upstreamPriceChart.addSeries(upstreamEquilibriumPrice, null);
        model.scheduleSoon(ActionOrder.GUI_PHASE, new Steppable() {
            @Override
            public void step(SimState simState) {

                double x = model.getMainScheduleTime();
                double y = inputMarket.getLatestObservation(MarketDataType.CLOSING_PRICE);
                y = Double.isNaN(y) ? 0 : y;
                price.add(x, y);
                upstreamEquilibriumPrice.add(x, getEquilibriumPriceUpstream());
                upstreamPriceChart.updateChartLater(0);

                model.scheduleTomorrow(ActionOrder.GUI_PHASE,this);

            }
        });

        downstreamPriceChart.removeAllSeries();
        final XYSeries downstreamPrice = new XYSeries("Furniture Price", false);
        downstreamPriceChart.addSeries(downstreamPrice, null);
        final XYSeries downstreamEquilibriumPrice = new XYSeries("Furniture Equilibrium Price");
        downstreamPriceChart.addSeries(downstreamEquilibriumPrice, null);
        model.scheduleSoon(ActionOrder.GUI_PHASE, new Steppable() {
            @Override
            public void step(SimState simState) {

                double x = model.getMainScheduleTime();
                double y = outputMarket.getLatestObservation(MarketDataType.CLOSING_PRICE);
                y = Double.isNaN(y) ? 0 : y;
                downstreamPrice.add(x, y);
                downstreamEquilibriumPrice.add(x, getEquilibriumPriceDownstream());

                downstreamPriceChart.updateChartLater(0);

                model.scheduleTomorrow(ActionOrder.GUI_PHASE,this);

            }
        });



        inputProductionChart.removeAllSeries();
        final XYSeries produced = new XYSeries("Wood Produced", false);
        final XYSeries traded = new XYSeries("Wood Traded", false);
        final XYSeries consumed = new XYSeries("Wood Consumed", false);
        inputProductionChart.addSeries(produced, null);
        inputProductionChart.addSeries(traded, null);
        inputProductionChart.addSeries(consumed, null);
        final XYSeries equilibriumQuantity = new XYSeries("Furniture Equilibrium Price");
        inputProductionChart.addSeries(equilibriumQuantity, null);
        model.scheduleSoon(ActionOrder.GUI_PHASE, new Steppable() {
            @Override
            public void step(SimState simState) {

                double x = model.getMainScheduleTime();
                double y = inputMarket.getLatestObservation(MarketDataType.VOLUME_PRODUCED);
                y = Double.isNaN(y) ? 0 : y;
                produced.add(x, y);
                y = inputMarket.getLatestObservation(MarketDataType.VOLUME_TRADED);
                y = Double.isNaN(y) ? 0 : y;
                traded.add(x, y);
                y = inputMarket.getLatestObservation(MarketDataType.VOLUME_CONSUMED);
                y = Double.isNaN(y) ? 0 : y;
                consumed.add(x, y);
                equilibriumQuantity.add(x, getEquilibriumQuantity());
                inputProductionChart.updateChartLater(0);
                //this should move it to the EDT

                model.scheduleTomorrow(ActionOrder.GUI_PHASE,this);

            }
        });




    }

    protected int getEquilibriumQuantity() {
        return EQUILIBRIUM_QUANTITY;
    }

    protected int getEquilibriumPriceDownstream() {
        return EQUILIBRIUM_PRICE_FURNITURE;
    }

    protected int getEquilibriumPriceUpstream() {
        return EQUILIBRIUM_PRICE_WOOD;
    }

    protected SupplyChainForm getScenarioFactory() {
        return new UpstreamMonopolistFactory();
    }

    public static void main(String[] args){
        UpstreamMonopolistGUI gui = new UpstreamMonopolistGUI(new MacroII(System.currentTimeMillis()));
        Console c = new Console(gui);
        c.setVisible(true);
    }




}

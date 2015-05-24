package model.gui.paper2;

import goods.UndifferentiatedGoodType;
import model.MacroII;
import model.scenario.MonopolistScenario;
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
 * Created by carrknight on 5/22/15.
 */
public class MonopolistGUI extends GUIState{

    MonopolistScenarioFactory factory;
    private JFrame settingPanel;

    private JFrame priceChartFrame;
    TimeSeriesChartGenerator priceChart;
    private JFrame quantityFrame;
    TimeSeriesChartGenerator quantityChart;




    public MonopolistGUI(MacroII state) {
        super(state);
    }

    @Override
    public void init(Controller controller) {

        super.init(controller);


        //chart
        priceChart = new sim.util.media.chart.TimeSeriesChartGenerator();
        priceChart.setTitle("Monopolist Prices");
        priceChart.setXAxisLabel("Time");
        priceChart.setYAxisLabel("Price");
        priceChartFrame = priceChart.createFrame();
        priceChartFrame.setLocationByPlatform(true);
        priceChartFrame.setVisible(true);
        priceChartFrame.pack();
        controller.registerFrame(priceChartFrame);
        priceChartFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);



        quantityChart = new sim.util.media.chart.TimeSeriesChartGenerator();
        quantityChart.setTitle("Monopolist Quantities");
        quantityChart.setXAxisLabel("Time");
        quantityChart.setYAxisLabel("Quantity");
        quantityFrame = quantityChart.createFrame();
        quantityFrame.setLocationByPlatform(true);
        quantityFrame.setVisible(true);
        quantityFrame.pack();
        controller.registerFrame(quantityFrame);
        quantityFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);


        //factory!
        factory = new MonopolistScenarioFactory();
        settingPanel = new JFrame();
        settingPanel.setContentPane(factory.getSettingPanel());
        settingPanel.setSize(400, 800);
        settingPanel.setName("Settings Panel");
    //    controller.registerFrame(settingPanel);
        settingPanel.setLocationByPlatform(true);
        settingPanel.setVisible(true);
        settingPanel.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);


    }

    @Override
    public void finish() {
        super.finish();
        settingPanel.setVisible(true);
        priceChartFrame.setVisible(false);
        quantityFrame.setVisible(false);
    }

    @Override
    public void start() {

        final MacroII model = (MacroII) this.state;
        final MonopolistScenario scenario = factory.apply(model);
        model.setScenario(scenario);

        super.start();
        //forget the settings panel
        settingPanel.setVisible(false);

        //find equilibrium
        final int profitMaximizingLaborForce = MonopolistScenario.
                findWorkerTargetThatMaximizesProfits(scenario.getDemandIntercept(),
                                                     scenario.getDemandSlope(),
                                                     scenario.getDailyWageIntercept(),
                                                     scenario.getDailyWageSlope(),
                                                     scenario.getLaborProductivity());
        final int profitMaximizingQuantity = profitMaximizingLaborForce*scenario.getLaborProductivity();
        final int profitMaximizingPrice = scenario.getDemandIntercept() -
                scenario.getDemandSlope() * profitMaximizingQuantity;


        model.scheduleSoon(ActionOrder.DAWN, new Steppable() {
            @Override
            public void step(SimState simState) {

                model.scheduleTomorrow(ActionOrder.DAWN,this);
            }
        });



        priceChart.removeAllSeries();
        final XYSeries price = new XYSeries("Monopolist Price", false);
        final XYSeries equilibrium = new XYSeries("Equilibrium",false);
        priceChart.addSeries(price, null);
        priceChart.addSeries(equilibrium, null);
        model.scheduleSoon(ActionOrder.GUI_PHASE, new Steppable() {
            @Override
            public void step(SimState simState) {

                double x = model.getMainScheduleTime();
                double y = model.getMarket(UndifferentiatedGoodType.GENERIC).getLatestObservation(MarketDataType.AVERAGE_CLOSING_PRICE);
                y = Double.isNaN(y) ? 0 : y;
                price.add(x, y);
                equilibrium.add(x,profitMaximizingPrice);
                priceChart.updateChartLater(0);

                model.scheduleTomorrow(ActionOrder.GUI_PHASE,this);

            }
        });


        quantityChart.removeAllSeries();
        final XYSeries quantity = new XYSeries("Monopolist Quantity", false);
        final XYSeries equilibriumQ = new XYSeries("Equilibrium Quantity",false);
        quantityChart.addSeries(quantity, null);
        quantityChart.addSeries(equilibriumQ, null);
        model.scheduleSoon(ActionOrder.GUI_PHASE, new Steppable() {
            @Override
            public void step(SimState simState) {

                double x = model.getMainScheduleTime();
                double y = model.getMarket(UndifferentiatedGoodType.GENERIC).getLatestObservation(MarketDataType.VOLUME_PRODUCED);
                y = Double.isNaN(y) ? 0 : y;
                quantity.add(x, y);
                equilibriumQ.add(x,profitMaximizingQuantity);
                quantityChart.updateChartLater(0);
                //this should move it to the EDT

                model.scheduleTomorrow(ActionOrder.GUI_PHASE,this);

            }
        });




    }





    public static void main(String[] args){
        MonopolistGUI gui = new MonopolistGUI(new MacroII(System.currentTimeMillis()));
        Console c = new Console(gui);
        c.setVisible(true);
    }

}

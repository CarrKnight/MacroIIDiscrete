/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.collectors;

import agents.EconomicAgent;
import agents.firm.Firm;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import financial.market.Market;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.scheduler.Priority;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * <h4>Description</h4>
 * <p/> A simple object that schedules itself at the end of each day and collects aggregate information.
 * <p/> It schedules itself at start() and acts during CLEANUP_DATA_GATHERING phase
 * <p/> It aggregates firms by their output. This means that a firm with multiple outputs will show up multiple times
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-01-30
 * @see
 */
public class DailyStatCollector implements Steppable{

    /**
     * link to the model
     */
    private final MacroII model;

    /**
     * This is the header to use for an hypothetical CSV file
     */
    private String[] header = null;

    /**
     * If this is given, calls it to write a csv file
     */

    private CSVWriter writer = null;

    /**
     * How much was produced today
     */
    private HashMap<GoodType,Integer> productionPerSector;


    /**
     * How much was consumed today
     */
    private HashMap<GoodType,Integer> consumptionPerSector;

    /**
     * How many workers were working today
     */
    private HashMap<GoodType,Integer> workersPerSector;

    /**
     * What's the current market price for this good
     */
    private HashMap<GoodType,Float> marketPrice;

    /**
     * The amount traded in the market
     */
    private HashMap<GoodType,Integer> marketVolume;

    /**
     * How much of the good is owned by the sellers
     */
    private HashMap<GoodType,Integer> sellerTotalInventory;

    /**
     * How much of the good is owned by the buyers
     */
    private HashMap<GoodType,Integer> buyerTotalInventory;


    /**
     * The flag is true for the output if at least one producing plant had its production halted
     */
    private HashMap<GoodType,Boolean> wereThereShortages;


    /**
     * The flag is true for the output if at least one producing plant had its production halted
     */
    private HashMap<GoodType,Integer> sumDemandGap;


    /**
     * The flag is true for the output if at least one producing plant had its production halted
     */
    private HashMap<GoodType,Integer> sumSupplyGap;




    /**
     * Receives the link to the model
     * @param model
     */
    public DailyStatCollector(MacroII model) {
        this.model = model;
        productionPerSector = new HashMap<>();
        consumptionPerSector = new HashMap<>();
        workersPerSector = new HashMap<>();
        marketPrice  = new HashMap<>();
        marketVolume  = new HashMap<>();
        sellerTotalInventory = new HashMap<>();
        buyerTotalInventory = new HashMap<>();
        wereThereShortages = new HashMap< >();
        sumDemandGap =new HashMap< >();
        sumSupplyGap = new HashMap<>();

    }

    /**
     * Receives the link to the model and a csv writer to write to file
     * @param model the SimState
     * @param writer the CSV writer
     */
    public DailyStatCollector(MacroII model, CSVWriter writer) {
        this(model);
        this.writer = writer;

    }

    /**
     * schedules itself
     */
    public void start()
    {
        model.scheduleSoon(ActionOrder.CLEANUP_DATA_GATHERING,this, Priority.AFTER_STANDARD);
    }


    @Override
    public void step(SimState state) {
        assert state == model;


        final Set<GoodType> sectorList = model.getGoodTypeMasterList().getListOfAllSectors();

        for(GoodType output : sectorList) //for each sector
        {
            final Market market = model.getMarket(output);
            if(output.isMachinery()) //don't bother with  non-goods
                continue;


            int production = 0; int workers = 0;int sellerInventory=0; int consumption = 0;
            boolean shortages= false; //prepare all the counts


            final Collection<EconomicAgent> sellers = market != null?  market.getSellers() : model.getAgents();
            for(EconomicAgent seller : sellers) //for each firm
            {


                //sum up inventory
                sellerInventory += seller.hasHowMany(output);
                production+=seller.getTodayProduction(output);
                consumption += seller.getTodayConsumption(output);


                if(seller instanceof Firm) //ignore non firms
                {
                    //sum up workers
                    workers += ((Firm) seller).getNumberOfWorkersWhoProduceThisGood(output);
                    assert workers >= 0;
                    //were there shortages?
                    shortages = shortages || ((Firm) seller).wereThereMissingInputsInAtLeastOnePlant(output);
                }

            }
            //go through buyers and check their inventories
            int buyerInventory = 0;
            final Collection<EconomicAgent> buyers = market != null? market.getBuyers() : Collections.emptyList();
            for(EconomicAgent buyer : buyers) //for each firm
            {
                if(! (buyer instanceof Firm)) //ignore non firms
                    continue;

                buyerInventory += buyer.hasHowMany(output);
                production+=buyer.getTodayProduction(output);
                consumption += buyer.getTodayConsumption(output);

            }
            final float todayAveragePrice = market != null ? market.getTodayAveragePrice() : 1;
            final int yesterdayVolume = market != null ? market.getYesterdayVolume() : 0;
            final int demandGaps =  market != null ?market.sumDemandGaps() : 0;
            final int supplyGap =  market != null ? market.sumSupplyGaps() : 0;




            productionPerSector.put(output,production);
            consumptionPerSector.put(output,consumption);
            workersPerSector.put(output,workers);
            marketPrice.put(output, todayAveragePrice);
            marketVolume.put(output,yesterdayVolume);
            sellerTotalInventory.put(output,sellerInventory);
            buyerTotalInventory.put(output,buyerInventory);
            wereThereShortages.put(output,shortages);
            sumDemandGap.put(output, demandGaps);
            sumSupplyGap.put(output, supplyGap);


        }

        if(writer != null)
        {
            try{
                if(header == null)
                {
                    buildHeader();
                    writer.writeNext(header);
                }
                writer.writeNext(buildCSVRow());
                writer.flush();
            }
            catch (IOException exception){
                System.err.println("couldn't print!");
            }

        }

        model.scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING, this,Priority.AFTER_STANDARD);


    }


    /**
     * Writes basically a csv line, except it is using # to separate
     * @return
     */
    public String toString()
    {

        String[] row = buildCSVRow();
        Joiner joiner = Joiner.on('#');

        return joiner.join(row);


    }

    /**
     * Build the row to pass to a CSV writer
     * @return an array of strings, each being a column entry in the row to write
     */
    public String[] buildCSVRow()
    {
        LinkedList<String> row = new LinkedList<>();
        for(GoodType type : productionPerSector.keySet())
        {
            if(productionPerSector.get(type) == null)
                continue;


            row.add(Integer.toString(productionPerSector.get(type)));
            row.add(Integer.toString(consumptionPerSector.get(type)));
            row.add(Integer.toString(workersPerSector.get(type)));
            row.add(Float.toString(marketPrice.get(type)));
            row.add(Integer.toString(marketVolume.get(type)));
            row.add(Integer.toString(sellerTotalInventory.get(type)));
            row.add(Integer.toString(buyerTotalInventory.get(type)));
            row.add(Boolean.toString(wereThereShortages.get(type)));
            row.add(Integer.toString(sumDemandGap.get(type)));
            row.add(Integer.toString(sumSupplyGap.get(type)));

        }
        return row.toArray(new String[row.size()]);

    }


    /**
     * Build the header for an hypothetical csv. Does so only once at the end of the first step
     */
    public void buildHeader()
    {
        Preconditions.checkState(header == null); // this should only be called to initialize

        LinkedList<String> todayRow = new LinkedList<>();

        for(GoodType type : productionPerSector.keySet())
        {
            if(productionPerSector.get(type) == null)
                continue;


            todayRow.add(type.getName() + '_' + "production");
            todayRow.add(type.getName() + '_' + "consumption");
            todayRow.add(type.getName() + '_' + "workers");
            todayRow.add(type.getName() + '_' + "price");
            todayRow.add(type.getName() + '_' + "volume");
            todayRow.add(type.getName() + '_' + "outputInventory");
            todayRow.add(type.getName() + '_' + "inputInventory");
            todayRow.add(type.getName() + '_' + "shortages");
            todayRow.add(type.getName() + '_' + "demandGap");
            todayRow.add(type.getName() + '_' + "supplyGap");

        }

        header = todayRow.toArray(new String[todayRow.size()]);
    }


    public HashMap<GoodType, Integer> getProductionPerSector() {
        return productionPerSector;
    }

    public HashMap<GoodType, Integer> getWorkersPerSector() {
        return workersPerSector;
    }

    public HashMap<GoodType, Float> getMarketPrice() {
        return marketPrice;
    }

    public HashMap<GoodType, Integer> getMarketVolume() {
        return marketVolume;
    }

    public HashMap<GoodType, Integer> getSellerTotalInventory() {
        return sellerTotalInventory;
    }

    public HashMap<GoodType, Integer> getBuyerTotalInventory() {
        return buyerTotalInventory;
    }

    public HashMap<GoodType, Boolean> getWereThereShortages() {
        return wereThereShortages;
    }


    /**
     * simple static that creates the csv writer then the daily stat collector and starts it. Usually called BEFORE macroII is started.
     * @param csvFile  the csv file
     * @param macroII the model reference
     */
    public static void addDailyStatCollectorToModel( File csvFile, MacroII macroII)
    {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(csvFile));
            DailyStatCollector collector = new DailyStatCollector(macroII,writer);
            collector.start();
        } catch (IOException e) {
            System.err.println("failed to create the daily stat collector file!");
        }

    }

    private static final int NUMBER_OF_RUNS = 300;
    private static final int STEPS_PER_RUN = 10000;


    private static class OneRunOfTheModel implements Runnable
    {

        @Override
        public void run() {

            MacroII model = new MacroII(System.currentTimeMillis());
            model.start();
            for(int i=0; i< STEPS_PER_RUN; i++)
                model.schedule.step(model);

        }
    }



    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        List<Future> receipts = new ArrayList<>(NUMBER_OF_RUNS);

        for(int i=0; i< NUMBER_OF_RUNS; i++)
        {
            Future receipt = executor.submit(new OneRunOfTheModel());
            receipts.add(receipt);
        }

        //here we make sure every run was completed
        for(Future receipt : receipts)
            receipt.get();

        executor.shutdown();
    }

}

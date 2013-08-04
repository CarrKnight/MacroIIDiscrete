/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.stats;

import agents.EconomicAgent;
import agents.firm.Firm;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.sun.istack.internal.Nullable;
import financial.Market;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedList;

/**
 * <h4>Description</h4>
 * <p/> A simple object that schedules itself at the end of each day and collects aggregate information.
 * <p/> It schedules itself at start() and acts during CLEANUP phase
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
    @Nullable
    private CSVWriter writer = null;

    /**
     * How much was produced today
     */
    private EnumMap<GoodType,Integer> productionPerSector;

    /**
     * How many workers were working today
     */
    private EnumMap<GoodType,Integer> workersPerSector;

    /**
     * What's the current market price for this good
     */
    private EnumMap<GoodType,Long> marketPrice;

    /**
     * The amount traded in the market
     */
    private EnumMap<GoodType,Integer> marketVolume;

    /**
     * How much of the good is owned by the sellers
     */
    private EnumMap<GoodType,Integer> sellerTotalInventory;

    /**
     * How much of the good is owned by the buyers
     */
    private EnumMap<GoodType,Integer> buyerTotalInventory;


    /**
     * The flag is true for the output if at least one producing plant had its production halted
     */
    private EnumMap<GoodType,Boolean> wereThereShortages;




    /**
     * Receives the link to the model
     * @param model
     */
    public DailyStatCollector(MacroII model) {
        this.model = model;
        productionPerSector = new EnumMap<>(GoodType.class);
        workersPerSector = new EnumMap<>(GoodType.class);
        marketPrice  = new EnumMap<>(GoodType.class);
        marketVolume  = new EnumMap<>(GoodType.class);
        sellerTotalInventory = new EnumMap<>(GoodType.class);
        buyerTotalInventory = new EnumMap<>(GoodType.class);
        wereThereShortages = new EnumMap<>(GoodType.class);

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
        model.scheduleSoon(ActionOrder.CLEANUP,this);
    }


    @Override
    public void step(SimState state) {
        assert state == model;


        for(GoodType output : GoodType.values() ) //for each sector
        {
            final Market market = model.getMarket(output);
            if(output.isLabor() || output.isMachinery() || market==null) //don't bother with non existing markets or non-goods
                continue;


            int production = 0; int workers = 0;int sellerInventory=0; boolean shortages= false; //prepare all the counts

            for(EconomicAgent seller : market.getSellers() ) //for each firm
            {
                if(! (seller instanceof Firm)) //ignore non firms
                    continue;


                //sum up production
                production += ((Firm) seller).getLastWeekProduction(output);
                assert production >= 0 : "negative production is weird";

                //sum up workers
                workers += ((Firm) seller).getTotalWorkersWhoProduceThisGood(output);
                assert workers >=0;

                //sum up inventory
                sellerInventory += seller.hasHowMany(output);

                //were there shortages?
                shortages = shortages || ((Firm) seller).wereThereMissingInputsInAtLeastOnePlant(output);


            }
            //go through buyers and check their inventories
            int buyerInventory = 0;
            for(EconomicAgent buyer : market.getBuyers() ) //for each firm
            {
                if(! (buyer instanceof Firm)) //ignore non firms
                    continue;

                buyerInventory += buyer.hasHowMany(output);



            }

            productionPerSector.put(output,production);
            workersPerSector.put(output,workers);
            marketPrice.put(output,market.getLastPrice());
            marketVolume.put(output,market.getYesterdayVolume());
            sellerTotalInventory.put(output,sellerInventory);
            buyerTotalInventory.put(output,buyerInventory);
            wereThereShortages.put(output,shortages);


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

        model.scheduleTomorrow(ActionOrder.CLEANUP, this);


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
            row.add(Integer.toString(workersPerSector.get(type)));
            row.add(Long.toString(marketPrice.get(type)));
            row.add(Integer.toString(marketVolume.get(type)));
            row.add(Integer.toString(sellerTotalInventory.get(type)));
            row.add(Integer.toString(buyerTotalInventory.get(type)));
            row.add(Boolean.toString(wereThereShortages.get(type)));

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


            todayRow.add(type.name() + '_' + "production");
            todayRow.add(type.name() + '_' + "workers");
            todayRow.add(type.name() + '_' + "price");
            todayRow.add(type.name() + '_' + "volume");
            todayRow.add(type.name() + '_' + "outputInventory");
            todayRow.add(type.name() + '_' + "inputInventory");
            todayRow.add(type.name() + '_' + "shortages");

        }

        header = todayRow.toArray(new String[todayRow.size()]);
    }


    public EnumMap<GoodType, Integer> getProductionPerSector() {
        return productionPerSector;
    }

    public EnumMap<GoodType, Integer> getWorkersPerSector() {
        return workersPerSector;
    }

    public EnumMap<GoodType, Long> getMarketPrice() {
        return marketPrice;
    }

    public EnumMap<GoodType, Integer> getMarketVolume() {
        return marketVolume;
    }

    public EnumMap<GoodType, Integer> getSellerTotalInventory() {
        return sellerTotalInventory;
    }

    public EnumMap<GoodType, Integer> getBuyerTotalInventory() {
        return buyerTotalInventory;
    }

    public EnumMap<GoodType, Boolean> getWereThereShortages() {
        return wereThereShortages;
    }
}

/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.utilities.stats.collectors;

import agents.EconomicAgent;
import agents.firm.Firm;
import au.com.bytecode.opencsv.CSVWriter;
import com.google.common.base.Preconditions;
import goods.GoodType;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.IOException;
import java.util.LinkedList;

/**
 * <h4>Description</h4>
 * <p/> This is a simple class that writes to CSV each day at cleanup phase. The way it works is rather weird:
 * <ul>
 *     <li> gets the list of registered sellers in a sector</li>
 *     <li> each evening it queries their sales department to price an imaginary good</li>
 *     <li> it also records their outflow (as a proxy for quantity sold) </li>
 *     It only cares about the sellers that are firms and that are registered at the end of day 1.
 * </ul>
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-07-04
 * @see
 */
public class ProducersStatCollector implements Steppable, Deactivatable{


    final private CSVWriter pricesWriter;

    final private CSVWriter quantitiesWriter;

    final private MacroII model;

    final private GoodType sector;

    /**
     * the list of all firms that exist.
     */
    private LinkedList<Firm> sellers;

    /**
     * when created, it doesn't schedule itself! Call start()
     * @param model the model to study
     * @param sector the sector in the market to study
     * @param pricesWriter the CSV writer where to write prices
     * @param quantitiesWriter the CSV writer where to write quanties
     */
    public ProducersStatCollector(MacroII model, GoodType sector, CSVWriter pricesWriter, CSVWriter quantitiesWriter) {

        this.model = model;
        this.sector=sector;
        this.pricesWriter=pricesWriter;
        this.quantitiesWriter=quantitiesWriter;



    }

    /**
     * schedules itself and register as deactivable
     */
    public void start() {
        model.scheduleSoon(ActionOrder.CLEANUP_DATA_GATHERING,this);
        //also set as deactivable
        model.registerDeactivable(this);
    }

    @Override
    public void step(SimState state) {

        if(sellers==null)
        {
            //fill the seller list (only done once) and write the header but don't flush yet
            instantiateMasterListAndWriteHeader();
            assert sellers != null;
        }

        //write new line
        String[] prices = new String[sellers.size()];
        String[] quantities = new String[sellers.size()];

        int i=0;
        //get data!
        for(Firm seller : sellers)
        {
            prices[i] = String.valueOf(seller.hypotheticalSellPrice(sector));
            quantities[i] = String.valueOf(seller.getSalesDepartmentRecordedOutflow(sector));
            i++;
        }
        //now print it out
        pricesWriter.writeNext(prices);
        quantitiesWriter.writeNext(quantities);

        //now flush it
        try {
            pricesWriter.flush();
            quantitiesWriter.flush();
        } catch (IOException e) {
            System.err.println("ProducersStatCollector failed to fluse!");
            e.printStackTrace();
        }

        model.scheduleTomorrow(ActionOrder.CLEANUP_DATA_GATHERING,this);



    }

    /**
     * reads in all sellers that are firms.
     */
    private void instantiateMasterListAndWriteHeader() {
        Preconditions.checkState(sellers==null);
        Preconditions.checkState(model.getMarket(sector) != null);
        //instatiate your master list
        sellers = new LinkedList<>();
        //read all registered sellers (including non-firms) in the market
        LinkedList<EconomicAgent> allRegisteredSellers = new LinkedList<>(model.getMarket(sector).getSellers());
        for(EconomicAgent a : allRegisteredSellers)
        {
            //ugly but necessary
            if(a instanceof Firm)
                sellers.add((Firm) a);
        }
        //now print it as a header
        String[] header = new String[sellers.size()];
        //go through every firm and print out their names
        int i=0;
        for(Firm f : sellers)
        {
            header[i]=f.getName();
            i++;
        }
        //post the header in both CSV writers
        pricesWriter.writeNext(header);
        quantitiesWriter.writeNext(header);

    }


    /**
     * close the writers and clear the lst
     */
    @Override
    public void turnOff() {
        sellers.clear();
        //now flush it
        try {
            pricesWriter.close();
            quantitiesWriter.close();
        } catch (IOException e) {
            System.err.println("ProducersStatCollector failed to close!");
            e.printStackTrace();
        }


    }
}

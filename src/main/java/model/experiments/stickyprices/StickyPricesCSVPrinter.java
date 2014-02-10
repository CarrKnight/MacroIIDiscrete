/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.experiments.stickyprices;

import model.scenario.OneLinkSupplyChainResult;

import java.nio.file.Paths;

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
 * @author carrknight
 * @version 2014-02-10
 * @see
 */
public class StickyPricesCSVPrinter {


    public static void main(String[] args)
    {
        //print out a simple run for all to see in my beautiful paper!

        long seed = 0;

        //all learned
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,100,0,true,true, Paths.get("runs","supplychai","paper","everybodyLearnedSlow_withInventory.csv").toFile());
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,1,100,true,true, Paths.get("runs","supplychai","paper","everybodyLearnedSticky_withInventory.csv").toFile());
        //beef learned
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,100,0,true,false, Paths.get("runs","supplychai","paper","beefLearnedSlow_withInventory.csv").toFile());
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,1,100,true,false, Paths.get("runs","supplychai","paper","beefLearnedSticky_withInventory.csv").toFile());
        //food learned
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,100,0,false,true, Paths.get("runs","supplychai","paper","foodLearnedSlow_withInventory.csv").toFile());
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,1,100,false,true, Paths.get("runs","supplychai","paper","foodLearnedSticky_withInventory.csv").toFile());
        //learning
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,100,0,false,false, Paths.get("runs","supplychai","paper","learningSlow_withInventory.csv").toFile());
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,1,100,false,false, Paths.get("runs","supplychai","paper","learningSticky_withInventory.csv").toFile());


        //non sticky
        OneLinkSupplyChainResult.beefMonopolistOneRun(seed,1,0,true,true, Paths.get("runs","supplychai","paper","nonsticky_withInventory.csv").toFile());



    }

}

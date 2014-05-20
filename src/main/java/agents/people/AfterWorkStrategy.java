/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package agents.people;

import agents.firm.Firm;
import goods.UndifferentiatedGoodType;
import model.MacroII;

/**
 * What should the worker do at the end of the work day? This is called at PREPARE_TO_TRADE step.
 * Created by carrknight on 5/18/14.
 */
public interface AfterWorkStrategy {

    /**
     * it's PREPARE_TO_TRADE time, work for the day is complete
     * @param p the person whose work is complete
     * @param model a link to the model, just in case
     * @param employer the employer, can be null (if null p is assumed to be unemployed)
     * @param wage the wage, valid only if an employer exists
     * @param wageKind the kind of good your wage is being paid in
     */
    public void endOfWorkDay(Person p, MacroII model, Firm employer, int wage, UndifferentiatedGoodType wageKind);

    public static class Factory
    {

        //all three classes work fine as a single instance, i guess

        private static DoNothingAfterWorkStrategy doNothingInstance = null;

        private static QuitJobAfterWorkStrategy precarioInstance = null;

        private static LookForBetterOffersAfterWorkStrategy lookForBetterOffersInstance = null;


        public static AfterWorkStrategy build(Class<? extends AfterWorkStrategy> type)
        {
            if(type.equals(DoNothingAfterWorkStrategy.class))
            {
                if(doNothingInstance == null) doNothingInstance = new DoNothingAfterWorkStrategy();
                return doNothingInstance;
            }
            if(type.equals(QuitJobAfterWorkStrategy.class))
            {
                if(precarioInstance == null) precarioInstance = new QuitJobAfterWorkStrategy();
                return precarioInstance;
            }
            if(type.equals(LookForBetterOffersAfterWorkStrategy.class))
            {
                if(lookForBetterOffersInstance == null)
                    lookForBetterOffersInstance = new LookForBetterOffersAfterWorkStrategy();
                return lookForBetterOffersInstance;
            }

            throw new RuntimeException("The strategy you were looking for is in another castle!");

        }

    }


}

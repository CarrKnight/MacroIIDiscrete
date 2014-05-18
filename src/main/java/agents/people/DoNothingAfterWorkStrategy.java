/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package agents.people;

import agents.firm.Firm;
import goods.UndifferentiatedGoodType;
import model.MacroII;

/**
 * The simplest after work plan: do nothing. If you have a job, don't quit it. If you don't have a job,
 * stay where you are.
 * Created by carrknight on 5/18/14.
 */
public class DoNothingAfterWorkStrategy implements AfterWorkStrategy {
    @Override
    public void endOfWorkDay(Person p, MacroII model, Firm employer, int wage, UndifferentiatedGoodType wageKind) {

    }
}

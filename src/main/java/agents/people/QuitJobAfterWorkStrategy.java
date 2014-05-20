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
 * This is what I used to call "precario". Basically if a person follows this strategy,
 * he/she will quit every day at the end of the work day.
 * Created by carrknight on 5/18/14.
 */
public class QuitJobAfterWorkStrategy implements AfterWorkStrategy {
    @Override
    public void endOfWorkDay(Person p, MacroII model, Firm employer, int wage, UndifferentiatedGoodType wageKind) {

        //if you have a job, quit!
        if(employer!=null)
        {
            assert wage>=0;
            assert wageKind!=null;
            p.quitWork();
        }
    }
}

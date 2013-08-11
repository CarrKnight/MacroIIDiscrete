package tests;

import agents.firm.Firm;
import agents.firm.WeeklyProfitReport;
import agents.firm.personell.HumanResources;
import agents.firm.production.Blueprint;
import agents.firm.production.Plant;
import agents.firm.sales.SalesDepartment;
import agents.firm.sales.SalesDepartmentAllAtOnce;
import goods.GoodType;
import model.MacroII;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
 * @author Ernesto
 * @version 2012-09-04
 * @see
 */
public class ProfitReportTest {


    @Test
    public void stubTest()
    {
        MacroII model = new MacroII(1);
        Firm firm = new Firm(model);

        //3 salesDepartment goods
        SalesDepartment sales1 = mock(SalesDepartmentAllAtOnce.class);
        firm.registerSaleDepartment(sales1,GoodType.GENERIC);
        SalesDepartment sales2 = mock(SalesDepartmentAllAtOnce.class);
        firm.registerSaleDepartment(sales2,GoodType.CAPITAL);
        SalesDepartment sales3 = mock(SalesDepartmentAllAtOnce.class);
        firm.registerSaleDepartment(sales3,GoodType.LABOR);



        //2 plants
        Plant plant1 = mock(Plant.class);
        Blueprint blueprint1 = new Blueprint.Builder().output(GoodType.GENERIC,5).output(GoodType.CAPITAL,2).build();
        when(plant1.getBlueprint()).thenReturn(blueprint1);
        firm.addPlant(plant1);
        HumanResources hr1 = mock(HumanResources.class);
        firm.registerHumanResources(plant1,hr1);

        Plant plant2 = mock(Plant.class);
        Blueprint blueprint2 = new Blueprint.Builder().output(GoodType.CAPITAL,4).output(GoodType.LABOR,4).build();
        when(plant2.getBlueprint()).thenReturn(blueprint2);
        firm.addPlant(plant2);
        HumanResources hr2 = mock(HumanResources.class);
        firm.registerHumanResources(plant2,hr2);





        when(hr1.getWagesPaid()).thenReturn(50l);
        when(hr2.getWagesPaid()).thenReturn(100l);
        when(plant1.weeklyFixedCosts()).thenReturn(50l + 10l);   //fixed costs of input strategy: wages + ammortized fixed costs!  (which I  just pull out of my ass)
        when(plant2.weeklyFixedCosts()).thenReturn(100l + 20l);   //fixed costs of input strategy: wages + ammortized fixed costs!
        when(sales1.getLastWeekMargin()).thenReturn(100l);
        when(sales2.getLastWeekMargin()).thenReturn(300l);
        when(sales3.getLastWeekMargin()).thenReturn(700l);
        int production1[] = new int[3]; production1[GoodType.GENERIC.ordinal()] = 5; production1[GoodType.CAPITAL.ordinal()] = 2;
        when(plant1.getLastWeekThroughput()).thenReturn(production1);
        int production2[] = new int[3]; production2[GoodType.CAPITAL.ordinal()] = 4; production2[GoodType.LABOR.ordinal()] = 4;
        when(plant2.getLastWeekThroughput()).thenReturn(production2);


        WeeklyProfitReport test = new WeeklyProfitReport(firm);
        test.weekEnd();
        assertEquals(test.getPlantProfits(plant1), 140,.001);
        assertEquals(test.getPlantProfits(plant2), 780,.001);












    }

}

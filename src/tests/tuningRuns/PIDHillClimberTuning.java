package tests.tuningRuns;

import agents.firm.Firm;
import agents.firm.personell.HumanResources;
import agents.firm.production.Plant;
import agents.firm.production.control.TargetAndMaximizePlantControl;
import agents.firm.production.control.maximizer.PIDHillClimber;
import au.com.bytecode.opencsv.CSVWriter;
import model.MacroII;

import java.io.FileWriter;
import java.io.IOException;

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
 * @author carrknight
 * @version 2013-02-15
 * @see
 */
public class PIDHillClimberTuning {

    /**
     *go through many PID parameters to find out what's best to maximize
     * PI =  -futureTarget*futureTarget + 20 * futureTarget +2 (whose max should be 11)
     */
    public static void main(String[] args)
    {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter("tuning.csv"));
            writer.writeNext(new String[]{"proportional","integrative","derivative","maxFound","deviance","variance"});


            for(float proportional = 0.001f; proportional <=0.3f; proportional += 0.010f ){
                float integral = 0f;
                    for(float derivative = 0f; derivative <=0.1f; derivative += 0.001f ){
                        //do each run 5 times and take averages
                        float futureTargetAverage =0f;
                        double deviation = 0;
                        double variance = 0;
                        for(int times = 0; times<5; times++)
                        {


                            HumanResources hr = mock(HumanResources.class);
                            TargetAndMaximizePlantControl control = mock(TargetAndMaximizePlantControl.class);
                            Plant plant = mock(Plant.class);
                            Firm firm = mock(Firm.class);  when(hr.getFirm()).thenReturn(firm);
                            when(firm.getModel()).thenReturn(new MacroII(times));
                            when(control.getPlant()).thenReturn(plant);
                            when(plant.maximumWorkersPossible()).thenReturn(30); when(plant.getBuildingCosts()).thenReturn(1l);
                            when(plant.minimumWorkersNeeded()).thenReturn(1);
                            when(hr.getFirm()).thenReturn(firm);
                            when(firm.getModel()).thenReturn(new MacroII(1l));
                            when(control.getHr()).thenReturn(hr); when(hr.maximumWorkersPossible()).thenReturn(30);
                            when(hr.getPlant()).thenReturn(plant);




                            //maximize!
                            PIDHillClimber maximizer = new PIDHillClimber(hr,control,proportional,integral,derivative);

                            //start the parameters
                            int target = 1;
                            float currentProfits = 1;
                            int oldTarget = 0;
                            float oldProfits = -1;
                            int futureTarget=0;


                            for(int i=0; i < 100; i++)
                            {
                                futureTarget = maximizer.chooseWorkerTarget(target,currentProfits,-1,-1,-1,-1,oldTarget,oldProfits); //todo fix this!
                                float futureProfits = -futureTarget*futureTarget + 20 * futureTarget +2;
                                deviation += Math.pow(futureTarget-11,2);
                                variance +=Math.pow(futureTarget-oldTarget,2);

                                oldTarget=target; oldProfits = currentProfits;
                                target = futureTarget; currentProfits = futureProfits;



                            }

                            futureTargetAverage +=futureTarget;
                        }

                        futureTargetAverage= futureTargetAverage/5f;
                        deviation = deviation/5f;
                        variance = variance/5f;
                        System.out.println(proportional + "," + integral + "," + derivative + "," + futureTargetAverage);

                        writer.writeNext(new String[]{Float.toString(proportional),Float.toString(integral)
                                ,Float.toString(derivative),Float.toString(futureTargetAverage),Double.toString(deviation)
                                ,Double.toString(variance)});
                    }

            }

            writer.close();


        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }


}

package model.utilities.pid;

/**
 * A forever hillclimbing attempt at finding the right ITAE <br>
 *     This climber suggests a parameter and assumes it is implemented but doesn't actually change anything itself
 * Created by carrknight on 9/22/14.
 */
public class ITAEHillClimber {


    private final CounterITAE<?> counter;


    final private int timePerStep;

    private int time =0;

    private double stepSize = 1;

    private double currentParameter = Double.NaN;

    private double previousParameter = Double.NaN;

    private double previousITAE = Double.NaN;

    public ITAEHillClimber(CounterITAE<?> counter,
                           int timePerStep, double startParameter) {
        this.counter = counter;
        this.timePerStep = timePerStep;
        currentParameter = startParameter;
    }

    /**
     * make hill-climber take one step.
     * @return The new suggested parameter or  NaN if not ready to suggest
     */
    public double maximizeStep(){

        //collect data
        counter.collectData();
        time++;

        if(time % timePerStep == 0) //decision time?
        {
            time = 0;
            double currentITAE = counter.getITAE();

            double direction;
            if(Double.isNaN(previousParameter) || previousParameter == currentParameter ||
                    currentParameter == 0)
                direction = 1;
            else
            {
                direction = Math.signum(currentParameter-previousParameter) *
                        Math.signum(previousITAE-currentITAE); //flipped because you want to minimise
                assert  direction == -1 || direction == 1 || direction ==0;
                //zeros then go up for even and down for odds
                if(direction == 0)
                    direction = ((int)currentParameter) % 2 == 0 ? 1 : -1;
            }





            counter.reset();
            time=0;
            previousParameter = currentParameter;
            previousITAE=currentITAE;
            currentParameter = Math.max(currentParameter + direction * stepSize,0);

            return  currentParameter;
        }


        return Double.NaN;




    }

    public double getStepSize() {
        return stepSize;
    }

    public void setStepSize(double stepSize) {
        this.stepSize = stepSize;
    }
}

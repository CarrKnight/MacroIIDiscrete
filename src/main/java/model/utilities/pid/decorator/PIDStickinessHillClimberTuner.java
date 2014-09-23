package model.utilities.pid.decorator;

import agents.firm.sales.SalesDepartment;
import model.MacroII;
import model.utilities.ActionOrder;
import model.utilities.Deactivatable;
import model.utilities.pid.ControllerInput;
import model.utilities.pid.CounterITAE;
import model.utilities.pid.ITAEHillClimber;
import model.utilities.pid.PIDController;
import model.utilities.stats.collectors.enums.SalesDataType;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A simple tuner that proceeds by trial and error to choose the right stickiness
 * Created by carrknight on 9/22/14.
 */
public class PIDStickinessHillClimberTuner extends ControllerDecorator implements Steppable, Deactivatable
{


    public final static ActionOrder WHEN_TO_COUNT = ActionOrder.DAWN;

    public static final SalesDataType DEFAULT_OUTPUT_TYPE = SalesDataType.OUTFLOW;

    public static final int DEFAULT_MAXIMIZATION_PHASE = 100;

    private final PIDController toTune;

    private final MacroII model;

    //this is needed to set targets to it
    private final CounterITAE<SalesDataType> counter;

    private BufferedWriter debugWriter;

    private final ITAEHillClimber climber;

    public PIDStickinessHillClimberTuner(MacroII model, PIDController toTune, SalesDepartment department, int burnOut) {
      this(model,toTune,new CounterITAE<>(department.getData(), DEFAULT_OUTPUT_TYPE),burnOut);

    }


    public PIDStickinessHillClimberTuner(MacroII model, PIDController controller,
                                         CounterITAE<SalesDataType> counter,int burnOut) {
       this(model, controller, counter, burnOut, DEFAULT_MAXIMIZATION_PHASE);
    }



    public PIDStickinessHillClimberTuner(MacroII model, PIDController controller,
                                         CounterITAE<SalesDataType> counter,int burnOut, int maximizationPhase) {
        super(controller);
        this.model = model;
        this.counter = counter;
        toTune = controller;
        climber = new ITAEHillClimber(counter,maximizationPhase,controller.getSpeed());
        model.scheduleAnotherDay(WHEN_TO_COUNT,this,burnOut);
    }


    private boolean active = true;


    /**
     * grab the flow target and use it for ITAE counter!
     */
    @Override
    public void adjust(ControllerInput input, boolean isActive, MacroII simState, Steppable user, ActionOrder phase) {
        counter.setTarget(input.getFlowTarget());
        super.adjust(input, isActive, simState, user, phase);
    }

    @Override
    public void turnOff() {
        active = false;
        if(debugWriter != null)
            try {
                debugWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void step(SimState simState) {
        if(!active)
            return;

        double step = climber.maximizeStep();
        if(!Double.isNaN(step)) {
            final int speed = (int) Math.round(step);
            toTune.setSpeed(speed);
            System.out.println("new speed: " + speed);
            try {
                debugWriter.write(((int)model.getMainScheduleTime()) + " , " + speed );
                debugWriter.newLine();
                debugWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }




        model.scheduleTomorrow(WHEN_TO_COUNT,this);
    }


    public void attachWriter(Path pathToFile)
    {
        try {
            debugWriter = Files.newBufferedWriter(pathToFile);
            final int time = Math.max((int) model.getMainScheduleTime(),0);
            debugWriter.write(" time, speed");
            debugWriter.newLine();
            debugWriter.write(time + " , " + toTune.getSpeed() );
            debugWriter.newLine();
            debugWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void setStepSize(double stepSize) {
        climber.setStepSize(stepSize);
    }

    public double getStepSize() {
        return climber.getStepSize();
    }
}

package agents.firm.production.control;

/**
 * <h4>Description</h4>
 * <p/> this is just a simple object containing a TargetAndMaximizePlantControl produced by a factory with link to the maximizer and targeter
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-03-07
 * @see
 */

import agents.firm.production.control.maximizer.WorkforceMaximizer;
import agents.firm.production.control.targeter.WorkforceTargeter;

public class
FactoryProducedTargetAndMaximizePlantControl<WT extends WorkforceTargeter, WM extends WorkforceMaximizer>
{

    private final WT workforceTargeter;

    private final WM workforceMaximizer;

    private final TargetAndMaximizePlantControl control;

    public FactoryProducedTargetAndMaximizePlantControl(WT workforceTargeter,
                                                        WM workforceMaximizer,
                                                        TargetAndMaximizePlantControl control) {
        this.workforceTargeter = workforceTargeter;
        this.workforceMaximizer = workforceMaximizer;
        this.control = control;
    }


    public WT getWorkforceTargeter() {
        return workforceTargeter;
    }

    public WM getWorkforceMaximizer() {
        return workforceMaximizer;
    }

    public TargetAndMaximizePlantControl getControl() {
        return control;
    }
}

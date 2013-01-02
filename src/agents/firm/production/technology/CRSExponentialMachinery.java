package agents.firm.production.technology;

import agents.EconomicAgent;
import goods.GoodType;
import agents.firm.production.Plant;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> CRS exponential has waiting time delta = alpha * number of workers
 *
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version %I%, %G%
 * @see
 */
public class CRSExponentialMachinery extends ExponentialMachinery {

    private float alpha = 0.1f;

    public CRSExponentialMachinery(@Nonnull GoodType type, @Nonnull EconomicAgent producer, long costOfProduction, @Nonnull Plant plant, float outputMultiplier, float alpha) {
        super(type, producer, costOfProduction, plant, outputMultiplier);
        this.alpha = alpha;
    }

    public CRSExponentialMachinery(@Nonnull GoodType type, @Nonnull EconomicAgent producer, long costOfProduction, @Nonnull Plant plant, float alpha) {
        super(type, producer, costOfProduction, plant);
        this.alpha = alpha;
    }



    @Override
    public float deltaFunction(int workers) {
        return alpha * workers;
    }
}

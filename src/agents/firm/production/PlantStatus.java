package agents.firm.production;

/**
 * This enum is there for the plant to show its production status
 */
public enum PlantStatus {

    /**
     * the plant is waiting for at least one input to be provided
     */
    WAITING_FOR_INPUT,

    /**
     * the plant is waiting for at least one more worker to start production
     */
    WAITING_FOR_WORKERS,

    /**
     * the plant is building whatever it needs to build.
     */
    PRODUCING,

    /**
     * The plant is ready to completeProductionRunNow!
     */
    READY,

    /**
     * The plant is too old to operate
     */
    OBSOLETE


}

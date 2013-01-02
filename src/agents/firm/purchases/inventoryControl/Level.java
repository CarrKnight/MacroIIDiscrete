package agents.firm.purchases.inventoryControl;

/**
 * <h4>Description</h4>
 * <p/>  This is used by  control to return a qualitative judgment on the size of the inventory/whatever. It might be useful for the pricing strategy or the firm or whatever
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-08-04
 * @see
 */
public enum Level {

    /**
     * This level is unacceptable; I imagine it to flag a state where plants are unable to restart production
     */
    DANGER,

    /**
     * This level is still unacceptable but probably the firm can still function at this level
     */
    BARELY,

    /**
     * At this level the firm is meeting its targets
     */
    ACCEPTABLE,

    /**
     * At this level the firm is having inputs that are too large.
     */
    TOOMUCH,



}

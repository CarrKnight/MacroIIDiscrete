/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package financial.utilities;

/**
 * <h4>Description</h4>
 * <p/> A very simple enum describing what actions are allowed by a participant in the market.
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
public enum ActionsAllowed {

    /**
     * The agent is allowed to trade, that is, to search among the quotes for somebody to trade with
     */
    SEARCH,

    /**
     * The agent is allowed only to submit a quote and wait for it to be filled
     */
    QUOTE,


}

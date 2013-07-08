/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package model.utilities.scheduler;

/**
 * <h4>Description</h4>
 * <p/> Priority is a way to impose some ordering within the same phase in a scheduler. The idea is that the order will be random
 * amongst steps of the same priorities, but the one with higher priority will come first
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-07-08
 * @see
 */
public enum Priority
{


    /**
     * with this priority the step will take place before the steps that don't specify their priority
     */
    BEFORE_STANDARD,

    /**
     * this is the priority of the steps that don't specify it
     */
    STANDARD,


    /**
     * with this priority the step will take place AFTER the steps that don't specify their priority
     */
    AFTER_STANDARD





}

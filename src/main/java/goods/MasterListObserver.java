/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package goods;

/**
 * <h4>Description</h4>
 * <p/> A simple "observer" pattern. You can register any of them to the GoodTypeMasterList and you will get
 * updates everytime there is a change to the master list
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2014-04-20
 * @see
 */
public interface MasterListObserver {

    /**
     * the master list will call this method when a new sector is added or the masterlist clears
     */
    public void theMasterListHasChanged();

}

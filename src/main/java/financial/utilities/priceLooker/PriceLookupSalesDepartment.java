/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package financial.utilities.priceLooker;

import agents.firm.sales.SalesDepartment;
import com.google.common.base.Preconditions;


/**
 * <h4>Description</h4>
 * <p/> Asks the sales department for last closing price.
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author carrknight
 * @version 2013-02-25
 * @see
 */
public class PriceLookupSalesDepartment implements PriceLookup {

    final private SalesDepartment department;

    /**
     * Get the sales department to ask it the last sales price
     * @param department  the sales department
     */
    public PriceLookupSalesDepartment( SalesDepartment department) {
        Preconditions.checkNotNull(department);
        this.department = department;
    }

    /**
     * Get the sales department price
     *
     * @return The price or -1 if there is no price
     */
    @Override
    public long getPrice()
    {
        return department.getLastClosingPrice();


    }
}

/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial.utilities.priceLooker;

import agents.firm.sales.SalesDepartment;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

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
    public PriceLookupSalesDepartment(@Nonnull SalesDepartment department) {
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

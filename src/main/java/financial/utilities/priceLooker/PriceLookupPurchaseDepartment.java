/*
 * Copyright (c) 2013 by Ernesto Carrella
 * Licensed under the Academic Free License version 3.0
 * See the file "LICENSE" for more information
 */

package financial.utilities.priceLooker;

import agents.firm.purchases.PurchasesDepartment;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

/**
 * <h4>Description</h4>
 * <p/> This price lookup returns the "predicted" purchase price of the purchase
 * department passed at constructor
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
public class PriceLookupPurchaseDepartment implements PriceLookup {

    /**
     * The department to look up
     */
    private final PurchasesDepartment department;


    public PriceLookupPurchaseDepartment(@Nonnull PurchasesDepartment department)
    {
        Preconditions.checkNotNull(department);
        this.department = department;
    }

    /**
     * Get the price you are supposed to look up to.
     *
     * @return The price or -1 if there is no price
     */
    @Override
    public long getPrice() {
        return department.predictPurchasePrice();
    }
}

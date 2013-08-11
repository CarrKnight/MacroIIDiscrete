package financial.utilities.priceLooker;

import agents.firm.purchases.PurchasesDepartment;
import static org.junit.Assert.*;import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * <h4>Description</h4>
 * <p/>
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
public class PriceLookupPurchaseDepartmentTest {

    @Test
    public void simpleLookupIsSimple()
    {
        PurchasesDepartment department = mock(PurchasesDepartment.class);

        PriceLookupPurchaseDepartment lookup = new PriceLookupPurchaseDepartment(department);

        when(department.predictPurchasePriceWhenIncreasingProduction()).thenReturn(10l);
        assertEquals(lookup.getPrice(),10l);


        when(department.predictPurchasePriceWhenIncreasingProduction()).thenReturn(-1l);
        assertEquals(lookup.getPrice(),-1l);
    }



}

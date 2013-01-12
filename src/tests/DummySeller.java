package tests;

import agents.EconomicAgent;
import agents.firm.Firm;
import agents.firm.ProfitReport;
import financial.Market;
import financial.MarketEvents;
import financial.utilities.PurchaseResult;
import financial.utilities.Quote;
import goods.Good;
import goods.GoodType;
import model.MacroII;

import javax.annotation.Nonnull;

import static org.mockito.Mockito.mock;

/**
 * <h4>Description</h4>
 * <p/> Used only for testing. Always return the same quote when asked what to buy
 * <p/>
 * <p/>
 * <h4>Notes</h4>
 * Created with IntelliJ
 * <p/>
 * <p/>
 * <h4>References</h4>
 *
 * @author Ernesto
 * @version 2012-07-26
 * @see
 */
public class DummySeller extends Firm {


    public long saleQuote;

    private Market market;

    public DummySeller(MacroII model,long quote) {
        super(model,false);
        saleQuote = quote;
        getProfitReport().turnOff();
        setProfitReport(mock(ProfitReport.class));
    }


    public DummySeller(MacroII model, long saleQuote, Market market) {
        super(model);
        this.saleQuote = saleQuote;
        this.market = market;
    }

    /**
     * A buyer asks the sales department for the price they are willing to sell one of their good.
     *
     * @param buyer the economic agent that asks you that
     * @return a price quoted or -1 if there are no quotes
     */
    @Override
    public Quote askedForASaleQuote(EconomicAgent buyer, GoodType type) {
        Good good = peekGood(type);
        if(good != null)
            return Quote.newSellerQuote(this,saleQuote,good);
        else
            return Quote.emptySellQuote(null);

    }

    public void setSaleQuote(long saleQuote) {
        this.saleQuote = saleQuote;
    }

    @Override
    public void reactToFilledAskedQuote(Good g, long price, EconomicAgent buyer) {
        //don't react

    }


    /**
     * Called by a buyer that wants to buy directly from this agent, not going through the market.
     *
     * @param buyerQuote  the quote (including the offer price) of the buyer
     * @param sellerQuote the quote (including the offer price) of the seller; I expect the buyer to have achieved this through asked for an offer function
     * @return a purchaseResult including whether the trade was succesful and if so the final price
     */
    @Nonnull
    @Override
    public PurchaseResult shopHere(@Nonnull Quote buyerQuote, @Nonnull Quote sellerQuote) {
        long finalPrice = market.price(sellerQuote.getPriceQuoted(),buyerQuote.getPriceQuoted());
        assert sellerQuote.getGood() != null;
        assert this.has(sellerQuote.getGood());


        //exchange hostages
        market.trade(buyerQuote.getAgent(),this,sellerQuote.getGood(),finalPrice,buyerQuote,sellerQuote);
        this.logEvent(this, MarketEvents.SOLD, this.getModel().getCurrentSimulationTimeInMillis(), "price: " + finalPrice + ", through buyFromHere()"); //sold a good
        assert !this.has(sellerQuote.getGood());


        PurchaseResult toReturn = PurchaseResult.SUCCESS;
        toReturn.setPriceTrade(finalPrice);
        return toReturn;
    }
}

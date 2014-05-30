/*
 * Copyright (c) 2014 by Ernesto Carrella
 * Licensed under MIT license. Basically do what you want with it but cite me and don't sue me. Which is just politeness, really.
 * See the file "LICENSE" for more information
 */

package model.scenario;

import agents.people.*;
import financial.market.EndOfPhaseOrderHandler;
import financial.market.OrderBookMarket;
import financial.market.OrderHandler;
import financial.utilities.PricePolicy;
import financial.utilities.ShopSetPricePolicy;
import goods.UndifferentiatedGoodType;
import model.MacroII;

/**
 * A simple, two people trad
 * Created by carrknight on 5/21/14.
 */
public class EdgeworthBoxScenario extends Scenario {

    Person person1;

    Person person2;

    /**
     * cobb-douglas alpha of person 1
     */
    private float alpha1 = 0.5f;

    /**
     * cobb-douglas alpha of person 2
     */
    private float alpha2 = 0.5f;

    /**
     * First kind of good traded
     */
    private UndifferentiatedGoodType xType = UndifferentiatedGoodType.GENERIC;

    /**
     * Second kind of good traded
     */
    private UndifferentiatedGoodType yType = UndifferentiatedGoodType.MONEY;

    /**
     * how much X each day is received/produced by person 1
     */
    private int firstPersonDailyEndowmentOfX = 10;
    /**
     * how much Y each day is received/produced by person 2
     */
    private int secondPersonDailyEndowmentOfY = 10;

    private OrderBookMarket goodMarket;

    //these two are useful to change during tests to show they are not important, but I don't see them mattering in
    //actual applications
    private OrderHandler orderHandler = new EndOfPhaseOrderHandler();
    private PricePolicy pricePolicy = new ShopSetPricePolicy();


    /**
     * Creates the scenario object, so that it links to the model.
     * =
     *
     * @param model
     */
    public EdgeworthBoxScenario(MacroII model) {
        super(model);
    }

    @Override
    public void start() {


        goodMarket = new OrderBookMarket(xType);
        goodMarket.setMoney(yType);
        goodMarket.setOrderHandler(orderHandler,model);
        goodMarket.setPricePolicy(pricePolicy); //make the seller price matter
        getMarkets().put(xType,goodMarket);
        
        person1 = new Person(getModel());
        person1.setName("Person1");
        person1.setProductionStrategy(new ConstantPersonalProductionStrategy(firstPersonDailyEndowmentOfX,xType));
        person1.setUtilityFunction(new CobbDouglas2GoodsUtility(xType, yType, alpha1));
        person1.setConsumptionStrategy(ConsumptionStrategy.Factory.build(ConsumeAllStrategy.class));
        person1.setTradingStrategy(new OneGoodBuyingAndSellingStrategy(goodMarket,person1,getModel()));
        getAgents().add(person1);

        person2 = new Person(getModel());
        person2.setName("Person2");
        person2.setProductionStrategy(new ConstantPersonalProductionStrategy(secondPersonDailyEndowmentOfY,yType));
        person2.setUtilityFunction(new CobbDouglas2GoodsUtility(xType, yType, alpha2));
        person2.setConsumptionStrategy(ConsumptionStrategy.Factory.build(ConsumeAllStrategy.class));
        person2.setTradingStrategy(new OneGoodBuyingAndSellingStrategy(goodMarket,person2,getModel()));
        getAgents().add(person2);






    }


    public Person getPerson1() {
        return person1;
    }

    public Person getPerson2() {
        return person2;
    }

    public OrderBookMarket getGoodMarket() {
        return goodMarket;
    }


    public float getAlpha1() {
        return alpha1;
    }

    public void setAlpha1(float alpha1) {
        this.alpha1 = alpha1;
    }

    public float getAlpha2() {
        return alpha2;
    }

    public void setAlpha2(float alpha2) {
        this.alpha2 = alpha2;
    }

    public UndifferentiatedGoodType getxType() {
        return xType;
    }

    public void setxType(UndifferentiatedGoodType xType) {
        this.xType = xType;
    }

    public UndifferentiatedGoodType getyType() {
        return yType;
    }

    public void setyType(UndifferentiatedGoodType yType) {
        this.yType = yType;
    }

    public int getFirstPersonDailyEndowmentOfX() {
        return firstPersonDailyEndowmentOfX;
    }

    public void setFirstPersonDailyEndowmentOfX(int firstPersonDailyEndowmentOfX) {
        this.firstPersonDailyEndowmentOfX = firstPersonDailyEndowmentOfX;
    }

    public int getSecondPersonDailyEndowmentOfY() {
        return secondPersonDailyEndowmentOfY;
    }

    public void setSecondPersonDailyEndowmentOfY(int secondPersonDailyEndowmentOfY) {
        this.secondPersonDailyEndowmentOfY = secondPersonDailyEndowmentOfY;
    }


    public OrderHandler getOrderHandler() {
        return orderHandler;
    }

    public void setOrderHandler(OrderHandler orderHandler) {
        this.orderHandler = orderHandler;
    }

    public PricePolicy getPricePolicy() {
        return pricePolicy;
    }

    public void setPricePolicy(PricePolicy pricePolicy) {
        this.pricePolicy = pricePolicy;
    }
}

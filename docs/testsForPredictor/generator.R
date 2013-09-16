file<- "~/code/MacroIIDiscrete/docs/testsForPredictor/cheater3.csv"
file<-"~/code/MacroIIDiscrete/runs//maximizerTest.csv"
supplychain <- read.csv(file)

#PRODUCTION

plot(supplychain$BEEF_price[1:6000],type="l",lwd=2,ylim=c(0,70),xlab="day",ylab="daily price or production")
lines(supplychain$BEEF_production[1:6000],col="blue",lwd=2)
legend(x=4000,y=60,c("price","production"),col=c("black","blue"),lwd=c(2,2))


#get the regression ready
observations<-seq.int(from=1,to=6000,by=7)
price<-supplychain$BEEF_price[observations]
production<-supplychain$BEEF_production[observations]
diffProd<-diff(production)
laggedProduction<-production[-length(production)]
todayprice<-price[-1]

#plot it!
plot(price,type="l",lwd=2,ylim=c(0,70),xlab="day",ylab="daily price or production")
lines(production,col="blue",lwd=2)
lines(0.8527228*price - 6.596947,col="red",lwd=2,lty=2)

toRegress<-data.frame(production =supplychain$BEEF_production[observations], price = supplychain$BEEF_price[observations] )
write.csv(toRegress,"~/code/MacroIIDiscrete/src/test/regressionTest.csv")

#we want to regress Delta f on lag of f and present p
lm(diffProd~laggedProduction+todayprice)
lm(diffProd~laggedProduction+todayprice,weights=1:length(laggedProduction))



#CONSUMPTION


plot(supplychain$BEEF_price[1:6000],type="l",lwd=2,ylim=c(0,70),xlab="day",ylab="daily price or production")
lines(supplychain$FOOD_production[1:6000],col="blue",lwd=2)
legend(x=4000,y=60,c("price","consumption"),col=c("black","blue"),lwd=c(2,2))


#get the regression ready
observations<-seq.int(from=1,to=6000,by=7)
price<-supplychain$BEEF_price[observations]
consumption<-supplychain$FOOD_production[observations]
diffconsumption<-diff(consumption)
laggedconsumption<-consumption[-length(consumption)]
todayprice<-price[-1]

#plot it!
plot(price,type="l",lwd=2,ylim=c(0,70),xlab="day",ylab="daily price or production")
lines(consumption,col="blue",lwd=2)
regression<-lm(diffconsumption~laggedconsumption+todayprice)
lines(-coef(regression)[3]/coef(regression)[2]*price - coef(regression)[1]/coef(regression)[2],col="red",lwd=2,lty=2)
lines(-(7/17)*(price - 101),col="yellow",lwd=2,lty=2)



toRegress<-data.frame(consumption =supplychain$FOOD_production[observations], price = supplychain$BEEF_price[observations] )
write.csv(toRegress,"~/code/MacroIIDiscrete/src/test/regressionTestConsumption.csv")

#we want to regress Delta f on lag of f and present p
lm(diffProd~laggedProduction+todayprice,weights=1:length(laggedProduction))
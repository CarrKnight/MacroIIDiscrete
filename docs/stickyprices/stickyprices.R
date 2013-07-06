simpleDelaySticky <- read.csv("~/code/MacroIIDiscrete/docs/stickyprices/merged.csv")
#start with the 5

gap5<-subset(simpleDelaySticky,gap==5)
gap5$acceptable<-factor(gap5$successes==5)
ggplot(data=gap5) + geom_tile(aes(x=buyerDelay, y= sellerDelay,fill = acceptable)) + 
  scale_color_manual(values= c("red","blue"),name="succeded 5 times out of 5")


#show the plots 

ggplot(data=gap5) + geom_tile(aes(x=buyerDelay, y= sellerDelay,fill = deviation,color=acceptable)) +  
  scale_fill_gradient(low="white",high="blue") + scale_color_manual(values = c("black","white"))

ggplot(data=gap5) + geom_tile(aes(x=buyerDelay, y= sellerDelay,fill = variance,color=acceptable)) +  
  scale_fill_gradient(low="white",high="red") + scale_color_manual(values = c("black","white"))

#now show the plots only for succesful ones
gap5s<-subset(gap5,successes==5)

ggplot(data=gap5s) + geom_tile(aes(x=buyerDelay, y= sellerDelay,fill = deviation,color=acceptable)) +  
  scale_fill_gradient(low="white",high="blue") + scale_color_manual(values = c("black","white"))

ggplot(data=gap5s) + geom_tile(aes(x=buyerDelay, y= sellerDelay,fill = variance,color=acceptable)) +  
  scale_fill_gradient(low="white",high="red") + scale_color_manual(values = c("black","white"))

#######################################################################################################

#now with the usual gap of 10

gap10<-subset(simpleDelaySticky,gap==5 & sellerDelay <= 25 & buyerDelay<68)
gap10$acceptable<-factor(gap10$successes==5)
ggplot(data=gap10) + geom_tile(aes(x=buyerDelay, y= sellerDelay,fill = acceptable)) + 
  scale_color_manual(values= c("red","blue"),name="succeded 5 times out of 5")


#show the plots 

ggplot(data=gap10) + geom_tile(aes(x=buyerDelay, y= sellerDelay,fill = deviation,color=acceptable)) +  
  scale_fill_gradient(low="white",high="blue") + scale_color_manual(values = c("black","white"))

ggplot(data=gap10) + geom_tile(aes(x=buyerDelay, y= sellerDelay,fill = variance,color=acceptable)) +  
  scale_fill_gradient(low="white",high="red") + scale_color_manual(values = c("black","white"))

#now show the plots only for succesful ones
gap10s<-subset(gap10,successes==5)

ggplot(data=gap10s) + geom_tile(aes(x=buyerDelay, y= sellerDelay,fill = deviation,color=acceptable)) +  
  scale_fill_gradient(low="white",high="blue") + scale_color_manual(values = c("black","white"))

ggplot(data=gap10s) + geom_tile(aes(x=buyerDelay, y= sellerDelay,fill = variance,color=acceptable)) +  
  scale_fill_gradient(low="white",high="red") + scale_color_manual(values = c("black","white"))






#create the "distance from failure"
y<-0
x<-0
for(i in 0:67)
{
  test<-subset(gap10,buyerDelay == i & acceptable==FALSE)
  yy<-ifelse(length(test$sellerDelay)==0,0,max(test$sellerDelay))
  y<-c(y,yy)
  x<-c(x,i)
} 
x<-x[-1] #needed for stepfun
 
border<-stepfun(x=x,y,f=0)

#distance point to point
taxi.distance.point<-function(x1,y1,x2,y2)
{
  return(abs(x1-x2)+abs(y1-y2))
}

#now a brute distance calculator
taxi.distance.curve<-function(xcoord,ycoord,stepFunction)
{
  distances<-0
  stopifnot(is.stepfun(stepFunction))
  for(i in 0:68)
  {    
    distances<-c(distances,taxi.distance.point(xcoord,ycoord,i,stepFunction(i)))
  }
  distances<-distances[-1]
  return(min(distances))
}
#this should set distance negative for 
row.distance<-function(xcoord,ycoord,acceptable)
  {
  print(xcoord)
  print(ycoord)
  print(acceptable)
  distance<-taxi.distance.curve(xcoord,ycoord,border)
  print(distance)
  distance<-ifelse(acceptable,distance,-distance)
  return(distance)
}

#add a new column for distance
gap10r<-within(gap10,distFromBorder<-row.distance(buyerDelay,sellerDelay,acceptable))
apply

  
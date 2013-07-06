#tuning plot!

library(ggplot2)
library(reshape2)





#read biiiig data set
tuning <- read.csv("~/code/tuningUnit.csv")
#get the subset with derivative =0
tuningToDraw<-subset(tuning, derivative <.0005) 
ggplot(data =tuningToDraw,aes(x=proportional,y=integrative))+ geom_tile(aes(fill = abs(maxFound-22))) #draw the heat map


#different approach: find all the ones that did well
successes<-subset(tuning,maxFound==22 )
#count where they are more common
ggplot(data =successes,aes(x=proportional,y=integrative)) + stat_bin2d(bins = 20) + scale_fill_gradient(low="blue",high="red")
ggplot(data =successes,aes(x=proportional,y=derivative)) + stat_bin2d(bins = 20)
which.min(successes$variance)
successes[1426,] #best seems to be p=0.001 i=0.003, d=0.001

head(successes[with(successes,order(successes$deviance)),],n=100)
#there is a tradeoff between deviance (overall, how far were you from the optimum) and variance (sum squares of steps)
ggplot(data =successes,aes(x=deviance,y=variance)) + geom_point()

#say that I want the deviance below 200000 and variance below 1.3M
candidates<-subset(successes,deviance < 170000 & variance < 1300000)
ggplot(data =candidates,aes(x=deviance,y=variance)) + geom_point()
#among these let me choose the one with the lowest variance
candidates[which.min(candidates$variance),]
#proportional 1.31, integrative 0.71, derivative 0.055

/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.wcss.tutorial03;
import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;

public class Students extends SimState
    {
    public Continuous2D yard = new Continuous2D(1.0,100,100);
    
    public int numStudents = 50;

    double forceToSchoolMultiplier = 0.01;
    double randomMultiplier = 0.1;

    public Students(long seed)
        {
        super(seed);
        }

    public void start()
        {
        super.start();
        
        // clear the yard
        yard.clear();
        
        // add some students to the yard
        for(int i = 0; i < numStudents; i++)
            {
            Student student = new Student();
            yard.setObjectLocation(student, 
                new Double2D(yard.getWidth() * 0.5 + random.nextDouble() - 0.5,
                    yard.getHeight() * 0.5 + random.nextDouble() - 0.5));

            schedule.scheduleRepeating(student);
            }
        }
        
    public static void main(String[] args)
        {
        doLoop(Students.class, args);
        System.exit(0);
        }    
    }

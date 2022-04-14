
/*

STRUCTURE FOR DATA
- 2d array, cycle out data
- uses indices for measure of time
- 

SENSORS
- 8 threads
- generate random number -100F - 70F
- data generated every 0.05s (simulated minute)
- after 60 simulated minutes, compile report
- stores data in data structure

REPORT
- print every simulated hour (12 hours total, 12 reports)
- track biggest difference of temp within 10 indices (across all sensors)

*/

import java.util.*;

public class A3Part2 {
    public static void main(String[] args) 
    {

        System.out.println("Program started");

        // stores data from sensors
        // 8 rows, one for each sensor
        // 60 columns, one for each minute in an hour
        int[][] data = new int[8][60];

        SensorsDone sensorsDone = new SensorsDone();

        // create threads/sensors
        // use threadNumbers to decide data row
        Sensor [] sensors = new Sensor[8];
        // create primary guest threads
        for (int i = 0; i < 8; i++)
        {
            sensors[i] = new Sensor(i, data, sensorsDone, sensors);
            sensors[i].start();
        }

    
    }


}

class SensorsDone {
    public static boolean allSensorsDone = false;
    public static boolean canContinue = false;

    public boolean areSensorsDone(Sensor [] sensors) 
    {
        allSensorsDone = true;
        for (int i = 0; i < 8; i++)
        {
             if (sensors[i].finishedHour == false)
                allSensorsDone = false;
                 
        }
        // parsed through check, return var
        return allSensorsDone;
    }

    public void resetSensorsDone() 
    {
        allSensorsDone = false;
    }
    public boolean canContinue() 
    {
        return canContinue;
    }
    public void setCanContinueTrue()
    {
        canContinue = true;
    }
        public void setCanContinueFalse()
    {
        canContinue = false;
    }
}

class Sensor extends Thread {

    int threadNumber;
    // data table
    int[][] data;
    int dataColumn;
    boolean finishedHour = false;
    SensorsDone sensorsDone;
    Sensor[] sensors;

    // var for random numbers
    Random rand = new Random();

    // constructor
    public Sensor (int threadNumber, int[][] data, SensorsDone sensorsDone, Sensor[] sensors)
    {
        this.threadNumber = threadNumber;
        this.data = data;
        this.sensorsDone = sensorsDone;
        this.sensors = sensors;
    }

    @Override
    public void run()
    {
        // generate reading for simulated minute temp
        // do this in a loop with a sleep time of 0.05 sec
        // minutes loop run 60 times, saves to table each time
        // increment index counter
        // nested inside of hour loop that runs 12 times

        System.out.println("Sensor " + threadNumber + " has started");

        
        // initialize data index counter
        //dataColumn = 0;
        for (int m = 0; m < 12; m++)
        {
            try 
            {
                // var to see if sensor is done reading for the hour
                finishedHour = false;
                // need to change this back to 60
                for (int i = 0; i < 10; i++)
                {
                    // random number generation
                    int randomNumber = rand.nextInt(171);
                    // corrects the range
                    int tempReading = randomNumber - 100;
        
                    //System.out.println("randomNumber is " + randomNumber);
                    //System.out.println("TEMPERATURE READ: " + tempReading + " saved into " + "[" + threadNumber + "]" + "[" + i + "]");
                    
                    // save data to table
                    data[threadNumber][i] = tempReading;
                    
                    
                    // sleep time for simulated minutes
                    //dataColumn++;
                    Thread.sleep(50);    // set to 5 seconds for testing 
                }
                // sensor is done, set true
                
                finishedHour = true;
    
                while (sensorsDone.canContinue() == false && threadNumber != 0)
                {
                    Thread.sleep(10);
                    // spins until all are done
                }
    
                Thread.sleep(500);
                if (sensorsDone.areSensorsDone(sensors) == true && threadNumber == 0)  
                {
                    int[] top = new int[5];
                    int[] bot = new int[5];
                    

                    for (int n = 0; n < 60; n++)
                    {
                        for (int l = 0; l < 5; l++)
                        {
                            top[l] = data[1][l];
                            bot[l] = data[2][l];
                        }
                    }
                    
                    // print out data report
                    System.out.println("=======DATA REPORT" + "=======");
                    System.out.print("Top 5 Highest Temperatures: ");
                    for (int n = 0; n < 5; n++)
                        System.out.print(top[n] + " ");
                    System.out.println("");
                    System.out.print("Top 5 Lowest Temperatures: ");
                    for (int n = 0; n < 5; n++)
                        System.out.print(bot[n] + " ");
                    System.out.println();
                    sensorsDone.setCanContinueTrue();
                    sensorsDone.resetSensorsDone();
                    Thread.sleep(100);
                    sensorsDone.setCanContinueFalse();
                    
                }
    
                
            }
                
            catch (Exception e) {
                System.out.println(e);
            }
        }
        
    }
}
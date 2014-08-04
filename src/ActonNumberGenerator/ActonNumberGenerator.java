package ActonNumberGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

/**
 * @author      Jacob Lashley       <chaosdeselva@gmail.com>
 * @version     1.0                 
 * @since       2014-08-04
 */
public class ActonNumberGenerator {
    
    //CONSTANT Values
    //Size of the Array
    public static final int numberArraySize = 997940;
    //The limits for the numbers
    public static final int[] distributionLimits = {83000, 1000, 500, 250, 100, 50, 25, 10, 5};
    
    //Counter of where we are during the insertion
    public static int numberArrayCounter = 0;
    //Last Number to be inserted into the array
    public static int lastNumber = 0;
    
    //Total inserted into each value so we do not go over the distribution limits
    public static int[] numberLimitArray = new int[20];
    //Array holding all of the randomly generated values
    public static int[] numberArray = new int[numberArraySize];
    
    //Random Number collection of integers
    public static RandomNumbers<Integer> randomNumbers = new RandomNumbers<>();
    
    /*
     * The class is meant to generate random numbers based on weights.  This was because the distribution limits with low amounts 
     * always ended soon with basic random functionality. 
     */
    public static class RandomNumbers<E> {
        //Map holding the weighted values
        private final NavigableMap<Double, E> map = new TreeMap<>();
        private final Random random;
        private double total = 0;

        
        public RandomNumbers() {
            this(new Random());
        }

        public RandomNumbers(Random random) {
            this.random = random;
        }
  
        /**
        * This will add the number and the weight to the collection
        *
        * @param weight weight of the number that can be random
        * @param result the number that can be looked up
        */
        public void add(double weight, E result) {
            if (weight <= 0) {
                return;
            }
            total += weight;
            map.put(total, result);
        }
    
        /**
        * This will find a new random number with a weighted bias
        *
        * @return will send the new random number back
        */
        public E getRandomNumber() {
            double value = random.nextDouble() * total;
            return map.ceilingEntry(value).getValue();
        }
    }
    
    /**
    * This is a basic random number lookup function
    *
    * @param min the lowest number the random number can lookup
    * @param max the limit the random value to lookup
    * @return send back the random number found
    */
    public static int basicRandomNumbers(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }
    
    /**
    * This is building the array with random numbers and doing validation checking
    *
    * @param idx the current index to insert a random number
    */
     public static void buildArray(int idx){
        boolean success = false;
        int randomNum = 0;
                
        //Run the loop until a random number has been successfully inserted
        while (!success){
            //Find a random number
            randomNum = randomNumbers.getRandomNumber();
            //If the random number is not the same as the last number and it is not past its limit
            if ( randomNum != lastNumber && checkDistribution(randomNum-1) ){
                //A random number was successfully inserted
                success = true;
                
                //Set number as the new last number
                lastNumber = randomNum;
                
                //Track the number in the limit array
                numberLimitArray[randomNum-1]++;
                
                //Set the random number in the array
                numberArray[idx] = randomNum;
                
            } else {
                //Check if the array has at least generated 10 random numbers before it starts trying to locate swaps as a resolution
                if ( numberArrayCounter > 10 ){
                    //Swap the random number with a new number from a random index
                    success = resolveArray(basicRandomNumbers(0,numberArrayCounter), randomNum, idx);
                    
                }
            }
        }
     }
    
    /**
    * This will resolve issues with the number being inserted into the array.
    * If the limit has been reaches or it was also the same as the last number 
    * then we will locate a new random number to insert.
    *
    * @param randomNum a random number that is the index of the whole array
    * @param currentNumber the current random number attempting to be inserted somewhere in the array
    * @param idx the index of the current insertion point in the array
    * @return true if the value was successfully swapped or false if not
    */
    public static boolean resolveArray(int randomNum, int currentNumber, int idx){
        //This is the node we found that will attempt to swap spots with the current number
        int oNode = numberArray[randomNum]; //Old Node
        
        //Check of the swap location has a next value...
        boolean inBoundsNext = ((randomNum+1) >= 0) && ((randomNum+1) < numberArray.length);
        
        //...if it has a next value then make sure it if not the same as the current number
        int nNode = 0;
        if ( inBoundsNext ){
            nNode = numberArray[randomNum+1]; //Next Node
        }
        
        //Check of the swap location has a previous value...
        boolean inBoundsPrev = ((randomNum-1) >= 0) && ((randomNum-1) < numberArray.length);
        
        //...if it has a previous value then make sure it if not the same as the current number
        int pNode = 0;
        if ( inBoundsPrev ){
            pNode = numberArray[randomNum-1]; //Previous Node
        }
        
        //If the number has a legit swap that can happen then do it
        if ( currentNumber != oNode && currentNumber != nNode && currentNumber != pNode && checkDistribution(currentNumber-1)  ){
            
            //Store the last number used as the swapped value
            lastNumber = oNode; 
            
            //Use the current location in the array as the swapped value
            numberArray[idx] = oNode;
            
            //Give the swapped index the current value
            numberArray[randomNum] = currentNumber;
            
            //Increase the limit to the current value because it did find a location just not the current index
            numberLimitArray[currentNumber-1]++;
            
            //Return true since a legit swap happened successfully
            return true;
        }
        return false;
    }
    
    /**
    * This will check the number attempting to be inserted into the array that the number
    * has not already reached its limit of how many times it can be generated.
    *
    * @param num the value that will be stored in the array as a valid random number
    * @return True if the limit has not been reached and False if it has
    */
    public static boolean checkDistribution(int num) {
        //1-12(83000), 13(1000), 14(500), 15(250), 16(100), 17(50), 18(25), 19(10), 20(5)
        //If any number less than 13 then use index of 0 for the limit
        if ( num < 12 ){ 
            if ( numberLimitArray[num] >= distributionLimits[0] ){
                return false;
            }
        } else {
            //So if 13 is passed in it will do 13-12 to use index 1
            if ( numberLimitArray[num] >= distributionLimits[num-11] ){
                return false;
            }
        }
        
        //The distribution has not reached the limit
        return true;
    }
    
   /**
    * This will write the array of numbers to a file
    */
    public static void writeToFile(){
        try {
            //Create an instance of a file with this name and extension
            File file = new File("test.output");

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            //Setup the buffer to write to the file
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            try (BufferedWriter bw = new BufferedWriter(fw)) {

                //Write to the file and create a new line after each number
                for ( int i = 0; i < numberArraySize; i++ ){
                    bw.write(String.valueOf(numberArray[i]));
                    bw.newLine();
                    
                    //If the value is equal to 20 then print it to the console
                    if ( numberArray[i] == 20 ){
                        System.out.println(" 20 Found on line #" + i);
                    }
                }       
            }
        } catch (IOException e) {
           System.out.println("Error Writing to a file.");
        }
    }
    
    /**
    * This is the main function the runs on execution
    *
    * @param args the command line arguments
    */
    public static void main(String[] args) {
        
        //Add the random number range with the weighted values to each number.
        randomNumbers.add(0.005, 20);
        randomNumbers.add(0.01, 19);
        randomNumbers.add(0.1, 18);
        randomNumbers.add(0.2, 17);
        randomNumbers.add(0.3, 16);
        randomNumbers.add(0.4, 15);
        randomNumbers.add(0.6, 14);
        randomNumbers.add(0.8, 13);
        randomNumbers.add(1.0, 12);
        randomNumbers.add(1.0, 11);
        randomNumbers.add(1.0, 10);
        randomNumbers.add(1.0, 9);
        randomNumbers.add(1.0, 8);
        randomNumbers.add(1.0, 7);
        randomNumbers.add(1.0, 6);
        randomNumbers.add(1.0, 5);
        randomNumbers.add(1.0, 4);
        randomNumbers.add(1.0, 3);
        randomNumbers.add(1.0, 2);
        randomNumbers.add(1.0, 1);

        //Run a loop to build the array with random numbers.
        for ( int i = 0; i < numberArraySize; i++ ){
            buildArray(i);
            numberArrayCounter++;
        }              
        
        //Write all the random numbers to a file.
        writeToFile();
    }
}

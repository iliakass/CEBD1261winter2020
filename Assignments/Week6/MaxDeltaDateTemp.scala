package com.cebd.spark

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.log4j._
import scala.math.min
import scala.math.max

/** Find the date with the greatest range between min and max*/
object MaxDeltaDateTemp {
  
  def parseLine(line:String) = {
    val fields = line.split(",") // comma separate values
    
    //val stationID = fields(0) // stationID was previously x._1; x._1 is now the Date
    val Date = fields(1) // fields(1) is the date in YYYYMMDD format
    
    val entryType = fields(2) // entryType is x._2 populated by Tmax, Tmin, Precipitation
  
    val temperature = fields(3).toFloat * 0.1f * (9.0f / 5.0f) + 32.0f // temp is x._3
    // values convert Celcius to Farenheit
    
    (Date, entryType, temperature) 
  }
    /** Our main function where the action happens */
  def main(args: Array[String]) {
   
    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)
    
    val conf = new  SparkConf().setMaster("local[*]").setAppName("MaxDeltaTemperatures").set("spark.driver.host", "localhost");
    // Create a SparkContext using every core of the local machine, named MaxDeltaTemperatures
    // alternative: val sc = new SparkContext("local[*]", "MaxDeltaTemperatures")
    val sc = new SparkContext(conf)
    
    // Read each line of input data
    val lines = sc.textFile("../SparkContent/1800.csv")
    
    // Convert to (stationID, entryType, temperature) tuples
    val parsedLines = lines.map(parseLine)
    
    // Filter out all but TMIN and TMAX entries
    // val Temps = parsedLines.filter(x => x._2 == "TMAX" || x._2 == "TMIN")
    val TempsMax = parsedLines.filter(x => x._2 == "TMAX")
    val TempsMin = parsedLines.filter(x => x._2 == "TMIN")
    
    // Convert to (stationID, temperature)
    val stationTempsMax = TempsMax.map(x => (x._1, x._3.toFloat ) )
    val stationTempsMin = TempsMin.map(x => (x._1, x._3.toFloat ) )
    
    
    // Exercise: Modify the code to list stations with the maximum difference (delta) between min and max temperature 
    // Interpretation: by station, show delta between min and max
    
    // Reduce by stationID retaining: the difference between the global Max for a Station and the global Min for the same Station
    val TempsByStationMax = stationTempsMax.reduceByKey( (x,y) => max(x,y)) 
    val TempsByStationMin = stationTempsMin.reduceByKey( (x,y) => min(x,y)) 
    
    // Collect, and determine number of keys
    // Ref.: https://www.geeksforgeeks.org/scala-map-count-method-with-example/
    val resultsMax = TempsByStationMax.collect()
    val resultsMax_size = resultsMax.count(z=>true) // number of keys present 
    val resultsMin = TempsByStationMin.collect()
    val resultsMin_size = resultsMin.count(z=>true)

    // Pre-populated array
    // Ref: https://stackoverflow.com/questions/2496241/create-and-populate-two-dimensional-array-in-scala
    var res_table = Array.tabulate(resultsMax_size,resultsMin_size)((x, y) => (x, y))
    
    
    // Ref: http://stackoverflow.com/questions/12969048/ddg#12969153
    // Ref: https://www.tutorialspoint.com/scala/scala_arrays.htm
    // Ref: https://alvinalexander.com/scala/how-to-create-multidimensional-arrays-in-scala-cookbook
    
    var max_array = Array.ofDim[String](resultsMax_size+1,3) // (rows, cols)
    var min_array = Array.ofDim[String](resultsMin_size+1,3)
    
    var res_array = Array.ofDim[Any](resultsMin_size+1,2) // results of the sum of max and -min array
    
    var i = 0
    
    for (result <- resultsMax.sorted) {//sorted
       val station = result._1
       val temp_1 = result._2
       val formattedTemp = (f"$temp_1%.2f").toFloat
       //val formattedTemp = (f"$temp_1%.2f F")
       //println(s"$station temperature: $formattedTemp") 
       
       max_array(i)(0) = "TMAX"
       max_array(i)(1) = station
       max_array(i)(2) = formattedTemp.toString
       
       i += 1
   }
   
   var j = 0
   for (result <- resultsMin.sorted) {//sorted
       val station = result._1
       val temp_1 = - result._2 // negative value conversion
       val formattedTemp = (f"$temp_1%.2f").toFloat
       //val formattedTemp = f"$temp_1%.2f F"
       //println(s"$station temperature: $formattedTemp") 
       
       min_array(j)(0) = "TMIN"
       min_array(j)(1) = station
       min_array(j)(2) = formattedTemp.toString
       
       j += 1
    }
    
   // Show array results for min and max seperately
   /*
   // Ref: https://www.tutorialspoint.com/scala/scala_arrays.htm
   for (n <- 0 to i-1) {
     for ( m <- 0 to 2) {
            println(" " + max_array(n)(m));
     }
   }
   
   for (n <- 0 to j-1) {
     for ( m <- 0 to 2) {
            println(" " + min_array(n)(m));
     }
   }
   */
   
   //Show max range between min and max by station
   var a = 0.0
   var b = 0.0
   var k = 0
   
   for (n <- 0 to j-1) {
      a =  (min_array(n)(2)).toFloat
      b =  (max_array(n)(2)).toFloat
      val c = a + b // sum of Max and -Min as Float
      res_array(n)(1) = (c)
      val d = min_array(n)(1) //+ max_array(n)(1)
      res_array(n)(0) = d // assignment of date as String
      //val formattedTemp = f"$c%.2f F"
      //println(s"$d temperature: $formattedTemp") 
      
      var res = Map(d -> c)
      k += 1
      
     // Obtain Max from Array
     // Ref: https://stackoverflow.com/questions/20285209/find-min-and-max-elements-of-array
      if(j-1 == k) {
         var res2 = res.max
        
           val date = res2._1
           val temp_1 = res2._2 
           //val formattedTemp = (f"$temp_1%.2f").toFloat
           val formattedTemp = f"$temp_1%.2f F"
           println(s"On $date max temperature range: $formattedTemp") 
         
      }
      
   }
   
  }
}
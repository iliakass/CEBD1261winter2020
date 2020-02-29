package com.cebd.spark

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.log4j._
import scala.math.min
import scala.math.max

/** Find the minimum temperature by weather station */
object MaxDeltaTemperatures {
  
  def parseLine(line:String)= {
    val fields = line.split(",") // comma separate values
    val stationID = fields(0) // stationID is x._1; fields(1) is the date, and it is not used by the parseline
    val entryType = fields(2) // entryType is x._2 populated by Tmax, Tmin, Precipitation
    val temperature = fields(3).toFloat * 0.1f * (9.0f / 5.0f) + 32.0f // temp is x._3
    (stationID, entryType, temperature) // values convert Celcius to Farenheit
  }
    /** Our main function where the action happens */
  def main(args: Array[String]) {
   
    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)
    
    val conf = new  SparkConf().setMaster("local[*]").setAppName("MaxTemperatures").set("spark.driver.host", "localhost");
    // Create a SparkContext using every core of the local machine, named MaxTemperatures
    // alternative: val sc = new SparkContext("local[*]", "MaxTemperatures")
    val sc = new SparkContext(conf)
    
    // Read each line of input data
    val lines = sc.textFile("../SparkContent/1800.csv")
    
    // Convert to (stationID, entryType, temperature) tuples
    val parsedLines = lines.map(parseLine)
    
    
    // Filter out all but TMIN and TMAX entries, parseline is skipped by definition
    val minTemps = parsedLines.filter(x => x._2 == "TMIN")
    val maxTemps = parsedLines.filter(x => x._2 == "TAX")
    
    // Convert to (stationID, temperature)
    val stationTempsMin = minTemps.map(x => (x._1, x._3.toFloat))
    val stationTempsMax = maxTemps.map(x => (x._1, x._3.toFloat))
    
    
    // Reduce by stationID retaining the minimum and maximum temperature found by Station
    val minTempsByStation = stationTempsMin.reduceByKey( (x,y) => min(x,y))
    val maxTempsByStation = stationTempsMax.reduceByKey( (x,y) => max(x,y))
    
    // Calculate delta for stations if and only if a station has a min and max temp
    
    // Collect, format
    val resultsMax = maxTempsByStation.collect()
    val resultsMin = minTempsByStation.collect()
    
    for (resultsMax <- resultsMax.sorted) {
      val stationMax = resultsMax._1
      val tempMax = resultsMax._2
      
      for (resultsMin <- resultsMin.sorted) {
         val stationMin = resultsMin._1
         
           if (stationMax == stationMin) {
             val tempMin = resultsMin._2
             val tempDelta = tempMax - tempMin
             
             val formattedTemp = f"$tempDelta%.2f F"
             println(s"$stationMax delta temperature: $formattedTemp") 
           }
         
         
      }
    }
    
    
    
  }
}
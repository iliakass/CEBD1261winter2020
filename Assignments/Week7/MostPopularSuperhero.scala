package com.cebd.spark

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.log4j._

/** Find the superhero with the most co-appearances. */
object MostPopularSuperhero {
  
  // Function to extract the hero ID and number of connections from each line
  def countCoOccurences(line: String) = {
    var elements = line.split("\\s+")
    ( elements(0).toInt, elements.length - 1 )
  }
  
  // Function to extract hero ID -> hero name tuples (or None in case of failure)
  def parseNames(line: String) : Option[(Int, String)] = {
    var fields = line.split('\"')
    if (fields.length > 1) {
      return Some(fields(0).trim().toInt, fields(1))
    } else {
      return None // flatmap will just discard None results, and extract data from Some results.
    }
  }
 
  /** Our main function where the action happens */
  def main(args: Array[String]) {
   
    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)
    
    val conf = new  SparkConf().setMaster("local[*]").setAppName("MostPopularSuperhero").set("spark.driver.host", "localhost");
    // Create a SparkContext using every core of the local machine, named MostPopularSuperhero
    //alternative: val sc = new SparkContext("local[*]", "MostPopularSuperhero")
    val sc = new SparkContext(conf)   
    
    // Build up a hero ID -> name RDD
    val names = sc.textFile("../SparkContent/Marvel-names.txt")
    val namesRdd = names.flatMap(parseNames)
    
    // Load up the superhero co-apperarance data
    val lines = sc.textFile("../SparkContent/Marvel-graph.txt")
    
    //     Convert to (heroID, number of connections) RDD
    val pairings = lines.map(countCoOccurences)
    
    // Combine entries that span more than one line
    val totalFriendsByCharacter = pairings.reduceByKey( (x,y) => x + y )
    
    // Flip it to # of connections, hero ID
    val flipped = totalFriendsByCharacter.map( x => (x._2, x._1) )
    
    
    // Exercise: Super Hero: Print the top 10 superheros as a list.
    
    // Find the top 10 of connections
    val mostPopular = flipped.sortByKey(false).take(10)
    val res = mostPopular
    
    // Create the list
    // Ref: https://stackoverflow.com/questions/6557169/how-to-declare-empty-list-and-then-add-string-in-scala
    // Ref: https://alvinalexander.com/scala/how-add-elements-to-a-list-in-scala-listbuffer-immutable
    var dk = List("PopName",Nil)
    
    // Print the results, flipping the (PopName, count) results to word: count as we go.
    for (result <- res) {
      
      // number of connections
      val count = result._1
      
      // Look up the name (lookup returns an array of results, so we need to access the first result with (0)).
      val PopName = namesRdd.lookup(result._2)(0)
      
      //println(s"$PopName: $count")
      dk = (PopName, count) :: dk
    }
    println(dk)
    
   
    
    /*
    // Find the max # of connections
    val mostPopular = flipped.max
    
    // Look up the name (lookup returns an array of results, so we need to access the first result with (0)).
    val mostPopularName = namesRdd.lookup(mostPopular._2)(0)
    
    // Print out our answer!
    println(s"$mostPopularName is the most popular superhero with ${mostPopular._1} co-appearances.") 
    */
    
    
  }
  
 
  
}

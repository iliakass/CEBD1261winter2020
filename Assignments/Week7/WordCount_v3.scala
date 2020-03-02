package com.cebd.spark

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.log4j._

/** Count up how many of each word occurs in a book, using regular expressions and sorting the final results */
object WordCount_v3 {
 
  /** Our main function where the action happens */
  def main(args: Array[String]) {
   
    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)
    
    val conf = new  SparkConf().setMaster("local[*]").setAppName("WordCount_v3").set("spark.driver.host", "localhost");
    // Create a SparkContext using every core of the local machine, named WordCountBetterSorted
    //alternative: val sc = new SparkContext("local[*]", "WordCount_v3")
    val sc = new SparkContext(conf)  
    
    // Load each line of my book into an RDD
    val input = sc.textFile("../SparkContent//book.txt")
    
    // Split using a regular expression that extracts words
    val words = input.flatMap(x => x.split("\\W+"))
    
    // Normalize everything to lowercase
    val lowercaseWords = words.map(x => x.toLowerCase())
    
    
    // Exclude “you”, “to”, “your”, “the”, “a”, “of”, “and”
    // Ref: https://stackoverflow.com/questions/42282599/filter-words-from-one-list-with-words-from-another
    val badWords = List("you","to","your","the","a","of","and")
    val removedWords = lowercaseWords.filter(!badWords.contains(_))
    
    // Count of the occurrences of each word
    val wordCounts = removedWords.map(x => (x, 1)).reduceByKey( (x,y) => x + y )
    
    // Flip (word, count) tuples to (count, word) and then sort by key (the counts)
    // Sort in Descending order using sortbyKey(false)
    // Ref: https://www.javatpoint.com/apache-spark-sortbykey-function
    
    // Take 10: https://stackoverflow.com/questions/46528820/why-the-number-of-partitions-in-sortbykey-is-not-equal-by-default-to-one 
    val wordCountsSorted = wordCounts.map( x => (x._2, x._1) ).sortByKey(false).take(10)
    val res = wordCountsSorted
    
    
    // Print the results, flipping the (count, word) results to word: count as we go.
    for (result <- res) {
      val count = result._1
      val word = result._2
      println(s"$word: $count")
    }
    
    
    
  }
  
}


package com.cebd.spark

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.sql._
import org.apache.log4j._

object SparkSQL {
  
  case class Person(ID:Int, name:String, age:Int, numFriends:Int)
  
  def mapper(line:String): Person = {
    val fields = line.split(',')  
    
    val person:Person = Person(fields(0).toInt, fields(1), fields(2).toInt, fields(3).toInt)
    return person
  }
  
  /** Our main function where the action happens */
  def main(args: Array[String]) {
    
    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)
    
    // Use new SparkSession interface in Spark 2.0
    val spark = SparkSession
      .builder
      .appName("SparkSQL")
      .config("spark.driver.host", "localhost")
      .master("local[*]")
          // .config("spark.sql.warehouse.dir", "file:///C:/temp") 
          // Necessary to work around a Windows bug in Spark 2.0.0; omit if you're not on Windows.
      
      .getOrCreate()
      

    val lines = spark.sparkContext.textFile("../SparkContent/fakefriends.csv")
    val people = lines.map(mapper)
    
    // Infer the schema, and register the DataSet as a table.
    import spark.implicits._
    val schemaPeople = people.toDS
    
    schemaPeople.printSchema()
    schemaPeople.createOrReplaceTempView("people")
    
    // SQL can be run over DataFrames that have been registered as a table
    val teenagers = spark.sql("SELECT * FROM people WHERE age >= 13 AND age <= 19")
    val results = teenagers.collect()
    
    //println(results.getClass) Get class or results variable
    //res: class [Lorg.apache.spark.sql.Row; // as an WrappedArray[org.apache.spark.sql.Row]
    
    //results.foreach(println)
    //println(results(0)) //ID:Int, name:String, age:Int, numFriends:Int
    
    // Convert to dataframe
    import org.apache.spark.sql.Row
    import org.apache.spark.sql.types.{StructType, StructField, StringType, IntegerType, ArrayType}
     
    //Ref: https://sparkbyexamples.com/spark/spark-explode-nested-array-to-rows/
    val arraySchema = new StructType()
    .add("ID",IntegerType)
    .add("name",StringType)
    .add("age",IntegerType)
    .add("numFriends",IntegerType)

    val df = spark.createDataFrame(
        spark.sparkContext.parallelize(results),arraySchema)
        
    df.printSchema()
    df.show()
    
    //Export dataframe to CSV
    //Ref: https://stackoverflow.com/questions/32527519/how-to-export-dataframe-to-csv-in-scala
    df.coalesce(1)
      .write
      .option("header", "true")
      .csv("../SparkContent/data.csv")
    
    spark.stop()
  }
}
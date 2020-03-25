package com.cebd.spark

import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.sql._
import org.apache.log4j._

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StructType, StructField, StringType, LongType, IntegerType, DoubleType}
       

object ProjectSparkSQL {
  
  case class Iris(
      sepal_length:String,
      sepal_width:String,
      petal_length:String,
      petal_width:String,
      species:String)
      
  //case class ParseOp[T](op: String => T)

  
  def mapper(line:String): Iris = {
    
    val fields = line.split(',')  
    
    val iris:Iris = Iris(
        fields(0).toString, //sepal_length
        fields(1).toString, //sepal_width
        fields(2).toString, //petal_length
        fields(3).toString, //petal_width
        fields(4).toString  //species
        ) 
        
    return iris
    
    // Ref: https://archive.ics.uci.edu/ml/datasets/Iris
    /* Data dictionary: 
				Attribute Information: sepal_length,sepal_width,petal_length,petal_width,species

				1. sepal length in cm 
				2. sepal width in cm 
				3. petal length in cm 
				4. petal width in cm 
				5. class: 
				-- Iris Setosa 
				-- Iris Versicolour 
				-- Iris Virginica
				
				Data Set Characteristics:  Multivariate
				Attribute Characteristics: Real
				Associated Tasks: Classification
				Number of Instances: 150
				Number of Attributes: 4
				Missing Values? No
				
				Source:
					Creator: R.A. Fisher 
					Donor: Michael Marshall (MARSHALL%PLU '@' io.arc.nasa.gov)
					
				Data Set Information:
					This is perhaps the best known database to be found in the pattern recognition literature. 
					Fisher's paper is a classic in the field and is referenced frequently to this day. (See Duda & Hart, for example.) 
					The data set contains 3 classes of 50 instances each, where each class refers to a type of iris plant. 
					One class is linearly separable from the other 2; the latter are NOT linearly separable from each other. 
					
				Ref: https://gist.github.com/curran/a08a1080b88344b0c8a7
				Each row of the table represents an iris flower, including its species and dimensions of its botanical parts, 
				sepal and petal, in centimeters.
				
				CSV source:
				https://tableconvert.com/?output=csv&data=https://gist.githubusercontent.com/curran/a08a1080b88344b0c8a7/raw/d546eaee765268bf2f487608c537c05e22e4b221/iris.csv
    */
    
    
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
      
    // IRIS file: tableconvert_2020-03-22_225401.csv
    val lines = spark.sparkContext.textFile("../SparkContent/tableconvert_2020-03-22_225401.csv")
    val iris = lines.map(mapper)
    
    // Infer the schema, and register the DataSet as a table.
        import spark.implicits._
        val schemaIris = iris.toDS
        
        //schemaIris.printSchema()
        // Res: 
          /*    root
               |-- sepal_length: string (nullable = true)
               |-- sepal_width: string (nullable = true)
               |-- petal_length: string (nullable = true)
               |-- petal_width: string (nullable = true)
               |-- species: string (nullable = true)
               * 
               */
        
        schemaIris.createOrReplaceTempView("iris")
    
    // SQL can be run over DataFrames that have been registered as a table
        val flowers = spark.sql("SELECT * FROM iris") // kept in case an SQL filter is needed during analysis
        val results = flowers.collect()
    
        
    // results.foreach(println)
    
    // println(results.getClass) // Get class of results variable
      // res: class [Lorg.apache.spark.sql.Row; // as an WrappedArray[org.apache.spark.sql.Row]
    
   // Convert results to dataframe
        
      
          //Ref: https://sparkbyexamples.com/spark/spark-explode-nested-array-to-rows/
          val arraySchema = new StructType()
          .add("sepal_length",StringType) //sepal_length
          .add("sepal_width",StringType) //sepal_width
          .add("petal_length",StringType) //petal_length
          .add("petal_width",StringType) //petal_width
          .add("species",StringType) //species
          
          val df = spark.createDataFrame(
              spark.sparkContext.parallelize(results),arraySchema)
          
      // df.printSchema()
          // Res: 
          /*    root
               |-- sepal_length: string (nullable = true)
               |-- sepal_width: string (nullable = true)
               |-- petal_length: string (nullable = true)
               |-- petal_width: string (nullable = true)
               |-- species: string (nullable = true)
               * 
               */
              
// // Start of Exploratory Analysis and Data Cleaning
              
      // df.show() 
          //res: only showing top 20 rows for dataframe
      
      // Drop the first row since it is a copy of the headers
      // Ref: https://stackoverflow.com/questions/45316810/how-to-delete-the-first-few-rows-in-dataframe-scala-ssark
      
          val rows = df.rdd.zipWithUniqueId().map {
            case (row, id) => Row.fromSeq(row.toSeq :+ id)
            }
    
          var df_mod = spark.createDataFrame(
              rows, StructType(df.schema.fields :+ StructField("id", LongType, false))
              )
    
          df_mod = df_mod
                    .filter($"id" > 0) // remove first header row 0
                    .drop("id") // drop temporarly create ID column
      
      //df_mod.show() 
        //res: show first 20 results for modified dataframe
    
 // Determine number of keys, in order to create a replacement array
      // Ref: https://www.geeksforgeeks.org/scala-map-count-method-with-example/
      val results_size = results.count(z=>true) // number of keys present 
      
      //println(results_size) 
        //res: 151 rows in the original array
      
      //println(df.count())
        //res: 151 rows in the original dataframe
      
      //println(df_mod.count()) 
        //res: 150 rows with the removed header row for the modified dataframe
      
      
 // Transfer 4 columns to Double expect Species column
      // Ref: https://stackoverflow.com/questions/29383107/how-to-change-column-types-in-spark-sqls-dataframe
      
      val df_copy = df_mod.select( 
          df_mod("sepal_length").cast(DoubleType).as("sepal_length"),
          df_mod("sepal_width").cast(DoubleType).as("sepal_width"),
          df_mod("petal_length").cast(DoubleType).as("petal_length"),
          df_mod("sepal_width").cast(DoubleType).as("sepal_width"),
          df_mod("species").cast(StringType).as("species")
      )
      
      //df_copy.printSchema()
        //res:
        /*      root
         |-- sepal_length: double (nullable = true)
         |-- sepal_width: double (nullable = true)
         |-- petal_length: double (nullable = true)
         |-- sepal_width: double (nullable = true)
         * 
         */
      
      //println(df_copy.getClass) 
        //res: class org.apache.spark.sql.Dataset
      
      
// Count number of null or blank entries for 4 attribute columns
      // Ref: https://stackoverflow.com/questions/41765739/count-the-number-of-non-null-values-in-a-spark-dataframe
      // df_copy.describe().filter($"summary" === "count").show
        //res: 
        /*
        +-------+------------+-----------+------------+-----------+-------+
        |summary|sepal_length|sepal_width|petal_length|sepal_width|species|
        +-------+------------+-----------+------------+-----------+-------+
        |  count|         150|        150|         150|        150|    150|
        +-------+------------+-----------+------------+-----------+-------+
        
        * 
        */
      
      // df_copy.describe().filter($"summary" === "isNullorBlank").show
        // res:
        /*
        +-------+------------+-----------+------------+-----------+-------+
        |summary|sepal_length|sepal_width|petal_length|sepal_width|species|
        +-------+------------+-----------+------------+-----------+-------+
        +-------+------------+-----------+------------+-----------+-------+
       	
       	* 
       	*/
      
      
 // Count number of null entries for for the species string column
      // Ref:https://stackoverflow.com/questions/40500732/scala-dataframe-null-check-for-columns
      // df_copy.filter("species IS null OR species == '' ").show()
      
      // Ref: https://stackoverflow.com/questions/44329398/count-empty-values-in-dataframe-column-in-spark-scala
          /*
          val df_show = df_copy.filter(
              df_copy("species").isNull 
              || df_copy("species") === "" 
              || df_copy("species").isNaN).count()
         
          println(df_show)
          		// res: 0
      		 	* 
      		 	*/

      
  // // End of Exploratory Analysis and Data Cleaning
      
    /*
    //Export dataframe to CSV for Analysis with other tools
    //Ref: https://stackoverflow.com/questions/32527519/how-to-export-dataframe-to-csv-in-scala
    df_copy.coalesce(1)
      .write
      .option("header", "true")
      .csv("../SparkContent/project_data.csv")
    
    *
    */
      
      
  // // Start of Classification through ML
  
      // Ref: https://spark.apache.org/docs/latest/ml-classification-regression.html#random-forest-classifier
        // Random forest classifier
        
        import org.apache.spark.ml.Pipeline
        import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier}
        import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
        import org.apache.spark.ml.feature.{IndexToString, StringIndexer, VectorIndexer}
        
        // Load DataFrame.
        val data = df_copy
          
        println("worksA")
        // Index labels, adding metadata to the label column.
        // Fit on whole dataset to include all labels in index.
        val labelIndexer = new StringIndexer()
          .setInputCol("label")
          .setOutputCol("indexedLabel")
          .fit(data)
        println("worksB")
        // Automatically identify categorical features, and index them.
        // Set maxCategories so features with > 4 distinct values are treated as continuous.
        val featureIndexer = new VectorIndexer()
          .setInputCol("features")
          .setOutputCol("indexedFeatures")
          .setMaxCategories(4)
          .fit(data)
        println("worksC")
        // Split the data into training and test sets (30% held out for testing).
        val Array(trainingData, testData) = data.randomSplit(Array(0.7, 0.3))
        println("worksD")
        // Train a RandomForest model.
        val rf = new RandomForestClassifier()
          .setLabelCol("indexedLabel")
          .setFeaturesCol("indexedFeatures")
          .setNumTrees(10)
        println("worksE")
        // Convert indexed labels back to original labels.
        val labelConverter = new IndexToString()
          .setInputCol("prediction")
          .setOutputCol("predictedLabel")
          .setLabels(labelIndexer.labels)
        
        // Chain indexers and forest in a Pipeline.
        val pipeline = new Pipeline()
          .setStages(Array(labelIndexer, featureIndexer, rf, labelConverter))
        
        // Train model. This also runs the indexers.
        val model = pipeline.fit(trainingData)
        
        // Make predictions.
        val predictions = model.transform(testData)
        
        // Select example rows to display.
        predictions.select("predictedLabel", "label", "features").show(5)
        
        // Select (prediction, true label) and compute test error.
        val evaluator = new MulticlassClassificationEvaluator()
          .setLabelCol("indexedLabel")
          .setPredictionCol("prediction")
          .setMetricName("accuracy")
        val accuracy = evaluator.evaluate(predictions)
        println(s"Test Error = ${(1.0 - accuracy)}")
        
        val rfModel = model.stages(2).asInstanceOf[RandomForestClassificationModel]
        println(s"Learned classification forest model:\n ${rfModel.toDebugString}")
              
      
  // // End of Classification through ML
      
    
      
    
      
    spark.stop()
  }
}
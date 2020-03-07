// Ref: https://towardsdatascience.com/healthcare-dataset-with-spark-6bf48019892b

// Setting up Spark and getting data

from pyspark.sql import SparkSession
import pyspark.sql as sparksql
spark = SparkSession.builder.appName('stroke').getOrCreate()
train = spark.read.csv('train_2v.csv', inferSchema=True,header=True)

// Exploration of data

train.printSchema()

// Analysis

// What type of work has more cases of stroke:

// Create DataFrame as a temporary view
train.createOrReplaceTempView('table')

// SQL
spark.sql("SELECT work_type, count(work_type) as work_type_count
FROM table WHERE stroke == 1 GROUP BY work_type ORDER BY
work_type_count DESC").show()

// who participated in this clinic measurement
spark.sql("SELECT gender, count (gender) as count_gender,
count(gender)*100/sum(count(gender)) over() as percent FROM table
GROUP BY gender").show()

// retrieve information about how many Female/Male have a stroke
spark.sql("SELECT gender, count(gender), (COUNT(gender) * 100.0)
/(SELECT count(gender) FROM table WHERE gender == 'Male') as
percentage FROM table WHERE stroke = '1' and gender = 'Male' GROUP
BY gender").show()

spark.sql("SELECT gender, count(gender), (COUNT(gender) * 100.0)
/(SELECT count(gender) FROM table WHERE gender == 'Female') as
percentage FROM table WHERE stroke = '1' and gender = 'Female' GROUP
BY gender").show()

// see if the age has an influence on stroke and what is the risk by age
spark.sql("SELECT age, count(age) as age_count FROM table WHERE
stroke == 1 GROUP BY age ORDER BY age_count DESC").show()

// use filter operation to calculate the number of stroke cases for people after 50 years
train.filter((train['stroke'] == 1) & (train['age'] > '50')).count()

// Cleaning data

// fill in missing values
train_f = train.na.fill('No Info', subset=['smoking_status'])

// fill in miss values with mean
from pyspark.sql.functions import mean
mean = train_f.select(mean(train_f['bmi'])).collect()
mean_bmi = mean[0][0]
train_f = train_f.na.fill(mean_bmi,['bmi'])

// The encoding allows algorithms which expect continuous features to use categorical features.
// StringIndexer -> OneHotEncoder -> VectorAssembler
from pyspark.ml.feature import (VectorAssembler,OneHotEncoder,
                                StringIndexer)

gender_indexer = StringIndex(inputCol = 'gender', outputCol = 'genderIndex')
gender_encoder = OneHotEncoder(inputCol = 'genderIndex', outputCol = 'genderVec')

ever_married_indexer = StringIndex(inputCol = 'ever_married', outputCol = 'ever_marriedIndex')
ever_married_encoder = OneHotEncoder(inputCol = 'ever_marriedIndex', outputCol = 'ever_marriedVec')

Residence_type_indexer = StringIndex(inputCol = 'Residence_type', outputCol = 'Residence_typeIndex')
Residence_type_encoder = OneHotEncoder(inputCol = 'Residence_typeIndex', outputCol = 'Residence_typeVec')

smoking_status_indexer = StringIndex(inputCol = 'smoking_status', outputCol = 'smoking_statusIndex')
smoking_status_encoder = OneHotEncoder(inputCol = 'smoking_statusIndex', outputCol = 'smoking_statusVec')

// Create an assembler, that combines a given list of columns 
// into a single vector column to train ML model. 

// Author uses the vector columns, that we got after one_hot_encoding.

assembler = VectorAssembler(inputCols=['genderVec',
 'age',
 'hypertension',
 'heart_disease',
 'ever_marriedVec',
 'work_typeVec',
 'Residence_typeVec',
 'avg_glucose_level',
 'bmi',
 'smoking_statusVec'],outputCol='features')

// Create a DecisionTree object
from pyspark.ml.classification import DecisionTreeClassifier
dtc = DecisionTreeClassifier(labelCol='stroke',featuresCol='features')

// To wrap all of that Spark ML represents such a workflow as a Pipeline, 
// which consists of a sequence of PipelineStages to be run in a specific order.

from pyspark.ml import Pipeline
pipeline = Pipeline(stages=[gender_indexer, ever_married_indexer, work_type_indexer, Residence_type_indexer,
                           smoking_status_indexer, gender_encoder, ever_married_encoder, work_type_encoder,
                           Residence_type_encoder, smoking_status_encoder, assembler, dtc])

// split dataset to train and test
train_data,test_data = train_f.randomSplit([0.7,0.3])

//  Fit the model: Author uses the pipeline that was created and train_data
model = pipeline.fit(train_data)

// transform the test_data
dtc_predictions = model.transform(test_data)

// evaluate a model
from pyspark.ml.evaluation import MulticlassClassificationEvaluator
# Select (prediction, true label) and compute test error
acc_evaluator = MulticlassClassificationEvaluator(labelCol="stroke", predictionCol="prediction", metricName="accuracy")
dtc_acc = acc_evaluator.evaluate(dtc_predictions)
print('A Decision Tree algorithm had an accuracy of: {0:2.2f}%'.format(dtc_acc*100))

// Note: A Decision Tree algorithm had an accuracy of: 98.08%

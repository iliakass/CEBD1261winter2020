object Week5_exec {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  
  
// Exercise 1

// Write a function to compute factorial (5! = 5*4*3*2*1)
// Ref: https://alvinalexander.com/scala/scala-factorial-recursion-example-recursive-programming

def factorial(n: Int): Int = n match {
	 	case 1 => 1
	 	case _ => n * factorial(n-1)
 	}                                         //> factorial: (n: Int)Int

factorial(5)                                      //> res0: Int = 120

// Then write another function to call fact function and println few examples (i.e, 6,8,4.52)
factorial(6)                                      //> res1: Int = 720
factorial(8)                                      //> res2: Int = 40320

// Ref: https://rosettacode.org/wiki/Gamma_function#Scala
import java.util.Locale._

  def stGamma(x:Double) : Double = {
  		math.sqrt(2*math.Pi/x) * math.pow((x/math.E), x)
  		}                                 //> stGamma: (x: Double)Double
 
  def laGamma(x:Double) : Double = {
    val p=Seq(676.5203681218851, -1259.1392167224028, 771.32342877765313,
             -176.61502916214059, 12.507343278686905, -0.13857109526572012,
                9.9843695780195716e-6, 1.5056327351493116e-7)
 
    if(x < 0.5) {
      math.Pi/(math.sin(math.Pi*x)*laGamma(1-x))
    }
    else {
      val x2=x-1
      val t=x2+7+0.5
      val a=p.zipWithIndex.foldLeft(0.99999999999980993)((r,v) => r+v._1/(x2+v._2+1))
      math.sqrt(2*math.Pi)*math.pow(t, x2+0.5)*math.exp(-t)*a
    }
  }                                               //> laGamma: (x: Double)Double
 
  
println("Gamma    Stirling             Lanczos")  //> Gamma    Stirling             Lanczos
for(x <- 4.50 to 4.53 by 0.01) {
	println("%.2f  ->  %.16f   %.16f".formatLocal(ENGLISH, x, stGamma(x), laGamma(x)))
                                                  //> 4.50  ->  11.4186515612713870   11.6317283965674460
                                                  //| 4.51  ->  11.5789653344715200   11.7945517189580560
                                                  //| 4.52  ->  11.7418190667569070   11.9599510118631840
                                                  //| 4.53  ->  11.9072558472699810   12.1279699231160580
  }
      

// Exercise 2

// We will work with lists. Here are some codes to learn how we work with lists:

val List_1 = List("Alice", "John", "Dina", "Valentin")
                                                  //> List_1  : List[String] = List(Alice, John, Dina, Valentin)
println(List_1(1))                                //> John
println(List_1.head)                              //> Alice
println(List_1.tail)                              //> List(John, Dina, Valentin)

for (name <- List_1) {
	println(name)                             //> Alice
                                                  //| John
                                                  //| Dina
                                                  //| Valentin
	}

// 2-a) Then write another function to compute the factorial via reading from list.
// For instance, you will get list as (1,2,3,4,5) then multiply them together and compute the factorial.

// Ref: https://stackoverflow.com/questions/22966705/scala-what-is-the-difference-between-size-and-length-of-a-seq
val List_2 = List(1,2,3,4,5)                      //> List_2  : List[Int] = List(1, 2, 3, 4, 5)
val m = (List_2.length)                           //> m  : Int = 5
var n = 1                                         //> n  : Int = 1

for (name <- 1 to m) {
	n = name * n
	println(n)
	}                                         //> 1
                                                  //| 2
                                                  //| 6
                                                  //| 24
                                                  //| 120

// 2-b) Use the reduce method (Ref: h​ttps://www.geeksforgeeks.org/scala-reduce-function/​)
// and re-compute the factorial number.

// source collection
val collection = List(1,2,3,4,5)                  //> collection  : List[Int] = List(1, 2, 3, 4, 5)
  
// converting every element to a pair of the form (x,1)
// 1 is initial frequency of all elements
val new_collection = collection.map(x => (x,1))   //> new_collection  : List[(Int, Int)] = List((1,1), (2,1), (3,1), (4,1), (5,1)
                                                  //| )

  
// adding elements at correspnding positions
val res = new_collection.reduce( (a,b) => ( a._1 * b._1,
                                            a._2 + b._2 ) )
                                                  //> res  : (Int, Int) = (120,5)
  
println(res)                                      //> (120,5)
println("Factorial of 5 = "+ res._1)              //> Factorial of 5 = 120


// 2-c) Extend the previous code to generate a list from a number (6 turns into list(1,2,3,4,5,6))
// then compute the factorial.

// Ref.: https://alvinalexander.com/scala/how-create-scala-list-range-fill-tabulate-constructors
var k = 6                                         //> k  : Int = 6
val true_collection = List.range(1, k+1)          //> true_collection  : List[Int] = List(1, 2, 3, 4, 5, 6)
val res_2 = (true_collection.map(x => (x,1))).reduce((a,b) => ( a._1 * b._1,
                                            a._2 + b._2 ) )
                                                  //> res_2  : (Int, Int) = (720,6)
println("Factorial of " + k + " = " + res_2._1)   //> Factorial of 6 = 720


// Exercise 3

// Generate a list from 1 to 45 then apply .filter to compute the following results:
// Ref.: https://alvinalexander.com/scala/how-create-scala-list-range-fill-tabulate-constructors


def quart_fct (q: Int, p:Int, o: Int) {
	var z = List.range(1, q+1)

// Sum of the numbers divisible by 4;
// Ref: https://alvinalexander.com/scala/how-to-use-filter-method-scala-collections-cookbook
// Ref: https://stackoverflow.com/questions/11127594/scala-filter-on-two-conditions

	var quart_coll = z.filter(b => (b % p == 0 && b < o))
	println(quart_coll)
	var res_3 = ( quart_coll.map(x => (x,1)) ) . reduce( (a,b) => ( a._1+b._1 ,
                                            									a._2 + b._2) )
	
	println("Sum of number divisble by " + p + " and smaller than " + o + " from a list up to number " + q + " = " + res_3._1)
	}                                         //> quart_fct: (q: Int, p: Int, o: Int)Unit

quart_fct(45,4,10000)                             //> List(4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44)
                                                  //| Sum of number divisble by 4 and smaller than 10000 from a list up to number
                                                  //|  45 = 264

// Sum of the squares of the numbers divisible by 3 and less than 20;
def quart_fct_2 (q: Int, p:Int, o: Int) {
	var z = List.range(1, q+1)
	
	var quart_coll = z.filter(b => (b % p == 0 && b < o))
	println(quart_coll)
	var res_3 = ( quart_coll.map(x => (x,1)) ) . reduce( (a,b) => ( (a._1+b._1)*(a._1+b._1) ,
                                            									a._2 + b._2) ) // overflow due to o being 20, return int, should be double
	
	println("Sum of number divisble by " + p + " and smaller than " + o + " from a list up to number " + q + " = " + res_3._1)
	}                                         //> quart_fct_2: (q: Int, p: Int, o: Int)Unit

quart_fct_2(45,3,20)                              //> List(3, 6, 9, 12, 15, 18)
                                                  //| Sum of number divisble by 3 and smaller than 20 from a list up to number 45
                                                  //|  = -1505051991


}
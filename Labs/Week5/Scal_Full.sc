object Scal_Full {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  
  
  // EXERCISE 1.0
  // 1- Write some code that takes the value of pi, doubles it, and then prints it within a string with
  // three decimal places of precision to the right.
   
  val piSinglePrecision_double : Float = 3.14159265f*2
                                                  //> piSinglePrecision_double  : Float = 6.2831855
  println(f"doubles value of pi, and then prints it within a string with three decimal places of precision to the right. Like $piSinglePrecision_double%.3f")
                                                  //> doubles value of pi, and then prints it within a string with three decimal p
                                                  //| laces of precision to the right. Like 6.283
  
  //2- Write some code inside println(boolean expressions) to return true and false (println(a < 6 && 3 == 3))
  val i = 0; val j = 1                            //> i  : Int = 0
                                                  //| j  : Int = 1
  println(i < j && 3 == 3)                        //> true
  println(i > j && 3 == 3)                        //> false
  
  //3- Explore .getclass and write a code to print int, float, boolean.
  		// Ref: https://alvinalexander.com/scala/how-to-determine-class-of-scala-object-getclass-method
  		
  def printClass(c: Any) { println(c.getClass) }  //> printClass: (c: Any)Unit
  printClass(1)                                   //> class java.lang.Integer
  printClass(1f)                                  //> class java.lang.Float
  printClass(true)                                //> class java.lang.Boolean
  
  //4- Explore .toString and print CEBD1261 in a println command
  // Just write your code below here; any time you save the file it will automatically display the results!
 
  val course : Int = 1261                         //> course  : Int = 1261
  printClass(course)                              //> class java.lang.Integer
  printClass(course.toString)                     //> class java.lang.String
  println("CEBD" + course.toString)               //> CEBD1261
  
  
  // EXERCISE 2.0
	 // 1- Write some code that prints out the first 10 values of the Fibonacci sequence.
	 // f(n+2) = f(n+1)+ f(n)
	 var fib = 0                              //> fib  : Int = 0
	 var fib_1 = 1; var fib_2 = 0             //> fib_1  : Int = 1
                                                  //| fib_2  : Int = 0
	 for (y <- 0 to 9) {
	 		fib_2 = fib_1 + fib
 			println(fib)
 			fib = fib_1
 			fib_1 = fib_2
 		}                                 //> 0
                                                  //| 1
                                                  //| 1
                                                  //| 2
                                                  //| 3
                                                  //| 5
                                                  //| 8
                                                  //| 13
                                                  //| 21
                                                  //| 34
	 
	 // This is the sequence where every number is the sum of the two numbers before it.
	 // So, the result should be 0, 1, 1, 2, 3, 5, 8, 13, 21, 34
	 
	 
	 // 2- Write a function that takes the number and says here is the cube of the input: 5 -> 125 is the cube
	 println({val t = 10; t^3})               //> 9
	 
	 // 3- Use the while loop and print cube of x values up to 10. Use a block in println.
	 var x = 0                                //> x  : Int = 0
	 while (x <= 10) {
	 	x += 1
	 	println(x*x*x)
  		}                                 //> 1
                                                  //| 8
                                                  //| 27
                                                  //| 64
                                                  //| 125
                                                  //| 216
                                                  //| 343
                                                  //| 512
                                                  //| 729
                                                  //| 1000
                                                  //| 1331
  		
  
  // EXERCISE 3.0
  
  // 1- Strings have a built-in .toUpperCase method. For example, "foo".toUpperCase gives you back FOO.
  // Write a function that converts a string to upper-case, and use that function of a few test strings.
 	def Str_Upper(x: String) : String = {
  		x.toUpperCase
  		}                                 //> Str_Upper: (x: String)String
  
  println(Str_Upper("apple"))                     //> APPLE
  println(Str_Upper("Hello World"))               //> HELLO WORLD
  println(Str_Upper("apple "+ 2))                 //> APPLE 2
  
  // Then, do the same thing using a function literal (transformStr) instead of a separate, named function.
 		//test :transformStr("apple",Str_Upper)
	def transformStr(x: String, f: String => String) : String = {
  		f(x)
  		}                                 //> transformStr: (x: String, f: String => String)String
  		
  transformStr("apple", x => x.toUpperCase)       //> res0: String = APPLE
  transformStr("Hello World " + 2, x => x.toUpperCase)
                                                  //> res1: String = HELLO WORLD 2
  
  // 2- Write a function to input a string chosen from {one, two, three, four}.
  
  //Ref.: https://docs.scala-lang.org/tour/pattern-matching.html
 	def Str_value(x: String) : Int = x match {
	 	case "one" => 1
	 	case "two" => 2
	 	case "three" => 3
	 	case "four" => 4
	 	}                                 //> Str_value: (x: String)Int
  
  // Then define a function to square the number and print the combined string as result like, three => the cube is 27.
  def Str_value_sq(x: String) : Int = {
  		 ( Str_value(x) * Str_value(x) )
  		 }                                //> Str_value_sq: (x: String)Int
  
  println("the square is " + Str_value_sq("four"))//> the square is 16
  
  // 3- Write a max function that picks the max of two numbers
  // Ref: http://allaboutscala.com/tutorials/chapter-8-beginner-tutorial-using-scala-collection-functions/scala-max-example/
  // Ref: https://alvinalexander.com/scala/fp-book/how-write-functions-take-function-input-parameters
  def max(x:Double, y:Double) = Seq(x,y).max      //> max: (x: Double, y: Double)Double
	
	
  // and another callmax function to call the first one with inputs
	def callmax(f: (Double, Double) => Double, x: Double, y: Double): Unit = {
    val result_max = f(x, y)
    println(s"Max of two numbers = " + result_max)
		}                                 //> callmax: (f: (Double, Double) => Double, x: Double, y: Double)Unit

	callmax(max,3.5,7.0)                      //> Max of two numbers = 7.0
  
}
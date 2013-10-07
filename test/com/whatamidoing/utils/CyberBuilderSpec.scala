package com.whatamidoing.utils

import org.scalatest._
import org.scalatest.matchers._

class CyberBuilderSpec extends FlatSpec  {
  
  import com.whatamidoing.utils.CypherBuilder
  
   "Given token" should "should return the token if it is valid" in {
	  var f = CypherBuilder.getValidTokenFunction("83e0f9fc-2c72-4d10-8350-13a206984c83")
	  var res = f()
	  println(res)
  }

}
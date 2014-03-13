package models

case class User(val userName : String = "", val password: String = "")
case class UserDetails(val email: Option[String] = Option(""), val firstName: String="", val lastName: String = "")

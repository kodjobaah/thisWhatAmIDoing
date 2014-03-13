package models

case class ForgottenPassword(val email :String = "")
case class ChangePassword(val password: String = "", confirmedPassword: String = "", changePasswordId: String ="")
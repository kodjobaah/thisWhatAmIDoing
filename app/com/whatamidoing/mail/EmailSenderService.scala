package com.whatamidoing.mail


import com.whatamidoing.mail.mailer.Mail
import com.whatamidoing.mail.mailer._

class EmailSenderService {


  import models.UserDetails
  import play.api.Play
  implicit var currentPlay = Play.current
  val mailUser = Play.current.configuration.getString("mail.user").get

  
  def sendInviteEmail(email:String, invId: String, userDetail: UserDetails) = {
    val inviteeEmail = userDetail.email.getOrElse("no-email-supplied")
    val firstname = userDetail.firstName
    val inviteMessage = s"""
    <html>
    <body>
    <div>
      <p>Hi,</p>
      <p>WAID (What Am I Doing ?) is an application that allows you to share live video strems</p>
      <br/>
      <p><bold><u>$firstname</u></bold> using this email:$inviteeEmail  wants to share what they are doing with you</p>
      <p>Click here to <a href="http://www.whatamidoing.info/whatamidoing?invitedId=$invId">View The Live Stream</></p>
    </div>
    </body>
    </html>
    """ 
    
    send a new Mail(
      from = (mailUser, "WAID (What Am I doing!!)"),
      to = email,
      subject = "What Am I Doing",
       message = inviteMessage,
      richMessage = Some("YES"))
  
  }

  def sendLinkToChangePassword(email:String, changePasswordId: String) = {

      val forgottenPassword = s"""
       	  <html>
	    <body>
	      <div>
	        <p>You have requested to change your password at WAID (What Am I Doing)</p>
		<p>Click here <a href="http://www.whatamidoing.info/changePassword?changePasswordId=$changePasswordId">To Change Password</a></p>
	        
	      </div>
            </body>
	  </html>
      """
       send a new Mail(
      from = (mailUser, "WAID (What Am I doing!!)"),
      
      to = email,
      subject = "WAID (What Am I Doing) - Change Password",
      message = forgottenPassword,
      richMessage = Some("YES")
      )


  }
  def sendRegistrationEmail(email:String, password: String) = {

          val inviteMessage = s"""
             <html>
              <body>
               <div>
               <p>Welcome to <strong>WAID (What Am I Doing?)</strong></p>
     	       <p>An account has been created for you. Just download the Android up and start sharing what you are doing: </p>
		<p><a href="https://play.google.com/store/apps/details?id=com.waid">Download WAID</a></p>
              <div>
               <table>
               <row>
                 <td>
      			email = $email 
      			</td>
      		  </row>
      		  <row>
      			<td>
                password = $password
      			</td>
      	      </row>
      	      </table>
	      </div>
	      <p>Click here  <a href="http://www.whatamidoing.info">To get more information</a></p>
             </body>
            </html>
      """
      
       send a new Mail(
      from = (mailUser, "WAID (What Am I doing!!)"),
      to = email,
      subject = "WAID (What Am I Doing) - Invite mail",
      richMessage = Some("YES"),
      message = inviteMessage)
     
  }
}

object EmailSenderService {
  
  def apply() = new EmailSenderService()
  
}
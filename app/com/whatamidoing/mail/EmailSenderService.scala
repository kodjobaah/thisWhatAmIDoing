package com.whatamidoing.mail


import com.whatamidoing.mail.mailer.Mail
import com.whatamidoing.mail.mailer._

class EmailSenderService {

  
  def sendInviteEmail(email:String, invitedId: String) = {
 
    send a new Mail(
      from = ("kodjobaah@gmail.com", "What Am I doing!!"),
      to = email,
      subject = "What Am I Doing",
      message = "Click on the link http://www.whatamidoing.info/whatamidoing?invitedId="+invitedId)

  }

  def sendRegistrationEmail(email:String, password: String) = {

          val inviteMessage = s"""
             <html>
              <body>
               <div>
      			An account has been create for you just download 
                the iphone up and start sharing what you are doing:
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
               <row>
               Click here  <a href="http://www.whatamidoing.info">To get more information</a>
               </row>
      	      </table>
             </body>
            </html>
      """
      
       send a new Mail(
      from = ("kodjobaah@gmail.com", "What Am I doing!!"),
      to = email,
      subject = "What Am I Doing - Invite mail",
      message = inviteMessage)
     
  }
}

object EmailSenderService {
  
  def apply() = new EmailSenderService()
  
}
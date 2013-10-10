package com.whatamidoing.mail


import com.whatamidoing.mail.mailer.Mail
import com.whatamidoing.mail.mailer._

class EmailSenderService {

  
  def sendInviteEmail(email:String, streamName: String) = {
 
    send a new Mail(
      from = ("kodjobaah@gmail.com", "What Am I doing!!"),
      to = email,
      subject = "What Am I Doing",
      message = "Click on the link http://5.79.24.141:9000/whatamidoing?stream="+streamName)

  }

  def sendRegistrationEmail(email:String, password: String) = {

          val inviteMessage = s"""
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
      	      </table>
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
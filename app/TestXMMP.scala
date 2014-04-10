import play.api.Logger
object TestXMPP extends App {

      import org.jivesoftware.smack.Connection
      import org.jivesoftware.smack.ConnectionConfiguration
      import org.jivesoftware.smack.XMPPConnection
      System.setProperty("smack.debugEnabled", "true");
      Connection.DEBUG_ENABLED = true
      Logger.info("---DIDLEY")
      // Create a connection to the jabber.org server.
      val config: ConnectionConfiguration = new ConnectionConfiguration("192.168.1.5",5222,"my")
      config.setSASLAuthenticationEnabled(false)
      val conn: XMPPConnection = new XMPPConnection(config)
      import org.jivesoftware.smack.XMPPException
      try { 
      	  conn.connect()  
         System.out.println("---ABLE TO CONNECT:"+conn.isConnected())
      	  conn.login("admin", "tigase")
	 System.out.println("success:"+conn.isAuthenticated())
        System.out.println("---ABLE TO LOGIN")

      // Create a MultiUserChat using a Connection for a room
      import org.jivesoftware.smackx.muc.MultiUserChat
      import scala.collection.JavaConversions._
      val hostedRooms = MultiUserChat.getHostedRooms(conn, "muc.testme.my")
      
      
      for(hostedRoom <- hostedRooms) {
         System.out.println("hosted room jid["+hostedRoom.getJid()+"] room name["+hostedRoom.getName()+"]")
      }


 
/*
	import org.jivesoftware.smack.packet.Presence
        val  presence = new Presence(Presence.Type.available)
        presence.setPriority(0)
	presence.setTo("room220@muc.my/JustMe")

	import org.jivesoftware.smackx.packet.MUCInitialPresence
	val init = new MUCInitialPresence()
	presence.addExtension(init)
        conn.sendPacket(presence)

           
      // Create a MultiUserChat using a Connection for a room
      import org.jivesoftware.smackx.muc.MultiUserChat
      val muc: MultiUserChat = new MultiUserChat(conn, "room7@muc.testme.my");

      // Create the room
      muc.create("testbot");

      // Send an empty room configuration form which indicates that we want
      // an instant room
      import org.jivesoftware.smackx.Form
      muc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));


      import com.whatamidoing.services.xmpp.AddHocCommands
	val sa = new AddHocCommands()
	sa.addNewVirtualHost(conn,"muc.testme.my")



	import org.jivesoftware.smack.packet.Presence
        val  presence = new Presence(Presence.Type.available)
        presence.setPriority(0)
	presence.setTo("room220@muc.testme.my.xe/JustMe")

	import org.jivesoftware.smackx.packet.MUCInitialPresence
	val init = new MUCInitialPresence()
	presence.addExtension(init)
        conn.sendPacket(presence)
 

      // Create a MultiUserChat using a Connection for a room
      import org.jivesoftware.smackx.muc.MultiUserChat
      val muc: MultiUserChat = new MultiUserChat(conn, "room7@muc.testme.my")



      // Create the room
      muc.create("thisandthat")
      import org.jivesoftware.smackx.Form
      val form: Form = muc.getConfigurationForm()
      System.out.println(form)
        import com.whatamidoing.services.xmpp.AddHocCommands
	val sa = new AddHocCommands()
	sa.addNewVirtualHost(conn,"muc.testme.my")



    // Send an empty room configuration form which indicates that we want
      // an instant room

      import org.jivesoftware.smackx.FormField

     // Get the the room's configuration form
  
	     val rn: FormField("

             val ff: FormField  = new FormField("muc#roomconfig_persistentroom");
             ff.setType(FormField.TYPE_BOOLEAN);
             ff.addValue("0");
             ff.setRequired(true);
             ff.setLabel("Make Room Persistent?");
             System.out.println(ff.toXML()); // - output values seems good.
             f.addField(ff);
                /////////////////////////////////////
             muc.sendConfigurationForm(f);
*/

     } catch {
       case ioe: XMPPException => ioe.printStackTrace()
     }


/*
     import org.jivesoftware.smack.AccountManager


    val  manager:  AccountManager = conn.getAccountManager();
    try {
        manager.createAccount("hadit","help");//username & paswd
    } catch {

      case e :XMPPException => {
      conn.disconnect()
      e.printStackTrace()
      }
    }


           
      // Create a MultiUserChat using a Connection for a room
      import org.jivesoftware.smackx.muc.MultiUserChat
      val muc: MultiUserChat = new MultiUserChat(conn, "kojo@conference.my");

      // Create the room
      muc.create("testbot");

      // Send an empty room configuration form which indicates that we want
      // an instant room
      import org.jivesoftware.smackx.Form
      muc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
*/


}
/*


	import org.jivesoftware.smack.ConnectionListener
            val listner = new ConnectionListener {
            @Override
           def reconnectionSuccessful() {
                System.out.println("Successfully reconnected to the XMPP server.")
            }
            
            @Override
            def reconnectionFailed(arg0: Exception) {
                System.out.println("Failed to reconnect to the XMPP server.")
            }
 
            @Override
            def reconnectingIn(seconds: Int ) {
                System.out.println("Reconnecting in " + seconds + " seconds.")
            }
            
            @Override
            def connectionClosedOnError(arg0: Exception) {
                System.out.println("Connection to XMPP server was lost.")
            }
            
            @Override
            def connectionClosed() {
                System.out.println("XMPP connection was closed.")
            }
        }
 	conn.addConnectionListener(listner)

*/
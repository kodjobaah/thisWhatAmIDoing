import play.api.Logger
import tigase.jaxmpp.core.client.BareJID
import tigase.jaxmpp.core.client.SessionObject
import tigase.jaxmpp.core.client.exceptions.JaxmppException
import tigase.jaxmpp.core.client.observer.Listener
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule
import tigase.jaxmpp.core.client.xmpp.modules.presence.PresenceModule.PresenceEvent
import tigase.jaxmpp.j2se.Jaxmpp
import tigase.jaxmpp.core.client.JID
import tigase.jaxmpp.j2se.connectors.socket.SocketConnector

object Tigase extends App {

        val contact:Jaxmpp = new Jaxmpp()

        contact.getProperties().setUserProperty(SocketConnector.SERVER_HOST,"my.xe");
        contact.getModulesManager().getModule( classOf[PresenceModule] ).addListener( PresenceModule.ContactChangedPresence, new Listener[PresenceModule.PresenceEvent]() {
     	    @throws(classOf[JaxmppException])
            def handleEvent(be:PresenceEvent) {
                 val status = if (be.getStatus() != null) be.getStatus() else "none"
                System.out.println( String.format( "Presence received:\t %1$s is now %2$s (%3$s)", be.getJid(), be.getShow(), status ) )
            }
        } )

        contact.getProperties().setUserProperty( SessionObject.USER_BARE_JID, BareJID.bareJIDInstance( "admin@my" ) );
        contact.getProperties().setUserProperty( SessionObject.PASSWORD, "letmein" );

        System.out.println( "Loging in..." );

        contact.login();

        System.out.println( "Waiting for the presence for 2 minutes" );

        Thread.sleep( 2 * 60 * 1000 );

        contact.sendMessage(JID.jidInstance("user@test.domain"), "Test", "This is a test");

        Thread.sleep( 2 * 60 * 1000 );

        contact.disconnect();
}


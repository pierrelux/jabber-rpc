/**
 * Copyright 2011  Pierre-Luc Bacon <pierrelucbacon@aqra.ca>
 * Based off the work of Ulrich Staudinger on XmppXmlRpcApi
 * svn://activequant.org/opt/repositories/xmppxmlrpcapi
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activequant.xmpprpc;

import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.provider.ProviderManager;

public class JabberRpcConnection implements Runnable {
	private String username;
	private String password;
	private String server;
	private String resource;
	private boolean exitFlag = true;

	private XmlRpcByteArrayProcessor xmlRpcProcessor = new XmlRpcByteArrayProcessor();;
	private XMPPConnection connection;

	private Hashtable<String, BlockingQueue<String>> packetIdToRpcRequests = new Hashtable<String, BlockingQueue<String>>();

	public JabberRpcConnection(String aUsername, String aServer, String aPassword,
			String aResource) throws Exception {
		username = aUsername;
		server = aServer;
		password = aPassword;
		resource = aResource;
	}

	public JabberRpcConnection(XMPPConnection conn) throws Exception {
		connection = conn; 
		installFilter();
	}
	
	/**
	 * Establish a connection to a XMPP server and install RPC IQ handlers.
	 * 
	 * @throws XMPPException
	 */
	public void connectToXmppServer() throws XMPPException {
		// Connect
		connection = new XMPPConnection(server);
		connection.connect();

		// Login
		connection.login(username, password, resource);

		installFilter();
	}

	protected void installFilter()
	{
		// Register an IQ provider for jabber-rpc
		ProviderManager.getInstance().addIQProvider("query", "jabber:iq:rpc",
				new RpcIqProvider());
		
		// Install RPC IQ Filter
		PacketFilter incomingIqSetFilter = new AndFilter(new PacketTypeFilter(
				IQ.class));

		// Wrap a call to the handle RPC packet method below into a
		// PacketListener object
		PacketListener myListenerSet = new PacketListener() {
			public void processPacket(Packet packet) {
				// Do something with the incoming packet here.
				if (packet instanceof RpcIQ) {
					handleRpcPacket((RpcIQ) packet);
				}
			}
		};

		// Register the listener.
		connection.addPacketListener(myListenerSet, incomingIqSetFilter);
	}
	
	/**
	 * Get the RPC response queue for some packet id.
	 * 
	 * @param aUniqueId
	 *            Packet id corresponding to a RPC packet queue.
	 * @return an RPC queue
	 */
	public BlockingQueue<String> getTheRpcResponseQueue(String aUniqueId) {
		if (packetIdToRpcRequests.get(aUniqueId) == null) {
			BlockingQueue<String> myQueue = new SynchronousQueue<String>();
			packetIdToRpcRequests.put(aUniqueId, myQueue);
		}
		return packetIdToRpcRequests.get(aUniqueId);
	}

	/**
	 * Process an RpcIq IQ that can be of type SET, RESULT or ERROR.
	 * 
	 * @param aPacket
	 */
	public void handleRpcPacket(RpcIQ aPacket) {
		try {
			RpcIQ incomingRpcIQ = (RpcIQ) aPacket;

			if (incomingRpcIQ.getType() == Type.SET) {
				String response = xmlRpcProcessor.execute(incomingRpcIQ
						.getPayload());

				// XML declaration that normally appears at the head of an
				// XML-RPC request or response when transported as the payload
				// of an HTTP POST request MUST BE omitted when it is
				// transported via a Jabber <iq/> stanza.
				// http://xmpp.org/extensions/xep-0009.html
				if (response.indexOf("?>") != 0) {
					response = response
							.substring(response.indexOf("?>") + 2);
				}
				
				// Return the result
				incomingRpcIQ.setType(Type.RESULT);
				incomingRpcIQ.setPayload(response);
				
				String from = incomingRpcIQ.getFrom();
				incomingRpcIQ.setFrom(incomingRpcIQ.getTo());
				incomingRpcIQ.setTo(from);
				
				connection.sendPacket(incomingRpcIQ);
				
			} else if (incomingRpcIQ.getType() == Type.RESULT) {
				String packetID = (String) aPacket.getPacketID();

				BlockingQueue<String> requestQueue = getTheRpcResponseQueue(packetID);
				requestQueue.put(incomingRpcIQ.getPayload());
				
			} else if (incomingRpcIQ.getType() == Type.ERROR) {
				String packetID = (String) aPacket.getPacketID();

				// TODO: WRAP IT INTO AN XMLRPC ERROR RESPONSE
				BlockingQueue<String> myOriginRequestQueue = getTheRpcResponseQueue(packetID);
				myOriginRequestQueue.put(incomingRpcIQ.getPayload());
			}
		} catch (XmlRpcException exception) {
			exception.printStackTrace();
		} catch (InterruptedException anEx) {
			anEx.printStackTrace();
		} catch (IOException anEx) {
			anEx.printStackTrace();
		}
	}

	/**
	 * Get a string representation of the JID.
	 * 
	 * @return A string of the form user@server.com/resource
	 */
	public String getJid() {
		System.out.println("Connection " + connection.getUser());
		return connection.getUser();
	}
	
	/**
	 * Send an IQ packet out over the established XMPP connection.
	 * @param packet
	 */
	void sendPacket(Packet packet) 
	{
		connection.sendPacket(packet);	
	}
	
	/**
	 * Set a handler mapping.
	 * @param pMapping
	 */
	void setHandlerMapping(XmlRpcHandlerMapping pMapping) 
	{
		xmlRpcProcessor.setHandlerMapping(pMapping);
	}
	
	@Override
	public void run() {
		// TODO Rewrite this. 
		while (exitFlag) {
			try {
				Thread.sleep(100);
			} catch (Exception anEx) {
				anEx.printStackTrace();
			}
		}
	}
}

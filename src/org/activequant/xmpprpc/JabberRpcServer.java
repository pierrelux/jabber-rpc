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

import java.util.ArrayList;

import org.activequant.xmpprpc.examplehandler.XmlRpcExampleHandler;
import org.apache.xmlrpc.XmlRpcException;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;

public class JabberRpcServer extends JabberRpcConnection {

	private InstanceBasedHandler objectMapper = new InstanceBasedHandler();

	/**
	 * Construct a Jabber-RPC Server to expose a given object.
	 * 
	 * @param username
	 *            Username on the XMPP server.
	 * @param server
	 *            XMPP server
	 * @param password
	 *            Password on the XMPP server
	 * @param resource
	 *            Resource to use on the XMPP server
	 * @throws Exception
	 */
	public JabberRpcServer(String username, String server, String password,
			String resource) throws Exception {
		super(username, server, password, resource);

		setHandlerMapping(objectMapper);
	}
	
	/**
	 * Construct a Jabber-RPC Server to expose a given object.
	 * @param conn An existing XMPPConnection, connected and logged-in
	 * @throws Exception
	 */
	public JabberRpcServer(XMPPConnection conn) throws Exception {
		super(conn);

		setHandlerMapping(objectMapper);
	}

	/**
	 * Expose an existing object over Jabber-RPC
	 * 
	 * @param objectName
	 * @param object
	 * @throws XmlRpcException
	 */
	public void exposeObject(String objectName, Object object)
			throws XmlRpcException {
		objectMapper.addHandler(objectName, object);
	}

	/**
	 * Sample.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		XMPPConnection serverConn = new XMPPConnection("localhost");
		serverConn.connect();
		serverConn.login("rodney", "brooks", "rpc");
		System.out.println("Connected");
		
		JabberRpcServer jabberRpcServer = new JabberRpcServer(serverConn);
		jabberRpcServer.exposeObject("examples", new XmlRpcExampleHandler());
		jabberRpcServer.start();
	}
}

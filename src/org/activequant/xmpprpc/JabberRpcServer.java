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

public class JabberRpcServer extends JabberRpcConnection {

	private InstanceBasedHandler objectMapper;

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

		objectMapper = new InstanceBasedHandler();

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

		JabberRpcServer jabberRpcServer = new JabberRpcServer("rodney",
				"localhost", "brooks", "rpc");
		jabberRpcServer.exposeObject("examples", new XmlRpcExampleHandler());
		jabberRpcServer.connectToXmppServer();

		Thread tserver = new Thread(jabberRpcServer);
		tserver.start();
		Thread.sleep(1000);

		JabberRpcClient xmppRpcClient = new JabberRpcClient("pierre-luc",
				"localhost", "test", "rpc", "rodney@localhost/rpc");
		xmppRpcClient.connectToXmppServer();
		Thread tclient = new Thread(xmppRpcClient);
		tclient.start();
		Thread.sleep(1000);

		Object myRet = xmppRpcClient.execute("examples.getRandomQuote",
				new ArrayList<Object>());
		System.out.println("Got : " + myRet.toString());
	}
}

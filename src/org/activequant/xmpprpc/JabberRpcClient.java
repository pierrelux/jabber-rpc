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
import java.util.List;

import org.activequant.xmpprpc.XmlRpcByteArrayTransport.XmlRpcByteArrayTransportFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class JabberRpcClient extends JabberRpcConnection {

	private XmlRpcClient xmlRpcClient;
	private String rpcServerJid;

	public JabberRpcClient(String username, String server, String password,
			String resource, String serverJid) throws Exception {
		super(username, server, password, resource);
		rpcServerJid = serverJid;

		xmlRpcClient = new XmlRpcClient();
		xmlRpcClient.setTransportFactory(new XmlRpcByteArrayTransportFactory(
				this, xmlRpcClient, rpcServerJid));
	}

	public JabberRpcClient(XMPPConnection conn, String serverJid)
			throws Exception {
		super(conn);
		rpcServerJid = serverJid;

		xmlRpcClient = new XmlRpcClient();
		xmlRpcClient.setTransportFactory(new XmlRpcByteArrayTransportFactory(
				this, xmlRpcClient, rpcServerJid));
	}

	/**
	 * Execute a XML-RPC call on a server.
	 * 
	 * @param remoteMethodName
	 * @param paramList
	 * @return The return value from the remote call.
	 * @throws XmlRpcException
	 */
	public synchronized Object execute(String remoteMethodName,
			List<Object> paramList) throws XmlRpcException {
		return xmlRpcClient.execute(remoteMethodName, paramList);
	}
	
	public static void main(String[] args) throws Exception {
		//Connection.DEBUG_ENABLED = true;

		XMPPConnection clientConn = new XMPPConnection("localhost");
		clientConn.connect();
		clientConn.login("pierre-luc", "test", "rpc");
		System.out.println("Connected");
			
		JabberRpcClient xmppRpcClient = new JabberRpcClient(clientConn, "rodney@localhost/rpc");

		Thread tclient = new Thread(xmppRpcClient);
		tclient.start();
		Thread.sleep(1000);
		
		Object myRet = xmppRpcClient.execute("examples.getRandomQuote",
				new ArrayList<Object>());
		System.out.println("Got : " + myRet.toString());
	}
}

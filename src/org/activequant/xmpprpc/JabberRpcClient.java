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

import java.util.List;

import org.activequant.xmpprpc.XmlRpcByteArrayTransport.XmlRpcByteArrayTransportFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;

public class JabberRpcClient extends JabberRpcConnection {

	private XmlRpcClient xmlRpcClient;
	private String rpcServerJid; 

	public JabberRpcClient(String username, String server, String password, String resource, String serverJid) throws Exception {
		super(username, server, password, resource);
		rpcServerJid = serverJid; 
		
		xmlRpcClient = new XmlRpcClient();
		xmlRpcClient.setTransportFactory(new XmlRpcByteArrayTransportFactory(this, xmlRpcClient, rpcServerJid));
	}
	
	/**
	 * Execute a XML-RPC call on a server.
	 * @param remoteMethodName
	 * @param paramList
	 * @return The return value from the remote call.
	 * @throws XmlRpcException
	 */
	public synchronized 
	Object execute(String remoteMethodName, List<Object> paramList) throws XmlRpcException
	{
		return xmlRpcClient.execute(remoteMethodName, paramList);
	}
}

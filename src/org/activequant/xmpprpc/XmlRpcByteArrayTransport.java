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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientException;
import org.apache.xmlrpc.client.XmlRpcStreamTransport;
import org.apache.xmlrpc.client.XmlRpcTransport;
import org.apache.xmlrpc.client.XmlRpcTransportFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.jivesoftware.smack.packet.IQ.Type;
import org.xml.sax.SAXException;

public class XmlRpcByteArrayTransport extends XmlRpcStreamTransport {
	private ByteArrayInputStream inputStream;
	private ByteArrayOutputStream outputStream;
	private String packetId = null;
	private String serverJid;
	private JabberRpcConnection xmppConnection;

	/**
	 * A class to pre-process XML-RPC for Jabber-RPC.
	 * 
	 * @param client
	 * @param xmlRpcServerJid
	 */
	protected XmlRpcByteArrayTransport(JabberRpcConnection connection,
			XmlRpcClient client, String xmlRpcServerJid) {
		super(client);

		xmppConnection = connection;
		serverJid = xmlRpcServerJid;
		outputStream = new ByteArrayOutputStream();
	}

	@Override
	protected void close() throws XmlRpcClientException {
		// TODO Do something
	}

	@Override
	protected boolean isResponseGzipCompressed(XmlRpcStreamRequestConfig config) {
		return false;
	}

	@Override
	protected InputStream getInputStream() throws XmlRpcException {
		try {
			// have to wait for the response return ... must be a blocking call
			// ...
			String response = xmppConnection.getTheRpcResponseQueue(packetId)
					.poll(300, TimeUnit.SECONDS);
			inputStream = new ByteArrayInputStream(response.getBytes());
			return inputStream;
		} catch (InterruptedException anEx) {
			throw new XmlRpcException(anEx.getMessage());
		}
	}

	@Override
	protected void writeRequest(ReqWriter writer) throws XmlRpcException,
			IOException, SAXException {
		writer.write(outputStream);
		String request = outputStream.toString();

		// XML declaration that normally appears at the head of an
		// XML-RPC request or response when transported as the payload
		// of an HTTP POST request MUST BE omitted when it is
		// transported via a Jabber <iq/> stanza.
		// http://xmpp.org/extensions/xep-0009.html
		if (request.indexOf("?>") != 0) {
			request = request.substring(request.indexOf("?>") + 2);
		}

		// have to wrap the bytes into an IQ packet and deliver it ...
		packetId = UniqueIdGenerator.getUniqueId();
		RpcIQ myIq = new RpcIQ();
		myIq.setTo(serverJid);
		myIq.setPacketID(packetId);
		myIq.setPayload(request);
		myIq.setFrom(xmppConnection.getJid());
		myIq.setType(Type.SET);
		xmppConnection.sendPacket(myIq);
	}

	/**
	 * Factory class for XmlRpcByteArrayClient transport.
	 */
	protected static class XmlRpcByteArrayTransportFactory extends
			XmlRpcTransportFactoryImpl {
		String serverJid;
		JabberRpcConnection xmppConnection;

		public XmlRpcByteArrayTransportFactory(JabberRpcConnection connection,
				XmlRpcClient aClient, String jid) {
			super(aClient);
			serverJid = jid;
			xmppConnection = connection;
		}

		@Override
		public XmlRpcTransport getTransport() {
			return new XmlRpcByteArrayTransport(xmppConnection, getClient(),
					serverJid);
		}
	}
}

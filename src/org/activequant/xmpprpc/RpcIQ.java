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

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.util.StringUtils;

public class RpcIQ extends IQ {

	private String payload = "";

	public String toString() {
		return this.getFrom() + " : " + payload;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String thePayload) {
		this.payload = thePayload;
	}

	@Override
	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<query xmlns=\"jabber:iq:rpc\">");
		// have to insert the rpc code here.
		buf.append(payload);
		buf.append("</query>");
		return buf.toString();

	}
}

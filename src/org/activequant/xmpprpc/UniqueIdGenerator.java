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

public class UniqueIdGenerator {

	public static String getUniqueId() {
		long myMs = System.currentTimeMillis();
		if (myMs == theLastMs)
			theIndex++;
		else
			theIndex = 0;
		String myId = theHost + "." + myMs + "." + theIndex;
		theLastMs = myMs;
		return myId;

	}

	private static int theIndex = 0;
	private static long theLastMs = 0L;
	private static String theHost = "";

}

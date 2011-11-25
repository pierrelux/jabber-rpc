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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.TypeConverterFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcInvocationException;
import org.apache.xmlrpc.metadata.Util;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcNoSuchHandlerException;

public class InstanceBasedHandler extends PropertyHandlerMapping {

	HashMap<String, Object> theInstanceHandlers = new HashMap<String, Object>();

	public void addHandler(String pKey, Object anObject) throws XmlRpcException {	
		
		registerPublicMethods(pKey, anObject.getClass());
	
		theInstanceHandlers.put(pKey, anObject);
	}

	/**
	 * Returns the {@link XmlRpcHandler} with the given name.
	 * 
	 * @param pHandlerName
	 *            The handlers name
	 * @throws XmlRpcNoSuchHandlerException
	 *             A handler with the given name is unknown.
	 */
	public XmlRpcHandler getHandler(String pHandlerName)
			throws XmlRpcNoSuchHandlerException, XmlRpcException {
		if (!theInstanceHandlers.containsKey(pHandlerName)) {
			if (pHandlerName.indexOf(".") != -1)
				pHandlerName = pHandlerName.substring(0, pHandlerName
						.indexOf("."));
			return new InstanceXmlRpcHandler(theInstanceHandlers
					.get(pHandlerName));
		}

		XmlRpcHandler result = (XmlRpcHandler) handlerMap.get(pHandlerName);
		if (result == null) {
			throw new XmlRpcNoSuchHandlerException("No such handler: "
					+ pHandlerName);
		}
		return result;
	}

}

/**
 * local class used for instance handling. 
 * @author ustaudinger
 *
 */
class InstanceXmlRpcHandler implements XmlRpcHandler {

	Object theObject;

	public InstanceXmlRpcHandler(Object anObject) {
		if (anObject == null) {
			System.out.println("anObject is null");
		}
		theObject = anObject;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object execute(XmlRpcRequest pRequest) throws XmlRpcException {

		Object[] args = new Object[pRequest.getParameterCount()];
		Class[] argClasses = new Class[pRequest.getParameterCount()];
		for (int j = 0; j < args.length; j++) {
			args[j] = pRequest.getParameter(j);
			argClasses[j] = pRequest.getParameter(j).getClass();
		}
		try {
			String myMethodName = pRequest.getMethodName();
			// strip everything before the first dot if it's still there.
			if(myMethodName.indexOf(".")!=-1)myMethodName = myMethodName.substring(myMethodName.indexOf(".")+1);
			
			Method myMethod = theObject.getClass().getMethod(
					myMethodName, argClasses);

			Object instance = theObject;
			for (int j = 0; j < args.length; j++) {
				args[j] = new TypeConverterFactoryImpl().getTypeConverter(
						args[j].getClass()).convert(args[j]);
			}
			return invoke(instance, myMethod, args);
		} catch (NoSuchMethodException anEx) {
			throw new XmlRpcException("No method matching arguments: "
					+ Util.getSignature(args));
		}
	}

	private Object invoke(Object pInstance, Method pMethod, Object[] pArgs)
			throws XmlRpcException {
		try {
			return pMethod.invoke(pInstance, pArgs);
		} catch (IllegalAccessException e) {
			throw new XmlRpcException("Illegal access to method "
					+ pMethod.getName() + " in class "
					+ theObject.getClass().getName(), e);
		} catch (IllegalArgumentException e) {
			throw new XmlRpcException("Illegal argument for method "
					+ pMethod.getName() + " in class "
					+ theObject.getClass().getName(), e);
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof XmlRpcException) {
				throw (XmlRpcException) t;
			}
			throw new XmlRpcInvocationException("Failed to invoke method "
					+ pMethod.getName() + " in class "
					+ theObject.getClass().getName() + ": " + t.getMessage(), t);
		}
	}

}

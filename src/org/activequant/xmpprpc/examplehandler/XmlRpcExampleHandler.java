package org.activequant.xmpprpc.examplehandler;

public class XmlRpcExampleHandler implements IXmlRpcExampleHandler {

	public String getStateName(int aStateId)
	{
		System.out.println("get state being called. ");
		return "Switzerland";
	}
	
	public String getRandomQuote() {
		return "To be or not to be.";
	}
	
}

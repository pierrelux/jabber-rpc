package org.activequant.xmpprpc.examplehandler;

public class XmlRpcExampleHandler implements IXmlRpcExampleHandler {

	int var = 1;
	
	public String getStateName(int aStateId)
	{
		System.out.println("get state being called. ");
		return "Switzerland";
	}
	
	public String getRandomQuote() {
		var += 1;
		return "To be or not to be. " + + var;
	}
	
}

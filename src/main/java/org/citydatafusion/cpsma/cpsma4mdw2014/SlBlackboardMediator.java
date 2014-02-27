package org.citydatafusion.cpsma.cpsma4mdw2014;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlBlackboardMediator extends BlackboardMediator {
	
	public static String agent = "sl";

	private Logger logger = LoggerFactory.getLogger(SlBlackboardMediator.class);
	
	SlBlackboardMediator(){}
	
	SlBlackboardMediator(String serverURL, String baseIRI) {
		super(serverURL,baseIRI);
	}
	
}


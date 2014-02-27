package org.citydatafusion.cpsma.cpsma4mdw2014;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VvrBlackboardMediator extends BlackboardMediator {
	
	private Logger logger = LoggerFactory.getLogger(VvrBlackboardMediator.class);

	
	VvrBlackboardMediator(){
		this.agent = "vvr";
	}
	
	VvrBlackboardMediator(String serverURL, String baseIRI) {
		this.agent = "sl";
		this.serverURL=serverURL;
		baseIRI = baseIRI+agent+"/";
	}


}

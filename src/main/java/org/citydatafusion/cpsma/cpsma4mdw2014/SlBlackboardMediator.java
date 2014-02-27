package org.citydatafusion.cpsma.cpsma4mdw2014;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlBlackboardMediator extends BlackboardMediator {
	
	private Logger logger = LoggerFactory.getLogger(SlBlackboardMediator.class);
	
	SlBlackboardMediator(){
		this.agent = "sl";
		this.baseIRI = super.baseIRI+agent+"/";
		this.prefixes = super.prefixes + "@prefix "+agent+": <http://www.streamreasoning.com/demos/mdw14/fuseki/data/"+agent+"/> . ";
	}
	
	SlBlackboardMediator(String serverURL, String baseIRI) {
		this();
		this.baseIRI = baseIRI+agent+"/";
		this.serverURL=serverURL;
	}
	
}


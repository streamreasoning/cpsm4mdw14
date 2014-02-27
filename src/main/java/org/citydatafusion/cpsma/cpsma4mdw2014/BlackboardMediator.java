package org.citydatafusion.cpsma.cpsma4mdw2014;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;

public abstract class BlackboardMediator {
	
	private static RequestBuilder rb;

	private Logger logger = LoggerFactory.getLogger(BlackboardMediator.class);
	
	protected static String agent ;
	protected final String prefixes = "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> . "
			+ "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
			+ "@prefix prov: <http://www.w3.org/ns/prov#> . "
			+ "@prefix cpsma: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/> . "
			+ "@prefix "+agent+": <http://www.streamreasoning.com/demos/mdw14/fuseki/data/"+agent+"/> . ";
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") {
		public StringBuffer format(Date date, StringBuffer toAppendTo,
				java.text.FieldPosition pos) {
			StringBuffer toFix = super.format(date, toAppendTo, pos);
			return toFix.insert(toFix.length() - 2, ':');
		};
	};
	
	protected HttpClient client = HttpClientBuilder.create().build();
	
	protected String serverURL = "http://www.streamreasoning.com/demos/mdw14/fuseki/cp_sma_ds/";
	protected String dataServerURL = serverURL+"data";
	protected String queryServerURL = serverURL+"query";
	protected String baseIRI = "http://www.streamreasoning.com/demos/mdw14/fuseki/data/"+agent+"/";
	
	
	BlackboardMediator() {}
	
	BlackboardMediator(String serverURL, String baseIRI) {
		this.serverURL=serverURL;
		baseIRI = baseIRI+agent+"/";
	}
	
	public String createGraphMetadata(Calendar cal) {
		String content = prefixes;
		content += agent + ":" + cal.getTimeInMillis()
				+ " a prov:Entity ; prov:wasAttributedTo cpsma:" + agent + ";";
		content += "prov:generatedAtTime \"" + sdf.format(cal.getTime())
				+ "\"^^xsd:dateTime . ";

		return content;

	}
	
	public void putNewGraph(Calendar cal, String model) {
		
		
		rb = RequestBuilder.put();
		rb.setUri(dataServerURL);
		rb.addHeader("Content-Type", "text/turtle");
		rb.addParameter("graph", baseIRI+cal.getTimeInMillis());
		try {
			rb.setEntity(new StringEntity(model));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); return;
		}
		HttpUriRequest m = rb.build();

		HttpResponse response;
		try {
			response = client.execute(m);
			logger.debug(response.toString());
		} catch (ClientProtocolException e) {
			// TODO something smarter ?
			e.printStackTrace(); return;
		} catch (IOException e) {
			// TODO something smarter ?
			e.printStackTrace(); return;
		}


		String content = createGraphMetadata(cal);	

		rb = RequestBuilder.post();
		rb.setUri(dataServerURL);
		rb.addHeader("Content-Type", "text/turtle");
		rb.addParameter("graph", "default");
		try {
			rb.setEntity(new StringEntity(content));
		} catch (UnsupportedEncodingException e) {
			// TODO something smarter ?
			e.printStackTrace(); 
			// TODO delete the graph just added
			return;
		}
		m = rb.build();

		try {
			response = client.execute(m);
			logger.debug(response.toString()); 
		} catch (ClientProtocolException e) {
			// TODO something smarter ?
			e.printStackTrace(); 
			// TODO delete the graph just added
			return;
		} catch (IOException e) {
			// TODO something smarter ?
			e.printStackTrace(); 
			// TODO delete the graph just added
			return;
		}
		
	}
	
	protected List<String> getRecentGraphs(Date cal) {

		String query = "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
				+ "prefix prov: <http://www.w3.org/ns/prov#> "
				+ "prefix cpsma: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/> "
				+ "SELECT " + "?g " + "WHERE { " 
				+ "?g a prov:Entity ; "
				+ "   prov:wasAttributedTo cpsma:" + agent + " ; "
				+ "   prov:generatedAtTime ?t . " 
				+ "FILTER (?t > \""	+ sdf.format(cal.getTime()) + "\"^^xsd:dateTime) " 
				+ "}";

		logger.debug(query);
		
		List<String> l = new LinkedList<String>();

		Query q = QueryFactory.create(query, Syntax.syntaxSPARQL_11);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
						queryServerURL,
						q);
		ResultSet r = qexec.execSelect();
		for (; r.hasNext();) {
			QuerySolution soln = r.nextSolution();
			l.add(soln.get("g").toString());
		}

		return l;
	}

	

}

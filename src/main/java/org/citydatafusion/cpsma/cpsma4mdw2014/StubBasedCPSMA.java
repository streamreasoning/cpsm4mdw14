package org.citydatafusion.cpsma.cpsma4mdw2014;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class StubBasedCPSMA {

	private static Logger logger = LoggerFactory.getLogger(StubBasedCPSMA.class);

	
	private static final String prefixes = "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> . "
			+ "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
			+ "@prefix prov: <http://www.w3.org/ns/prov#> . "
			+ "@prefix cpsma: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/> . "
			+ "@prefix sl: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/sl/> . "
			+ "@prefix vm: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/vm/> . ";
	private static final String SL = "sl";
	private static final String VM = "vm";

	private static final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ") {
		public StringBuffer format(Date date, StringBuffer toAppendTo,
				java.text.FieldPosition pos) {
			StringBuffer toFix = super.format(date, toAppendTo, pos);
			return toFix.insert(toFix.length() - 2, ':');
		};
	};

	public static String createGraphMetadata(Calendar cal, String agent) {
		String content = prefixes;
		content += agent + ":" + cal.getTimeInMillis()
				+ " a prov:Entity ; prov:wasAttributedTo cpsma:" + agent + ";";
		content += "prov:generatedAtTime \"" + sdf.format(cal.getTime())
				+ "\"^^xsd:dateTime . ";

		return content;

	}

	public static boolean validateSyntax(String filePath, String lang) {
		Model model = ModelFactory.createDefaultModel();

		InputStream in = FileManager.get().open(filePath);

		try {
			model.read(in, null, lang);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static void putNewGraph(String graph, String filePath)
			throws ClientProtocolException, IOException {
		HttpClient client = HttpClientBuilder.create().build();

		RequestBuilder rb = RequestBuilder.put();
		rb.setUri("http://www.streamreasoning.com/demos/mdw14/fuseki/cp_sma_ds/data");
		rb.addHeader("Content-Type", "text/turtle");
		rb.addParameter("graph", graph);
		rb.setEntity(new FileEntity(new File(filePath)));
		HttpUriRequest m = rb.build();

		HttpResponse response = client.execute(m);

		logger.debug(response.toString());

	}

	public static void addMetadataToDefaultGraph(Calendar cal, String agent)
			throws ClientProtocolException, IOException {
		String content = createGraphMetadata(cal, agent);

		HttpClient client = HttpClientBuilder.create().build();

		RequestBuilder rb = RequestBuilder.post();
		rb.setUri("http://www.streamreasoning.com/demos/mdw14/fuseki/cp_sma_ds/data");
		rb.addHeader("Content-Type", "text/turtle");
		rb.addParameter("graph", "default");
		rb.setEntity(new StringEntity(content));
		HttpUriRequest m = rb.build();

		HttpResponse response = client.execute(m);

		// TODO log!!!
		System.out.println(response);

	}

	public static List<String> getRecentGraphs(Date cal, String agent) {

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
						"http://www.streamreasoning.com/demos/mdw14/fuseki/cp_sma_ds/query",
						q);
		ResultSet r = qexec.execSelect();
		for (; r.hasNext();) {
			QuerySolution soln = r.nextSolution();
			l.add(soln.get("g").toString());
		}

		return l;
	}

	public static Model getGraph(String graph) throws ClientProtocolException,
			IOException {
		HttpClient client = HttpClientBuilder.create().build();

		RequestBuilder rb = RequestBuilder.get();
		rb.setUri("http://www.streamreasoning.com/demos/mdw14/fuseki/cp_sma_ds/data");
		rb.addHeader("Accept", "text/turtle; charset=utf-8");
		rb.addParameter("graph", graph);
		HttpUriRequest m = rb.build();

		HttpResponse response = client.execute(m);
		
		logger.debug(response.toString());
		
		HttpEntity entity = response.getEntity();
		
		String responseStr = EntityUtils.toString(entity);
		Model model = ModelFactory.createDefaultModel();
		return model.read(new StringReader(responseStr), null,"TURTLE");
		
	}

	public static void SLwritesForVVR() {

	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws InterruptedException
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ClientProtocolException,
			IOException, InterruptedException, ParseException {

//		 for (int i = 0; i < 2; i++) {
//		
//		 Calendar cal = Calendar.getInstance();
//		 cal.getTime();
//		
//		 if (validateSyntax("src/main/resources/exampleOfSlOutput.ttl", "TURTLE")) {
//		 putNewGraph(
//		 "http://www.streamreasoning.com/demos/mdw14/fuseki/data/sl/"
//		 + cal.getTimeInMillis(),
//		 "src/main/resources/exampleOfSlOutput.ttl");
//		 }
//		
//		 addMetadataToDefaultGraph(cal, SL);
//				
//		 Thread.currentThread().sleep(1000);
//		
//		 }
//
//		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
//		String dateInString = "27-02-2014 13:00:00";
//		Date date = sdf.parse(dateInString);
//		
//		List<String> l = getRecentGraphs(date,SL);
//		
//		for(String s : l) {
//			Model m = getGraph(s);
//			m.write(System.out);
//		}
		
		SlBlackboardMediator sl = new SlBlackboardMediator();
		
//		for (int i = 0; i < 2; i++) {
//			
//			 Calendar cal = Calendar.getInstance();
//			 cal.getTime();
//			
//			 if (validateSyntax("src/main/resources/exampleOfSlOutput.ttl", "TURTLE")) {
//				 String content = new Scanner(new File("src/main/resources/exampleOfSlOutput.ttl")).useDelimiter("\\Z").next();
//				 sl.putNewGraph(cal,content);
//			 }
//		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		String dateInString = "27-02-2014 13:00:00";
		Date date = sdf.parse(dateInString);
		
		List<String> l = sl.getRecentGraphs(date);
		
		System.out.println(l);
		
		
		
		
	}

}

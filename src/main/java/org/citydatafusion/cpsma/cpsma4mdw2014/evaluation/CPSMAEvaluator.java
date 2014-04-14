package org.citydatafusion.cpsma.cpsma4mdw2014.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;

public class CPSMAEvaluator {

	static protected List<String> getGraphsWrittenByVVR(Date date) {

		String query = "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
				+ "prefix prov: <http://www.w3.org/ns/prov#> "
				+ "prefix cpsma: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/> "
				+ "SELECT " + "?g " + "WHERE { " + "?g a prov:Entity ; "
				+ "   prov:wasAttributedTo cpsma:vvr ; "
				+ "   prov:generatedAtTime ?t . " + "FILTER (?t >= \""
				+ sdf.format(date.getTime()) + "\"^^xsd:dateTime) " + "}";


		List<String> l = new LinkedList<String>();

		Query q = QueryFactory.create(query, Syntax.syntaxSPARQL_11);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				queryServerURL, q);
		ResultSet r = qexec.execSelect();
		for (; r.hasNext();) {
			QuerySolution soln = r.nextSolution();
			l.add(soln.get("g").toString());
		}

		return l;
	}

	static protected List<String> getUsersWithTips(String graph) {

		String query = "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
				+ "prefix prov: <http://www.w3.org/ns/prov#> "
				+ "prefix cpsma: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/> " +
				" prefix sma: <http://www.citydatafusion.org/ontologies/2014/1/sma#> " +
				" prefix smaERR: <http://www.citydatafusion.org/ontology/2014/1/sma#> " +
				"prefix sioc: <http://rdfs.org/sioc/ns#> " +
				"prefix cse: <http://www.citydatafusion.org/ontologies/2014/1/cse#> " +
				"prefix dc: <http://purl.org/dc/terms/> "
				+ "SELECT " 
				+ "DISTINCT ?user " 
				//			"?venue  ?prob ?date " 
				+ "WHERE { " 
				+ "GRAPH <"+graph+"> {" +
				"?user smaERR:shows_interest [ smaERR:about ?venue ; smaERR:has_probability ?prob ] " +
				//			  "FILTER (?prob < 1 " +
				//			  "&& ?prob > 0.001" +
				//			  ") " +
				"}}"  
				;

		//	System.out.println(query);

		List<String> l = new LinkedList<String>();

		Query q = QueryFactory.create(query, Syntax.syntaxSPARQL_11);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				queryServerURL, q);
		ResultSet r = qexec.execSelect();
		for (; r.hasNext();) {
			QuerySolution soln = r.nextSolution();
			l.add(soln.get("user").toString());
		}

		return l;
	}

	static List<String> getTop5Tips(String graph, String user) {

		String query = "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
				+ "prefix prov: <http://www.w3.org/ns/prov#> "
				+ "prefix cpsma: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/> " +
				" prefix sma: <http://www.citydatafusion.org/ontologies/2014/1/sma#> " +
				" prefix smaERR: <http://www.citydatafusion.org/ontology/2014/1/sma#> " +
				"prefix sioc: <http://rdfs.org/sioc/ns#> " +
				"prefix cse: <http://www.citydatafusion.org/ontologies/2014/1/cse#> " +
				"prefix dc: <http://purl.org/dc/terms/> "
				+ "SELECT " 
				+ "?venue " 
				+ "WHERE { " 
				+ "GRAPH <"+graph+"> {" +
				"<"+user+"> smaERR:shows_interest [ smaERR:about ?venue ; smaERR:has_probability ?prob ] " +
				//				  "FILTER ( ?prob > 0.01" +
				//				  ") " +
				"}} ORDER BY DESC(?prob) LIMIT 5 "  
				;

		//		System.out.println(query);

		List<String> l = new LinkedList<String>();

		Query q = QueryFactory.create(query, Syntax.syntaxSPARQL_11);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				queryServerURL, q);
		ResultSet r = qexec.execSelect();
		for (; r.hasNext();) {
			QuerySolution soln = r.nextSolution();
			l.add(soln.get("venue").toString());
		}

		return l;
	}

	private static final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ") {
		public StringBuffer format(Date date, StringBuffer toAppendTo,
				java.text.FieldPosition pos) {
			StringBuffer toFix = super.format(date, toAppendTo, pos);
			return toFix.insert(toFix.length() - 2, ':');
		};
	};



	static String serverURL = "http://www.streamreasoning.com/demos/mdw14/fuseki/blackboard/";
	static String dataServerURL = serverURL + "data";
	static String queryServerURL = serverURL + "query";

	private static void writeInfoOnFile(String info, long i){
		try {
			File f = new File("tips_evaluation");
			if(!f.exists())
				f.mkdirs();
			
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("tips_evaluation/" + i + ".txt", true)));
			out.print(info);
			out.flush();
			out.close();
		}catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub


		long p =  12*60*60*1000; // 12hours in millis
		long offset = p*6; //3 days
		int j = 0;
		//		long i=1395363600000l; // 21.3.2014 experiment
		//		long i=1395910800000l+offset; // 27.3.2014 experiment
		long i=1396908000000l+offset; // 8.04.2014 + 3 days for fuorisalone 2014
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(i);
		writeInfoOnFile("getting graphs\n",i);
		List<String> graphs = getGraphsWrittenByVVR(c.getTime());

		for (String graph : graphs) {
			writeInfoOnFile("getting users for graph "+graph+"\n",i);
			List<String> users = getUsersWithTips(graph);
			//			GregorianCalendar c2 = new GregorianCalendar();
			//			c2.setTimeInMillis(1365631200000l+j*p+offset);
			//			System.out.println("Evaluating tips for "+ sdf.format(c2.getTime()));
			//			
			writeInfoOnFile("user, accuracy\n",i);
			for (String user : users) {

				String venues = "";
				List<String> tips = getTop5Tips(graph, user);
				int totalTips = tips.size();
				for (String tip :tips) {
					venues+="<"+tip+">,";
				}
				if(totalTips>0) {
					writeInfoOnFile(user+",",i);

					// query for fuorisalone 2013 evealutaion. NOTE possibly bugged 

					//		String query = "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
					//				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
					//				+ "prefix prov: <http://www.w3.org/ns/prov#> "
					//				+ "prefix cpsma: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/> " +
					//				" prefix sma: <http://www.citydatafusion.org/ontologies/2014/1/sma#> " +
					//				" prefix smaERR: <http://www.citydatafusion.org/ontology/2014/1/sma#> " +
					//				"prefix sioc: <http://rdfs.org/sioc/ns#> " +
					//				"prefix cse: <http://www.citydatafusion.org/ontologies/2014/1/cse#> " +
					//				"prefix dc: <http://purl.org/dc/terms/> "
					//				+ "SELECT " 
					//				+ "(COUNT(DISTINCT ?tweet)/"+totalTips+" AS ?acc )" 
					//				+ "WHERE { " 
					//				  + "SERVICE <http://www.streamreasoning.com/demos/mdw14/fuseki/cse_info/query> {" +
					//				  "GRAPH <http://www.streamreasoning.com/demos/mdw14/fuseki/cse_info/total_link_MDW2013> {" +
					//				  " ?tweet sioc:topic ?venue ." +
					//				  " FILTER (?venue IN ("+venues.subSequence(0, venues.length()-1)+")) . " +
					//				  "}" +
					//				  "GRAPH <http://www.streamreasoning.com/demos/mdw14/fuseki/cse_info/MDW_2013_tweets> {" +
					//				  " ?tweet " +
					////				  " sioc:content ?content ;" +
					//				  " sioc:has_creator <"+user+"> ;" +
					////				  " dc:created ?date . " +
					////				  "FILTER ( ?date > \"" + sdf.format(c2.getTime()) + "\"^^xsd:dateTime) " +
					//				  "}" +
					//				  "}" +
					//				   
					//				 
					//				 "}" ;

					// query for fuorisalone 2014

					String query = "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
							+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
							+ "prefix prov: <http://www.w3.org/ns/prov#> "
							+ "prefix cpsma: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/> " +
							" prefix sma: <http://www.citydatafusion.org/ontologies/2014/1/sma#> " +
							" prefix smaERR: <http://www.citydatafusion.org/ontology/2014/1/sma#> " +
							"prefix sioc: <http://rdfs.org/sioc/ns#> " +
							"prefix cse: <http://www.citydatafusion.org/ontologies/2014/1/cse#> " +
							"prefix dc: <http://purl.org/dc/terms/> "
							+ "SELECT " 
							+ "(COUNT(DISTINCT ?venue)/"+totalTips+" AS ?acc )" 
							+ "WHERE { " 
							+ "?g a prov:Entity ; "
							+ "   prov:wasAttributedTo cpsma:sl ; "
							+ "   prov:generatedAtTime ?t . " 
							+ "FILTER (?t >= \""
							+ sdf.format(c.getTime()) + "\"^^xsd:dateTime) " 
							+ "GRAPH ?g {" +
							" ?tweet sioc:topic ?venue ;" +
							"        sioc:has_creator <"+user+"> ." +
							" FILTER (?venue IN ("+venues.subSequence(0, venues.length()-1)+")) . " 
							+ "}}";






					//		System.out.println(query);

					Query q = QueryFactory.create(query, Syntax.syntaxSPARQL_11);
					QueryExecution qexec = QueryExecutionFactory.sparqlService(
							queryServerURL, q);
					ResultSet r = qexec.execSelect();


					for (; r.hasNext();) {
						QuerySolution soln = r.nextSolution();
						writeInfoOnFile(soln.get("acc").toString()+"\n",i);
					}

				}}
			j++;

		}
	}

}

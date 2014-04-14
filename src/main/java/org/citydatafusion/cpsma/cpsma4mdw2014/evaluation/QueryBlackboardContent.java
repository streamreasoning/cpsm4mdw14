package org.citydatafusion.cpsma.cpsma4mdw2014.evaluation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.citydatafusion.cpsma.cpsma4mdw2014.Agents;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;

public class QueryBlackboardContent {
	
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
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	
		GregorianCalendar c = new GregorianCalendar();
		// 
		c.setTimeInMillis(1397000000000l);
		
		GregorianCalendar c1 = new GregorianCalendar();
		// 
		c1.setTimeInMillis(1365670800000l);
		
		GregorianCalendar c2 = new GregorianCalendar();
		// 
		c2.setTimeInMillis(1365670800000l+24*60*60*1000);
		
		GregorianCalendar c3 = new GregorianCalendar();
		// 
		c3.setTimeInMillis(1395363600000l);
	
		String tweetVenueLink = "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
				+ "prefix prov: <http://www.w3.org/ns/prov#> "
				+ "prefix cpsma: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/> " +
				" prefix sma: <http://www.citydatafusion.org/ontologies/2014/1/sma#> " +
				"prefix sioc: <http://rdfs.org/sioc/ns#> " +
				"prefix cse: <http://www.citydatafusion.org/ontologies/2014/1/cse#>"
				+ "SELECT " +
				"?g " +
//				"?tweet " +
////				"?id " +
				"?content " +
				"?topic " +
//				"?name " +
////				"?sent " + 
//				 "(COUNT(?tweet) AS ?n) " +
//				 "(COUNT(DISTINCT ?topic) AS ?n)" +
				 "WHERE { " + "?g a prov:Entity ; "
				+ "   prov:wasAttributedTo cpsma:" + Agents.SL + " ; "
				+ "   prov:generatedAtTime ?t . " 
				+ "FILTER (?t >= \""
				+ sdf.format(c.getTime()) + "\"^^xsd:dateTime) " +
				  "GRAPH ?g {" +
				  " ?tweet sioc:content ?content ; sioc:topic ?topic ; sioc:has_creator ?user ; sma:sentiment ?sent. " +
				  "?user sioc:id ?id  . " +
				  "FILTER (regex(str(?topic),'venue','i'))" +
//				  "FILTER (regex(str(?topic),'mdw2014','i'))" +
//				  "FILTER (regex(str(?content),'fuorisalone|mdw|milanodesignweek','i'))" +
				  "}" +
				  "SERVICE <http://www.streamreasoning.com/demos/mdw14/fuseki/silk_ds/query> {" +
				  "GRAPH <http://www.streamreasoning.com/demos/mdw14/fuseki/silk_ds/static_info> {" +
				  " ?topic cse:name ?name" +
				  "}" +
				  "}" +
				 "}" +
				"ORDER BY ASC(?t)" +
				"";
		
		String allTriplesInAgraph = "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
				+ "prefix prov: <http://www.w3.org/ns/prov#> "
				+ "prefix cpsma: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/> " +
				" prefix sma: <http://www.citydatafusion.org/ontologies/2014/1/sma#> " +
				"prefix sioc: <http://rdfs.org/sioc/ns#> " +
				"prefix cse: <http://www.citydatafusion.org/ontologies/2014/1/cse#>"
				+ "SELECT " 
				+ "?g " +
				"?s ?p ?o " 
				+ "WHERE { " + "?g a prov:Entity ; "
				+ "   prov:wasAttributedTo cpsma:" + Agents.VVR + " ; "
				+ "   prov:generatedAtTime ?t . " 
				+ "FILTER (?t >= \""
				+ sdf.format(c.getTime()) + "\"^^xsd:dateTime) " +
				  "GRAPH ?g {" +
				  "?s ?p ?o " +
				  "}" +
				 "}" +
				"ORDER BY DESC(?t)" +
				"LIMIT 10";
		
		String allGraphs = "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
				+ "prefix prov: <http://www.w3.org/ns/prov#> "
				+ "prefix cpsma: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/> " +
				" prefix sma: <http://www.citydatafusion.org/ontologies/2014/1/sma#> " +
				"prefix sioc: <http://rdfs.org/sioc/ns#> " +
				"prefix cse: <http://www.citydatafusion.org/ontologies/2014/1/cse#>"
				+ "SELECT " 
				+ "?g "  
				+ "WHERE { " + "?g a prov:Entity ; "
				+ "   prov:wasAttributedTo cpsma:" + Agents.VVR + " ; "
				+ "   prov:generatedAtTime ?t . " 
				+ "FILTER (?t >= \""
				+ sdf.format(c3.getTime()) + "\"^^xsd:dateTime) " +
				 "}" +
				"ORDER BY DESC(?t)" +
//				"LIMIT 10" +
				"";
		
		String numberOfTweetsPerUser = "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
				+ "prefix prov: <http://www.w3.org/ns/prov#> "
				+ "prefix cpsma: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/> " +
				" prefix sma: <http://www.citydatafusion.org/ontologies/2014/1/sma#> " +
				"prefix sioc: <http://rdfs.org/sioc/ns#> " +
				"prefix cse: <http://www.citydatafusion.org/ontologies/2014/1/cse#>"
				+ "SELECT " +
				"?user " +
				" (count(DISTINCT ?user) AS ?p )" 
				+ "WHERE { " + "?g a prov:Entity ; "
				+ "   prov:wasAttributedTo cpsma:" + Agents.VVR + " ; "
				+ "   prov:generatedAtTime ?t . " 
				+ "FILTER (?t >= \""
				+ sdf.format(c.getTime()) + "\"^^xsd:dateTime) " +
				  "GRAPH ?g {" +
				  " ?tweet sioc:content ?content ; sioc:topic ?topic ; sioc:has_creator ?user ; sma:sentiment ?sent. " +
				  "?user sioc:id ?id  . " +
				  "FILTER (regex(str(?topic),'venue','i'))" +
				  "}" +
				  "SERVICE <http://www.streamreasoning.com/demos/mdw14/fuseki/silk_ds/query> {" +
				  "GRAPH <http://www.streamreasoning.com/demos/mdw14/fuseki/silk_ds/static_info> {" +
				  " ?topic cse:name ?name" +
				  "}" +
				  "}" +
				 "}" +
				"ORDER BY DESC(?p)";
		
		String evaluate = "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
				+ "prefix prov: <http://www.w3.org/ns/prov#> "
				+ "prefix cpsma: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/> " +
				" prefix sma: <http://www.citydatafusion.org/ontologies/2014/1/sma#> " +
				" prefix smaERR: <http://www.citydatafusion.org/ontology/2014/1/sma#> " +
				"prefix sioc: <http://rdfs.org/sioc/ns#> " +
				"prefix cse: <http://www.citydatafusion.org/ontologies/2014/1/cse#> " +
				"prefix dc: <http://purl.org/dc/terms/> "
				+ "SELECT " 
				+ "?user (COUNT(?tweet)/20 AS ?acc )" 
//				"?venue  ?prob ?date " 
				+ "WHERE { " 
				+ "?g a prov:Entity ; "
				+ "   prov:wasAttributedTo cpsma:" + Agents.VVR + " ; "
				+ "   prov:generatedAtTime ?t . " 
				+ "FILTER (?t >= \""
				+ sdf.format(c.getTime()) + "\"^^xsd:dateTime) " +
				  "GRAPH ?g {" +
				  "?user smaERR:shows_interest [ smaERR:about ?venue ; smaERR:has_probability ?prob ] " +
				  "FILTER (?prob < 1 && ?prob > 0.01) " +
				  "}" 
				  + "SERVICE <http://www.streamreasoning.com/demos/mdw14/fuseki/cse_info/query> {" +
				  "GRAPH <http://www.streamreasoning.com/demos/mdw14/fuseki/cse_info/total_link_MDW2013> {" +
				  " ?tweet sioc:topic ?venue . " +
				  "}" +
				  "GRAPH <http://www.streamreasoning.com/demos/mdw14/fuseki/cse_info/MDW_2013_tweets> {" +
				  " ?tweet " +
//				  " sioc:content ?content ;" +
				  " sioc:has_creator ?user ;" +
				  " dc:created ?date . " +
				  "FILTER ( ?date > \"" + sdf.format(c1.getTime()) + "\"^^xsd:dateTime) " +
				  "}" +
				  "}" +
				   
				 
				 "}" +
				 "" +
				"GROUP BY ?user " 
//				+ "LIMIT 1"
				;
		
		String whatToReccommend = "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
				+ "prefix prov: <http://www.w3.org/ns/prov#> "
				+ "prefix cpsma: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/> " +
				" prefix sma: <http://www.citydatafusion.org/ontologies/2014/1/sma#> " +
				"prefix sioc: <http://rdfs.org/sioc/ns#> " +
				"prefix cse: <http://www.citydatafusion.org/ontologies/2014/1/cse#> " +
				"prefix dc: <http://purl.org/dc/terms/> " +
				"prefix event: <http://purl.org/NET/c4dm/event.owl#> " +
				"prefix tl: <http://www.w3.org/2006/time#> " +
				"prefix xsd: <http://www.w3.org/2001/XMLSchema#> " +
				"prefix venue: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/venue/> " 
				+ "SELECT " 
				+ "?venue ?event ?name ?url " 
				+ "WHERE { " 
				  + "SERVICE <http://www.streamreasoning.com/demos/mdw14/fuseki/cse_info/query> {" + 
				" ?venue cse:is_location_of ?event . " +
				"?event event:time [ " +
				"		a tl:Interval; " +
				"		tl:hasBeginning [ tl:inXSDDateTime ?starTime ] ; " +
				"		tl:hasEnd [ tl:inXSDDateTime ?endTime ] " +
				"		]. " +
				"FILTER (?starTime <= \""+ sdf.format(c1.getTime()) + "\"^^xsd:dateTime && " +
				"?endTime > \""+ sdf.format(c2.getTime()) + "\"^^xsd:dateTime ) " +
				" ?event cse:name ?name ." +
				"}" +
				 "}" +
				 "" +
//				"ORDER BY ?user " +
//				"LIMIT 20" +
				"";
		
		String query = tweetVenueLink;

		System.out.println(query);

		Query q = QueryFactory.create(query, Syntax.syntaxSPARQL_11);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				queryServerURL, q);
		ResultSet r = qexec.execSelect();

		ResultSetFormatter.outputAsCSV(System.out,r);
	}

}

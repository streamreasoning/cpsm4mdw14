package org.citydatafusion.cpsma.cpsma4mdw2014;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.vocabulary.RDF;

public class BlackboardMediator {

	private Logger logger = LoggerFactory.getLogger(BlackboardMediator.class);

	private String agent;
	private DatasetAccessor da;

	private String prefixes = "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> . "
			+ "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
			+ "@prefix prov: <http://www.w3.org/ns/prov#> . "
			+ "@prefix cpsma: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/> . ";

	@SuppressWarnings("serial")
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") {
		public StringBuffer format(Date date, StringBuffer toAppendTo,
				java.text.FieldPosition pos) {
			StringBuffer toFix = super.format(date, toAppendTo, pos);
			return toFix.insert(toFix.length() - 2, ':');
		};
	};

	private String serverURL = "http://www.streamreasoning.com/demos/mdw14/fuseki/blackboard/";
	private String dataServerURL = serverURL + "data";
	private String queryServerURL = serverURL + "query";
	private String baseIRI = "http://www.streamreasoning.com/demos/mdw14/fuseki/data/";

	public BlackboardMediator(String agent){
		this.agent = agent;
		this.baseIRI += agent+"/";
		this.prefixes += "@prefix "+agent+": <http://www.streamreasoning.com/demos/mdw14/fuseki/data/"+agent+"/> . ";
	}

	public BlackboardMediator(String agent, String serverURL, String baseIRI) {
		this(agent);
		this.baseIRI = baseIRI+agent+"/";
		this.serverURL=serverURL;
	}

	private String createGraphMetadata(long timestamp) {
		String content = prefixes;
		content += agent + ":" + timestamp
				+ " a prov:Entity ; prov:wasAttributedTo cpsma:" + agent + ";";
		Date d = new Date(timestamp);
		content += "prov:generatedAtTime \"" + sdf.format(d)
				+ "\"^^xsd:dateTime . ";

		return content;

	}
	
	private Model createGraphMetadataDatasetAccessor(long timestamp, String graphName) throws DatatypeConfigurationException {
		Model tempMetadataGraph = ModelFactory.createDefaultModel();

		tempMetadataGraph.add(new ResourceImpl(graphName), RDF.type, new ResourceImpl("http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/Entity"));
		tempMetadataGraph.add(new ResourceImpl(graphName), new PropertyImpl("http://www.w3.org/ns/prov#wasAttributedTo"), new ResourceImpl("http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/" + agent));

		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(timestamp);
		XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);

		tempMetadataGraph.add(new ResourceImpl(graphName), new PropertyImpl("http://www.w3.org/ns/prov#generatedAtTime"), tempMetadataGraph.createTypedLiteral(xmlCalendar.toXMLFormat(), XSDDatatype.XSDdateTime));

		return tempMetadataGraph;
	}

	public void putNewGraph(Date date, String model) throws BlackboardException {

		{
			HttpClient client = HttpClientBuilder.create().build();

			RequestBuilder rb = RequestBuilder.put();
			rb.setUri(dataServerURL);
			rb.addHeader("Content-Type", "text/turtle");
			rb.addParameter("graph", baseIRI + date.getTime());
			try {
				rb.setEntity(new StringEntity(model));
			} catch (UnsupportedEncodingException e) {
				logger.error(e.getMessage(),e);
				throw new BlackboardException("Unable to put new graph",e);
			}
			HttpUriRequest m = rb.build();

			HttpResponse response;
			try {
				response = client.execute(m);
				logger.debug(response.toString());
			} catch (ClientProtocolException e) {
				logger.error(e.getMessage(),e);
				throw new BlackboardException("Unable to put new graph",e);
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
				throw new BlackboardException("Unable to put new graph", e);
			}
		}
		{
			String content = createGraphMetadata(date.getTime());

			HttpClient client = HttpClientBuilder.create().build();

			RequestBuilder rb = RequestBuilder.post();
			rb.setUri(dataServerURL);
			rb.addHeader("Content-Type", "text/turtle");
			rb.addParameter("graph", "default");
			try {
				rb.setEntity(new StringEntity(content));
			} catch (UnsupportedEncodingException e) {
				// TODO delete the graph just added
				logger.error(e.getMessage(),e);
				throw new BlackboardException("Graph put, unable to add its metadata",e);
			}
			HttpUriRequest m = rb.build();

			try {
				HttpResponse response = client.execute(m);
				logger.debug(response.toString());
			} catch (ClientProtocolException e) {
				// TODO delete the graph just added
				logger.error(e.getMessage(),e);
				throw new BlackboardException("Graph put, unable to add its metadata",e);
			} catch (IOException e) {
				// TODO delete the graph just added
				logger.error(e.getMessage(),e);
				throw new BlackboardException("Graph put, unable to add its metadata",e);
			}
		}

	}

	public void putNewGraph(Date date, String userId, String model) throws BlackboardException {

		if(agent.equals(Agents.VM)){

			try {

				HttpClient client = HttpClientBuilder.create().build();

				RequestBuilder rb = RequestBuilder.put();
				rb.setUri(dataServerURL);
				rb.addHeader("Content-Type", "text/turtle");
				rb.addParameter("graph", baseIRI + userId + "_" + date.getTime());
				rb.setEntity(new StringEntity(model));

				HttpUriRequest m = rb.build();

				HttpResponse response;
				response = client.execute(m);
				logger.debug(response.toString());


				String content = createGraphMetadata(date.getTime());

				client = HttpClientBuilder.create().build();

				rb = RequestBuilder.post();
				rb.setUri(dataServerURL);
				rb.addHeader("Content-Type", "text/turtle");
				rb.addParameter("graph", "default");
				rb.setEntity(new StringEntity(content));

				m = rb.build();

				response = client.execute(m);
				logger.debug(response.toString());
			} catch (ClientProtocolException e) {
				logger.error(e.getMessage(),e);
				throw new BlackboardException("Unable to put new graph",e);
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
				throw new BlackboardException("Unable to put new graph", e);
			}
		} else {
			logger.error("This method could be executed only by the Visitor Modeler. Please try to use putNewGraph(Date date, String model). ");
			throw new BlackboardException("This method could be executed only by the Visitor Modeler. Please try to use putNewGraph(Date date, String model). ");
		}

	}
	
	public void putNewGraphDatasetAccessor(Date date, Model model) throws BlackboardException {
		try {
			da = DatasetAccessorFactory.createHTTP(dataServerURL);
			da.add(createGraphMetadataDatasetAccessor(date.getTime(), baseIRI + date.getTime()));
			da.putModel(baseIRI + date.getTime(), model);
		} catch (DatatypeConfigurationException e) {
			logger.error(e.getMessage(),e);
			throw new BlackboardException(e.getMessage(),e);
		}

	}	

	public List<String> getRecentGraphNames(Date date) {

		String query = "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  "
				+ "prefix prov: <http://www.w3.org/ns/prov#> "
				+ "prefix cpsma: <http://www.streamreasoning.com/demos/mdw14/fuseki/data/cpsma/> "
				+ "SELECT " + "?g " + "WHERE { " + "?g a prov:Entity ; "
				+ "   prov:wasAttributedTo cpsma:" + agent + " ; "
				+ "   prov:generatedAtTime ?t . " + "FILTER (?t >= \""
				+ sdf.format(date.getTime()) + "\"^^xsd:dateTime) " + "}";

		logger.debug(query);

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

	public String getGraph(String graphName) throws BlackboardException {
		HttpClient client = HttpClientBuilder.create().build();

		RequestBuilder rb = RequestBuilder.get();
		rb.setUri(dataServerURL);
		rb.addHeader("Accept", "text/turtle; charset=utf-8");
		rb.addParameter("graph", graphName);
		HttpUriRequest m = rb.build();

		HttpResponse response;
		try {
			response = client.execute(m);
			logger.debug(response.toString());
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity);
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage(),e);
			throw new BlackboardException("Unable to get recent graph",e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			throw new BlackboardException("Unable to get recent graph",e);
		}
	}

	public Model getGraphModelDatasetAccessor(String graphName) {
		return da.getModel(graphName);
	}

}

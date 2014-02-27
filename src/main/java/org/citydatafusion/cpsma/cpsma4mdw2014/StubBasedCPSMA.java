package org.citydatafusion.cpsma.cpsma4mdw2014;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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


	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException  {


		
		SlBlackboardMediator sl = new SlBlackboardMediator();
		
		for (int i = 0; i < 2; i++) {
			
			 Calendar cal = Calendar.getInstance();
			 cal.getTime();
			
			 if (validateSyntax("src/main/resources/exampleOfSlOutput.ttl", "TURTLE")) {
				 String content = new Scanner(new File("src/main/resources/exampleOfSlOutput.ttl")).useDelimiter("\\Z").next();
				 sl.putNewGraph(cal,content);
			 }
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		String dateInString = "27-02-2014 13:00:00";
		Date date = sdf.parse(dateInString);
		
		List<String> l = sl.getRecentGraphs(date);
		
		
		for(String s : l) {
		String responseStr = sl.getGraph(s);
		Model m = ModelFactory.createDefaultModel();
		m.read(new StringReader(responseStr), null,"TURTLE");
		m.write(System.out);
	}
		
		
		
		
	}

}

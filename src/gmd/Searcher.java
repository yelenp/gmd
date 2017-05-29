package gmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONArray;
import org.json.JSONObject;

public class Searcher {
	
	public static ArrayList<OrphaDataObject> searchOrphaData(int id) {
		String url = "http://couchdb.telecomnancy.univ-lorraine.fr/orphadatabase/_design/clinicalsigns/_view/GetDiseaseClinicalSignsNoLang?key=" + id;
		return getOrphaData(url);
	}
	
	public static ArrayList<OrphaDataObject> searchOrphaData(String clinicalSign) {
		String url = "";
		try {
			url = "http://couchdb.telecomnancy.univ-lorraine.fr/orphadatabase/_design/clinicalsigns/_view/GetDiseaseByClinicalSign?key=" + URLEncoder.encode(clinicalSign, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return getOrphaData(url);
	}
	
	private static ArrayList<OrphaDataObject> getOrphaData(String url) {
		ArrayList<OrphaDataObject> result = new ArrayList<OrphaDataObject>();
		try {
			URL urlObject = new URL(url);
			HttpURLConnection connection = (HttpURLConnection)urlObject.openConnection();
			connection.setRequestMethod("GET");
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuffer response = new StringBuffer();
			String readLine;
			while((readLine = reader.readLine()) != null) {
				response.append(readLine);
			}
			reader.close();
			JSONObject globalJsonObject = new JSONObject(response.toString());
			JSONArray jsonArray = globalJsonObject.getJSONArray("rows");
			for(int i=0; i<jsonArray.length(); i++) {
				JSONObject data = jsonArray.getJSONObject(i).getJSONObject("value");
				int orphaNumber = data.getJSONObject("disease").getInt("OrphaNumber");
				String disease = data.getJSONObject("disease").getJSONObject("Name").getString("text");
				String clinicalSign = data.getJSONObject("clinicalSign").getJSONObject("Name").getString("text");
				String frequency = data.getJSONObject("data").getJSONObject("signFreq").getJSONObject("Name").getString("text");
				result.add(new OrphaDataObject(orphaNumber, disease, clinicalSign, frequency));
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static ArrayList<String> searchSider(String query, String table, String fieldToSearch, String fieldToGet) {
		String url = "jdbc:mysql://neptune.telecomnancy.univ-lorraine.fr:3306/gmd";
		String user = "gmd-read";
		String password = "esial";
	ArrayList<String> result = new ArrayList<String>();
		try {
			Connection connection = DriverManager.getConnection(url, user, password);
			String preparedQuery = "SELECT " + fieldToGet + " FROM " + table + " WHERE " + fieldToSearch + " = ?";
			PreparedStatement statement = connection.prepareStatement(preparedQuery);
			statement.setString(1, query);
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()) {
				result.add(resultSet.getString(fieldToGet));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static ArrayList<String> searchHPOAnnotations(String query, String fieldToSearch, String fieldToGet) {
		String url = "jdbc:sqlite:" + Configuration.get("hpoAnnotationsData");
		ArrayList<String> result = new ArrayList<String>();
		try {
			Connection connection = DriverManager.getConnection(url);
			String preparedQuery = "SELECT " + fieldToGet + " FROM phenotype_annotation WHERE " + fieldToSearch + " = ?";
			PreparedStatement statement = connection.prepareStatement(preparedQuery);
			statement.setString(1, query);
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()) {
				result.add(resultSet.getString(fieldToGet));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static ArrayList<String> searchHPO(String query, String fieldToSearch, String fieldToGet) {
		ArrayList<String> result = new ArrayList<String>();
		try {
			IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(Configuration.get("hpoIndex"))));
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			Analyzer analyzer = new StandardAnalyzer();
			QueryParser queryParser = new QueryParser(fieldToSearch, analyzer);
			Query queryObject = queryParser.parse(query);
			TopDocs documents = indexSearcher.search(queryObject, Integer.MAX_VALUE);
			ScoreDoc[] scoredDocuments = documents.scoreDocs;
			for(int i = 0; i < scoredDocuments.length; i++) {
				result.add(indexSearcher.doc(scoredDocuments[i].doc).get(fieldToGet));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static ArrayList<String> searchATC(String ATCCode) {
		ArrayList<String> result = new ArrayList<String>();
		try {
			IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(Configuration.get("atcIndex"))));
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			Analyzer analyzer = new StandardAnalyzer();
			QueryParser queryParser = new QueryParser("ATCCode", analyzer);
			Query queryObject = queryParser.parse(ATCCode);
			TopDocs documents = indexSearcher.search(queryObject, Integer.MAX_VALUE);
			ScoreDoc[] scoredDocuments = documents.scoreDocs;
			for(int i = 0; i < scoredDocuments.length; i++) {
				result.add(indexSearcher.doc(scoredDocuments[i].doc).get("label"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static ArrayList<String> searchStitch(String compoundId) {
		ArrayList<String> result = new ArrayList<String>();
		try {
			IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(Configuration.get("stitchIndex"))));
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			Analyzer analyzer = new StandardAnalyzer();
			QueryParser queryParser = new QueryParser("compoundId", analyzer);
			Query queryObject = queryParser.parse(compoundId);
			TopDocs documents = indexSearcher.search(queryObject, Integer.MAX_VALUE);
			ScoreDoc[] scoredDocuments = documents.scoreDocs;
			for(int i = 0; i < scoredDocuments.length; i++) {
				result.add(indexSearcher.doc(scoredDocuments[i].doc).get("ATCCode"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static ArrayList<String> searchOmimOnto(String query, String fieldToSearch, String fieldToGet) {
		ArrayList<String> result = new ArrayList<String>();
		try {
			IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(Configuration.get("omimOntoIndex"))));
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			Analyzer analyzer = new StandardAnalyzer();
			QueryParser queryParser = new QueryParser(fieldToSearch, analyzer);
			Query queryObject = queryParser.parse(query);
			TopDocs documents = indexSearcher.search(queryObject, Integer.MAX_VALUE);
			ScoreDoc[] scoredDocuments = documents.scoreDocs;
			for(int i = 0; i < scoredDocuments.length; i++) {
				result.add(indexSearcher.doc(scoredDocuments[i].doc).get(fieldToGet));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static ArrayList<String> searchOmim(String query, String fieldToGet) {
		ArrayList<String> result = new ArrayList<String>();
		try {
			IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(Configuration.get("omimIndex"))));
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			Analyzer analyzer = new StandardAnalyzer();
			QueryParser queryParser = new QueryParser("content", analyzer);
			Query queryObject = queryParser.parse(query);
			TopDocs documents = indexSearcher.search(queryObject, Integer.MAX_VALUE);
			ScoreDoc[] scoredDocuments = documents.scoreDocs;
			for(int i = 0; i < scoredDocuments.length; i++) {
				result.add(indexSearcher.doc(scoredDocuments[i].doc).get(fieldToGet));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
}
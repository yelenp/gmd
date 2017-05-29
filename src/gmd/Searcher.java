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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
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
		if(clinicalSign.contains("*")) {
			return searchOrphaDataWithRegex(clinicalSign);
		} else {
			clinicalSign = "\"" + clinicalSign + "\"";
			String url = "";
			try {
				url = "http://couchdb.telecomnancy.univ-lorraine.fr/orphadatabase/_design/clinicalsigns/_view/GetDiseaseByClinicalSign?key=" + URLEncoder.encode(clinicalSign, StandardCharsets.UTF_8.toString());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return getOrphaData(url);
		}
	}
	
	private static ArrayList<OrphaDataObject> searchOrphaDataWithRegex(String clinicalSign) {
		String url = "http://couchdb.telecomnancy.univ-lorraine.fr/orphadatabase/_design/clinicalsigns/_view/GetDiseaseByClinicalSign?";
		String[] split = clinicalSign.split("\\*");
		System.out.println(split.length);
		try {
			if(split.length == 1) {
				int starIndex = clinicalSign.indexOf("*");
				if(starIndex == clinicalSign.length() - 1) {
					url += "startKey=" + URLEncoder.encode(split[0], StandardCharsets.UTF_8.toString());
				} else if(starIndex == 0) {
					url += "endKey=" + URLEncoder.encode(split[0], StandardCharsets.UTF_8.toString());
				}
			} else if(split.length == 2) {
				System.out.println("optex");
				url += "startKey=" + URLEncoder.encode(split[0], StandardCharsets.UTF_8.toString()) + "&endKey=" + URLEncoder.encode(split[1], StandardCharsets.UTF_8.toString());
				System.out.println(url);
			}
			//url = URLEncoder.encode(clinicalSign, StandardCharsets.UTF_8.toString());
			System.out.println(url);
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
	
	public static HashSet<String> searchSider(String query, String table, String fieldToSearch, String fieldToGet) {
		if(query.contains("*")) {
			return searchSiderWithRegex(query, table, fieldToSearch, fieldToGet);
		} else {
			String url = "jdbc:mysql://neptune.telecomnancy.univ-lorraine.fr:3306/gmd";
			String user = "gmd-read";
			String password = "esial";
			HashSet<String> result = new HashSet<String>();
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
	}
	
	public static HashSet<ArrayList<String>> searchSider(String query, String table, String fieldToSearch, ArrayList<String> fieldsToGet) {
		if(query.contains("*")) {
			return searchSiderWithRegex(query, table, fieldToSearch, fieldsToGet);
		} else {
			String url = "jdbc:mysql://neptune.telecomnancy.univ-lorraine.fr:3306/gmd";
			String user = "gmd-read";
			String password = "esial";
			HashSet<ArrayList<String>> result = new HashSet<ArrayList<String>>();
			try {
				Connection connection = DriverManager.getConnection(url, user, password);
				String preparedQuery = "SELECT ";
				int l = fieldsToGet.size();
				for(int i=0; i<l-1; i++) {
					preparedQuery += fieldsToGet.get(i) + ", ";
				}
				preparedQuery += fieldsToGet.get(l-1);
				preparedQuery += " FROM " + table + " WHERE " + fieldToSearch + " = ?";
				PreparedStatement statement = connection.prepareStatement(preparedQuery);
				statement.setString(1, query);
				ResultSet resultSet = statement.executeQuery();
				while(resultSet.next()) {
					ArrayList<String> tuple = new ArrayList<String>();
					for(String fieldToGet : fieldsToGet) {
						tuple.add(resultSet.getString(fieldToGet));
					}
					result.add(tuple);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return result;
		}
	}
	
	private static HashSet<String> searchSiderWithRegex(String query, String table, String fieldToSearch, String fieldToGet) {
		String url = "jdbc:mysql://neptune.telecomnancy.univ-lorraine.fr:3306/gmd";
		String user = "gmd-read";
		String password = "esial";
		HashSet<String> result = new HashSet<String>();
		try {
			Connection connection = DriverManager.getConnection(url, user, password);
			query = query.replace("*", "%");
			String sql = "SELECT " + fieldToGet + " FROM " + table + " WHERE " + fieldToSearch + " LIKE '" + query + "'";
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while(resultSet.next()) {
				result.add(resultSet.getString(fieldToGet));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private static HashSet<ArrayList<String>> searchSiderWithRegex(String query, String table, String fieldToSearch, ArrayList<String> fieldsToGet) {
		String url = "jdbc:mysql://neptune.telecomnancy.univ-lorraine.fr:3306/gmd";
		String user = "gmd-read";
		String password = "esial";
		HashSet<ArrayList<String>> result = new HashSet<ArrayList<String>>();
		try {
			Connection connection = DriverManager.getConnection(url, user, password);
			query = query.replace("*", "%");
			String sql = "SELECT ";
			int l = fieldsToGet.size();
			for(int i=0; i<l-1; i++) {
				sql += fieldsToGet.get(i) + ", ";
			}
			sql += fieldsToGet.get(l-1);
			sql += " FROM " + table + " WHERE " + fieldToSearch + " LIKE '" + query + "'";
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while(resultSet.next()) {
				ArrayList<String> tuple = new ArrayList<String>();
				for(String fieldToGet : fieldsToGet) {
					tuple.add(resultSet.getString(fieldToGet));
				}
				result.add(tuple);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	public static HashSet<String> searchSiderAnd(ArrayList<String> queries, String table, ArrayList<String> fieldsToSearch, String fieldToGet) {
		String url = "jdbc:mysql://neptune.telecomnancy.univ-lorraine.fr:3306/gmd";
		String user = "gmd-read";
		String password = "esial";
		HashSet<String> result = new HashSet<String>();
		try {
			Connection connection = DriverManager.getConnection(url, user, password);
			String preparedQuery = "SELECT " + fieldToGet + " FROM " + table + " WHERE ";
			int l = fieldsToSearch.size();
			for(int i=0; i<l-1; i++) {
				preparedQuery += fieldsToSearch.get(i) + " = ? AND ";
			}
			preparedQuery += fieldsToSearch.get(l-1) + " = ?";
			PreparedStatement statement = connection.prepareStatement(preparedQuery);
			assert(queries.size() == fieldsToSearch.size());
			int it = 1;
			for(String query : queries) {
				statement.setString(it, query);
				it++;
			}
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()) {
				result.add(resultSet.getString(fieldToGet));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	*/
	
	public static HashSet<String> searchHPOAnnotations(String query, String fieldToSearch, String fieldToGet) {
		if(query.contains("*")) {
			return searchHPOAnnotationsWithRegex(query, fieldToSearch, fieldToGet);
		} else {
			String url = "jdbc:sqlite:" + Configuration.get("hpoAnnotationsData");
			HashSet<String> result = new HashSet<String>();
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
	}
	
	private static HashSet<String> searchHPOAnnotationsWithRegex(String query, String fieldToSearch, String fieldToGet) {
		String url = "jdbc:sqlite:" + Configuration.get("hpoAnnotationsData");
		HashSet<String> result = new HashSet<String>();
		try {
			Connection connection = DriverManager.getConnection(url);
			query = query.replace("*", "%");
			String sql = "SELECT " + fieldToGet + " FROM phenotype_annotation WHERE " + fieldToSearch + " LIKE '" + query + "'";
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while(resultSet.next()) {
				result.add(resultSet.getString(fieldToGet));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static HashSet<String> searchHPO(String query, String[] fieldsToSearch, String[] fieldsToGet) {
		HashSet<String> result = new HashSet<String>();
		try {
			IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(Configuration.get("hpoIndex"))));
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			Analyzer analyzer = new StandardAnalyzer();
			MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fieldsToSearch, analyzer);
			//QueryParser queryParser = new QueryParser(luceneQuery, analyzer);
			Query queryObject = queryParser.parse(query);
			TopDocs documents = indexSearcher.search(queryObject, Integer.MAX_VALUE);
			ScoreDoc[] scoredDocuments = documents.scoreDocs;
			for(int i = 0; i < scoredDocuments.length; i++) {
				for(String fieldToGet : fieldsToGet) {
					if(indexSearcher.doc(scoredDocuments[i].doc).get(fieldToGet) != null) {
						result.add(indexSearcher.doc(scoredDocuments[i].doc).get(fieldToGet));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static HashSet<String> searchATC(String ATCCode) {
		HashSet<String> result = new HashSet<String>();
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
	
	public static HashSet<String> searchStitch(String query, String[] fieldsToSearch) {
		HashSet<String> result = new HashSet<String>();
		try {
			IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(Configuration.get("stitchIndex"))));
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			Analyzer analyzer = new StandardAnalyzer();
			MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fieldsToSearch, analyzer);
			//QueryParser queryParser = new QueryParser(fieldToSearch, analyzer);
			Query queryObject = queryParser.parse(query);
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
	
	public static HashSet<String> searchOmim(String query, String fieldToSearch, String fieldToGet) {
		HashSet<String> result = new HashSet<String>();
		try {
			IndexReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(Configuration.get("omimIndex"))));
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			Analyzer analyzer = new StandardAnalyzer();
			QueryParser queryParser = new QueryParser(fieldToSearch, analyzer);
			Query queryObject = queryParser.parse(query);
			TopDocs documents = indexSearcher.search(queryObject, Integer.MAX_VALUE);
			ScoreDoc[] scoredDocuments = documents.scoreDocs;
			for(int i = 0; i < scoredDocuments.length; i++) {
				String[] split = indexSearcher.doc(scoredDocuments[i].doc).get(fieldToGet).split(";;");
				for(String splitElement : split) {
					result.add(splitElement);
				}
				//result.add(indexSearcher.doc(scoredDocuments[i].doc).get(fieldToGet));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
}
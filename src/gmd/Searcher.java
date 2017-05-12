package gmd;

import java.io.IOException;
import java.nio.file.Paths;
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

public class Searcher {
	
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
	
	public static ArrayList<String> searchOmim(String query) {
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
				result.add(indexSearcher.doc(scoredDocuments[i].doc).get("content"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}
}
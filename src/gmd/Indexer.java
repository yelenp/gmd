package gmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {
	
	public static void indexData() {
		indexHPO();
		indexATC();
		indexStitch();
		indexOmimOnto();
		indexOmim();
	}
	
	private static void indexHPO() {
		String HPOPath = Configuration.get("hpoData");
		try {
			Directory indexDirectory = FSDirectory.open(Paths.get(Configuration.get("hpoIndex")));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig indexConfiguration = new IndexWriterConfig(analyzer);
			indexConfiguration.setOpenMode(OpenMode.CREATE);
			IndexWriter indexWriter = new IndexWriter(indexDirectory, indexConfiguration);
			InputStream inputStream = Files.newInputStream(Paths.get(HPOPath));
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			String line = "";
			Document document = new Document();
			boolean start = false;
			String id = "";
			while((line = reader.readLine()) != null) {
				if(line.isEmpty()) {
					if(start) {
						document.add(new TextField("id", id, Store.YES));
						indexWriter.addDocument(document);
						document = new Document();
						id = "";
						start = false;
					}
				} else if(line.contains("[Term]")) {
					start = true;
					line = reader.readLine();
				}
				if(start) {
					String[] split = line.split(": ");
					switch(split[0]) {
						case "id":
							id = split[1];
							break;
						case "name":
							document.add(new TextField("name", split[1], Store.YES));
							break;
						case "alt_id":
							document.add(new TextField("altId", split[1], Store.YES));
							break;
						case "def":
							document.add(new TextField("def", split[1], Store.YES));
							break;
						case "comment":
							document.add(new TextField("comment", split[1], Store.YES));
							break;
						case "synonym":
							document.add(new TextField("synonym", split[1], Store.YES));
							break;
						case "xref":
							document.add(new TextField("xref", split[1], Store.YES));
							break;
						case "is_a":
							document.add(new TextField("isA", split[1].split(" ! ")[0], Store.YES));
							break;
						case "replaced_by":
							id = split[1];
							break;
						case "consider":
							id = split[1];
							break;
					}
				}
			}
			reader.close();
			indexWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void indexATC() {
		Pattern pattern = Pattern.compile("([A-Z]+[0-9]{2}[A-Z]{2}[0-9]{2}) ([A-Za-z ]{1,})");
		Matcher matcher = null;
		String ATCPath = Configuration.get("atcData");
		try {
			Directory indexDirectory = FSDirectory.open(Paths.get(Configuration.get("atcIndex")));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig indexConfiguration = new IndexWriterConfig(analyzer);
			indexConfiguration.setOpenMode(OpenMode.CREATE);
			IndexWriter indexWriter = new IndexWriter(indexDirectory, indexConfiguration);
			InputStream inputStream = Files.newInputStream(Paths.get(ATCPath));
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			String line = "";
			Document document = new Document();
			while((line = reader.readLine()) != null) {
				matcher = pattern.matcher(line);
				if(matcher.find()) {
					document.add(new TextField("ATCCode", matcher.group(1), Store.YES));
					document.add(new TextField("label", matcher.group(2), Store.YES));
					indexWriter.addDocument(document);
					document = new Document();
				}
			}
			reader.close();
			indexWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private static void indexStitch() {
		String stitchPath = Configuration.get("stitchData");
		try {
			Directory indexDirectory = FSDirectory.open(Paths.get(Configuration.get("stitchIndex")));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig indexConfiguration = new IndexWriterConfig(analyzer);
			indexConfiguration.setOpenMode(OpenMode.CREATE);
			IndexWriter indexWriter = new IndexWriter(indexDirectory, indexConfiguration);
			InputStream inputStream = Files.newInputStream(Paths.get(stitchPath));
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			String line = "";
			// are there specific lines to ATC data?
			int toSkip = 0;
			// skip some meta data
			while(!(line = reader.readLine()).startsWith("CID")) {
				toSkip++;
			}
			ArrayList<Integer> lineNumbers = new ArrayList<Integer>();
			int lineCounter = toSkip + 1;
			if(line.contains("ATC")) {
				lineNumbers.add(lineCounter);
			}
			lineCounter++;
			int precedentIndex = 0;
			boolean continuous = true;
			while((line = reader.readLine()) != null) {
				if(line.contains("ATC")) {
					lineNumbers.add(lineCounter);
					if(lineCounter != lineNumbers.get(precedentIndex) + 1) {
						continuous = false;
					}
					precedentIndex++;
				}
				lineCounter++;
			}
			reader.close();
			Collections.sort(lineNumbers);
			int start = lineNumbers.get(0);
			int end = lineNumbers.get(lineNumbers.size() - 1);
			inputStream = Files.newInputStream(Paths.get(stitchPath));
			reader = new BufferedReader(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)));
			lineCounter = 0;
			while(lineCounter < start - 1) {
				reader.readLine();
				lineCounter++;
			}
			// are ATC data localized on a continuous range of lines?
			/*
			boolean continuous = true;
			for(int i=0; i<lineNumbers.size() - 1; i++) {
				if(lineNumbers.get(i) + 1 != lineNumbers.get(i + 1)) {
					continuous = false;
					break;
				}
			}
			*/
			Document document = new Document();
			Document anotherDocument = new Document();
			if(continuous) {
				for(int i=start; i<end; i++) {
					line = reader.readLine();
					String[] split = line.split("\t");
					split[0] = split[0].replace("m", "");
					split[1] = split[1].replace("s", "");
					document.add(new TextField("compoundId", split[0], Store.YES));
					document.add(new TextField("ATCCode", split[3], Store.YES));
					indexWriter.addDocument(document);
					document = new Document();
					if(!split[0].equals(split[1])) {
						anotherDocument.add(new TextField("compoundId", split[1], Store.YES));
						anotherDocument.add(new TextField("ATCCode", split[3], Store.YES));
						indexWriter.addDocument(anotherDocument);
						anotherDocument = new Document();
					}
				}
			} else {
				for(int i=start; i<end; i++) {
					line = reader.readLine();
					String[] split = line.split("\t");
					if(split[2].contains("ATC")) {
						split[0] = split[0].replace("m", "");
						split[1] = split[1].replace("s", "");
						document.add(new TextField("compoundId", split[0], Store.YES));
						document.add(new TextField("ATCCode", split[3], Store.YES));
						indexWriter.addDocument(document);
						document = new Document();
						if(!split[0].equals(split[1])) {
							anotherDocument.add(new TextField("compoundId", split[1], Store.YES));
							anotherDocument.add(new TextField("ATCCode", split[3], Store.YES));
							indexWriter.addDocument(anotherDocument);
							anotherDocument = new Document();
						}
					}
				}
			}
			reader.close();
			indexWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void indexOmimOnto() {
		String omimOntoPath = Configuration.get("omimOntoData");
		try {
			Directory indexDirectory = FSDirectory.open(Paths.get(Configuration.get("omimOntoIndex")));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig indexConfiguration = new IndexWriterConfig(analyzer);
			indexConfiguration.setOpenMode(OpenMode.CREATE);
			IndexWriter indexWriter = new IndexWriter(indexDirectory, indexConfiguration);
			InputStream inputStream = Files.newInputStream(Paths.get(omimOntoPath));
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			String line = "";
			Document document = new Document();
			while((line = reader.readLine()) != null) {
				String[] split = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
				//document.add(new TextField("classId", split[0], Store.NO));
				document.add(new TextField("label", split[1], Store.YES));
				document.add(new TextField("synonyms", split[2], Store.YES));
				// split[3] is always empty
				// split[4] is always false
				// split[5] can be in this format: 
				// C2720212|C3151899
				// split[6] can be in this format: 
				// http://purl.bioontology.org/ontology/STY/T047|http://purl.bioontology.org/ontology/STY/T019
				document.add(new TextField("CUIs", split[5], Store.YES));
				//document.add(new TextField("semanticTypes", split[6], Store.NO));
				// split[7] can be in this format: 
				// http://purl.bioontology.org/ontology/OMIM/MTHU000083|http://purl.bioontology.org/ontology/OMIM/MTHU000072
				//document.add(new TextField("parents", split[7], Store.NO));
				indexWriter.addDocument(document);
				document = new Document();
			}
			reader.close();
			indexWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void indexOmim() {
		String omimPath = Configuration.get("omimData");
		try {
			Directory indexDirectory = FSDirectory.open(Paths.get(Configuration.get("omimIndex")));
			Analyzer analyzer = new StandardAnalyzer();
			IndexWriterConfig indexConfiguration = new IndexWriterConfig(analyzer);
			indexConfiguration.setOpenMode(OpenMode.CREATE);
			IndexWriter indexWriter = new IndexWriter(indexDirectory, indexConfiguration);
			InputStream inputStream = Files.newInputStream(Paths.get(omimPath));
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			String line = "";
			boolean index = false;
			String content = "";
			Document document = new Document();
			while((line = reader.readLine()) != null) {
				if(line.contains("*FIELD*")) {
					if(line.contains("CS")) {
						index = true;
						line = reader.readLine();
					} else {
						index = false;
						document.add(new TextField("content", content.trim(), Store.YES));
						indexWriter.addDocument(document);
						content = "";
						document = new Document();
					}
				}
				if(index) {
					content += line;
					content += System.lineSeparator();
				}
			}
			reader.close();
			indexWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
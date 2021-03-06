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
	
	public static void indexHPO() {
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
			String name = "";
			
			ArrayList<String> altIds = new ArrayList<String>();
			ArrayList<String> synonyms = new ArrayList<String>();
			ArrayList<String> xrefs = new ArrayList<String>();
			ArrayList<String> isAs = new ArrayList<String>();
			
			
			//String altId = "";
			String def = "";
			String comment = "";
			//String synonym = "";
			//String xref = "";
			//String isA = "";
			while((line = reader.readLine()) != null) {
				if(line.isEmpty()) {
					if(start) {
						document.add(new TextField("id", id, Store.YES));
						document.add(new TextField("name", name, Store.YES));
						//document.add(new TextField("altId", altId, Store.YES));
						document.add(new TextField("def", def, Store.YES));
						document.add(new TextField("comment", comment, Store.YES));
						//document.add(new TextField("synonym", synonym, Store.YES));
						//document.add(new TextField("xref", xref, Store.YES));
						//document.add(new TextField("isA", isA, Store.YES));
						
						for(String altId : altIds) {
							document.add(new TextField("altId", altId, Store.YES));
						}
						
						
						String synonymString = "";
						for(String synonym : synonyms) {
							synonymString += synonym + ",";
							//document.add(new TextField("synonym", synonym, Store.YES));
						}
						//synonymString = synonymString.trim();
						if(synonyms.size() >= 1) {
							synonymString = synonymString.substring(0, synonymString.length() - 1);
						}
						document.add(new TextField("synonym", synonymString, Store.YES));
						String xrefString = "";
						for(String xref : xrefs) {
							xrefString += xref + ",";
							//document.add(new TextField("xref", xref, Store.YES));
						}
						if(xrefs.size() >= 1) {
							xrefString = xrefString.substring(0, xrefString.length() - 1);
						}
						document.add(new TextField("xref", xrefString, Store.YES));
						
						for(String isA : isAs) {
							document.add(new TextField("isA", isA, Store.YES));
						}
						
						indexWriter.addDocument(document);
						document = new Document();
						id = "";
						name = "";
						//altId = "";
						def = "";
						comment = "";
						//synonym = "";
						//xref = "";
						//isA = "";
						altIds.clear();
						synonyms.clear();
						xrefs.clear();
						isAs.clear();
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
							name += split[1] + System.lineSeparator();
							//document.add(new TextField("name", split[1], Store.YES));
							break;
						case "alt_id":
							altIds.add(split[1]);
							//altId += split[1] + System.lineSeparator();
							//document.add(new TextField("altId", split[1], Store.YES));
							break;
						case "def":
							def += split[1] + System.lineSeparator();
							//document.add(new TextField("def", split[1], Store.YES));
							break;
						case "comment":
							comment += split[1] + System.lineSeparator();
							//document.add(new TextField("comment", split[1], Store.YES));
							break;
						case "synonym":
							synonyms.add(split[1].split("\"")[1]);
							//synonym += split[1] + System.lineSeparator();
							//document.add(new TextField("synonym", split[1], Store.YES));
							break;
						case "xref":
							if(split[1].contains("UMLS")) {
								xrefs.add(split[1].split(":")[1]);
							}
							//xref += split[1] + System.lineSeparator();
							//document.add(new TextField("xref", split[1], Store.YES));
							break;
						case "is_a":
							isAs.add(split[1].split(" ! ")[0]);
							//isA += split[1].split(" ! ")[0] + System.lineSeparator();
							//document.add(new TextField("isA", split[1].split(" ! ")[0], Store.YES));
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
	
	public static void indexATC() {
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
					document.add(new TextField("ATCCode", matcher.group(1).trim(), Store.YES));
					document.add(new TextField("label", matcher.group(2).trim(), Store.YES));
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

	public static void indexStitch() {
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
			//Document anotherDocument = new Document();
			if(continuous) {
				for(int i=start; i<end; i++) {
					line = reader.readLine();
					String[] split = line.split("\t");
					split[0] = split[0].replace("m", "1");
					split[1] = split[1].replace("s", "0");
					document.add(new TextField("compoundId1", split[0], Store.YES));
					document.add(new TextField("compoundId2", split[1], Store.YES));
					document.add(new TextField("ATCCode", split[3], Store.YES));
					indexWriter.addDocument(document);
					document = new Document();
					/*
					if(!split[0].equals(split[1])) {
						anotherDocument.add(new TextField("compoundId", split[1], Store.YES));
						anotherDocument.add(new TextField("ATCCode", split[3], Store.YES));
						indexWriter.addDocument(anotherDocument);
						anotherDocument = new Document();
					}
					*/
				}
			} else {
				for(int i=start; i<end; i++) {
					line = reader.readLine();
					String[] split = line.split("\t");
					if(split[2].contains("ATC")) {
						split[0] = split[0].replace("m", "1");
						split[1] = split[1].replace("s", "0");
						document.add(new TextField("compoundId1", split[0], Store.YES));
						document.add(new TextField("compoundId2", split[1], Store.YES));
						document.add(new TextField("ATCCode", split[3], Store.YES));
						indexWriter.addDocument(document);
						document = new Document();
						/*
						if(!split[0].equals(split[1])) {
							anotherDocument.add(new TextField("compoundId", split[1], Store.YES));
							anotherDocument.add(new TextField("ATCCode", split[3], Store.YES));
							indexWriter.addDocument(anotherDocument);
							anotherDocument = new Document();
						}
						*/
					}
				}
			}
			reader.close();
			indexWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void indexOmimOnto() {
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
	
	public static void indexOmim() {
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
			Document document = new Document();
			while((line = reader.readLine()) != null) {
				if(line.contains("*RECORD*")) {
					while(!(line = reader.readLine()).contains("*FIELD* TI")) {
						reader.readLine();
					}
					String diseases = "";
					while(!(line = reader.readLine()).contains("*FIELD*")) {
						diseases += line;
					}
					//String[] split = diseases.replaceAll("(%|#|\\^|\\*)[0-9]{6} ", "").split(";;");
					//for(String disease : split) {
					//	document.add(new TextField("disease", disease, Store.YES));
					//}
					diseases = diseases.replaceAll("(%|#|\\^|\\*)[0-9]{6} ", "");
					document.add(new TextField("disease", diseases, Store.YES));
					while((line = reader.readLine()) != null && !line.contains("*FIELD* CS")) {
						reader.readLine();
					}
					while((line = reader.readLine()) != null && !line.contains("*FIELD*")) {
						if(!line.contains(":") && !line.trim().isEmpty()) {
							String clinicalSign = line.replaceAll(";", "");
							document.add(new TextField("clinicalSign", clinicalSign, Store.YES));
						}
					}
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
	
	public static void indexOmimOld() {
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
			boolean indexTI = false;
			boolean indexCS = false;
			boolean newIndexTI = false;
			Document document = new Document();
			String content = "";
			
			int it = 0;
			
			while((line = reader.readLine()) != null) {
				if(line.contains("*FIELD*")) {
					if(line.contains("TI")) {
						if(indexTI) {
							newIndexTI = true;
						}
						indexTI = true;
						content = "";
						line = reader.readLine();
						if(line.contains("#111250 BLOOD GROUP SYSTEM, LANDSTEINER-WIENER; LW")) {
						while(!((line = reader.readLine()).contains("*FIELD*"))) {
							
							System.out.println(line);
							content += line + " ";
							it++;
						}
						System.out.println(line);
						//System.out.println("zone");
						//System.out.println(line);
						//System.out.println(!(line = reader.readLine()).contains("*FIELD*"));
						//System.out.println("end zone");
						
						content = content.trim();
						//System.out.println("opex"+content);
						String[] split = content.replaceAll("(%|#|\\^|\\*)[0-9]{6}", "").split(";;");
						for(String disease : split) {
							document.add(new TextField("disease", disease, Store.YES));
						}
						}
					} else if(line.contains("CS")) {
						indexCS = true;
						while(!(line = reader.readLine()).contains("*FIELD*")) {
							if(!line.contains(":") && !line.trim().isEmpty()) {
								String clinicalSign = line.replaceAll(";", "");
								document.add(new TextField("clinicalSign", clinicalSign, Store.YES));
								it++;
							}
						}
					} else {
						if(newIndexTI) {
							indexCS = false;
						}
						if(indexTI && indexCS) {
							indexWriter.addDocument(document);
							document = new Document();
							indexTI = false;
							indexCS = false;
							newIndexTI = false;
						}
					}
				}
				it++;
				//System.err.println(it);
			}
			reader.close();
			indexWriter.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
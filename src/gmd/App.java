package gmd;

import java.util.ArrayList;


public class App{
	
	public static void main(String[] args) {
		System.out.println("Call initialize() for initializing the application.");
		
		//initialize();
		//Indexer.indexOmim();
		
		
		String symptom = "Severe psychomotor retardation";
		String[] Disease;
		//Disease
		ArrayList<String> resultOmim = new ArrayList<String>();
		//ArrayList<OrphaDataObject> resultOrphData = new ArrayList<OrphaDataObject>();
		
		//Synonym symptom
		ArrayList<String> resultHPO = new ArrayList<String>();
		
		// Link between HPO and OMIM & OrphData
		ArrayList<String> resultHPOAnnotations = new ArrayList<String>();
		
		//Link between OMIM and Sider
		ArrayList<String> resultOmimOnto = new ArrayList<String>();
		
		ArrayList<String> resultSider = new ArrayList<String>();
		
		//Link between Sider ans ATC
		ArrayList<String> resultStitch = new ArrayList<String>();
		
		//Drug Label
		ArrayList<String> resultATC = new ArrayList<String>();
		
		resultOmim = Searcher.searchOmim(symptom,"disease");
		resultHPO = Searcher.searchHPO(symptom, "name", "id");
		resultOmimOnto = Searcher.searchOmimOnto(resultOmim.get(4), "label", "CUIs");
		
		//resultHPOAnnotations = Searcher.searchHPOAnnotations(resultHPO.get(0), "sign_id", "disease_label");
		
		
		System.out.println("Omim : " + resultOmim);
		System.out.println("HPO : " + resultHPO);
		System.out.println("Omim into : " + resultOmimOnto);
		//System.out.println("HPO Annotations : " + resultHPOAnnotations);
		
		window window = new window(resultOmim);
	}
	
	public static void initialize() {
		Indexer.indexData();
	}
}
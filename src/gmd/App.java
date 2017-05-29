package gmd;

import java.util.ArrayList;
import javax.swing.JFrame;

public class App{
	
	public static void main(String[] args) {
		System.out.println("Call initialize() for initializing the application.");
		
		//initialize();
		//Indexer.indexOmimOnto();
		String symptom = "Severe psychomotor retardation";
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
		
		resultOmim = Searcher.searchOmim(symptom);
		resultHPO = Searcher.searchHPO(symptom, "name", "id");
		resultOmimOnto = Searcher.searchOmimOnto(symptom, "label", "CUIs");
		
		resultHPOAnnotations = Searcher.searchHPOAnnotations(resultHPO.get(0), "sign_id", "disease_label");
		
		
		System.out.println("Omim : " + resultOmim);
		System.out.println("HPO : " + resultHPO);
		System.out.println("Omim into : " + resultOmimOnto);
		System.out.println("HPO Annotations : " + resultHPOAnnotations);
	}
	
	public static void initialize() {
		Indexer.indexData();
	}
}
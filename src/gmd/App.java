package gmd;

import java.util.ArrayList;
import java.util.HashSet;

public class App {
	
	public static void main(String[] args) {
		
		//Indexer.indexOmim();
		
		//Indexer.indexStitch();
		
		//Indexer.indexATC();
		
		//System.out.println("Call initialize() for initializing the application.");
		
		//ArrayList<OrphaDataObject> list = Searcher.searchOrphaData(59);
		//System.out.println(list.get(1).getDisease());
		
		/*
		ArrayList<String> x = Searcher.searchSider("C0042024", "meddra_all_indications", "cui", "stitch_compound_id");
		
		if(x.isEmpty()) {
			System.out.println("nothing");
		}
		
		for(String xs : x) {
			ArrayList<String> ATCCodes = Searcher.searchStitch(xs);
			for(String atcCode : ATCCodes) {
				System.out.println(Searcher.searchATC(atcCode));
			}
			
		}
		*/
		
		/*
		
		String sideEffect = "Calcium deficiency";
		ArrayList<String> cuis =  Searcher.searchSider(sideEffect, "meddra_all_se", "side_effect_name", "cui");
		
		for(String cui : cuis) {
			ArrayList<String> tmp = Searcher.searchSider(cui, "meddra_all_indications", "cui", "stitch_compound_id");
			for(String t : tmp) {
				
				ArrayList<String> tmp2 = Searcher.searchStitch(t);
				
				for(String t2 : tmp2) {
					System.out.println(Searcher.searchATC(t2));
				}
				
				
			}
		}
		
		*/
		//Indexer.indexHPO();
		
		//HashSet<String> x = Searcher.searchHPO("\"Bladder incontinence\"", new String[]{"name", "synonym"}, new String[]{"id", "altId"});
		//System.out.println(x);
		
		//Indexer.indexHPO();
		
		//Mapper.search("\"Bladder incontinence\"");
		
		//Mapper.searchSideEffectsAndDrugs("Calcium deficiency");
		
		
		//ArrayList<HashSet<String>> Mapper.searchDiseasesAndDrugs("acne");
		
		//ArrayList<HashSet<String>> diseasesAndDrugs = Mapper.searchDiseasesAndDrugs("acne");
		//System.out.println(diseasesAndDrugs.get(0));
		//System.out.println(diseasesAndDrugs.get(1));
		
		//ArrayList<HashSet<String>> causingAndIndicatedDrugs = Mapper.searchCausingAndIndicatedDrugs("Abdominal cramps");
		//System.out.println(causingAndIndicatedDrugs.get(0));
		//System.out.println(causingAndIndicatedDrugs.get(1));
		
		//Mapper.searchRareDiseases("\"*urine*\"");
		
		//Indexer.indexHPO();
		
		//ArrayList<String> clinicalSigns = new ArrayList<String>();
		//clinicalSigns.add("z*");
		//clinicalSigns.add("headache");
		//Mapper.searchSynonyms(clinicalSigns);
		
		//ArrayList<String> clinicalSigns = new ArrayList<String>();
		//clinicalSigns.add("\"Abnormal colour of the urine/cholic/dark urines\"");
		//clinicalSigns.add("\"Xerophthalmia/dry eyes\"");
		//Mapper.searchRareDiseases(clinicalSigns);
		
		//ArrayList<String> clinicalSigns = new ArrayList<String>();
		//clinicalSigns.add("\"Posterior urethral valves\"");
		//clinicalSigns.add("\"Long eyelashes\"");
		//Mapper.searchGeneticDiseases(clinicalSigns);
		
		//ArrayList<String> clinicalSigns = new ArrayList<String>();
		//clinicalSigns.add("Pain");
		//clinicalSigns.add("Rhinitis");
		
		//ArrayList<String> clinicalSigns = new ArrayList<String>();
		//clinicalSigns.add("acne");
		//clinicalSigns.add("headache");
		//Mapper.searchDiseaseIndications(clinicalSigns);
		
		//Mapper.searchSideEffects(clinicalSigns);
		//Mapper.searchSideEffectIndications(clinicalSigns);
		
	}
	
	public static void initialize() {
		Indexer.indexData();
	}
}
package gmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Scheme {

	private static ArrayList<String> clinicalSigns = new ArrayList<String>();
	private static HashMap<String, HashSet<String>> diseases = new HashMap<String, HashSet<String>>();
	private static HashMap<String, HashSet<String>> synonyms = new HashMap<String, HashSet<String>>();
	private static HashMap<String, HashSet<String>> sideEffects = new HashMap<String, HashSet<String>>();
	private static HashMap<String, HashSet<String>> indications = new HashMap<String, HashSet<String>>();
	
	private static HashMap<String, HashSet<String>> diseaseSources = new HashMap<String, HashSet<String>>();
	private static HashMap<String, HashSet<String>> sideEffectSources = new HashMap<String, HashSet<String>>();
	private static HashMap<String, HashSet<String>> indicationSources = new HashMap<String, HashSet<String>>();
	
	private static double diseasesQualityScoreSum = 0.0;
	private static double sideEffectsQualityScoreSum = 0.0;
	private static double indicationsQualityScoreSum = 0.0;
	
	private static int diseasesQualityScoreCounter = 0;
	private static int sideEffectsQualityScoreCounter = 0;
	private static int indicationsQualityScoreCounter = 0;
	
	private static double diseasesQualityScore = 0.0;
	private static double sideEffectsQualityScore = 0.0;
	private static double indicationsQualityScore = 0.0;
	
	private static int diseasesMappingsCount = 0;
	private static int sideEffectsMappingsCount = 0;
	private static int indicationsMappingsCount = 0;
	
	private static int diseasesLostMappingsCount = 0;
	private static int sideEffectsLostMappingsCount = 0;
	private static int indicationsLostMappingsCount = 0;
	
	public static void clear() {
		clinicalSigns.clear();
		diseases.clear();
		synonyms.clear();
		sideEffects.clear();
		indications.clear();
		
		diseasesQualityScoreSum = 0.0;
		sideEffectsQualityScoreSum = 0.0;
		indicationsQualityScoreSum = 0.0;
		
		diseasesQualityScoreCounter = 0;
		sideEffectsQualityScore = 0.0;
		indicationsQualityScore = 0.0;
		
		diseasesQualityScore = 0.0;
		sideEffectsQualityScore = 0.0;
		indicationsQualityScore = 0.0;
		
		diseasesMappingsCount = 0;
		sideEffectsMappingsCount = 0;
		indicationsMappingsCount = 0;
		
		diseasesLostMappingsCount = 0;
		sideEffectsLostMappingsCount = 0;
		indicationsLostMappingsCount = 0;
	}
	
	public static void updateDiseasesMappingsCount(int count) {
		diseasesMappingsCount += count;
	}
	
	public static void updateSideEffectsMappingsCount(int count) {
		sideEffectsMappingsCount += count;
	}
	
	public static void updateIndicationsMappingsCount(int count) {
		indicationsMappingsCount += count;
	}
	
	public static void updateDiseasesLostMappingsCount(int count) {
		diseasesLostMappingsCount += count;
	}
	
	public static void updateSideEffectsLostMappingsCount(int count) {
		sideEffectsLostMappingsCount += count;
	}
	
	public static void updateIndicationsLostMappingsCount(int count) {
		indicationsLostMappingsCount += count;
	}
	
	public static void updateDiseasesQualityScore(double score) {
		diseasesQualityScoreSum += score;
		diseasesQualityScoreCounter++;
		diseasesQualityScore = diseasesQualityScoreSum / (double)diseasesQualityScoreCounter;
	}
	
	public static void updateSideEffectsQualityScore(double score) {
		sideEffectsQualityScoreSum += score;
		sideEffectsQualityScoreCounter++;
		sideEffectsQualityScore = sideEffectsQualityScoreSum / (double)sideEffectsQualityScoreCounter;
	}
	
	public static void updateIndicationsQualityScore(double score) {
		indicationsQualityScoreSum += score;
		indicationsQualityScoreCounter++;
		indicationsQualityScore = indicationsQualityScoreSum / (double)indicationsQualityScoreCounter;
	}
	
	public static void addClinicalSigns(ArrayList<String> pClinicalSigns) {
		clinicalSigns.addAll(pClinicalSigns);
	}
	
	public static void addDiseases(HashMap<String, HashSet<String>> pDiseases) {
		for(String clinicalSign : pDiseases.keySet()) {
			if(diseases.containsKey(clinicalSign)) {
				HashSet<String> oldDiseases = diseases.get(clinicalSign);
				oldDiseases.addAll(pDiseases.get(clinicalSign));
				diseases.put(clinicalSign, oldDiseases);
			} else {
				diseases.put(clinicalSign, pDiseases.get(clinicalSign));
			}
		}
	}
	
	public static void addDiseaseSources(HashSet<String> diseases, String source) {
		for(String disease : diseases) {
			if(diseaseSources.containsKey(disease)) {
				HashSet<String> oldSources = diseaseSources.get(disease);
				oldSources.add(source);
				diseaseSources.put(disease, oldSources);
			} else {
				HashSet<String> tmp = new HashSet<String>();
				tmp.add(source);
				diseaseSources.put(disease, tmp);
			}
		}
	}
	
	public static void addSideEffectSources(HashSet<String> sideEffects, String source) {
		for(String sideEffect : sideEffects) {
			if(sideEffectSources.containsKey(sideEffect)) {
				HashSet<String> oldSources = sideEffectSources.get(sideEffect);
				oldSources.add(source);
				sideEffectSources.put(sideEffect, oldSources);
			} else {
				HashSet<String> tmp = new HashSet<String>();
				tmp.add(source);
				sideEffectSources.put(sideEffect, tmp);
			}
		}
	}
	
	public static void addIndicationSources(HashSet<String> indications, String source) {
		for(String indication : indications) {
			if(indicationSources.containsKey(indication)) {
				HashSet<String> oldSources = indicationSources.get(indication);
				oldSources.add(source);
				indicationSources.put(indication, oldSources);
			} else {
				HashSet<String> tmp = new HashSet<String>();
				tmp.add(source);
				indicationSources.put(indication, tmp);
			}
		}
	}
	
	public static void addSynonyms(HashMap<String, HashSet<String>> pSynonyms) {
		for(String disease : pSynonyms.keySet()) {
			if(synonyms.containsKey(disease)) {
				HashSet<String> oldSynonyms = synonyms.get(disease);
				oldSynonyms.addAll(pSynonyms.get(disease));
				synonyms.put(disease, oldSynonyms);
			} else {
				synonyms.put(disease, pSynonyms.get(disease));
			}
		}
	}
	
	public static void addSideEffects(HashMap<String, HashSet<String>> pSideEffects) {
		for(String clinicalSign : pSideEffects.keySet()) {
			if(sideEffects.containsKey(clinicalSign)) {
				HashSet<String> oldSideEffects = sideEffects.get(clinicalSign);
				oldSideEffects.addAll(pSideEffects.get(clinicalSign));
				sideEffects.put(clinicalSign, oldSideEffects);
			} else {
				sideEffects.put(clinicalSign, pSideEffects.get(clinicalSign));
			}
		}
	}
	
	public static void addIndications(HashMap<String, HashSet<String>> pIndications) {
		for(String sideEffect : pIndications.keySet()) {
			if(indications.containsKey(sideEffect)) {
				HashSet<String> oldIndications = indications.get(sideEffect);
				oldIndications.addAll(pIndications.get(sideEffect));
				indications.put(sideEffect, oldIndications);
			} else {
				indications.put(sideEffect, pIndications.get(sideEffect));
			}
		}
	}

	public static ArrayList<String> getClinicalSigns() {
		return clinicalSigns;
	}

	public static HashMap<String, HashSet<String>> getDiseases() {
		return diseases;
	}

	public static HashMap<String, HashSet<String>> getSynonyms() {
		return synonyms;
	}

	public static HashMap<String, HashSet<String>> getSideEffects() {
		return sideEffects;
	}

	public static HashMap<String, HashSet<String>> getIndications() {
		return indications;
	}
	
	public static double getDiseasesQualityScore() {
		return diseasesQualityScore;
	}
	
	public static double getSideEffectsQualityScore() {
		return sideEffectsQualityScore;
	}
	
	public static double getIndicationsQualityScore() {
		return indicationsQualityScore;
	}
	
	public static int getDiseasesMappingsCount() {
		return diseasesMappingsCount;
	}
	
	public static int getSideEffectsMappingsCount() {
		return sideEffectsMappingsCount;
	}
	
	public static int getIndicationsMappingsCount() {
		return indicationsMappingsCount;
	}
	
	public static int getDiseasesLostMappingsCount() {
		return diseasesLostMappingsCount;
	}
	
	public static int getSideEffectsLostMappingsCount() {
		return sideEffectsLostMappingsCount;
	}
	
	public static int getIndicationsLostMappingsCount() {
		return indicationsLostMappingsCount;
	}

	public static HashMap<String, HashSet<String>> getDiseaseSources() {
		return diseaseSources;
	}

	public static HashMap<String, HashSet<String>> getSideEffectSources() {
		return sideEffectSources;
	}

	public static HashMap<String, HashSet<String>> getIndicationSources() {
		return indicationSources;
	}

	public static void setDiseases(HashMap<String, HashSet<String>> diseases) {
		Scheme.diseases = diseases;
	}

	public static void setSynonyms(HashMap<String, HashSet<String>> synonyms) {
		Scheme.synonyms = synonyms;
	}

	public static void setSideEffects(HashMap<String, HashSet<String>> sideEffects) {
		Scheme.sideEffects = sideEffects;
	}

	public static void setIndications(HashMap<String, HashSet<String>> indications) {
		Scheme.indications = indications;
	}
}
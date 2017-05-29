package gmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Mapper {
	
	private static Scheme scheme;
	
	public static Scheme getScheme() {
		return scheme;
	}
	
	public static void search(String clinicalSign) {
		Scheme.clear();
		ArrayList<String> clinicalSigns = new ArrayList<String>();
		clinicalSign = clinicalSign.toLowerCase();
		if(clinicalSign.contains("and")) {
			String[] split = clinicalSign.split("and");
			for(String splitElement : split) {
				splitElement = splitElement.trim();
				clinicalSigns.add(splitElement);
			}
		} else {
			clinicalSigns.add(clinicalSign);
		}
		searchSynonyms(clinicalSigns);
		searchNormalDiseases(clinicalSigns);
		searchRareDiseases(clinicalSigns);
		searchGeneticDiseases(clinicalSigns);
		searchDiseaseIndications(clinicalSigns);
		searchSideEffects(clinicalSigns);
		searchSideEffectIndications(clinicalSigns);
		
		HashMap<String, HashSet<String>> synonyms = Scheme.getSynonyms();
		HashMap<String, HashSet<String>> diseases = Scheme.getDiseases();
		HashMap<String, HashSet<String>> sideEffects = Scheme.getSideEffects();
		HashMap<String, HashSet<String>> indications = Scheme.getIndications();
		
		HashSet<String> finalSynonyms = new HashSet<String>();
		HashSet<String> finalDiseases = new HashSet<String>();
		HashSet<String> finalSideEffects = new HashSet<String>();
		HashSet<String> finalIndications = new HashSet<String>();
		
		String firstClinicalSign = clinicalSigns.get(0);
		
		finalSynonyms.addAll(synonyms.get(firstClinicalSign));
		finalDiseases.addAll(diseases.get(firstClinicalSign));
		finalSideEffects.addAll(sideEffects.get(firstClinicalSign));
		finalIndications.addAll(indications.get(firstClinicalSign));
		
		for(int i=1; i<clinicalSigns.size(); i++) {
			String currentClinicalSign = clinicalSigns.get(i);
			System.err.println(finalSynonyms);
			if(synonyms.containsKey(currentClinicalSign)) {
				System.err.println(synonyms.get(currentClinicalSign));
				finalSynonyms.retainAll(synonyms.get(currentClinicalSign));
			}
			System.err.println(finalSynonyms);
			if(diseases.containsKey(currentClinicalSign)) {
				finalDiseases.retainAll(diseases.get(currentClinicalSign));
			}
			if(sideEffects.containsKey(currentClinicalSign)) {
				finalSideEffects.retainAll(sideEffects.get(currentClinicalSign));
			}
			if(indications.containsKey(currentClinicalSign)) {
				finalIndications.retainAll(indications.get(currentClinicalSign));
			}
		}
		
		HashMap<String, HashSet<String>> tmp = new HashMap<String, HashSet<String>>();
		tmp.put("", finalSynonyms);
		Scheme.setSynonyms(tmp);
		
		tmp = new HashMap<String, HashSet<String>>();
		tmp.put("", finalDiseases);
		Scheme.setDiseases(tmp);
		
		tmp = new HashMap<String, HashSet<String>>();
		tmp.put("", finalSideEffects);
		Scheme.setSideEffects(tmp);
		
		tmp = new HashMap<String, HashSet<String>>();
		tmp.put("", finalIndications);
		Scheme.setIndications(tmp);
		
		System.out.println(Scheme.getIndications().values());
	}
	
	public static void searchSynonyms(ArrayList<String> clinicalSigns) {
		for(String clinicalSign : clinicalSigns) {
			HashSet<String> synonyms = Searcher.searchHPO(clinicalSign, new String[]{"name"}, new String[]{"synonym"});
			HashSet<String> newSynonyms = new HashSet<String>();
			for(String synonymString : synonyms) {
				String[] split = synonymString.split(",");
				for(String splitElement : split) {
					if(!splitElement.trim().isEmpty()) {
						newSynonyms.add(splitElement);
					}
				}
			}
			HashMap<String, HashSet<String>> temporarySynonyms = new HashMap<String, HashSet<String>>();
			temporarySynonyms.put(clinicalSign, newSynonyms);
			Scheme.addSynonyms(temporarySynonyms);
		}
		System.out.println(Scheme.getSynonyms());
	}
	
	public static void searchNormalDiseases(ArrayList<String> clinicalSigns) {
		int loss = 0;
		int it = 0;
		for(String clinicalSign : clinicalSigns) {
			HashSet<String> normalDiseases = new HashSet<String>();
			HashSet<String> clinicalSignIds = Searcher.searchHPO(clinicalSign, new String[]{"name", "synonym"}, new String[]{"id", "altId"});
			if(clinicalSignIds.isEmpty()) {
				loss++;
			}
			it++;
			for(String clinicalSignId : clinicalSignIds) {
				HashSet<String> diseases = Searcher.searchHPOAnnotations(clinicalSignId, "sign_id", "disease_label");
				if(diseases.isEmpty()) {
					loss++;
				}
				it++;
				for(String disease : diseases) {
					String[] diseaseSplits = disease.split(";");
					for(String diseaseSplit : diseaseSplits) {
						diseaseSplit = diseaseSplit.replaceAll("(#)|(%)|(@)|([0-9]{6})|(\\s[0-9]+)", "");
						if(!diseaseSplit.isEmpty()) {
							normalDiseases.add(diseaseSplit.trim());
						}
					}
				}
			}
			HashMap<String, HashSet<String>> temporaryDiseases = new HashMap<String, HashSet<String>>();
			temporaryDiseases.put(clinicalSign, normalDiseases);
			Scheme.addDiseases(temporaryDiseases);
			Scheme.addDiseaseSources(normalDiseases, "HPO Annotations");
			normalDiseases.clear();
		}
		double score = (double)loss / (double)it;
		Scheme.updateDiseasesQualityScore(score);
		Scheme.updateDiseasesMappingsCount(it);
		Scheme.updateDiseasesLostMappingsCount(loss);
	}
	
	public static void searchRareDiseases(ArrayList<String> clinicalSigns) {
		int loss = 0;
		int it = 0;
		for(String clinicalSign : clinicalSigns) {
			ArrayList<OrphaDataObject> rareDiseases = null;
			rareDiseases = Searcher.searchOrphaData(clinicalSign);
			if(rareDiseases.isEmpty()) {
				loss++;
			}
			it++;
			HashSet<String> diseases = new HashSet<String>();
			for(OrphaDataObject rareDisease : rareDiseases) {
				diseases.add(rareDisease.getDisease());
			}
			HashMap<String, HashSet<String>> temporaryDiseases = new HashMap<String, HashSet<String>>();
			temporaryDiseases.put(clinicalSign, diseases);
			Scheme.addDiseases(temporaryDiseases);
			Scheme.addDiseaseSources(diseases, "OrphaData");
		}
		double score = (double)loss / (double)it;
		Scheme.updateDiseasesQualityScore(score);
		Scheme.updateDiseasesMappingsCount(it);
		Scheme.updateDiseasesLostMappingsCount(loss);
	}
	
	public static void searchGeneticDiseases(ArrayList<String> clinicalSigns) {
		int loss = 0;
		int it = 0;
		for(String clinicalSign : clinicalSigns) {
			HashSet<String> geneticDiseases = Searcher.searchOmim(clinicalSign, "clinicalSign", "disease");
			if(geneticDiseases.isEmpty()) {
				loss++;
			}
			it++;
			HashMap<String, HashSet<String>> temporaryDiseases = new HashMap<String, HashSet<String>>();
			temporaryDiseases.put(clinicalSign, geneticDiseases);
			Scheme.addDiseases(temporaryDiseases);
			Scheme.addDiseaseSources(geneticDiseases, "OMIM");
		}
		double score = (double)loss / (double)it;
		Scheme.updateDiseasesQualityScore(score);
		Scheme.updateDiseasesMappingsCount(it);
		Scheme.updateDiseasesLostMappingsCount(loss);
	}
	
	public static void searchDiseaseIndications(ArrayList<String> clinicalSigns) {
		int loss = 0;
		int it = 0;
		for(String clinicalSign : clinicalSigns) {
			HashSet<String> CUIs = Searcher.searchHPO(clinicalSign, new String[]{"name", "synonym"}, new String[]{"xref"});
			if(CUIs.isEmpty()) {
				loss++;
			}
			it++;
			HashSet<String> newCUIs = new HashSet<String>();
			for(String CUI : CUIs) {
				String[] split = CUI.split(",");
				for(String splitElement : split) {
					if(!splitElement.isEmpty()) {
						newCUIs.add(splitElement);
					}
				}
			}
			
			/*
			HashSet<String> signIds = Searcher.searchHPO(clinicalSign, new String[]{"name", "synonym"}, new String[]{"id", "alt_id"});
			for(String signId : signIds) {
				HashSet<String> diseaseTerms = Searcher.searchHPOAnnotations(signId, "sign_id", "disease_db_and_id");
				
				HashSet<String> tmpCUIs = Searcher.searchHPO(signId, new String[]{"id", "alt_id"}, new String[]{"xref"});
				
				for(String diseaseTerm : diseaseTerms) {
					if(diseaseTerm != null && diseaseTerm.contains("ORPHA")) {
						int diseaseTermId = Integer.parseInt(diseaseTerm.split(":")[1]);
						ArrayList<OrphaDataObject> diseases = Searcher.searchOrphaData(diseaseTermId);
						for(OrphaDataObject disease : diseases) {
							
							
														
							System.out.println(disease.getDisease());
						}
					}
				}
			}
			*/

			for(String CUI : newCUIs) {
				HashSet<String> compoundsIds = Searcher.searchSider(CUI, "meddra_all_indications", "cui", "stitch_compound_id");
				if(compoundsIds.isEmpty()) {
					loss++;
				}
				it++;
				for(String compoundId : compoundsIds) {
					HashSet<String> ATCCodes = Searcher.searchStitch(compoundId, new String[]{"compoundId1"});
					if(ATCCodes.isEmpty()) {
						loss++;
					}
					it++;
					for(String ATCCode : ATCCodes) {
						HashSet<String> drugs = Searcher.searchATC(ATCCode);
						if(drugs.isEmpty()) {
							loss++;
						}
						it++;
						HashMap<String, HashSet<String>> temporaryIndications = new HashMap<String, HashSet<String>>();
						temporaryIndications.put(clinicalSign, drugs);
						Scheme.addIndications(temporaryIndications);
						Scheme.addIndicationSources(drugs, "Sider");
					}
				}
			}
		}
		double score = (double)loss / (double)it;
		Scheme.updateIndicationsQualityScore(score);
		Scheme.updateIndicationsMappingsCount(it);
		Scheme.updateIndicationsLostMappingsCount(loss);
	}
	
	public static void searchSideEffects(ArrayList<String> clinicalSigns) {
		int loss = 0;
		int it = 0;
		ArrayList<String> fieldsToGet = new ArrayList<String>();
		fieldsToGet.add("stitch_compound_id1");
		fieldsToGet.add("stitch_compound_id2");
		for(String clinicalSign : clinicalSigns) {
			HashSet<ArrayList<String>> compoundIds1And2 = Searcher.searchSider(clinicalSign, "meddra_all_se", "side_effect_name", fieldsToGet);		
			if(compoundIds1And2.isEmpty()) {
				loss++;
			}
			it++;
			for(ArrayList<String> compoundIds : compoundIds1And2) {
				HashSet<String> ATCCodes = Searcher.searchStitch("compoundId1:" + compoundIds.get(0) + " AND compoundId2:" + compoundIds.get(1), new String[]{"compoundId1", "compoundId2"});
				if(ATCCodes.isEmpty()) {
					loss++;
				}
				it++;
				for(String ATCCode : ATCCodes) {
					HashSet<String> causingDrugs = Searcher.searchATC(ATCCode);
					if(causingDrugs.isEmpty()) {
						loss++;
					}
					it++;
					HashMap<String, HashSet<String>> temporarySideEffects = new HashMap<String, HashSet<String>>();
					temporarySideEffects.put(clinicalSign, causingDrugs);
					Scheme.addSideEffects(temporarySideEffects);
					Scheme.addSideEffectSources(causingDrugs, "Sider");
				}
			}
		}
		double score = (double)loss / (double)it;
		Scheme.updateSideEffectsQualityScore(score);
		Scheme.updateSideEffectsMappingsCount(it);
		Scheme.updateSideEffectsLostMappingsCount(loss);
	}
	
	public static void searchSideEffectIndications(ArrayList<String> clinicalSigns) {
		int loss = 0;
		int it = 0;
		for(String clinicalSign : clinicalSigns) {
			HashSet<String> sideEffectCUIs = Searcher.searchSider(clinicalSign, "meddra_all_se", "side_effect_name", "cui");
			if(sideEffectCUIs.isEmpty()) {
				loss++;
			}
			it++;
			for(String sideEffectCUI : sideEffectCUIs) {
				HashSet<String> sideEffectIndicationCUIs = Searcher.searchSider(sideEffectCUI, "meddra_all_indications", "cui", "stitch_compound_id");
				if(sideEffectIndicationCUIs.isEmpty()) {
					loss++;
				}
				it++;
				for(String sideEffectIndicationCUI : sideEffectIndicationCUIs) {
					HashSet<String> ATCCodes = Searcher.searchStitch(sideEffectIndicationCUI, new String[]{"compoundId1"});
					if(ATCCodes.isEmpty()) {
						loss++;
					}
					it++;
					for(String ATCCode : ATCCodes) {
						HashSet<String> drugs = Searcher.searchATC(ATCCode);
						if(drugs.isEmpty()) {
							loss++;
						}
						it++;
						HashMap<String, HashSet<String>> temporaryIndications = new HashMap<String, HashSet<String>>();
						temporaryIndications.put(clinicalSign, drugs);
						Scheme.addIndications(temporaryIndications);
						Scheme.addIndicationSources(drugs, "Sider");
					}
				}
			}
		}
		double score = (double)loss / (double)it;
		Scheme.updateIndicationsQualityScore(score);
		Scheme.updateIndicationsMappingsCount(it);
		Scheme.updateIndicationsLostMappingsCount(loss);
	}
	
	public static ArrayList<HashSet<String>> searchCausingAndIndicatedDrugs(String clinicalSign) {
		HashSet<String> globalCausingDrugs = new HashSet<String>();
		HashSet<String> globalIndicatedDrugs = new HashSet<String>();
		ArrayList<String> fieldsToGet = new ArrayList<String>();
		fieldsToGet.add("stitch_compound_id1");
		fieldsToGet.add("stitch_compound_id2");
		HashSet<ArrayList<String>> compoundIds1And2 = Searcher.searchSider(clinicalSign, "meddra_all_se", "side_effect_name", fieldsToGet);		
		for(ArrayList<String> compoundIds : compoundIds1And2) {
			HashSet<String> ATCCodes = Searcher.searchStitch("compoundId1:" + compoundIds.get(0) + " AND compoundId2:" + compoundIds.get(1), new String[]{"compoundId1", "compoundId2"});
			for(String ATCCode : ATCCodes) {
				HashSet<String> causingDrugs = Searcher.searchATC(ATCCode);
				globalCausingDrugs.addAll(causingDrugs);
			}
		}
		HashSet<String> sideEffectCUIs = Searcher.searchSider(clinicalSign, "meddra_all_se", "side_effect_name", "cui");
		for(String sideEffectCUI : sideEffectCUIs) {
			//System.out.println(sideEffectCUI);
			// search side effect drugs
			//HashSet<String> drugsCausingSideEffectsCUIs = Searcher.searchStitch(sideEffectCUI, "");
			//for(String drugCausingSideEffectsCUI : drugsCausingSideEffectsCUIs) {
			//	System.out.println(drugCausingSideEffectsCUI);
			//	HashSet<String> sideEffects = Searcher.searchATC(drugCausingSideEffectsCUI);
			//	globalSideEffects.addAll(sideEffects);
			//}
			//System.out.println(globalSideEffects);
			// search side effect indications
			HashSet<String> sideEffectIndicationCUIs = Searcher.searchSider(sideEffectCUI, "meddra_all_indications", "cui", "stitch_compound_id");
			for(String sideEffectIndicationCUI : sideEffectIndicationCUIs) {
				HashSet<String> ATCCodes = Searcher.searchStitch(sideEffectIndicationCUI, new String[]{"compoundId1"});
				for(String ATCCode : ATCCodes) {
					HashSet<String> drugs = Searcher.searchATC(ATCCode);
					globalIndicatedDrugs.addAll(drugs);
				}
			}
		}
		
		ArrayList<HashSet<String>> result = new ArrayList<HashSet<String>>();
		result.add(globalCausingDrugs);
		result.add(globalIndicatedDrugs);
		return result;
	}

	public static ArrayList<HashSet<String>> searchDiseasesAndDrugs(String clinicalSign) {
		HashSet<String> globalDiseases = new HashSet<String>();
		HashSet<String> globalDrugs = new HashSet<String>();
		HashSet<String> clinicalSignIds = Searcher.searchHPO(clinicalSign, new String[]{"name", "synonym"}, new String[]{"id", "altId"});
		for(String clinicalSignId : clinicalSignIds) {
			HashSet<String> diseases = Searcher.searchHPOAnnotations(clinicalSignId, "sign_id", "disease_label");
			for(String disease : diseases) {
				String[] diseaseSplits = disease.split(";");
				for(String diseaseSplit : diseaseSplits) {
					diseaseSplit = diseaseSplit.replaceAll("(#)|(%)|(@)|([0-9]{6})|(\\s[0-9]+)", "");
					if(!diseaseSplit.isEmpty()) {
						globalDiseases.add(diseaseSplit.trim());
					}
				}
			}
		}
		HashSet<String> CUIs = Searcher.searchHPO(clinicalSign, new String[]{"name", "synonym"}, new String[]{"xref"});
		for(String CUI : CUIs) {
			HashSet<String> compoundsIds = Searcher.searchSider(CUI, "meddra_all_indications", "cui", "stitch_compound_id");
			for(String compoundId : compoundsIds) {
				HashSet<String> ATCCodes = Searcher.searchStitch(compoundId, new String[]{"compoundId1"});
				for(String ATCCode : ATCCodes) {
					HashSet<String> drugs = Searcher.searchATC(ATCCode);
					globalDrugs.addAll(drugs);
				}
			}
		}
		
		ArrayList<HashSet<String>> result = new ArrayList<HashSet<String>>();
		result.add(globalDiseases);
		result.add(globalDrugs);
		return result;
		
		//System.out.println(globalDiseases);
		//System.out.println(globalDrugs);
			
		
		// cause : maladies
			// OMIM_ONTO
				// content
					// disease 
					/**
					 * *FIELD* TI
+182870 SPECTRIN, BETA, ERYTHROCYTIC; SPTB
;;SPECTRIN, BETA-I
SPHEROCYTOSIS, TYPE 2, INCLUDED; SPH2, INCLUDED;;
SPHEROCYTOSIS, HEREDITARY, 2, INCLUDED; HS2, INCLUDED;;
ELLIPTOCYTOSIS 3, INCLUDED; EL3, INCLUDED
					 */
		
		
			// HPO
				// name
				// synonym
					// id (pour HPO Annotations)
					// altId (pour HPO Annotations)
					// xref (pour OMIM?)
			// HPO Annotations
				// sign_id
					// disease_id (pour OrphaData) OU DIRECTEMENT disease_label 
					// disease_label [PARSER, ex : #613206 SPASTIC PARAPLEGIA 44, AUTOSOMAL RECESSIVE; SPG44]
					
		
			// traitement : médicaments
				// name
				// synonym
					// xref: UMLS [Optimiser [Indexer seulement UMLS:*] : xref: SNOMEDCT_US:302866003 ; Parser, ex : UMLS:C0020615]
						// meddra_all_indications
						//res = Searcher.searchSider("C0042024", "meddra_all_indications", "cui", "stitch_compound_id");
						// SearchATC(res);
		
		// cause : médicaments
			// res =  Searcher.searchSider(clinicalSign, "meddra_all_se", "side_effect_name", "cui");
			// SearchATC(res)
		
			// traitement : médicaments
		
		// res2 = Searcher.searchSider(res, "meddra_all_indications", "cui", "stitch_compound_id");
		// SearchATC(res2)
	}
}

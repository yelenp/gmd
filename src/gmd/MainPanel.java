package gmd;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;


public class MainPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	static Mapper mapper;
	
	public MainPanel() {
		super(new BorderLayout());
		
		JPanel corePanel = new JPanel();
		corePanel.setLayout(new GridLayout(8, 1));
		
		Font headerFont = new Font("Arial Bold", 1, 15);
		
		JLabel synonymsLabel = new JLabel("Synonyms (0)", JLabel.CENTER);
		synonymsLabel.setFont(headerFont);
		corePanel.add(synonymsLabel);
		
		JTextArea synonymsField = new JTextArea();
		synonymsField.setEditable(false);
		//corePanel.add(synonymsField);
		JScrollPane synonymsScroll = new JScrollPane(synonymsField);
		corePanel.add(synonymsScroll);
		
		JLabel diseasesLabel = new JLabel("Diseases (0)", JLabel.CENTER);
		diseasesLabel.setFont(headerFont);
		corePanel.add(diseasesLabel);
		
		JTable diseasesTable = new JTable();
		DefaultTableModel diseasesModel = new DefaultTableModel(0, 0);
		String[] diseasesTableHeaders = new String[]{"Disease", "Source(s)", "Score"};
		diseasesModel.setColumnIdentifiers(diseasesTableHeaders);
		diseasesTable.setModel(diseasesModel);
		JScrollPane diseasesTableScroll = new JScrollPane(diseasesTable);
		corePanel.add(diseasesTableScroll);
		
		JLabel sideEffectsLabel = new JLabel("Side effects (0)", JLabel.CENTER);
		sideEffectsLabel.setFont(headerFont);
		corePanel.add(sideEffectsLabel);
		
		JTable sideEffectsTable = new JTable();
		DefaultTableModel sideEffectsModel = new DefaultTableModel(0, 0);
		String[] sideEffectsTableHeaders = new String[]{"Side effect", "Source(s)", "Score"};
		sideEffectsModel.setColumnIdentifiers(sideEffectsTableHeaders);
		sideEffectsTable.setModel(sideEffectsModel);
		JScrollPane sideEffectsTableScroll = new JScrollPane(sideEffectsTable);
		corePanel.add(sideEffectsTableScroll);
		
		JLabel indicationsLabel = new JLabel("Indications (0)", JLabel.CENTER);
		indicationsLabel.setFont(headerFont);
		corePanel.add(indicationsLabel);
		
		JTable indicationsTable = new JTable();
		DefaultTableModel indicationsModel = new DefaultTableModel(0, 0);
		String[] indicationsTableHeaders = new String[]{"Indication", "Source(s)", "Score"};
		indicationsModel.setColumnIdentifiers(indicationsTableHeaders);
		indicationsTable.setModel(indicationsModel);
		JScrollPane indicationsScroll = new JScrollPane(indicationsTable);
		corePanel.add(indicationsScroll);
		
		JPanel searchPanel = new JPanel();
		//searchPanel.setLayout(new GridLayout(2, 1));
		searchPanel.setLayout(new BorderLayout());
		JTextField searchField = new JTextField();
		searchPanel.add(searchField, BorderLayout.CENTER);
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				synonymsField.setText("");
				while(diseasesModel.getRowCount() > 0) {
					diseasesModel.removeRow(0);
				}
				while(sideEffectsModel.getRowCount() > 0) {
					sideEffectsModel.removeRow(0);
				}
				while(indicationsModel.getRowCount() > 0) {
					indicationsModel.removeRow(0);
				}
				mapper.search(searchField.getText());
				Collection<HashSet<String>> synonyms = mapper.getScheme().getSynonyms().values();
				HashSet<String> finalSynonyms = new HashSet<String>();
				Iterator<HashSet<String>> synonymsIterator = synonyms.iterator();
				while(synonymsIterator.hasNext()) {
					finalSynonyms.addAll(synonymsIterator.next());
				}
				String synonymsString = "";
				ArrayList<String> sortedFinalSynonyms = new ArrayList<String>(finalSynonyms);
				Collections.sort(sortedFinalSynonyms);
				int numberOfSynonyms = sortedFinalSynonyms.size();
				for(int i = 0; i < numberOfSynonyms - 1; i++) {
					synonymsString += sortedFinalSynonyms.get(i) + "\n";
				}
				if(numberOfSynonyms >= 1) {
					synonymsString += sortedFinalSynonyms.get(numberOfSynonyms - 1);
				}
				synonymsLabel.setText("Synonyms (" + finalSynonyms.size() + ")");
				synonymsField.setText(synonymsString);
				synonymsField.repaint();
				
				Collection<HashSet<String>> diseases = Scheme.getDiseases().values();
				HashSet<String> finalDiseases = new HashSet<String>();
				Iterator<HashSet<String>> diseasesIterator = diseases.iterator();
				while(diseasesIterator.hasNext()) {
					finalDiseases.addAll(diseasesIterator.next());
				}
				ArrayList<String> sortedFinalDiseases = new ArrayList<String>(finalDiseases);
				Collections.sort(sortedFinalDiseases);
				
				double lostRate = (double)Scheme.getDiseasesLostMappingsCount() / (double)Scheme.getDiseasesMappingsCount();
				lostRate *= 100;
				diseasesLabel.setText("Diseases (" + finalDiseases.size() + ") [Mappings done: " + Scheme.getDiseasesMappingsCount() + ", mappings lost: " + Scheme.getDiseasesLostMappingsCount() + " (" + Math.round(lostRate) + "%)]");
				
				HashMap<String, Integer> scoresByDiseases = new HashMap<String, Integer>();
				for(String disease : sortedFinalDiseases) {
					HashSet<String> sources = Scheme.getDiseaseSources().get(disease);
					ArrayList<String> sortedSources = new ArrayList<String>(sources);
					int score = sortedSources.size();
					scoresByDiseases.put(disease, score);
					//diseasesModel.addRow(new Object[]{disease, Scheme.getDiseaseSources().get(disease)});
				}
				Stream<Map.Entry<String, Integer>> sortedDiseases = scoresByDiseases
						.entrySet().stream()
					    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));
				Iterator<Entry<String, Integer>> diseaseIterator = sortedDiseases.iterator();
				while(diseaseIterator.hasNext()) {
					Entry<String, Integer> entry = diseaseIterator.next();
					diseasesModel.addRow(new Object[]{entry.getKey(), Scheme.getDiseaseSources().get(entry.getKey()), entry.getValue()});
				}
				
				Collection<HashSet<String>> sideEffects = Scheme.getSideEffects().values();
				HashSet<String> finalSideEffects = new HashSet<String>();
				Iterator<HashSet<String>> sideEffectsIterator = sideEffects.iterator();
				while(sideEffectsIterator.hasNext()) {
					finalSideEffects.addAll(sideEffectsIterator.next());
				}
				ArrayList<String> sortedFinalSideEffects = new ArrayList<String>(finalSideEffects);
				Collections.sort(sortedFinalSideEffects);
				
				lostRate = (double)Scheme.getSideEffectsLostMappingsCount() / (double)Scheme.getSideEffectsMappingsCount();
				lostRate *= 100;
				sideEffectsLabel.setText("Side effects (" + finalSideEffects.size() + ") [Mappings done: " + Scheme.getSideEffectsMappingsCount() + ", mappings lost: " + Scheme.getSideEffectsLostMappingsCount() + " (" + Math.round(lostRate) + "%)]");
				
				HashMap<String, Integer> scoresBySideEffects = new HashMap<String, Integer>();
				for(String sideEffect : sortedFinalSideEffects) {
					HashSet<String> sources = Scheme.getSideEffectSources().get(sideEffect);
					ArrayList<String> sortedSources = new ArrayList<String>(sources);
					int score = sortedSources.size();
					scoresBySideEffects.put(sideEffect, score);
					//sideEffectsModel.addRow(new Object[]{sideEffect, Scheme.getSideEffectSources().get(sideEffect)});
				}
				Stream<Map.Entry<String, Integer>> sortedSideEffects = scoresBySideEffects
						.entrySet().stream()
					    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));
				Iterator<Entry<String, Integer>> sideEffectIterator = sortedSideEffects.iterator();
				while(sideEffectIterator.hasNext()) {
					Entry<String, Integer> entry = sideEffectIterator.next();
					sideEffectsModel.addRow(new Object[]{entry.getKey(), Scheme.getSideEffectSources().get(entry.getKey()), entry.getValue()});
				}
				
				Collection<HashSet<String>> indications = Scheme.getIndications().values();
				HashSet<String> finalIndications = new HashSet<String>();
				Iterator<HashSet<String>> indicationsIterator = indications.iterator();
				while(indicationsIterator.hasNext()) {
					finalIndications.addAll(indicationsIterator.next());
				}
				ArrayList<String> sortedFinalIndications = new ArrayList<String>(finalIndications);
				Collections.sort(sortedFinalIndications);
				
				lostRate = (double)Scheme.getIndicationsLostMappingsCount() / (double)Scheme.getIndicationsMappingsCount();
				lostRate *= 100;
				indicationsLabel.setText("Indications (" + finalIndications.size() + ") [Mappings done: " + Scheme.getIndicationsMappingsCount() + ", mappings lost: " + Scheme.getIndicationsLostMappingsCount() + " (" + Math.round(lostRate) + "%)]");
				
				HashMap<String, Integer> scoresByIndications = new HashMap<String, Integer>();
				for(String indication : sortedFinalIndications) {
					HashSet<String> sources = Scheme.getIndicationSources().get(indication);
					ArrayList<String> sortedSources = new ArrayList<String>(sources);
					int score = sortedSources.size();
					scoresByIndications.put(indication, score);
					//indicationsModel.addRow(new Object[]{indication, sortedSources});
				}
				Stream<Map.Entry<String, Integer>> sortedIndications = scoresByIndications
						.entrySet().stream()
					    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));
				Iterator<Entry<String, Integer>> indicationIterator = sortedIndications.iterator();
				while(indicationIterator.hasNext()) {
					Entry<String, Integer> entry = indicationIterator.next();
					indicationsModel.addRow(new Object[]{entry.getKey(), Scheme.getIndicationSources().get(entry.getKey()), entry.getValue()});
				}
				System.err.println(Scheme.getDiseasesLostMappingsCount());
				System.err.println(Scheme.getDiseasesMappingsCount());
				System.err.println(Scheme.getDiseasesQualityScore());
				System.err.println("--");
				System.err.println(Scheme.getSideEffectsLostMappingsCount());
				System.err.println(Scheme.getSideEffectsMappingsCount());
				System.err.println(Scheme.getSideEffectsQualityScore());
				System.err.println("--");
				System.err.println(Scheme.getIndicationsLostMappingsCount());
				System.err.println(Scheme.getIndicationsMappingsCount());
				System.err.println(Scheme.getIndicationsQualityScore());
				
				//System.err.println(Scheme.getSideEffectsQualityScore());
				//System.err.println(Scheme.getIndicationsQualityScore());
				
			}
		});
		searchPanel.add(searchButton, BorderLayout.EAST);
		add(searchPanel, BorderLayout.NORTH);
	
		add(corePanel, BorderLayout.CENTER);
		
	}
}

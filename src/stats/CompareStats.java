package stats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CompareStats {
	String aidaXMLName;
	String csawXMLName;
	Set<String> annotatedFilesSet; // set of files which are annotated
	Set<String> aidaEntities;
	Set<String> csawEntities;

	public CompareStats(String aidaXMLName, String csawXMLName) {
		this.aidaXMLName = aidaXMLName;
		this.csawXMLName = csawXMLName;
	}

	public static void main(String args[]) throws ParserConfigurationException,
			SAXException, IOException {
		//	
		String aidaXMLName = "data/entitiesWiki2012";
		String csawXMLName = "data/CSAW_Annotations.xml";
		String both_filename = "report/entitiesInBoth.tex";
		String missed_filename = "report/entitiesMissed.tex"; //by either
		
		String stats_filename = "report/stats.tex";

		CompareStats cs = new CompareStats(aidaXMLName, csawXMLName);
		
		// First get the list of files that were annotated by CSAW team.
		// Required  because AIDA has tagging for 604 files.
		cs.fillAnnotatedFilesSet();

		// Fill the maps : Entities -> Integer
		//HashMap<String, Integer> aidaEntitiesCountMap = cs.readToMap(aidaXMLName);
		HashMap<String, Integer> aidaEntitiesCountMap = cs.readToMapFromList(aidaXMLName);
		HashMap<String, Integer> csawEntitiesCountMap = cs.readToMap(csawXMLName);

		// Get the set of entities
		cs.aidaEntities = aidaEntitiesCountMap.keySet();
		cs.csawEntities = csawEntitiesCountMap.keySet();

		// Open the files to dump numbers to
		BufferedWriter both_file = new BufferedWriter(new FileWriter(both_filename));
		BufferedWriter missed_file = new BufferedWriter(new FileWriter(missed_filename));
		BufferedWriter stats_file = new BufferedWriter(new FileWriter(stats_filename));

		//dump the overall entity stats
		cs.overallEntityStats(stats_file, missed_file);
		Iterator<Entry<String, Integer>> csawIter = csawEntitiesCountMap.entrySet().iterator();
	
		DescriptiveStatistics score_stats = new DescriptiveStatistics();
		System.out.println(aidaEntitiesCountMap.size());
		
		
		while (csawIter.hasNext()) { // for each entity tagged by CSAW
			Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>) csawIter
					.next();
			String entity = pair.getKey();
			int csawCount = pair.getValue();
			// for the same entity, see how much time it has appeared in Aida
			Integer aidaCount = aidaEntitiesCountMap.get(entity); // get the
																	// count for
																	// aida
			if (aidaCount == null) { // This entity was missed by AIDA
				continue;
			}

			double percCovered = (double) aidaCount / csawCount;
			score_stats.addValue(percCovered);
			both_file.write(entity + " & " + csawCount + "& "
							+ aidaCount + "&" + Double.toString(percCovered)
					+ "\\\\\n ");
			//System.out.println(entity + " -> CSAW :  " + csawCount + ", AIDA : "
				//	+ aidaCount + ", score : " + Double.toString(percCovered)
					//+ "\n");
		}

				

		//write stats file
	
		stats_file.write("$n$ & " + score_stats.getN() + "\\\\ \n");
		stats_file.write("min & " + score_stats.getMin() + "\\\\ \n");
		stats_file.write("max & " + score_stats.getMax() + "\\\\ \n");
		stats_file.write("mean & " + score_stats.getMean() + "\\\\ \n");
		stats_file.write("std dev & " + score_stats.getStandardDeviation() + "\\\\ \n");
		stats_file.write("median & " + score_stats.getPercentile(50) + "\\\\ \n");
		stats_file.write("skewness & " + score_stats.getSkewness() + "\\\\ \n");
		stats_file.write("kurtosis & " + score_stats.getKurtosis() + "\\\\ \n");
		
		//close all files
		both_file.close();
		missed_file.close();
		stats_file.close();
		
	}

	
	private void overallEntityStats(BufferedWriter statsfile, BufferedWriter bw) throws IOException {
	//set operations are in place
		Set<String> backup_aida = new HashSet<String>(aidaEntities);
		Set<String> backup_csaw = new HashSet<String>(csawEntities);
		//statsfile.write("\\begin{tabular}{l|l}\n \\hline \n ");
		statsfile.write("Total entities annotated by AIDA & " + aidaEntities.size() + "\\\\ \n");
		statsfile.write("Total entities annotated by CSAW Team & " + csawEntities.size() + "\\\\ \n");
		backup_csaw.retainAll(aidaEntities);
		statsfile.write("Total entities common to both & " + backup_csaw.size() + "\\\\ \n");
		
		backup_csaw = new HashSet<String>(csawEntities);
		backup_csaw.removeAll(aidaEntities);
		backup_aida.removeAll(csawEntities);
		statsfile.write("CSAW Entities missed by AIDA : (full list follows)  & " + backup_csaw.size() + "\\\\ \n");
		statsfile.write("AIDA Entities missed by CSAW : (full list follows)  & " + backup_aida.size() + "\\\\ \n");
		
		bw.write("CSAW Entities missed by AIDA : \n");
		int i = 0;
		Iterator<String> iterator = backup_csaw.iterator();
		while(iterator.hasNext()) {
			bw.write(iterator.next() + "  ");
			if(i % 3 == 0) {
				bw.write("\n");
			}
			i++;
		}
		bw.write("AIDA Entities missed by CSAW : \n");
		iterator = backup_aida.iterator();
		while(iterator.hasNext()) {
			bw.write(iterator.next() + "  ");
			if(i % 3 == 0) {
				bw.write("\n");
			}
			i++;
		}
		backup_csaw = csawEntities;
		backup_aida = aidaEntities;
	}

	private HashMap<String, Integer> readToMap(String fileName)
			throws ParserConfigurationException, SAXException, IOException {
		HashMap<String, Integer> countMap = new HashMap<String, Integer>();
		final String annotationTag = "annotation";
		final String docNameTag = "docName";
		final String wikiNameTag = "wikiName";
		File fXmlFile = new File(fileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		NodeList annotationList = doc.getElementsByTagName(annotationTag);
		int annon = 0;
		for (; annon < annotationList.getLength(); annon++) {
			Node annonNode = annotationList.item(annon);
			if (annonNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) annonNode;
				String entityString = eElement
						.getElementsByTagName(wikiNameTag).item(0)
						.getTextContent();

				String currFileName = eElement.getElementsByTagName(docNameTag)
						.item(0).getTextContent();
				if (annotatedFilesSet.contains(currFileName)) { // if the
																// current file
																// was annotated
																// by csaw team
					Integer currentCount = countMap.get(entityString);
					if (null == currentCount) {
						countMap.put(entityString, 1);
					} else {
						countMap.put(entityString, currentCount + 1);
					}
				}
			}
		}
		return countMap;
	}
	private HashMap<String, Integer> readToMapFromList(String fileName)
			throws ParserConfigurationException, SAXException, IOException {
		HashMap<String, Integer> countMap = new HashMap<String, Integer>();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String entityString;
		while((entityString = br.readLine()) != null) {
					Integer currentCount = countMap.get(entityString);
					if (null == currentCount) {
						countMap.put(entityString, 1);
					} else {
						countMap.put(entityString, currentCount + 1);
					}
		}
		br.close();
		return countMap;
	}

	private void fillAnnotatedFilesSet()
			throws ParserConfigurationException, SAXException, IOException {
		HashMap<String, Integer> countMap = new HashMap<String, Integer>();
		final String annotationTag = "annotation";
		final String docNameTag = "docName";
		final String wikiNameTag = "wikiName";
		HashSet<String> enSet = new HashSet<String>();
		File fXmlFile = new File(csawXMLName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		NodeList annotationList = doc.getElementsByTagName(annotationTag);
		int annon = 0;
		System.out.println("Total annotations : " + annotationList.getLength());
		for (; annon < annotationList.getLength(); annon++) {
			Node annonNode = annotationList.item(annon);
			// System.out.println("\nCurrent Element :" +
			// annonNode.getNodeName());
			if (annonNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) annonNode;
				String docString = eElement.getElementsByTagName(docNameTag)
						.item(0).getTextContent();
				String entityString = eElement
						.getElementsByTagName(wikiNameTag).item(0)
						.getTextContent();
				enSet.add(entityString);
				Integer currentCount = countMap.get(docString);
				if (null == currentCount) {
					countMap.put(docString, 1);
				} else {
					countMap.put(docString, currentCount + 1);
				}

			}
		}
		Set<String> setOfAnnonFiles = countMap.keySet();
		//System.out.println("Total entities : " + enSet.size());
		//System.out.println("Docs : " + setOfAnnonFiles.size());
		// System.out.println(enSet);

		/*
		 * Iterator<Entry<String, Integer>> csawIter =
		 * countMap.entrySet().iterator(); while(csawIter.hasNext()) {
		 * Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>)
		 * csawIter.next(); String annotatedFileName = pair.getKey(); setOf
		 * System.out.println(annotatedFileName); }
		 */
		annotatedFilesSet = setOfAnnonFiles;
	}

	/*
	 * private Set<String> getAnnotatedFiles1(String fileName) throws
	 * ParserConfigurationException, SAXException, IOException { HashMap<String,
	 * Integer> countMap = new HashMap<String, Integer>(); final String
	 * annotationTag = "annotation"; final String docNameTag = "docName"; final
	 * String wikiNameTag = "wikiName"; HashSet<String> enSet = new
	 * HashSet<String>(); HashSet<String> aidaFileSet = new HashSet<String>();
	 * File fXmlFile = new File(fileName); DocumentBuilderFactory dbFactory =
	 * DocumentBuilderFactory.newInstance(); DocumentBuilder dBuilder =
	 * dbFactory.newDocumentBuilder(); Document doc = dBuilder.parse(fXmlFile);
	 * doc.getDocumentElement().normalize(); NodeList annotationList =
	 * doc.getElementsByTagName(annotationTag); int annon = 0;
	 * System.out.println("Total annotations : " + annotationList.getLength());
	 * for (; annon < annotationList.getLength(); annon++) { Node annonNode =
	 * annotationList.item(annon); // System.out.println("\nCurrent Element :" +
	 * // annonNode.getNodeName()); if (annonNode.getNodeType() ==
	 * Node.ELEMENT_NODE) { Element eElement = (Element) annonNode; String
	 * docString = eElement .getElementsByTagName(docNameTag).item(0)
	 * .getTextContent(); String entityString = eElement
	 * .getElementsByTagName(wikiNameTag).item(0) .getTextContent();
	 * enSet.add(entityString); Integer currentCount = countMap.get(docString);
	 * if (null == currentCount) { countMap.put(docString, 1); } else {
	 * countMap.put(docString, currentCount + 1); }
	 * if(annotatedFilesSet.contains(docString)) { aidaFileSet.add(docString); }
	 * } } Set<String> setOfAnnonFiles = countMap.keySet();
	 * System.out.println("Total entities : " + enSet.size());
	 * System.out.println("Docs : " + aidaFileSet.size());
	 * //System.out.println(enSet); return setOfAnnonFiles; }
	 */
}

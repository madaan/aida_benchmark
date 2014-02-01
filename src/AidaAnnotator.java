//sg

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import mpi.aida.Disambiguator;
import mpi.aida.Preparator;
import mpi.aida.config.settings.DisambiguationSettings;
import mpi.aida.config.settings.PreparationSettings;
import mpi.aida.config.settings.disambiguation.CocktailPartyDisambiguationSettings;
import mpi.aida.config.settings.preparation.StanfordHybridPreparationSettings;
import mpi.aida.data.DisambiguationResults;
import mpi.aida.data.PreparedInput;
import mpi.aida.data.ResultEntity;
import mpi.aida.data.ResultMention;
import mpi.aida.graph.similarity.exception.MissingSettingException;


public class AidaAnnotator {

	PreparationSettings preparationSettings;
	DisambiguationSettings disambiguationSettings;
	String corpusPath; //path to the data files

	public AidaAnnotator(PreparationSettings preparationSettings, DisambiguationSettings disambiguationSettings, String corpusPath) {
		this.preparationSettings = preparationSettings;
		this.disambiguationSettings = disambiguationSettings;
		this.corpusPath =corpusPath;
	}

	public static void main(String args[]) throws MissingSettingException
	{
		AidaAnnotator aa = new AidaAnnotator(new StanfordHybridPreparationSettings(), //settings for NER
											new CocktailPartyDisambiguationSettings(), //settings for AIDA
											"data/test" //path to corpus
		);		
		try {
			aa.process();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//takes the path to the data files as input and generates a disambiguated 
	private void process() throws IOException
	{
		Preparator p = new Preparator();
		File corpus = new File(corpusPath);
		File[] listOfFiles = corpus.listFiles();
		for(File afile : listOfFiles) {
			String inputText = readFileToString(afile.getCanonicalPath());
			System.out.println("Disambiguating File : " + afile.getName());
			DisambiguationResults dres = annotate(inputText, p);
			writeXML(dres, afile.getName());
		}	
	}
	
	//annotates the text provided as input and returns results
	private DisambiguationResults annotate(String inputText, Preparator p) 
	{
		//preparing the input
				
				PreparedInput input = p.prepare(inputText, preparationSettings);
				// Disambiguate the input with the graph coherence algorithm.
				//disSettings = new FastLocalDisambiguationSettings();
				Disambiguator d = new Disambiguator(input, disambiguationSettings);
				DisambiguationResults results = null;
				try {
					results = d.disambiguate();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return results;
	}

	//returns the file (specified by filename) in a string
	private String readFileToString(String fileName) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		try {
			br = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	void writeXML(DisambiguationResults results, String fileName) throws IOException
	{
		// Print the disambiguation results.
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("AIDA_Annotations.xml", true)));
			writer.println("<AIDA.entityAnnotations>");
			for (ResultMention rm : results.getResultMentions()) {
				ResultEntity re = results.getBestEntity(rm);
				writer.println("<annotation>");
				writer.println("<docName>" + fileName + "</docName>");
				writer.println("<userId>AIDA</userId>");
				writer.println("<wikiName>" + re.getEntity().replace('_', ' ') + "</wikiName>");
				writer.println("<offset>" + rm.getCharacterOffset() + "</offset>");
				writer.println("<length>" + rm.getCharacterLength() + "</length>");
				writer.println("</annotation>");
		  	
			}
			writer.println("</AIDA.entityAnnotations>");
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
}

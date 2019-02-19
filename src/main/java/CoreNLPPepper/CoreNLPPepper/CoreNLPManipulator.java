package CoreNLPPepper.CoreNLPPepper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.impl.PepperManipulatorImpl;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperManipulator;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.PepperModule;
import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleNotReadyException;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
//import eu.fbk.dh.tint.runner.TintPipeline;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.util.StringUtils;
import java.util.*;

/**
 * This is a dummy implementation to show how a {@link PepperManipulator} works.
 * Therefore it just prints out some information about a corpus like the number
 * of nodes, edges and for instance annotation frequencies. <br/>
 * This class can be used as a template for an own implementation of a
 * {@link PepperManipulator} Take a look at the TODO's and adapt the code. If
 * this is the first time, you are implementing a Pepper module, we strongly
 * recommend, to take a look into the 'Developer's Guide for Pepper modules',
 * you will find on
 * <a href="http://corpus-tools.org/pepper/">http://corpus-tools.org/pepper</a>.
 * 
 * @author Okinina
 */
@Component(name = "CoreNLPManipulatorComponent", factory = "PepperManipulatorComponentFactory")
public class CoreNLPManipulator extends PepperManipulatorImpl {
	// =================================================== mandatory
	// ===================================================
	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong> <br/>
	 * A constructor for your module. Set the coordinates, with which your
	 * module shall be registered. The coordinates (modules name, version and
	 * supported formats) are a kind of a fingerprint, which should make your
	 * module unique.
	 */
	public CoreNLPManipulator() {
		super();
		setName("CoreNLPManipulator");
		// TODO change suppliers e-mail address
		setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
		// TODO change suppliers homepage
		setSupplierHomepage(URI.createURI(PepperConfiguration.HOMEPAGE));
		// TODO add a description of what your module is supposed to do
		setDesc("The manipulator, traverses over the document-structure and prints out some information about it, like the frequencies of annotations, the number of nodes and edges and so on. ");
	}

	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong> <br/>
	 * This method creates a customized {@link PepperMapper} object and returns
	 * it. You can here do some additional initialisations. Thinks like setting
	 * the {@link Identifier} of the {@link SDocument} or {@link SCorpus} object
	 * and the {@link URI} resource is done by the framework (or more in detail
	 * in method {@link #start()}). The parameter <code>Identifier</code>, if a
	 * {@link PepperMapper} object should be created in case of the object to
	 * map is either an {@link SDocument} object or an {@link SCorpus} object of
	 * the mapper should be initialized differently. <br/>
	 * 
	 * @param Identifier
	 *            {@link Identifier} of the {@link SCorpus} or {@link SDocument}
	 *            to be processed.
	 * @return {@link PepperMapper} object to do the mapping task for object
	 *         connected to given {@link Identifier}
	 */
	public PepperMapper createPepperMapper(Identifier Identifier) {
		CoreNLPMapper mapper = new CoreNLPMapper();
		return (mapper);
	}

	/**
	 * This class is a dummy implementation for a mapper, to show how it works.
	 * Pepper or more specific this dummy implementation of a Pepper module
	 * creates one mapper object per {@link SDocument} object and
	 * {@link SCorpus} object each. This ensures, that each of those objects is
	 * run independently from another and runs parallelized. <br/>
	 * The method {@link #mapSCorpus()} is supposed to handle all
	 * {@link SCorpus} object and the method {@link #mapSDocument()} is supposed
	 * to handle all {@link SDocument} objects. <br/>
	 * In our dummy implementation, we just print out some information about a
	 * corpus to system.out. This is not very useful, but might be a good
	 * starting point to explain how access the several objects in Salt model.
	 */
	public static class CoreNLPMapper extends PepperMapperImpl implements GraphTraverseHandler {
		/**
		 * Creates meta annotations, if not already exists
		 */
		@Override
		public DOCUMENT_STATUS mapSCorpus() {
			if (getCorpus().getMetaAnnotation("date") == null) {
				getCorpus().createMetaAnnotation(null, "date", "1989-12-17");
			}
			return (DOCUMENT_STATUS.COMPLETED);
		}

		/**
		 * prints out some information about document-structure
		 */
		@Override
		public DOCUMENT_STATUS mapSDocument() {
			

			//testStanfordEnglish();
			
			testStanfordGerman();

			//testStanfordItalian();


			// create a StringBuilder, to be filled with informations (we need
			// to intermediately store the results, because of parallelism of
			// modules)
			String format = "|%-15s: %15s |%n";
			StringBuilder out = new StringBuilder();
			out.append("\n");
			// print out the id of the document
			out.append(getDocument().getId());
			out.append("\n");
			out.append("+---------------------------------+\n");
			// print out the general number of nodes
			out.append(String.format(format, "nodes", getDocument().getDocumentGraph().getNodes().size()));
			addProgress((double) (1 / 7));
			// print out the general number of relations
			out.append(String.format(format, "relations", getDocument().getDocumentGraph().getRelations().size()));
			addProgress((double) (1 / 7));
			// print out the general number of primary texts
			out.append(String.format(format, "texts", getDocument().getDocumentGraph().getTextualDSs().size()));
			addProgress((double) (1 / 7));
			// print out the general number of tokens
			out.append(String.format(format, "tokens", getDocument().getDocumentGraph().getTokens().size()));
			addProgress((double) (1 / 7));
			// print out the general number of spans
			out.append(String.format(format, "spans", getDocument().getDocumentGraph().getSpans().size()));
			addProgress((double) (1 / 7));
			// print out the general number of structures
			out.append(String.format(format, "structures", getDocument().getDocumentGraph().getStructures().size()));
			addProgress((double) (1 / 7));

			// create alist of all root nodes of the current document-structure
			List<SNode> roots = getDocument().getDocumentGraph().getRoots();
			// traverse the document-structure beginning at the roots in
			// depth-first order top down. The id 'CoreNLPTraversal' is used for
			// uniqueness, in case of one class uses multiple traversals. This
			// object then takes the call-backs implemented with methods
			// checkConstraint, nodeReached and nodeLeft
			getDocument().getDocumentGraph().traverse(roots, GRAPH_TRAVERSE_TYPE.TOP_DOWN_DEPTH_FIRST,
					"CoreNLPTraversal", this);

			// print out computed frequencies
			for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
				out.append(String.format(format, entry.getKey(), entry.getValue()));
			}
			addProgress((double) (1 / 7));
			out.append("+---------------------------------+\n");
			System.out.println(out.toString());

			return (DOCUMENT_STATUS.COMPLETED);
		}
		
		/**
		 * tests Stanford Core NLP parser for German
		 */
		public void testStanfordGerman(){
			String sampleGermanText = "Für das Deutsche, beispielsweise, werden Tokenisierung und Lemmatisierung nicht unterstützt.";
	        Annotation germanAnnotation = new Annotation(sampleGermanText);
	        Properties germanProperties = StringUtils.argsToProperties(
	                new String[]{"-props", "StanfordCoreNLP-german.properties"});
	        StanfordCoreNLP pipeline = new StanfordCoreNLP(germanProperties);
	        pipeline.annotate(germanAnnotation);
	        for (CoreMap sentence : germanAnnotation.get(CoreAnnotations.SentencesAnnotation.class)) {
	            Tree sentenceTree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
	            System.out.println(sentenceTree);
	        }
		}

		/**
		 * tests Stanford Core NLP parser for Italian
		 */
		/*public void testStanfordItalian(){
			// Initialize the Tint pipeline
			TintPipeline pipelineTint = new TintPipeline();

			// Load the default properties
			// see https://github.com/dhfbk/tint/blob/master/tint-runner/src/main/resources/default-config.properties
			try {
				pipelineTint.loadDefaultProperties();
			}
			catch (IOException e) { //TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Add a custom property
			// pipeline.setProperty("my_property","my_value");

			// Load the models pipeline.load();

			// Use for example a text in a String
			String textItalian = "I topi non avevano nipoti.";

			// Get the original Annotation (Stanford CoreNLP)
			Annotation stanfordAnnotation = pipelineTint.runRaw(textItalian);

			System.out.println(textItalian);
			System.out.println(stanfordAnnotation);

			// **or**

			// Get the JSON // (optionally getting the original Stanford CoreNLP Annotation as return value)
			//InputStream stream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
			//Annotation annotation = pipeline.run(stream, System.out, TintRunner.OutputFormat.JSON);
		}*/


		/**
		 * tests Stanford Core NLP parser for English
		 */
		public void testStanfordEnglish() {
			/*
			 * String[] englishArgs = new String[]{"-file",
			 * "/home/nadiushka/pepper/CoreNLPPepper/sample-english.txt",
			 * "-outputFormat", "text", "-props",
			 * "/home/nadiushka/pepper/CoreNLPPepper/english.properties"}; try {
			 * StanfordCoreNLP.main(englishArgs); } catch (IOException e) { //
			 * TODO Auto-generated catch block e.printStackTrace(); }
			 */
			
			
			// creates a StanfordCoreNLP object, with POS tagging,
			// lemmatization, NER, parsing, and coreference resolution
			Properties props = new Properties();
			//props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
			props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

			// read some text in the text variable
			String text = "This is a test of the Stanford CoreNLP Code. Stanford makes some interesting code!";

			// create an empty Annotation just with the given text
			Annotation document = new Annotation(text);

			// run all Annotators on this text
			pipeline.annotate(document);
			System.out.println("Almost done!!");
			// demonstrate typical usage

			ArrayList<String> textList = new ArrayList<String>();
			ArrayList<String> posList = new ArrayList<String>();
			ArrayList<String> lemmaList = new ArrayList<String>();
			//ArrayList<String> namedList = new ArrayList<String>();

			for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
				// get the tree for the sentence
				Tree tree = sentence.get(TreeAnnotation.class);
				// get the tokens for the sentence and iterate over them
				for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
					// get token attributes
					String tokenText = token.get(TextAnnotation.class);
					String tokenPOS = token.get(PartOfSpeechAnnotation.class);
					String tokenLemma = token.get(LemmaAnnotation.class);
					//String tokenNE = token.get(NamedEntityTagAnnotation.class);

					textList.add(tokenText);
					posList.add(tokenPOS);
					lemmaList.add(tokenLemma);
					//namedList.add(tokenNE);
				}
			}

			System.out.println("");
			System.out.println("Input text: " + text);
			System.out.println("Parsed text:");
			System.out.println(textList);
			System.out.println("Parts of Speach:");
			System.out.println(posList);
			System.out.println("Lemma Annotation:");
			System.out.println(lemmaList);
			//System.out.println("Named Entity:");
			//System.out.println(namedList);

		}

		/** A map storing frequencies of annotations of processed documents. */
		private Map<String, Integer> frequencies = new Hashtable<String, Integer>();

		/**
		 * This method is called for each node in document-structure, as long as
		 * {@link #checkConstraint(GRAPH_TRAVERSE_TYPE, String, SRelation, SNode, long)}
		 * returns true for this node. <br/>
		 * In our dummy implementation it just collects frequencies of
		 * annotations.
		 */
		@Override
		public void nodeReached(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode,
				SRelation sRelation, SNode fromNode, long order) {
			if (currNode.getAnnotations().size() != 0) {
				// step through all annotations to collect them in frequencies
				// table
				for (SAnnotation annotation : currNode.getAnnotations()) {
					Integer frequence = frequencies.get(annotation.getName());
					// if annotation hasn't been seen yet, create entry in
					// frequencies set frequency to 0
					if (frequence == null) {
						frequence = 0;
					}
					frequence++;
					frequencies.put(annotation.getName(), frequence);
				}
			}
		}

		/**
		 * This method is called on the way back, in depth first mode it is
		 * called for a node after all the nodes belonging to its subtree have
		 * been visited. <br/>
		 * In our dummy implementation, this method is not used.
		 */
		@Override
		public void nodeLeft(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SNode currNode, SRelation edge,
				SNode fromNode, long order) {
		}

		/**
		 * With this method you can decide if a node is supposed to be visited
		 * by methods
		 * {@link #nodeReached(GRAPH_TRAVERSE_TYPE, String, SNode, SRelation, SNode, long)}
		 * and
		 * {@link #nodeLeft(GRAPH_TRAVERSE_TYPE, String, SNode, SRelation, SNode, long)}
		 * . In our dummy implementation for instance we do not need to visit
		 * the nodes {@link STextualDS}.
		 */
		@Override
		public boolean checkConstraint(GRAPH_TRAVERSE_TYPE traversalType, String traversalId, SRelation edge,
				SNode currNode, long order) {
			if (currNode instanceof STextualDS) {
				return (false);
			} else {
				return (true);
			}
		}
	}

	// =================================================== optional
	// ===================================================
	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong> <br/>
	 * This method is called by the pepper framework after initializing this
	 * object and directly before start processing. Initializing means setting
	 * properties {@link PepperModuleProperties}, setting temporary files,
	 * resources etc. . returns false or throws an exception in case of
	 * {@link PepperModule} instance is not ready for any reason.
	 * 
	 * @return false, {@link PepperModule} instance is not ready for any reason,
	 *         true, else.
	 */
	@Override
	public boolean isReadyToStart() throws PepperModuleNotReadyException {
		// TODO make some initializations if necessary
		return (super.isReadyToStart());
	}
}

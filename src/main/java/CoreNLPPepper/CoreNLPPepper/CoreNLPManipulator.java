package CoreNLPPepper.CoreNLPPepper;

import static java.util.Arrays.asList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
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
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SSequentialDS;
import org.corpus_tools.salt.common.SSequentialRelation;
import org.corpus_tools.salt.common.STextOverlappingRelation;
import org.corpus_tools.salt.common.STimeOverlappingRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.core.GraphTraverseHandler;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SGraph.GRAPH_TRAVERSE_TYPE;
import org.corpus_tools.salt.core.SNode;

import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.graph.Identifier;
import org.corpus_tools.salt.graph.Node;
import org.corpus_tools.salt.util.DataSourceSequence;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.core.SLayer;

import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
//import eu.fbk.dh.tint.runner.TintPipeline;
//import eu.fbk.dh.tint.runner.TintRunner;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.util.StringUtils;

import is2.data.SentenceData09;
import is2.lemmatizer.Lemmatizer;

import opennlp.tools.util.Span;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;

import java.nio.file.Paths;

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
		setSupplierContact(URI.createURI("nadezda.okinina@eurac.edu"));
		setSupplierHomepage(URI.createURI("https://github.com/commul/CoreNLPPepper"));
		setDesc("Does sentence splitting, tokenization, POS-tagging for German, English and Italian. For German and English uses Stanford Core NLP, for Italian uses Open NLP.");
		setProperties(new CoreNLPManipulatorProperties());
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
		//Will contain the name of the label that will allow to merge 2 or more structures, if they have the same value of this label.
		private String language;
		private Boolean doSentenceSplit;
		private Boolean doTokenize;
		private Boolean doPOStag;
		private Boolean doLemmatize;
		private String attributeToIgnore; //attribute of a span annotation to ignore
		private String annotationsUnifyingAttribute;
		private SDocument doc;
		private SDocumentGraph docGraph;
		private String documentTextString;
		private SSequentialDS  sSequentialDS;
		private HashMap tokenBeginHash; // key: start offset of the token; value: ArrayList<Object> [ Token , end offset ]
		//private HashMap tokenEndHash; // key: end offset of the token; value: Token , start
		private HashMap tagcodeStartEndHash; // key: tagcode; value: ArrayList <Integer> [start offset, end offset]
		
		private HashMap tagcodeSpanHash; // key: tagcode; value: SSpanImpl
		private ArrayList <STextualRelation> sTextRelsToRemove;
		private ArrayList <SToken> sTokensToRemove;
		private ArrayList <SSpan> spansToDelete;
		
		private HashMap tokenIdStartEndHash; // key: token id; value: ArrayList <Integer> [start offset, end offset]
		private HashMap spanTokensHash; // key: span id; value: ArrayList<Object> [SSpan, ArrayList <STokens>]
		
				
		/**
		* Constructor of TranscannoMapper.
		* Initialises the annotationsUnifyingAttribute:
		* 		either with the value entered by the user or with the default value "tagcode".
		*/
		//public CoreNLPMapper() {
			//super();
			//CoreNLPManipulatorProperties coreNLPManipulatorProperties = new CoreNLPManipulatorProperties();
			/*
			CoreNLPManipulatorProperties coreNLPManipulatorProperties = (CoreNLPManipulatorProperties) this.getProperties();
			System.out.println("coreNLPManipulatorProperties.toString():");
			System.out.println(coreNLPManipulatorProperties.toString());
			language = coreNLPManipulatorProperties.getLanguageProperty().toLowerCase();
			*/
			
			
		//}
				
		/**
		 * Creates meta annotations, if not already exists
		 */
		@Override
		public DOCUMENT_STATUS mapSCorpus() {
			
			if (getCorpus().getMetaAnnotation("date") == null) {
				Date date = new Date();
				getCorpus().createMetaAnnotation(null, "date", date.toString());
			}
			
			return (DOCUMENT_STATUS.COMPLETED);
		}
		
				
		private ArrayList<String> lemmatizeItalian(String tokens[]){		
			
			final ArrayList<String> ttResultArrayL = new ArrayList<String>();
	        try {
	        	URL location = CoreNLPManipulator.class.getProtectionDomain().getCodeSource().getLocation();
	        	Path path = Paths.get(location.toURI()).resolve("/treetagger").normalize();
	        	//System.out.println("path.toString(): " + path.toString());
	        	/*
	        	InputStream treetDirInputStream = CoreNLPManipulator.class.getResourceAsStream("/treetagger");
	        	File treetDirtempFile = File.createTempFile("tempfileTreetDir", ".tmp");
	        	treetDirtempFile.deleteOnExit();
				FileOutputStream outtreetDir = new FileOutputStream(treetDirtempFile);
				IOUtils.copy(treetDirInputStream, outtreetDir);
				
				boolean isDir = treetDirtempFile.isDirectory();
	        	System.out.println("treetDirtempFile.isDirectory():");
	        	System.out.println(Boolean.toString(isDir));
	        	*/
				/*
	        	//String treetaggerPath = CoreNLPManipulator.class.getResource("/treetagger").getPath(); //Didn't work
	        	String treetaggerPath = CoreNLPManipulator.class.getResource("/treetagger").getPath();
	        	boolean isDir = CoreNLPManipulator.class.getResource("/treetagger").isDirectory();
	        	System.out.println("CoreNLPManipulator.class.getResource(\"/treetagger\").isDirectory():");
	        	System.out.println(Boolean.toString(isDir));
	        	System.out.println("treetaggerPath: " + treetaggerPath);
				System.setProperty("treetagger.home", treetaggerPath);
				*/
				//System.setProperty("treetagger.home", path.toString());
				TreeTaggerWrapper tt = new TreeTaggerWrapper();
				
				
				InputStream italianTTlInputStream = CoreNLPManipulator.class.getResourceAsStream("/treetagger/italian-utf8.par");

				File italianTTtempFile = File.createTempFile("tempfileItalianForTreetagger", ".tmp");
				italianTTtempFile.deleteOnExit();
				FileOutputStream out = new FileOutputStream(italianTTtempFile);
				IOUtils.copy(italianTTlInputStream, out);				
				
				tt.setModel(italianTTtempFile.getPath());
	            
	            TokenHandler tokenHandler = new TokenHandler<String>() {
	                public void token(String token, String pos, String lemma) {
	                    ttResultArrayL.add(lemma);
	                    //System.out.println("lemma line 224: " + lemma);
	                }
	            };
	            
	            tt.setHandler(tokenHandler);
	            tt.process(asList(tokens));
	            tt.destroy();
	            
	        }catch(URISyntaxException e){
	        	e.printStackTrace();
	        }catch (TreeTaggerException e) {
				e.printStackTrace();
			}catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
	        //String[] arrayResult = ttResultArrayL.toArray(new String[ttResultArrayL.size()]);
	        
	        return ttResultArrayL;
		}
		
		
		private ArrayList<String> lemmatizeGerman(String tokens[], Lemmatizer mateLemmatizer){
			ArrayList <String> lemmasArrayList = new ArrayList<String>();
			//try{
				/*
				InputStream germanLemmaModelInputStream = CoreNLPManipulator.class.getResourceAsStream("/models/lemma_ger_3.6.model");

				File tempFile = File.createTempFile("tempfile", ".tmp");
				tempFile.deleteOnExit();
				FileOutputStream out = new FileOutputStream(tempFile);
				IOUtils.copy(germanLemmaModelInputStream, out);

				Lemmatizer mateLemmatizer = new Lemmatizer(tempFile.getPath());
				*/
				String[]tokensForMate = new String[tokens.length + 1];
				tokensForMate[0] = "<root>";  // Mate tools expect root token
				for (int i = 0; i < tokens.length; i++) {
					tokensForMate[i+1] = tokens[i];
				}
				SentenceData09 inputSentenceData = new SentenceData09();
				inputSentenceData.init(tokensForMate);
				SentenceData09 lemmatizedSentence = mateLemmatizer.apply(inputSentenceData);


				for(String lemma : lemmatizedSentence.plemmas) {  
					//System.out.println("lemma: " + lemma);
					lemmasArrayList.add(lemma);
				}
				/*
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
				 */
			return lemmasArrayList;
		}
		
		
		private void addOpenNLPTokens(SLayer tokensLayer){
			String tokenizerBinPath = "";
			String posTaggerBinPath = "";
			String sentenceSplitterBinPath = "";
			if(language.equals("it")){
				tokenizerBinPath = "/models/it-token.bin";
				posTaggerBinPath = "/models/it-pos-maxent.bin";
				sentenceSplitterBinPath = "/models/it-sent.bin";
			}else if(language.equals("de")){
				tokenizerBinPath = "/models/de_token.bin";
				posTaggerBinPath = "/models/de_pos_maxent.bin";
				sentenceSplitterBinPath = "/models/de_sent.bin";
			}
			try (InputStream modelIn = CoreNLPManipulator.class.getResourceAsStream(tokenizerBinPath)) {
				TokenizerModel model = new TokenizerModel(modelIn);
				Tokenizer tokenizer = new TokenizerME(model);
				
				//SSequentialDS  sSequentialDS= docGraph.createTextualDS(documentTextString);
				
				InputStream sentenceSplitModelIn = CoreNLPManipulator.class.getResourceAsStream(sentenceSplitterBinPath);
				SentenceModel sentenceSplitModel = new SentenceModel(sentenceSplitModelIn);				
				SentenceDetectorME sentenceDetector = new SentenceDetectorME(sentenceSplitModel);
				String sentences[] = sentenceDetector.sentDetect(documentTextString);
				
				// Initialize the POS tagger
				InputStream posModelIn = CoreNLPManipulator.class.getResourceAsStream(posTaggerBinPath);
				POSModel posModel = new POSModel(posModelIn);				
				POSTaggerME posTagger = new POSTaggerME(posModel);
				
				//Initialize the German lemmatizer
				InputStream germanLemmaModelInputStream = CoreNLPManipulator.class.getResourceAsStream("/models/lemma_ger_3.6.model");
				File tempFile = File.createTempFile("tempfile", ".tmp");
				tempFile.deleteOnExit();
				FileOutputStream out = new FileOutputStream(tempFile);
				IOUtils.copy(germanLemmaModelInputStream, out);
				Lemmatizer mateLemmatizer = new Lemmatizer(tempFile.getPath());
				
				int sentenceCounter = 0;
				int previousTextLength = 0;
				//Do tokenization, POStagging and lemmatization for each of the sentences
				for (String sentenceTextString: sentences){
					//System.out.println("previousTextLength line 349: '" + String.valueOf(previousTextLength) + "'");
					previousTextLength = documentTextString.indexOf(sentenceTextString, previousTextLength);
					//System.out.println("previousTextLength line 351: '" + String.valueOf(previousTextLength) + "'");
					//System.out.println("sentenceTextString line 352: '" + sentenceTextString + "'");
					ArrayList<SToken> sentenceSTokensArrayList = new ArrayList<SToken>();
					//String tokens[] = tokenizer.tokenize(documentTextString);
					String tokens[] = tokenizer.tokenize(sentenceTextString);

					// POS tag if requested by the parameters entered by the user
					// Otherwise just create an empty tags String table
					String tags[] = new String[tokens.length];
					if(doPOStag){
						tags = posTagger.tag(tokens);
					}

					// Lemmatize if requested by the parameters entered by the user
					// Otherwise just create an empty lemmas String table
					ArrayList <String> lemmasArrayList = new ArrayList<String>();				
					if(doLemmatize && language.equals("de")){					
						lemmasArrayList = lemmatizeGerman(tokens, mateLemmatizer);							
					}else if(doLemmatize && language.equals("it")){
						lemmasArrayList = lemmatizeItalian(tokens);
					}

					// Add the token, POS and lemma information to the documentGraph (Salt model) 
					//Span tokenSpans[] = tokenizer.tokenizePos(documentTextString);
					Span tokenSpans[] = tokenizer.tokenizePos(sentenceTextString);
					//SSequentialDS  sSequentialDS= docGraph.createTextualDS(documentTextString);

					int tokenNumber = 0;
					for (Span tokenSpan : tokenSpans) {
						//System.out.println(tokenSpan.toString());
						//System.out.println(tokens[tokenNumber]);
						//System.out.println(tags[tokenNumber]);

						SToken stok = docGraph.createToken(sSequentialDS, previousTextLength + tokenSpan.getStart(), previousTextLength + tokenSpan.getEnd());
						sentenceSTokensArrayList.add(stok);
						tokensLayer.addNode(stok);
						//adding a part-of-speech annotation to the new token
						if(doPOStag){
							stok.createAnnotation(null, "pos", tags[tokenNumber]);
						}

						if(doLemmatize){
							//System.out.println("lemmasArrayList.get(tokenNumber): " + lemmasArrayList.get(tokenNumber));
							stok.createAnnotation(null, "lemma", lemmasArrayList.get(tokenNumber));
						}
						//adding a lemma annotation to the new token
						//stok.createAnnotation(null, "lemma", "be");

						tokenNumber += 1;
					}
					
					if(doSentenceSplit){
						SSpan sentenceSpan = docGraph.createSpan(sentenceSTokensArrayList); // here should be a list of STokens
						sentenceSpan.createAnnotation(null, "sentence", "sentence_" + String.valueOf(sentenceCounter));
						//sentencesLayer.addNode(span);
								
					}
					sentenceCounter += 1;
					previousTextLength += sentenceTextString.length();
					//System.out.println("previousTextLength line 410: '" + String.valueOf(previousTextLength) + "'");
				}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		/*
		private addSentencesToLayer (SDocumentGraph docGraph, SLayer sentencesLayer, String documentTextString){

			if (language.substring(0,2).toLowerCase().equals("it")){

				try (InputStream modelIn = CoreNLPManipulator.class.getResourceAsStream("/models/it-sent.bin")) {
					SentenceModel model = new SentenceModel(modelIn);

					SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);

					String sentences[] = sentenceDetector.sentDetect(documentTextString);

					for (String sentence : sentences) {
						System.out.println(sentence);
						SSpan span = docGraph.createSSpan(listOfSTokens); // here should be a list of STokens
						sentencesLayer.addNode(span);
						//adding a part-of-speech annotation to the new token
						span.createAnnotation(null, "pos", "VBZ");
						//adding a lemma annotation to the new token
						span.createAnnotation(null, "lemma", "be");
					}

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		*/
			
		
		private void addStanfordAnnotationsToSaltModel(Annotation annotation){
			//SSequentialDS  sSequentialDS = docGraph.createTextualDS(documentTextString);
			//List<STextualDS> sTextualDSs = docGraph.getTextualDSs();
			/*
			StringBuilder strBuilder = new StringBuilder();
			for (STextualDS tds: sTextualDSs){
				System.out.println("tds.getData() line 455: " + tds.getData());
				strBuilder.append(tds.getData());
			}
			System.out.println("strBuilder line 458: " + strBuilder.toString());
			*/
			//SSequentialDS  sSequentialDS = sTextualDSs.get(0);
			
			if (doTokenize){
				//creating a layer named tokens
				SLayer tokensLayer = SaltFactory.createSLayer();
				tokensLayer.setName("tokens");
				doc.addLayer(tokensLayer);
				
				//creating a layer named sentences
				//SLayer sentencesLayer = SaltFactory.createSLayer();
				//sentencesLayer.setName("sentences");
				//Only add the layer to the document if requested by the user through the parameter SentenceSplit
				//if (doSentenceSplit){
					//doc.addLayer(sentencesLayer);
				//}
				
				//Will contain tokens of the whole text of the document
				ArrayList<String> tokensArrayList = new ArrayList<String>();
				
				int sentenceCounter = 0;				
				for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
					//Will contain tokens of the sentence
					ArrayList<SToken> sentenceSTokensArrayList = new ArrayList<SToken>();
					// get the tokens for the sentence and iterate over them
					for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
						//System.out.println("token.originalText(): " + token.originalText());
						tokensArrayList.add(token.originalText());						
						
						SToken stok = docGraph.createToken(sSequentialDS, token.beginPosition(), token.endPosition());					
						tokensLayer.addNode(stok);						
						sentenceSTokensArrayList.add(stok);
						
						ArrayList<Object> tokenAndItsEndArrayList = new ArrayList<Object>();
						tokenAndItsEndArrayList.add(stok);
						tokenAndItsEndArrayList.add(token.endPosition());
						
						tokenBeginHash.put(token.beginPosition(), tokenAndItsEndArrayList);
						
						if ( doPOStag){
							String tokenPOS = token.get(PartOfSpeechAnnotation.class);
							stok.createAnnotation(null, "pos", tokenPOS);
						}
						
						//if ( language.substring(0,2).toLowerCase().equals("en") && doLemmatize ){
						if ( doLemmatize ){
							String tokenLemma = token.get(LemmaAnnotation.class);
							//System.out.println("tokenLemma: " + tokenLemma);
							stok.createAnnotation(null, "lemma", tokenLemma);
						}
						
					}
					
					if(doSentenceSplit){
						SSpan sentenceSpan = docGraph.createSpan(sentenceSTokensArrayList); // here should be a list of STokens
						sentenceSpan.createAnnotation(null, "sentence", "sentence_" + String.valueOf(sentenceCounter));
						//sentencesLayer.addNode(span);
								
					}
					
					sentenceCounter += 1;
				}
				
			}
		}
		
		private void transferNew(){
			//ArrayList <SSpan> spansToDelete = new ArrayList <SSpan>();
			
			Iterator itSpanTokensHash = spanTokensHash.entrySet().iterator();
		    while (itSpanTokensHash.hasNext()) {
		        Map.Entry pair = (Map.Entry)itSpanTokensHash.next();
		        String spanId = (String) pair.getKey();
		        ArrayList <Object> spanTokensAL = (ArrayList) pair.getValue();
		        SSpan span = (SSpan) spanTokensAL.get(0);
		        spansToDelete.add(span);
		        //System.out.println("span.toString() line 562: " + span.toString());
		        ArrayList <SToken> tokensAL = (ArrayList <SToken>) spanTokensAL.get(1);
		        
		        int startOfSpan = 1000000000;
		        int endOfSpan = 0;
		        
		        for (SToken stoken: tokensAL){
		        	if (stoken.getId().indexOf("#sTok") > -1 ){
		        		String tokenId = stoken.getId();
		        		String endOfId = tokenId.substring(tokenId.lastIndexOf("#") + 1);
		        		//System.out.println("endOfId line 571: " + endOfId);
		        		try{
		        			List <Integer> startEndList = (List <Integer>) tokenIdStartEndHash.get(endOfId);
		        			Integer startTextRel = startEndList.get(0);
		        			//System.out.println("startTextRel.toString() line 576: " + startTextRel.toString());
		        			if (startTextRel.intValue() < startOfSpan){
		        				startOfSpan = startTextRel.intValue();
		        			}
		        			Integer endTextRel = startEndList.get(1);
		        			//System.out.println("endTextRel.toString() line 581: " + endTextRel.toString());
		        			if (endTextRel.intValue() > endOfSpan){
		        				endOfSpan = endTextRel.intValue();
		        			}

		        		}catch(Exception e){
		        			//System.out.println("exception line 587:");
		        			//System.out.println(e);
		        		}
		        	}
		        }
		        
		        //System.out.println("startOfSpan: " + String.valueOf(startOfSpan) + " endOfSpan: " + String.valueOf(endOfSpan));
		        
		        if (endOfSpan == 0 && startOfSpan == 1000000000){
		        	continue;
		        }
		        
		        ArrayList <SToken> tokensArrayForNewSpan = new ArrayList <SToken>();
		        
		        Iterator itTokenBeginHash = tokenBeginHash.entrySet().iterator();
		        while (itTokenBeginHash.hasNext()) {
			        Map.Entry pairT = (Map.Entry)itTokenBeginHash.next();
			        Integer tokenBeginPosition = (Integer) pairT.getKey();
			        ArrayList<Object> tokenBeginArray = (ArrayList<Object>) pairT.getValue();
			        SToken token = (SToken) tokenBeginArray.get(0);
			        Integer tokenEnd = (Integer) tokenBeginArray.get(1);		        
			        
			        if (tokenBeginPosition.intValue() >= startOfSpan && tokenEnd.intValue()<=endOfSpan){
			        	//System.out.println("will add token line 610: " + token.toString());
			        	tokensArrayForNewSpan.add(token);
			        }			        
		        }
		        
		        SSpan newSpan = docGraph.createSpan(tokensArrayForNewSpan); // here should be a list of STokens
	        	newSpan.createAnnotation(null, "new", "new");
	        	for (SAnnotation anno: span.getAnnotations()){
	        		if (!anno.getName().equals(attributeToIgnore)){
	        			//System.out.println("will add annotation line 619: " + anno.toString());
	        			try{
	        				newSpan.addAnnotation(anno);
	        			}catch(Exception e){
	        				//System.out.println("span.toString() line 623: " + span.toString());
	        			}
	        		}
	        	}
		    }
		    
		    spansToDelete.add(span);
		    
		    //Remove the old spans and their old tokens. Will also need to remove old tokens that had not been covered by any spans
		    /*
		    for (SSpan span : spansToDelete){
		    	System.out.println("span.toString() line 634 to delete: " + span.toString());
		    	List <SRelation> spanOutRelations = span.getOutRelations();
		    	try{
		    		for (SRelation srel: spanOutRelations){
		    			SNode spanTargetNode = (SNode) srel.getTarget();
		    			System.out.println("spanTargetNode.toString() line 638 to delete: " + spanTargetNode.toString());
		    			docGraph.removeNode(spanTargetNode);
		    			docGraph.removeRelation(srel);
		    		}
		    	}catch(Exception e){
		    		System.out.println("e line 644: " + e.toString());
		    	}
		    	docGraph.removeNode(span);
		    }
		    */
		}
		
		private void findTokens(){
			List <STextualRelation> listTextualRelations = docGraph.getTextualRelations();
			
			for (STextualRelation sTextRel: listTextualRelations){
				SToken token = sTextRel.getSource();
				//try{
					//if (token.getId().indexOf("#sTok") > -1 ){	// If it's a token
						Integer startTextRel = sTextRel.getStart();				
						Integer endTextRel = sTextRel.getEnd();
						List <Integer> startEndList = new ArrayList <Integer> ();
						startEndList.add(startTextRel);
						startEndList.add(endTextRel);
						//tagcodeStartEndHash.put(token.getId(), startEndList);
						String id = token.getId();
						String endOfId = id.substring(id.lastIndexOf("#") + 1);
						//System.out.println("endOfId line 637: " + endOfId);
						//System.out.println("startTextRel line 637: " + startTextRel.toString() + " endTextRel: " + endTextRel.toString());
						tokenIdStartEndHash.put(endOfId, startEndList);
					//}
				//}
				//catch(Exception e ){
					//System.out.println("e.toString(): " + e.toString());
				//}
			}
			
			List <SSpanningRelation> listSpanningRelations = docGraph.getSpanningRelations();
			for (SSpanningRelation sSpanningRel: listSpanningRelations){
				SToken tokenSpan = sSpanningRel.getTarget();
				
				List <SRelation> listTokenSpanInRelations = tokenSpan.getInRelations();
				for (SRelation tokenSpanInRel: listTokenSpanInRelations){
					SToken nodeTokenInTartget = (SToken) tokenSpanInRel.getTarget(); // SToken
					SSpan nodeTokenInSource = (SSpan) tokenSpanInRel.getSource(); //SSpan
					
					String spanId = nodeTokenInSource.getId();
					//System.out.println("spanId line 646: " + spanId);
					
					if (spanTokensHash.containsKey(spanId)) {
						ArrayList <Object> spanTokensAL = (ArrayList <Object>) spanTokensHash.get(spanId);
						ArrayList <SToken> tokensAL = (ArrayList <SToken>) spanTokensAL.get(1);
						tokensAL.add(nodeTokenInTartget);
						spanTokensAL.set(1, tokensAL);
						spanTokensHash.replace(spanId, spanTokensAL);

					} else {
						ArrayList <Object> spanTokensAL = new ArrayList <Object>();
						ArrayList <SToken> tokensAL = new ArrayList <SToken>();
						tokensAL.add(nodeTokenInTartget);
						spanTokensAL.add(nodeTokenInSource);
						spanTokensAL.add(tokensAL);
						
						spanTokensHash.put(spanId, spanTokensAL);
					}
				}
			}
			
			
		}
		
		/*
		 * Works only for spans that cover 1 token
		 * If a span covers more than 1 token, does not work
		 */
		private void fillTagcodeHashes2(){
			List <STextualRelation> listTextualRelations = docGraph.getTextualRelations();
			
			for (STextualRelation sTextRel: listTextualRelations){
				SToken token = sTextRel.getSource();
				try{
					//if (token.getId().indexOf("#sTok") ==-1 ){	// If it's not a token			
						Integer startTextRel = sTextRel.getStart();				
						Integer endTextRel = sTextRel.getEnd();
						List <Integer> startEndList = new ArrayList <Integer> ();
						startEndList.add(startTextRel);
						startEndList.add(endTextRel);
						//tagcodeStartEndHash.put(token.getId(), startEndList);
						String id = token.getId();
						String endOfId = id.substring(id.lastIndexOf("#") + 1);
						//tagcodeStartEndHash.put(endOfId, startEndList);
						
						if (tagcodeStartEndHash.containsKey(endOfId)) {
							System.out.println("endOfId already in hash: " + endOfId);
							System.out.println("startTextRel: " + String.valueOf(startTextRel));
							System.out.println("endTextRel: " + String.valueOf(endTextRel));
							
							List <Integer> startEndListFromPrevious = (List <Integer>) tagcodeStartEndHash.get(endOfId);
							List <Integer> newStartEndList = new ArrayList <Integer> ();
							newStartEndList.add(Math.min(startTextRel.intValue(), startEndListFromPrevious.get(0).intValue()), Math.max(endTextRel.intValue(), startEndListFromPrevious.get(1).intValue()));
							tagcodeStartEndHash.replace(endOfId, newStartEndList);
							System.out.println("newStartEndList: " + newStartEndList.toString());
						} else {
							System.out.println("endOfId not in hash: " + endOfId);
							System.out.println("startTextRel: " + String.valueOf(startTextRel));
							System.out.println("endTextRel: " + String.valueOf(endTextRel));
							
							tagcodeStartEndHash.put(endOfId, startEndList);
						}

					//}else{
						//sTextRelsToRemove.add(sTextRel);
						//sTokensToRemove.add(token);
					//}
				}catch(NullPointerException e){
					System.out.println("e.toString() line 577: " + e.toString());
					System.out.println("token.toString() line 578: " + token.toString());
					System.out.println("token.getId() line 579: " + token.getId());
					//docGraph.removeNode(token);
	        		//docGraph.removeRelation(sTextRel);
					sTextRelsToRemove.add(sTextRel);
					sTokensToRemove.add(token);
				}
			}
			
			
			List <SSpan> listsTranscrSpans = docGraph.getSpans();
			for (SSpan transcrSpan: listsTranscrSpans){
				System.out.println("transcrSpan.toString() line 568:" + transcrSpan.toString());
				System.out.println(transcrSpan.getId());
				try{
					String id = transcrSpan.getId();
					int lastIndexGrill = id.lastIndexOf("#")+1;
					int lastIndexUnderscore = id.lastIndexOf("_");
					if (lastIndexUnderscore > lastIndexGrill){
						System.out.println("id.substring(lastIndexGrill,lastIndexUnderscore): " + id.substring(lastIndexGrill,lastIndexUnderscore));
						tagcodeSpanHash.put(id.substring(lastIndexGrill,lastIndexUnderscore), transcrSpan);
					}else{
						System.out.println("id.substring(lastIndexGrill): " + id.substring(lastIndexGrill));
						tagcodeSpanHash.put(id.substring(lastIndexGrill), transcrSpan);
					
					}
				}catch(NullPointerException e){
					System.out.println("e.toString() line 601: " + e.toString());
					System.out.println("transcrSpan.toString() line 602: " + transcrSpan.toString());
					System.out.println("transcrSpan.getId() line 603: " + transcrSpan.getId());
				}
			}			
			
		}
		
		private void fillTagcodeHashes(){
			List <STextualRelation> listTextualRelations = docGraph.getTextualRelations();
			
			for (STextualRelation sTextRel: listTextualRelations){
				SToken token = sTextRel.getSource();
				try{
					System.out.println("token.getId() line 556: " + token.getId());
					System.out.println("token.toString() line 557: " + token.toString());
					SAnnotation tagcode = token.getAnnotation(annotationsUnifyingAttribute);
					System.out.println("token.getId() line 559: " + token.getId());
					
					//System.out.println("tagcode.getValue() line 551: " + tagcode.getValue());
					//System.out.println("tagcode.toString() line 552: " + tagcode.toString());				
					Integer startTextRel = sTextRel.getStart();				
					Integer endTextRel = sTextRel.getEnd();
					List <Integer> startEndList = new ArrayList <Integer> ();
					startEndList.add(startTextRel);
					startEndList.add(endTextRel);
					//tagcodeStartEndHash.put(token.getId(), startEndList);
					tagcodeStartEndHash.put(tagcode.getValue(), startEndList); // worked well, but takes only the first token
					
					// Remove the old tokens: removes the annotations, but not the tokens.
					// Annotations don't get transfered on the new tokens: don't know why
					//for (SLayer layer: token.getLayers()){
						//token.removeLayer(layer);
					//}
				}catch(NullPointerException e){
					System.out.println("e.toString() line 577: " + e.toString());
					System.out.println("token.toString() line 578: " + token.toString());
					System.out.println("token.getId() line 579: " + token.getId());
					//docGraph.removeNode(token);
	        		//docGraph.removeRelation(sTextRel);
					sTextRelsToRemove.add(sTextRel);
					sTokensToRemove.add(token);
				}
			}
			
			
			List <SSpan> listsTranscrSpans = docGraph.getSpans();
			for (SSpan transcrSpan: listsTranscrSpans){
				//System.out.println("transcrSpan.toString() line 568:");
				//System.out.println(transcrSpan.toString());
				try{
					System.out.println("transcrSpan.getId() line 593: " + transcrSpan.getId());
					SAnnotation tagcodeSpan = transcrSpan.getAnnotation("tagcode");
					System.out.println("transcrSpan.getId() line 595: " + transcrSpan.getId());
					System.out.println("transcrSpan.toString() line 596: " + transcrSpan.toString());
					//System.out.println("tagcodeSpan.getValue() line 572: " + tagcodeSpan.getValue());
					//System.out.println("tagcodeSpan.toString() line 573: " + tagcodeSpan.toString());
					tagcodeSpanHash.put(tagcodeSpan.getValue(), transcrSpan);
				}catch(NullPointerException e){
					System.out.println("e.toString() line 601: " + e.toString());
					System.out.println("transcrSpan.toString() line 602: " + transcrSpan.toString());
					System.out.println("transcrSpan.getId() line 603: " + transcrSpan.getId());
				}
			}			
			
		}
		
		
		private void transferSpansOnNewTokens(){
			Iterator itTagcodeStartEndHash = tagcodeStartEndHash.entrySet().iterator();
		    while (itTagcodeStartEndHash.hasNext()) {
		        Map.Entry pair = (Map.Entry)itTagcodeStartEndHash.next();
		        String tagcode = (String) pair.getKey();
		        ArrayList <Integer> startEndArray = (ArrayList) pair.getValue();
		        Integer startSpan = startEndArray.get(0);
		        Integer endSpan = startEndArray.get(1);
		        
		        //System.out.println("line 593. tagcode: " + tagcode + " startSpan: " + Integer.toString(startSpan)+ " endSpan: " + Integer.toString(endSpan));
		        
		        ArrayList <SToken> tokensArrayForNewSpan = new ArrayList <SToken>();
		        
		        Iterator itTokenBeginHash = tokenBeginHash.entrySet().iterator();
		        while (itTokenBeginHash.hasNext()) {
			        Map.Entry pairT = (Map.Entry)itTokenBeginHash.next();
			        Integer tokenBeginPosition = (Integer) pairT.getKey();
			        ArrayList<Object> tokenBeginArray = (ArrayList<Object>) pairT.getValue();
			        SToken token = (SToken) tokenBeginArray.get(0);
			        Integer tokenEnd = (Integer) tokenBeginArray.get(1);		        
			        
			        if (tokenBeginPosition.intValue() >= startSpan.intValue() && tokenEnd.intValue()<=endSpan.intValue()){
			        	//System.out.println("will add token line 606: " + token.toString());
			        	tokensArrayForNewSpan.add(token);
			        }			        
		        }
		        
		        try{
		        	SSpan span = (SSpan) tagcodeSpanHash.get(tagcode);
		        	//System.out.println("span for this tagcode line 613: " + span.toString());
		        	SSpan newSpan = docGraph.createSpan(tokensArrayForNewSpan); // here should be a list of STokens
		        	//newSpan.createAnnotation(null, "new", "new");
		        	for (SAnnotation anno: span.getAnnotations()){
		        		if (!anno.getName().equals(attributeToIgnore)){
		        			//System.out.println("will add annotation line 665: " + anno.toString());
		        			newSpan.addAnnotation(anno);
		        		}
		        	}
		        	
		        	spansToDelete.add(span);
		        	/*
		        	//Remove the tokens covered by the old span and the old span itself
		        	List <SRelation> spanOutRelations = span.getOutRelations();
		        	for (SRelation srel: spanOutRelations){
		        		SNode spanTargetNode = (SNode) srel.getTarget();
		        		docGraph.removeNode(spanTargetNode);
		        		docGraph.removeRelation(srel);
		        	}
		        	
		        	docGraph.removeNode(span);
		        	*/
		        	
		        }catch(Exception e){
		        	System.out.println("e.toString() line 620: " + e.toString());
					System.out.println("tagcode line 621: " + tagcode);
					SSpan newSpan = docGraph.createSpan(tokensArrayForNewSpan);
					newSpan.createAnnotation(null, tagcode, tagcode );
		        }
		    }
		}
		
		
		private void annotateGerman(){
			if (doTokenize){
				//creating a layer named tokens
				SLayer tokensLayer = SaltFactory.createSLayer();
				tokensLayer.setName("tokens");
				doc.addLayer(tokensLayer);				
				addOpenNLPTokens(tokensLayer);
			}
		}
		
		private void annotateEnglish(){
			// creates a StanfordCoreNLP object
			Properties englishStanfordProperties = new Properties();			
			englishStanfordProperties.setProperty("annotators", "tokenize, ssplit, pos, lemma");
			StanfordCoreNLP englishStanfordPipeline = new StanfordCoreNLP(englishStanfordProperties);
			// create an empty Annotation just with the given text
			Annotation englishAnnotation = new Annotation(documentTextString);
			// run all Annotators on this text
			englishStanfordPipeline.annotate(englishAnnotation);
			// Register the annotations in the Salt model
			addStanfordAnnotationsToSaltModel(englishAnnotation);	
		}
		
		private void annotateItalian(){
			if (doTokenize){
				//creating a layer named tokens
				SLayer tokensLayer = SaltFactory.createSLayer();
				tokensLayer.setName("tokens");
				doc.addLayer(tokensLayer);				
				addOpenNLPTokens(tokensLayer);
			}
			
		}
		
		/**
		 * prints out some information about document-structure
		 */
		@Override
		public DOCUMENT_STATUS mapSDocument() {
			language = (String) getProperties().getProperties().getProperty(CoreNLPManipulatorProperties.LANGUAGE);
			//System.out.println("language: " + language);
			
			if (language.toLowerCase().equals("inglese") || language.toLowerCase().equals("english") || language.toLowerCase().equals("eng")){
				language = "en";
			}else if (language.toLowerCase().equals("german") || language.toLowerCase().equals("dt") || language.toLowerCase().equals("tedesco") || language.toLowerCase().equals("deutsch") || language.toLowerCase().equals("deu")){
				language = "de";
			}
			else if (language.toLowerCase().equals("italiano") || language.toLowerCase().equals("ita") || language.toLowerCase().equals("italian") || language.toLowerCase().equals("italienisch")){
				language = "it";
			}

			doSentenceSplit = true;
			String sentenceSplitPropertyValue = (String) getProperties().getProperties().getProperty(CoreNLPManipulatorProperties.SentenceSplit);
			//System.out.println("sentenceSplitPropertyValue: " + sentenceSplitPropertyValue);
			if (sentenceSplitPropertyValue == null){
				sentenceSplitPropertyValue = "true";
			}else if (sentenceSplitPropertyValue.equals("false") || sentenceSplitPropertyValue.equals("0")){
				doSentenceSplit = false;
			}
			
			if (doSentenceSplit == true){
				doTokenize = true;
			}
			
			doTokenize = true;
			String tokenizePropertyValue = (String) getProperties().getProperties().getProperty(CoreNLPManipulatorProperties.Tokenize);
			//System.out.println("tokenizePropertyValue: " + tokenizePropertyValue);
			if (tokenizePropertyValue == null){
				tokenizePropertyValue = "true";
			}else if (tokenizePropertyValue.equals("false") || tokenizePropertyValue.equals("0")){
				doTokenize = false;
			}
			
			doPOStag = true;
			String posTagPropertyValue = (String) getProperties().getProperties().getProperty(CoreNLPManipulatorProperties.POStag);
			//System.out.println("posTagPropertyValue: " + posTagPropertyValue);
			if (posTagPropertyValue == null){
				doPOStag = true;
				doTokenize = true;
			}else if (posTagPropertyValue.equals("false") || posTagPropertyValue.equals("0")){
				doPOStag = false;
			}
			
			if (doPOStag == true){
				doTokenize = true;
			}
			
			doLemmatize = true;
			String lemmatizePropertyValue = (String) getProperties().getProperties().getProperty(CoreNLPManipulatorProperties.Lemmatize);
			//System.out.println("lemmatizePropertyValue: " + lemmatizePropertyValue);
			if (lemmatizePropertyValue == null){
				doLemmatize = true;
				doTokenize = true;
			}else if (lemmatizePropertyValue.equals("false") || lemmatizePropertyValue.equals("0")){
				doLemmatize = false;
			}
			if (doLemmatize == true){
				doTokenize = true;
			}
			
			attributeToIgnore = (String) getProperties().getProperties().getProperty(CoreNLPManipulatorProperties.AttributeToIgnore);
			if (attributeToIgnore == null){
				attributeToIgnore = "mode";
			}
			
			annotationsUnifyingAttribute = (String) getProperties().getProperties().getProperty(CoreNLPManipulatorProperties.AnnotationsUnifyingAttribute);
			if (annotationsUnifyingAttribute == null){
				annotationsUnifyingAttribute = "tagcode";
			}
			
			
			doc = getDocument();
			docGraph = doc.getDocumentGraph();
			// access all primary text nodes (getting a list)
			List<STextualDS> listTextualDSs = docGraph.getTextualDSs();
			sSequentialDS = listTextualDSs.get(0);
			documentTextString = sSequentialDS.getData().toString();
			
			tokenBeginHash = new HashMap();
			tagcodeStartEndHash = new HashMap();
			tagcodeSpanHash = new HashMap();
			spanTokensHash = new HashMap();
			tokenIdStartEndHash = new HashMap();
			
			sTextRelsToRemove = new ArrayList <STextualRelation>();
			sTokensToRemove = new ArrayList <SToken>();
			
			spansToDelete = new ArrayList <SSpan>();
			fillTagcodeHashes2();
			findTokens();
			
			if (language.equals("en")){
				annotateEnglish();
			}else if (language.equals("de")){
				annotateGerman();
			}else if (language.equals("it")){
				annotateItalian();
			}
			
			transferSpansOnNewTokens();
			
			transferNew();
			
			// Delete old spans and their tokens
			for (SSpan span : spansToDelete){
				try{
					System.out.println("span.toString() line 634 to delete: " + span.toString());
					List <SRelation> spanOutRelations = span.getOutRelations();
					try{
						for (SRelation srel: spanOutRelations){
							SNode spanTargetNode = (SNode) srel.getTarget();
							System.out.println("spanTargetNode.toString() line 638 to delete: " + spanTargetNode.toString());
							docGraph.removeNode(spanTargetNode);
							docGraph.removeRelation(srel);
						}
					}catch(Exception e){
						System.out.println("e line 644: " + e.toString());
					}
					docGraph.removeNode(span);
				}catch(Exception e){
					System.out.println(e.toString());
				}
		    }
			
			// Remove old tokens and their textual relations
			for (STextualRelation str: sTextRelsToRemove){
				try{
					docGraph.removeRelation(str);
				}catch(Exception e){
					System.out.println(e.toString());
				}
			}
			
			for (SToken st: sTokensToRemove){
				try{
					docGraph.removeNode(st);
				}catch(Exception e){
					System.out.println(e.toString());
				}
			}
			
			return (DOCUMENT_STATUS.COMPLETED);
		}
		
		/**
		 * tests Stanford Core NLP parser for German
		 */
		/*
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
		*/
		
		/**
		 * tests Open NLP parser for Italian
		 */
		/*
		public void testStanfordItalianOpenNLP(){
			
			try (InputStream modelIn = CoreNLPManipulator.class.getResourceAsStream("/models/it-sent.bin")) {
				  SentenceModel model = new SentenceModel(modelIn);
				  
				  SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
				  
				  String sentences[] = sentenceDetector.sentDetect(" Una frase in italiano. Cosa potrei scrivere? Non so. ");
				  
				  for (String sentence : sentences) {
			            System.out.println(sentence);
			       }
				  
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
		/**
		 * tests Stanford Core NLP parser for Italian
		 */
		/**
		public void testStanfordItalianTint(){
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

			// Load the models
			pipelineTint.load();

			// Use for example a text in a String
			String textItalian = "I topi non avevano nipoti.";

			// Get the original Annotation (Stanford CoreNLP)
			//Annotation stanfordAnnotation = pipelineTint.runRaw(textItalian);

			//System.out.println(textItalian);
			//System.out.println(stanfordAnnotation);

			InputStream stream;
			ByteArrayOutputStream baos;
			
			try{			
				stream = new ByteArrayInputStream(textItalian.getBytes(StandardCharsets.UTF_8));
				baos = new ByteArrayOutputStream();
				pipelineTint.run(stream, baos, TintRunner.OutputFormat.READABLE);
				System.out.println(baos.toString());
			}
			catch (IOException e){
				System.out.println(e.toString());
			}

			// **or**

			// Get the JSON // (optionally getting the original Stanford CoreNLP Annotation as return value)
			//InputStream stream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
			//Annotation annotation = pipeline.run(stream, System.out, TintRunner.OutputFormat.READABLE); // http://tint.fbk.eu/apidocs/eu/fbk/dh/tint/runner/TintRunner.OutputFormat.html
		}
*/

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
			
			/**
			// creates a StanfordCoreNLP object, with POS tagging,
			// lemmatization, NER, parsing, and coreference resolution
			Properties props = new Properties();
			//props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
			props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse");
			//props.setProperty("annotators", "tokenize, ssplit, pos, lemma"); // When I asked for more functionalities (ner, for example, I got an out of memory erro)
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
			ArrayList<String> treeList = new ArrayList<String>();
			//ArrayList<String> namedList = new ArrayList<String>();

			for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
				// get the tree for the sentence
				Tree tree = sentence.get(TreeAnnotation.class);
				treeList.add(tree.toString());
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
			*/
			/**
			System.out.println("");
			System.out.println("Input text: " + text);
			System.out.println("Parsed text:");
			System.out.println(textList);
			System.out.println("Parts of Speach:");
			System.out.println(posList);
			System.out.println("Lemma Annotation:");
			System.out.println(lemmaList);
			System.out.println("parse Annotation:");
			System.out.println(treeList);
			*/
			//System.out.println("Named Entity:");
			//System.out.println(namedList);

		}
		
		private void transferOldAnnotationsToNewTokens(){
			Set <SLayer> layersSet = docGraph.getLayers();
			for (SLayer layer: layersSet){
				System.out.println("layer.toString():");
				System.out.println(layer.toString());
				Set <SNode> listOfLayerNodes = layer.getNodes();
				for (SNode n: listOfLayerNodes){
					System.out.println("n.toString() line 543: "+n.toString());
				}
			}
			//docGraph.getTokens().get(0);
			//docGraph.getTextualRelations().get(0);
			//STextualRelation rel = new TextualRelation(); // contient le debut et la fin: entre token et TextDS
			
			// Gives the new tokens
			List <STextualRelation> listTextualRelations = docGraph.getTextualRelations();
			for (STextualRelation sTextRel: listTextualRelations){
				System.out.println("sTextRel.toString() line 553: "+sTextRel.toString());
				//System.out.println(sTextRel.toString());
				
				SToken tokenTextRel = sTextRel.getSource();  // SToken with lemma and pos
				System.out.println("tokenTextRel.toString() line 557: "+tokenTextRel.toString());
				//System.out.println(tokenTextRel.toString());
				
				Integer startTextRel = sTextRel.getStart();
				System.out.println("startTextRel.toString() line 561: "+startTextRel.toString());
				System.out.println(startTextRel.toString()); // Gives the position in the document string
				
				Integer endTextRel = sTextRel.getEnd();
				System.out.println("endTextRel.toString() line 565: "+endTextRel.toString());
				//System.out.println(endTextRel.toString()); // Gives the position in the document string
				/*
				DataSourceSequence dss = new DataSourceSequence();
				dss.setStart(startTextRel);
				dss.setEnd(endTextRel);
				List <SNode> nodesBySequence = docGraph.getNodesBySequence(dss);
				for (SNode nbS: nodesBySequence){
					System.out.println("nbS.toString() line 573: "+nbS.toString());
				}
				*/
			}
			
			/*
			List <SSpanningRelation> listSpanningRelations = docGraph.getSpanningRelations();
			for (SSpanningRelation sSpanningRel: listSpanningRelations){
				boolean notInteresting = false;
				//System.out.println("sSpanningRel.toString():");
				//System.out.println(sSpanningRel.toString());
				SSpan span = sSpanningRel.getSource();
				Set<SAnnotation> sAnnotations = span.getAnnotations();
				if (sAnnotations != null) {
					for (SAnnotation anno : sAnnotations) {
						if ("sentence".equals(anno.getName())) {
							notInteresting = true;
						}
					}
				}
				
				if (notInteresting){
					continue;
				}
				
				SToken tokenSpan = sSpanningRel.getTarget();
				System.out.println("span.toString() line 598:");
				System.out.println(span.toString());
				System.out.println("tokenSpan.toString() line 600:");
				System.out.println(tokenSpan.toString());
				
				List <SToken> listsOfOverlappedTokens = docGraph.getOverlappedTokens(tokenSpan);
				
				docGraph.getSortedTokenByText();
				
				List <SSpan> listsTranscrSpans = docGraph.getSpans(); // to do before the second tokenization
				for (SSpan transcrSpan: listsTranscrSpans){
					System.out.println("transcrSpan.toString() line 609:");
					System.out.println(transcrSpan.toString());
				}
				
				List <SRelation> listTokenSpanOutRelations = tokenSpan.getOutRelations();
				for (SRelation tokenSpanOutRel: listTokenSpanOutRelations){					
					System.out.println("tokenSpanOutRel.getName() line 615: " + tokenSpanOutRel.getName());
					//System.out.println("tokenSpanOutRel.getType(): " + tokenSpanOutRel.getType()); // type will be null
					//Node nodeTokenOutTartget = tokenSpanOutRel.getTarget(); // Will be the whole text SNAME=sText1
					//System.out.println("nodeTokenOutTartget.toString(): " + nodeTokenOutTartget.toString());
					Node nodeTokenOutSource = tokenSpanOutRel.getSource();
					System.out.println("nodeTokenOutTartget.toString() line 620: " + nodeTokenOutSource.toString());
				
				}
				
				
				List <SRelation> listTokenSpanInRelations = tokenSpan.getInRelations();
				for (SRelation tokenSpanInRel: listTokenSpanInRelations){
					//System.out.println("tokenSpanInRel.getType() line 627: " + tokenSpanInRel.getType());
					Node nodeTokenInTartget = tokenSpanInRel.getTarget(); // SToken
					System.out.println("nodeTokenInTartget.toString() line 629: " + nodeTokenInTartget.toString());
					Node nodeTokenInSource = tokenSpanInRel.getSource(); //SSpan
					System.out.println("nodeTokenInSource.toString() line 631: " + nodeTokenInSource.toString());
				}
				
			}
			*/
			
			
			/*
			List <SSpan> spansList = docGraph.getSpans();			
			for (SSpan span: spansList){
				System.out.println("span.toString():");
				System.out.println(span.toString());
				Set<SAnnotation> sAnnotations = span.getAnnotations();
				if (sAnnotations != null) {
					for (SAnnotation anno : sAnnotations) {
						// TODO support mappings of resolver vis map
						if ("tagcode".equals(anno.getName())) {
							
						}
					}
				}
			}
			*/
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

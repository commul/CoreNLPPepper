package CoreNLPPepper.CoreNLPPepper;

import java.util.ArrayList;
import java.util.List;

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;

public class CoreNLPManipulatorProperties extends PepperModuleProperties{
	
	public final static String LANGUAGE = "Language";
	public final static String SentenceSplit = "SentenceSplit";
	public final static String Tokenize = "Tokenize";
	public final static String POStag = "POStag";
	public final static String Lemmatize = "Lemmatize";
	public final static String AttributeToIgnore = "AttributeToIgnore";
	public final static String AnnotationsUnifyingAttribute = "annotationsUnifyingAttribute";
	
	/**
	 * Add the property AnnotationsUnifyingAttribute to the list of CoreNLPManipulator properties.
	 */
	public CoreNLPManipulatorProperties(){
		this.addProperty(new PepperModuleProperty<String>(LANGUAGE, String.class, "The language of the text to be treated. Accepts 3 values: en, de, it. Defaults to en."));
		this.addProperty(new PepperModuleProperty<String>(SentenceSplit, String.class, "If set to true, splits the text into sentences. Default true."));
		this.addProperty(new PepperModuleProperty<String>(Tokenize, String.class, "If set to true, tokenizes the text. Default true."));
		this.addProperty(new PepperModuleProperty<String>(POStag, String.class, "If set to true, POS tags the text. Default true."));
		this.addProperty(new PepperModuleProperty<String>(Lemmatize, String.class, "If set to true, lemmatize the text. Default true."));
		this.addProperty(new PepperModuleProperty<String>(AttributeToIgnore, String.class, "Attribute of existing span annotations to ignore. Default mode."));
		this.addProperty(new PepperModuleProperty<String>(AnnotationsUnifyingAttribute, String.class, "Attribute of existing span annotations that need to be transfered to the new tokens, which uniquely characterises each of the existent annotations. Default: 'tagcode'"));
	}
	
	/**
	 * Returns the value of the property Language.
	 * 
	 * @return String the language of the text
	 */
	public String getLanguageProperty(){
		return((String)this.getProperty("Language").getValue());
	}
	
	/**
	 * Checks if the name of the Language entered by the user is a valid name of one of the languages treated by this module: English, German or Italian.
	 */
	public boolean checkLanguageProperty(PepperModuleProperty<?> prop){
		//calls the check of constraints in parent,
		//for instance if a required value is set
		super.checkProperty(prop);
		if ("Language".equals(prop.getName())){
			String propertyValue = (String)prop.getValue();
			
			List<String> possibleLanguageNames = new ArrayList<>();
			possibleLanguageNames.add("en");
			possibleLanguageNames.add("eng");
			possibleLanguageNames.add("english");
			possibleLanguageNames.add("inglese");
			possibleLanguageNames.add("de");
			possibleLanguageNames.add("dt");
			possibleLanguageNames.add("deu");
			possibleLanguageNames.add("deutsch");
			possibleLanguageNames.add("german");
			possibleLanguageNames.add("tedesco");
			possibleLanguageNames.add("it");
			possibleLanguageNames.add("ita");
			possibleLanguageNames.add("italiano");
			possibleLanguageNames.add("italian");
			possibleLanguageNames.add("italienisch");
			
			Boolean userValueNotInList = true;
			
			for(String lang: possibleLanguageNames){
				if ( propertyValue.toLowerCase().equals(lang) ){
					userValueNotInList = false;
				}
			}

			if (userValueNotInList == true){
				throw new PepperModuleException("The language name is incorrect. Choose between en, de and it.");
			}
		
		}
	return(true);
	}
	
	/**
	 * Returns the value of the property SentenceSplit.
	 * 
	 * @return String "true" or "false"
	 */
	public String getSentenceSplitProperty(){
		return((String)this.getProperty("SentenceSplit").getValue());
	}
	
	/**
	 * Checks if the name of the Language entered by the user is a valid name of one of the languages treated by this module: English, German or Italian.
	 * Possible values of the propertyName parameter: "SentenceSplit", "Tokenize", "POStag"
	 */
	public boolean checkTrueFalseProperty(PepperModuleProperty<?> prop, String propertyName){
		//calls the check of constraints in parent,
		//for instance if a required value is set
		super.checkProperty(prop);
		if (propertyName.equals(prop.getName())){
			String propertyValue = (String)prop.getValue();
			
			List<String> possiblePropertyNames = new ArrayList<>();
			possiblePropertyNames.add("true");
			possiblePropertyNames.add("false");
			possiblePropertyNames.add("1");
			possiblePropertyNames.add("0");
			
			Boolean userValueNotInList = true;
			
			for(String ppn: possiblePropertyNames){
				if ( propertyValue.toLowerCase().equals(ppn) ){
					userValueNotInList = false;
				}
			}

			if (userValueNotInList == true){
				throw new PepperModuleException("The value of the property " + propertyName + " is incorrect. It has to by either true or false.");
			}
		
		}
	return(true);
	}
	
	/**
	 * Returns the value of the property Tokenize.
	 * 
	 * @return String "true" or "false"
	 */
	public String getTokenizeProperty(){
		return((String)this.getProperty("Tokenize").getValue());
	}
	
	/**
	 * Returns the value of the property POStag.
	 * 
	 * @return String "true" or "false"
	 */
	public String getPOStagProperty(){
		return((String)this.getProperty("POStag").getValue());
	}
	
	/**
	 * Returns the value of the property Lemmatize.
	 * 
	 * @return String "true" or "false"
	 */
	public String getLemmatizeProperty(){
		return((String)this.getProperty("Lemmatize").getValue());
	}

}

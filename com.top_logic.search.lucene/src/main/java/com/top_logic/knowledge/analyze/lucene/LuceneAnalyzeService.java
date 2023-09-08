/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.analyze.lucene;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.convert.FormatConverterFactory;
import com.top_logic.convert.converters.FormatConverter;
import com.top_logic.convert.converters.FormatConverterException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.dsa.ex.UnknownDBException;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.analyze.AnalyzeException;
import com.top_logic.knowledge.analyze.AnalyzeService;
import com.top_logic.knowledge.analyze.DefaultAnalyzeService;
import com.top_logic.knowledge.analyze.KnowledgeObjectResult;
import com.top_logic.knowledge.analyze.KnowledgeObjectResultImpl;
import com.top_logic.knowledge.indexing.lucene.LuceneIndex;
import com.top_logic.knowledge.objects.KOAttributes;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;

/**
 * Lucene implementation of the {@link AnalyzeService}.
 *
 * @author Dieter Rothbächer
 */
@ServiceDependencies({LuceneIndex.Module.class, FormatConverterFactory.Module.class, MimeTypes.Module.class})
public class LuceneAnalyzeService extends DefaultAnalyzeService<LuceneAnalyzeService.Config> {

	private static final boolean DEBUG = Logger.isDebugEnabled(LuceneAnalyzeService.class);
	
    /** The configuration key for the property maxcount of document finder */
    public static final String PROP_DF_MAXCOUNT  = "DefaultMaxCountDocFinder";
    
    /** The configuration key for the property threshold of document finder */
    public static final String PROP_DF_THRESHOLD = "DefaultThresholdDocFinder";

    /** The configuration key for the property maxcount of feature extractor */
    public static final String PROP_FE_MAXCOUNT  = "DefaultMaxCountFeatureExtractor";
    
    /** The configuration key for the property threshold of feature extractor */
    public static final String PROP_FE_THRESHOLD = "DefaultThresholdFeatureExtractor";

    /** The token type for words/terms in a Lucene document */
    public static final String WORD_TOKENTYPE    = "<ALPHANUM>";
    
    /** The minimum number of characters that a word/term should consist of */
    public static final int WORD_MINSIZE         = 2;

    /** To be used with search. */
    public static final boolean USE_AND          = true;

    /** To be used with search. */
    public static final boolean USE_OR           = false;

    /** To be used with similarity search to specify how many key words are used. */
    public static final int MAX_SIMLIAR_TERMS    = 10;

    /** The Lucene index */   
    private final LuceneIndex index;

	/**
	 * Configuration for {@link LuceneAnalyzeService}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends DefaultAnalyzeService.Config<LuceneAnalyzeService> {
		/**
		 * Default maximal count feature extractor.
		 */
		int getDefaultMaxCountFeatureExtractor();

		/**
		 * Threshold feature extractor.
		 */
		double getDefaultThresholdFeatureExtractor();

		/**
		 * Default maximal count document finder.
		 */
		int getDefaultMaxCountDocFinder();

		/**
		 * Threshold feature document finder.
		 */
		double getDefaultThresholdDocFinder();
	}


    /**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link LuceneAnalyzeService}.
	 */
	public LuceneAnalyzeService(InstantiationContext context, Config config) {
		super(context, config);

        this.index = LuceneIndex.getInstance();
    }

    /**
     * @see com.top_logic.knowledge.analyze.AnalyzeService#serviceAvailable()
     */
    @Override
	public boolean serviceAvailable() {
        return true;
    }

    /**
     * @see com.top_logic.knowledge.analyze.AnalyzeService#findSimilar(KnowledgeObject)
     */
    @Override
	public Collection<? extends KnowledgeObject> findSimilar(KnowledgeObject document) 
        throws AnalyzeException {

		if (DEBUG) {
			String message = "Finding documents similar to: " + document.getObjectName();
			Logger.debug(message, LuceneAnalyzeService.class);
		}
		int maxCount = getConfig().getDefaultMaxCountDocFinder();
        
        Collection<String> keywords = this.extractKeywords(document);
        Iterator<String> theIter = keywords.iterator();
        Collection<KnowledgeObject> theResults = new HashSet<>();
        int theCounter = 0;
        while (theIter.hasNext() && (theCounter < MAX_SIMLIAR_TERMS)) {
            String theTerm = theIter.next();    
            theResults.addAll(this.search(theTerm, maxCount));
            theCounter++;
        }

        // Retain at most maxCount results
        if (theResults.size() > maxCount) {
            Set<KnowledgeObject> theNewResult = new HashSet<> (maxCount);
            Iterator<KnowledgeObject> theResIt = theResults.iterator();
			while (theResIt.hasNext() && (theNewResult.size() < maxCount)) {
                KnowledgeObject theRes = theResIt.next();
                theNewResult.add (theRes);
            }
            theResults = theNewResult;
        }
        
		if (DEBUG) {
			String message = "Similar documents: " + theResults;
			Logger.debug(message, LuceneAnalyzeService.class);
		}
        return theResults;

    }
    
	@Override
	public Collection<? extends KnowledgeObjectResult> findSimilarRanked(KnowledgeObject aDoc) throws AnalyzeException {
		if (DEBUG) {
			String message = "Finding documents similar ranked as: " + aDoc.getObjectName();
			Logger.debug(message, LuceneAnalyzeService.class);
		}
		int theMaxCount = getConfig().getDefaultMaxCountDocFinder();
		List<String> keyWords = this.extractKeywords(aDoc);
		Set<KnowledgeObject> similarKOs = new TreeSet<>(KnowledgeObjectComparator.INSTANCE);
		
		IndexSearcher theSearcher = this.index.getSearcher();
		while ((similarKOs.size() < theMaxCount) && !keyWords.isEmpty()) {
			similarKOs.addAll(this.searchWordList(keyWords, theMaxCount, theSearcher));

			keyWords.remove(keyWords.size() - 1);
		}

		int theCount = similarKOs.size();
		int theMax = theCount;
		List<KnowledgeObjectResult> result = new ArrayList<>();

		for (KnowledgeObject ko : similarKOs) {
			if (ko != aDoc) {
				KnowledgeObjectResultImpl koResult = new KnowledgeObjectResultImpl(ko, (double) theCount / theMax);
				result.add(koResult);
				theCount--;
			}
		}

		if (DEBUG) {
			String message = "Similar ranked documents are: " + result;
			Logger.debug(message, LuceneAnalyzeService.class);
		}
		return (result);
	}

    private static final class KnowledgeObjectComparator implements Comparator<KnowledgeObject> {
    	
    	public static final	KnowledgeObjectComparator INSTANCE = new KnowledgeObjectComparator();
		
		private KnowledgeObjectComparator() {
			// singleton instance
		}

        @Override
		public int compare(KnowledgeObject arg0, KnowledgeObject arg1) {
			return KBUtils.compareIds(arg0.getObjectName(), arg1.getObjectName());
        }
    }

    /**
     * Search for Lucene documents containing the given words.
     * 
     * @param    aWordList   The list of words to be searched for
     * @param    aCounter    The number of documents requested
     * @param    aSearcher   The Lucene searcher to be used for searching
     * 
     * @return   a collection of Lucene documents matching the query
     */
    private List<KnowledgeObject> searchWordList(List<?> aWordList, int aCounter, IndexSearcher aSearcher) {
		if (DEBUG) {
			String message = "Searching for " + aCounter + " documents containing the words: " + aWordList;
			Logger.debug(message, LuceneAnalyzeService.class);
		}
        StringBuilder theTerm = new StringBuilder();

        for (Iterator<?> theIt = aWordList.iterator(); theIt.hasNext(); ) {
            theTerm.append(theIt.next());
            theTerm.append(StringServices.BLANK_CHAR);
        }

        try {
            Query theQuery = this.getQueryForExpression(theTerm.toString(), USE_AND);

			List<KnowledgeObject> results = search(theQuery, aCounter, aSearcher);
			if (DEBUG) {
				Logger.debug("Search results: " + results, LuceneAnalyzeService.class);
			}
			return results;
        }
        catch (Exception ex) {
            Logger.error("Unable to search for " + theTerm, ex, this);

            return (Collections.emptyList());
        }
    }

//
//        Iterator   theIter     = theWords.iterator();
//        Collection theResults  = new HashSet();
//        int        theCounter  = 0;
//
//        while (theIter.hasNext() && (theCounter < MAX_SIMLIAR_TERMS) && (theResults.size() < theMaxCount)) {
//            String theTerm = (String)theIter.next();    
//            theResults.addAll(this.searchRanked(theTerm, theMaxCount));
//            theCounter++;
//        }
//        
//        // There should be a better algorithm that takes into account the relevance of the hits
//        // and how many keywords they contain... 
//        // Same for the non-ranked method above...
//        
//        // Retain at most maxCount results        
//        if (theResults.size() > theMaxCount) {
//            Set theNewResult = new HashSet (theMaxCount);
//            Iterator theResIt = theResults.iterator();
//            int i=0;
//            while (theResIt.hasNext() && (i <theMaxCount)) {
//                Object theRes = theResIt.next();
//                theNewResult.add (theRes);
//            }
//            theResults = theNewResult;
//        }
//        
//        return theResults;
//
//    }

    /**
     * @see com.top_logic.knowledge.analyze.AnalyzeService#extractKeywords(KnowledgeObject)
     */
    @Override
	public List<String> extractKeywords (KnowledgeObject document) throws AnalyzeException {
		if (DEBUG) {
			String message = "Extracting keywords from document: " + document.getObjectName();
			Logger.debug(message, LuceneAnalyzeService.class);
		}
		int maxCount = getConfig().getDefaultMaxCountFeatureExtractor();
		double threshold = getConfig().getDefaultThresholdFeatureExtractor();

        // retrieve the document text
        String theContent = this.getFilteredContent(this.getPhysicalResource(document));
        StringReader contentReader = new StringReader(theContent);
		HashMap<String, Integer> countByKeyword = this.findKeywordsCounted(contentReader);
		int wordCount = 0;
		for (Integer keywordCount : countByKeyword.values()) {
			wordCount += keywordCount;
		}
    
		Set<Entry<String, Integer>> theEntries = countByKeyword.entrySet(); // set of Map.Entry

        // convert collection into a list. In the future it could be made more efficient
		ArrayList<Entry<String, Integer>> theEntryList = new ArrayList<>(theEntries);

        // Sort the collection of words in the order of their freq. count
        Collections.sort(theEntryList, DescendingIntegerHolderComparator.INSTANCE);

        int theListSize = theEntryList.size();
        if (theListSize > maxCount) {
            theListSize = maxCount;
        }
        ArrayList<String> theKeywords = new ArrayList<>();
        for (int i = 0; i < theListSize; i++) {
			Integer count = theEntryList.get(i).getValue();
			double weight = (double) count / wordCount; // in threshold measure
            if (weight < threshold) {
                // the weight of the term is under the threshold, 
                // stop extracting keywords
                break;
            }
            theKeywords.add(theEntryList.get(i).getKey());
        }

		if (DEBUG) {
			String message = "Extracted keywords from document " + document.getObjectName() + " are: " + theKeywords;
			Logger.debug(message, LuceneAnalyzeService.class);
		}
        return theKeywords;
    }

    /** 
     * Extracts features out of a given text.
     *
     * @param aReader   The reader of words in the document
     *
     * @return  The features/terms with the count.
     * @throws AnalyzeException in case of an error
     */
	protected HashMap<String, Integer> findKeywordsCounted(Reader aReader) throws AnalyzeException {

		HashMap<String, Integer> theHashMap = new HashMap<>();
        
        // building the analyzer
        Analyzer theAnalyzer = LuceneIndex.getInstance().getDefaultDocumentAnalyzer();
		try (TokenStream theTokenizer = theAnalyzer.tokenStream(LuceneIndex.FIELD_CONTENTS, aReader)) {
			theTokenizer.reset();
            while (theTokenizer.incrementToken()) {
				final String theWord = theTokenizer.getAttribute(CharTermAttribute.class).toString();
            	final String theType = theTokenizer.getAttribute(TypeAttribute.class).type();
                // if current token is a word it's ok, otherwise continue with 
                // next token
                if (!(theType.equals(WORD_TOKENTYPE)) || theWord.length() < WORD_MINSIZE) {
                    continue;
                }
                
                // token has already occured in document
				if (theHashMap.containsKey(theWord)) {
                    // increment counter
					theHashMap.put(theWord, theHashMap.get(theWord) + 1);
                }
                // new token
                else {
                    // set counter for the first occurence of a word
					theHashMap.put(theWord, 1);
                }
            }
			theTokenizer.end();
            
            return theHashMap;
        }
        catch (IOException ioe) {
            throw new AnalyzeException(ioe.toString());
        }
    }

    /**
     * Search documents satisfying specific conditions described by an expression
     *
     * @param expression - the keyword to look for
     * @param maxCount - the maximum number of hits returned
     *
     * @return  A collection of knowledge objects. Empty collection
     *          in case nothing was found.
     *
     * @throws AnalyzeException in case of an error
     */
    protected Collection<? extends KnowledgeObject> search(String expression, int maxCount)
        throws AnalyzeException {

        try {
			if (DEBUG) {
				String message = "Finding " + maxCount + " documents with expression: " + expression;
				Logger.debug(message, LuceneAnalyzeService.class);
			}
            Query parserQuery = this.getQueryForExpression(expression, USE_OR);
			List<KnowledgeObject> result = search(parserQuery, maxCount);

			if (DEBUG) {
				String message = "Documents found: " + result;
				Logger.debug(message, LuceneAnalyzeService.class);
			}
			return result;
        }
        catch (ParseException pe) {
            Logger.error("Could not parse the search expression.", pe, this);
            throw new AnalyzeException(pe.toString());
        } 
        catch (IOException ioe) {
            Logger.error("Could not parese the search expression.", ioe, this);
            throw new AnalyzeException(ioe.toString());
        }
    }

    /**
     * Search documents satisfying specific conditions described by an expression
     *
     * @param expression - the keyword to look for
     * @param maxCount - the maximum number of hits returned
     *
     * @return  A collection of {@link KnowledgeObjectResult}s
     *          Empty collection in case nothing was found.
     *
     * @throws AnalyzeException in case of an error
     */
    protected Collection<? extends KnowledgeObjectResult> searchRanked(String expression, int maxCount)
        throws AnalyzeException {
        
        Collection<KnowledgeObjectResult> rankedResults = new ArrayList<>();    
        Collection<? extends KnowledgeObject> unrankedResults = this.search(expression, maxCount);
        for (KnowledgeObject ko : unrankedResults) {
            KnowledgeObjectResultImpl theResult = new KnowledgeObjectResultImpl(ko, 1.0d);
            rankedResults.add(theResult);
        }
        return rankedResults;
    }

    /**
     * retrieves the filepath and name (= physical resource) of the object
     *
     * @param kObj the knowledge object whose filepath is to be retrieved
     *
     * @throws AnalyzeException in case of an error
     */
    private String getPhysicalResource(KnowledgeObject kObj)
        throws AnalyzeException {
            
        try {
            // return the physical resource of the object
            return (String) kObj.getAttributeValue(KOAttributes.PHYSICAL_RESOURCE);
        }
        catch (NoSuchAttributeException noattr) {
            AnalyzeException ex = new AnalyzeException(
                                              KOAttributes.PHYSICAL_RESOURCE
                                              + " is not an attribute of "
                                              + kObj.getClass().getName()
                                            );
            Logger.error("getPhysicalResource: " + ex.toString(), noattr, this);
            throw ex;
        }
    }

	/**
	 * Retrieves and filters the content of a resource
	 * 
	 * @param aDocumentURL
	 *        the physical resource of a knowledge object
	 * 
	 * @throws AnalyzeException
	 *         in case of an error
	 */
	private String getFilteredContent(String aDocumentURL) throws AnalyzeException {

		String mimeType = MimeTypes.getInstance().getMimeType(aDocumentURL);
		FormatConverterFactory converterFactory = FormatConverterFactory.getInstance();
		FormatConverter converter = converterFactory.getFormatConverter(mimeType);

		String content;
		if (converter != null) {
			try {
				content = getConvertedContent(aDocumentURL, converter, mimeType);
			}
			catch (Exception ex) {
				String theMessage = "Unable to filter content of resource '" + aDocumentURL + '\'';
				
				if (ex instanceof FormatConverterException) {
					Logger.warn(theMessage + " (reason is: " + ex.getMessage() + ")!", this);
				}
				else {
					Logger.warn(theMessage + "!", ex, this);
				}
				// If an error occured and UseSimpleFilter is set, try it...
				content =  getSimpleContent(aDocumentURL);
			}
		} else {
			content = getSimpleContent(aDocumentURL);
		}

		if (content == null) {
			Logger.warn("FileContent was null, returning empty String...", this);
			return "";
		}
		content = this.parseStringContent(content);

		return content;
	}

	private String getConvertedContent(String documentURL, FormatConverter converter, String mimeType)
			throws DatabaseAccessException, UnknownDBException, IOException {
		try (InputStream input = new DataAccessProxy(documentURL).getEntry()) {
			try (Reader theReader = converter.convert(input, mimeType)) {
				try (StringWriter writer = new StringWriter()) {
					StreamUtilities.copyReaderWriterContents(theReader, writer);
					return writer.getBuffer().toString();
				}
			}
		}
	}

	private String getSimpleContent(String documentURL) {
		if (DEBUG) {
			Logger.debug("Using simple filter for " + documentURL, LuceneAnalyzeService.class);
		}
		try {
			try (InputStream in = new DataAccessProxy(documentURL).getEntry()) {
		    	return this.readFileContent(in);
			}
		}
		catch (Exception ex) {
		    Logger.warn("getFilteredContent (simple filter): " + ex, LuceneAnalyzeService.class);
		    throw new AnalyzeException("getSimpleContent: " + ex);
		}
	}

    /**
     * retrieves the content of the object (a file).
     *
     * @param theIStream the input extracted from some knowledge object
     *
     * @return The contents of the file excluding null-charcters.
     *
     * @throws AnalyzeException in case of an error
     */
    private String readFileContent(InputStream theIStream)
        throws AnalyzeException {
            
        String theFileContent = null;
        InputStreamReader theReader = null;
        StringWriter theWriter = null;

        try {
            theReader = new InputStreamReader(theIStream);
            theWriter = new StringWriter();

            char theChar;
            int theData;

            // Read the source, filtering out non-printable characters
            while ((theData = theReader.read()) != -1) {
                if ( ((theData >= 32) && (theData < 128)) || ((theData >= 160) && (theData < 256)) ) {
                    theChar = (char) theData;
                }
                else {
                    theChar = ' ';
                }

                theWriter.write(theChar);
            }

            theWriter.flush();
        }
        catch (Exception ex) {
            Logger.error("Couldn't read : " + ex, ex, this);
            throw new AnalyzeException("Couldn't read " + ex);
        }
        finally {
            if (theReader != null) {
                try {
                    theReader.close();
                }
                catch (IOException ex) {
                    Logger.error("Could not close the InputStreamReader.", ex, this);
                }
            }

            if (theWriter != null) {
                try {
                    theWriter.close();
                }
                catch (IOException ex) {
                    Logger.error("Could not close the StringWriter.", ex, this);
                }
            }
        }

        theFileContent = theWriter.toString();

        return theFileContent;
    }
    
    /**
     * Parse the given String to filter non-printable characters.
     *
     * @param   aString     the String to be filtered
     */
    private String parseStringContent(String aString) {
        
        char theChar;
        int theData;
        int theLength = aString.length();
        StringBuffer theBuffer = null;

        // Search for non-printable characters and replace them
        for (int i = 0; i < theLength; i++) {
            theChar = aString.charAt(i);
            theData = theChar;
            if ( !(((theData >= 32) && (theData < 128)) || ((theData >= 160) && (theData < 256))) ) {
                // Lazy init for StringBuffer
                //  (no init if no chars have to be replaced)
                if (theBuffer == null) {
                    theBuffer = new StringBuffer(aString);
                }
                theBuffer.setCharAt(i, ' ');
            }
        }

        if (theBuffer != null) {
            return theBuffer.toString();
        }
        else {
            return aString;
        }
    }

    /**
     * Searches for documents matching a query
     *
     * @param expression    The keyword to look for
     * @param maxCount      The maximum number of hits returned
     *
     * @return  A collection of knowledge objects. Empty collection
     *          in case nothing was found.
     *
     * @throws AnalyzeException in case of an error
     */
    private synchronized List<KnowledgeObject> search(Query expression, int maxCount) throws AnalyzeException {
        IndexSearcher theSearcher = this.index.getSearcher();
		return (this.search(expression, maxCount, theSearcher));
    }

    /** 
     * Search for Lucene documents with the given Lucene query.
     * 
     * @param   expression  The Lucene search query
     * @param   maxCount    The maximum number of Lucene documents we want
     * @param   aSearcher   The Lucene searcher to be used
     * 
     * @return  a collection of KOs matching the Lucene query
     */
    private synchronized List<KnowledgeObject> search(Query expression, int maxCount, IndexSearcher aSearcher) {
        List<KnowledgeObject> result = new ArrayList<>();

        try {
            TopDocs     theHits   = aSearcher.search(expression, maxCount);
            final ScoreDoc[] scoreDocs = theHits.scoreDocs;
            int      theLength = scoreDocs.length; // number of hits
            Document theDoc    = null;

            if (theLength > maxCount) {
                theLength = maxCount;
            }
			TLID[] theIds = new TLID[theLength];
            for (int i = 0; i < theLength; i++) {
				theDoc = aSearcher.doc(scoreDocs[i].doc);
				theIds[i] = IdentifierUtil.fromExternalForm(theDoc.get(LuceneIndex.FIELD_KO_ID));
            }
            
            for (int i = 0; i < theIds.length; i++) {
                KnowledgeObject theKO = LuceneIndex.getKO(theIds[i], 
                                                   theDoc.get(LuceneIndex.FIELD_DOCTYPE), 
                                                   this.kbase);
                if (theKO != null) {
                    result.add(theKO);
                }
            }
        }
        catch (IOException ioe) {
            Logger.error("Could not find expression in Lucene index.", ioe, this);
            throw new AnalyzeException(ioe.toString());
        }

        return result;
    }
    
    /**
     * Convert a User's search expression into a Lucene query.
     * This is done by a Lucene QueryParser. Currently the "OR"
     * operator is used for conjunction of terms. All terms
     * are automatically "wildcarded".
     * 
     * @param   anExpression    The user's search expression to be parsed.
     * @param   useAND          Use AND operator if true
     * @return  The Lucene search query.
     */
    private Query getQueryForExpression(String anExpression, boolean useAND) 
        throws AnalyzeException, IOException, ParseException {
            
        String theOper = useAND ? " AND " : " OR ";    
            
        // getting the analyzer must not be closed after usage
        Analyzer theAnalyzer = LuceneIndex.getInstance().getDefaultDocumentAnalyzer();
        // create QueryParser expression
        int tokenCount = 0;
        StringBuffer parserExpression = new StringBuffer();

		try (
				StringReader reader = new StringReader(anExpression);
				TokenStream tokenStream = theAnalyzer.tokenStream(LuceneIndex.FIELD_CONTENTS, reader)) {
			tokenStream.reset();
    		while (tokenStream.incrementToken()) {  // get the next token of the search expression
    			if (! (tokenCount == 0) ) {
    				parserExpression.append(theOper);
    			}
				parserExpression.append(tokenStream.getAttribute(CharTermAttribute.class));
    			parserExpression.append("*");    // append wildcard
    			tokenCount++;
    		}
			tokenStream.end();
    	}
        // parse QueryParser expression to a normal Lucene search query
    	QueryParser theQueryParser = LuceneIndex.getInstance().getQueryParser(LuceneIndex.FIELD_CONTENTS);
        Query parserQuery = theQueryParser.parse(parserExpression.toString());
        return parserQuery;
    }

    /**
	 * Comparator for sorting {@link Entry}, by sorting {@link Entry#getValue() values} descending.
	 */
	protected static class DescendingIntegerHolderComparator implements Comparator<Entry<String, Integer>> {
    	
		/** Singleton instance of {@link DescendingIntegerHolderComparator}. */
    	public static final DescendingIntegerHolderComparator INSTANCE = new DescendingIntegerHolderComparator();
		
		private DescendingIntegerHolderComparator() {
			// singleton instance
		}
        
		@Override
		public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
			if (o1.getValue() < o2.getValue()) {
				return 1;
			} else if (o1.getValue() > o2.getValue()) {
				return -1;
			} else {
				return 0;
			}
		}
    }
    
}


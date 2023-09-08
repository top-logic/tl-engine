/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.top_logic.basic.Logger;
import com.top_logic.basic.Settings;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.util.Resources;

/**
 * Base class for MS-Office classes.
 *
 * This class provides some basic functions to convert data from and to MS-
 * Office.
 *
 * Moreover implementing classes will provide high level methods for reading 
 * and writing office files. High level means that values from the office
 * files can be read and written by simple {@link java.util.Map maps}.
 * 
 * TODO KHA/MGA/JCO 
 *      optimize reference handling by releasing parts of the stack.
 *      Reuse application instead of quitting it each time
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class AbstractOffice extends ManagedClass {

	/** The time out in milliseconds. */
    private static final long TIME_OUT = 3000;

    /** The counter to wait for accessing the office. */
    private static final int COUNTER = 60;

    /** Max number of replacement passes. */
	protected static final int MAX_PASSES = 5;

    /** Helper for synchronizing the calls. */
    private long lastCall;

    /** Helper for synchronizing the calls. */
	private Boolean lock = Boolean.FALSE;

	/** The token replacers. */
	protected Map<String, TokenReplacer> tokenReplacers = new LinkedHashMap<>();

    /**
     * Create a new AbstractOffice.
     */
    public AbstractOffice() {
    }

	/**
	 * Creates an {@link AbstractOffice} from the given configuration.
	 * 
	 * @param context
	 *        context to instantiate inner configurations.
	 * @param config
	 *        configuration of this {@link AbstractOffice}
	 */
	protected AbstractOffice(InstantiationContext context, ServiceConfiguration<?> config) {
		super(context, config);
	}

    /**
     * Return the extension of the office application.
     * 
     * @return    The extension of the implementing office application.
     */
    public abstract String getExtension();

    /**
     * Create an application.
     * 
     * This application is the source for all operations on the office.
     * 
     * @param    someRefs    The stack of references to be released later.
     * @return   The new created application object.
     * @throws   Exception    If creation fails.
     */
    protected abstract Object createApplication(Stack<?> someRefs) throws Exception;

    /**
     * Release all open references to the office.
     * 
     * This method will be called on every exit of the public interfaces to
     * enshure the correct shutdown of the office application.
     * 
     * @param    someRefs    The stack of references to be released.
     */
    protected abstract void releaseStack(Stack<?> someRefs);

    /**
	 * Return the document with the given name.
	 * 
	 * This method will return the matching document object to be used in the instance, not a file.
	 * 
	 * @param data
	 *        The name of the office document to be opened.
	 * @param anApplication
	 *        The Application
	 * @param someRefs
	 *        The stack of references to be released later.
	 * @return The requested office document.
	 * @throws Exception
	 *         If reading of the word document fails.
	 */
	protected abstract Object getDocument(BinaryData data, Object anApplication, Stack<?> someRefs) throws Exception;

    /**
     * Return the values from the given office document.
     * 
     * The returned map will contain unique names (names of the fields) and
     * the values from that field.
     *  
     * @param    aDoc        The document to be parsed.
     * @param    someRefs    The stack of references to be released later.
     * @param    aMapping    Will be used to map from internal items to Strings or Basic JavaObjects
     * @return   The values read from the office document (a map with the name 
     *           and the value of the form field in the given document).
     * @throws   Exception    If reading fails.
     */
    protected abstract Map<String, Object> getValuesFromDoc(Object aDoc, Mapping<String, Object> aMapping, Stack<?> someRefs) throws Exception;

    /**
     * Set the given value to the given object.
     * 
     * The object is taken from the method
     * {@link #getFields(Object, Stack)} and will contain the fields 
     *   read from the  office file.
     * 
     * @param anAppl 		the application
     * @param aDoc 			the document
     * @param anObject    	The object to be replaced
     * @param aKey			the key to be replaced (only needed by some functions)
     * @param aValue      	The value to be set to that object.
     * @return true if a replacement has been made
     */
    protected abstract boolean setResult(Object anAppl, Object aDoc, Object anObject, String aKey, Object aValue, Stack<?> someRefs) throws Exception;

    /**
     * Return a map of fields extracted from the given document.
     * 
     * This method will be used for replacing values of the fields in the
     * given document. It'll be used by other methods for getting the writable fields.
     * 
     * @param    aDoc    The document to be parsed.
     * @throws   Exception    If extracting the information fails.
     */
    protected abstract Map getFields(Object aDoc, Stack<?> someRefs) throws Exception;

    protected abstract void save(Object anAppl, Object aDoc, String aName, Stack<?> someRefs) throws Exception;

    protected abstract void closeDocument(Object aDoc, Stack<?> someRefs) throws Exception;

    /**
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
		StringBuilder toString = new StringBuilder();
		toString.append(this.getClass().getName());
		toString.append(" [");
		toString.append("lock: ");
		synchronized (this) {
			toString.append(this.lock);
			toString.append(", last call: ");
			toString.append(this.lastCall);
		}
		toString.append("]");
		return toString.toString();
    }

    public String getVersion() throws OfficeException {
        Stack theStack = new Stack();

        try {
            return (this.getVersion(theStack));
        }
        catch (OfficeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new OfficeException("Unable to get version from office.", ex);
        }
        finally {
            this.releaseStack(theStack);
        }
    }

    /**
     * Extract all texts from the given stream.
     * 
     * The returned map will contain a unique key, representing the position 
     * of the found text in the stream. The value will be the found value. The
     * kind of the key and the value depend on the implementing class.
     * 
     * @param    aSource    The stream to be parsed.
     * @return   The map of found texts from that stream.
     * @throws   OfficeException    If parsing fails for a reason.
     */
    public Map getValues(InputStream aSource) throws OfficeException {
        try {
			File theFile = File.createTempFile("tmp", this.getExtension(), Settings.getInstance().getTempDir());
			try {
				FileUtilities.copyToFile(aSource, theFile);

				return this.getValues(BinaryDataFactory.createBinaryData(theFile));
			} finally {
				theFile.delete();
			}
        } 
        catch (IOException ex) {
            Logger.error("failed to getValues()", ex, AbstractOffice.class);
        }

		return (null);
    }
    
    /**
     * Extract all texts from the given file.
     * 
     * The returned map will contain a unique key, representing the position 
     * of the found text in the file. The value will be the found value. The
     * kind of the key and the value depend on the implementing class.
     * 
     * @param    aSource    The file to be parsed.
     * @param    aMapping   converts internal values to Strings or basic Java objects.
     * @return   The map of found texts from that file null when aSource was null
     *           or did not use the expected extension.
     * @throws   OfficeException    If parsing fails for a reason.
     */
	public Map<String, Object> getValues(BinaryData aSource, Mapping aMapping) throws OfficeException {
        if ((aSource != null) && aSource.getName().endsWith(this.getExtension())) {
            Object theDoc   = null;
            Stack  theStack = new Stack();
            theStack.ensureCapacity(256); // these tend to become large ...

            try {
                this.isReady();

				theDoc = this.getDocument(aSource, this.createApplication(theStack), theStack);

                return (this.getValuesFromDoc(theDoc, aMapping, theStack));
            }
            catch (OfficeException ex) {
                throw ex;
            }
            catch (Throwable ex) {
                String theMessage = "Unable to get values from " + aSource;
            
				Logger.warn(theMessage, ex, AbstractOffice.class);
            
                throw new OfficeException(theMessage, ex);
            }
            finally {
                this.closeFile(theDoc, theStack);
                this.releaseStack(theStack);
            }
        }
        return null;
    }

    /**
     * Extract all texts from the given file.
     * 
     * The returned map will contain a unique key, representing the position 
     * of the found text in the file. The value will be the found value. The
     * kind of the key and the value depend on the implementing class.
     * 
     * @param    aSource    The file to be parsed.
     * @return   The map of found texts from that file.
     * @throws   OfficeException    If parsing fails for a reason.
     */
	public Map<String, Object> getValues(BinaryData aSource) throws OfficeException {
		return getValues(aSource, getStringMapping());
	}

    /**
     * Set the values found in the given map to the given stream and store the 
     * result in a new file with the given name.
     * 
     * This method will first create a copy of the given stream (having the 
     * given name) and replace all values in the copy.
     * 
     * @param    aSource    The source stream to make the replacements on.
     * @param    aDest      The name of the new file to be created.
     * @param    aMap       The map containing the replacements for the text.
     * @return   <code>true</code>, if everything works fine.
     * @throws   OfficeException    If executing fails for a reason.
     */
	public boolean setValues(BinaryData aSource, File aDest, Map<String, Object> aMap) throws OfficeException {
        if (aSource != null) {
			try (InputStream theSrc = aSource.getStream()) {
				return setValues(theSrc, aDest, aMap);
			} catch (FileNotFoundException e) {
				throw new OfficeException ("Template not found: " + aSource, e);
			} catch (IOException ex) {
				throw new OfficeException("Unable to close input stream for " + aSource, ex);
			}
			         
        }
        
        return this.setValues((InputStream) null, aDest, aMap);
    }
    
    /** 
     * Perform replacements in a template document and generate a new document with the replements
     * 
     * @param aTemplate		the template file
     * @param aDestFile	    File identifying the generated document
     * @param aValueMap		the map of token name / token value pairs. The type of the token
     * 						value depends on the TokenReplacer implementation
     * @return true if creation succeeded
     */
    public boolean setValues(InputStream aTemplate, File aDestFile, Map<String, Object> aValueMap) throws OfficeException {
        if (aTemplate != null) {
            try {
				FileUtilities.copyToFile(aTemplate, aDestFile);

                Object theDoc   = null;
                String theName  = aDestFile.getCanonicalPath();
                Stack  theStack = new Stack();

                try {
                    this.isReady();

                    Object theAppl = this.createApplication(theStack);
					theDoc = this.getDocument(BinaryDataFactory.createBinaryData(aDestFile), theAppl, theStack);

                    if (aValueMap != null) {
                    	TokenReplacer theDefaultReplacer = new DefaultReplacer();
                    	
                    	boolean continueReplace;
                    	int theCount=0;
                    	do {
                    		continueReplace = false;
                    		// Sort descending by size to avoid replacing longer tokens that have shorter ones in the names 
                    		// with the shorter token's value (whole name search does not work for token with _ or - in them -
                    		// at least in Word)
                    		List<String> theKeys = new ArrayList<>(aValueMap.keySet());
                    		Collections.sort(theKeys, getTokenKeyComparator());
                    		Map<String, Object> theNextMap = new HashMap<>();

                    		for (String theToken : theKeys) {
								// Try exact match
								TokenReplacer theReplacer = this.tokenReplacers.get(theToken);

								// Try startsWith match
								if (theReplacer == null) {
									Iterator<String> theReplacerNames = this.tokenReplacers.keySet().iterator();

									while (theReplacer == null && theReplacerNames.hasNext()) {
										String theReplacerName = theReplacerNames.next();

										if (theToken.startsWith(theReplacerName)) {
											theReplacer = this.tokenReplacers.get(theReplacerName);
										}
									}
								}
								
								// Try default replacer
								if (theReplacer == null) { 
									theReplacer = theDefaultReplacer;
								}
								
								if (theReplacer != null) {
									try {
										Object theRepl = aValueMap.get(theToken);
										boolean replaced = theReplacer.replaceToken(theToken, theRepl, theAppl, theDoc, theStack); 
										if (!replaced) {
											theNextMap.put(theToken, theRepl);
										}
										continueReplace =  replaced | continueReplace;
									}
									catch (Exception ex) {
										Logger.warn("Problem replacing token " + theToken, ex, AbstractOffice.class);
									}
								}
								else {
									Logger.warn("No TokenReplacer found for token " + theToken, AbstractOffice.class);
								}
							}
	                    	
	                    	aValueMap = theNextMap;
	                    	theCount++;
                    	} while (continueReplace && theCount < MAX_PASSES);
                    	
                    	if (continueReplace && theCount >= MAX_PASSES) {
                    		Logger.warn ("Stopping replacement in " + aDestFile + " after " + MAX_PASSES 
                    				+ " passes to avoid infinite loop." 
                    				+ " Please check Replacers and your replacement data!", AbstractOffice.class);
                    	}
                    }
    				
                    this.save(theAppl, theDoc, theName, theStack);

                    return true;

                }
                catch(OfficeNotReadyException oe){
                    throw oe;
                }
                catch (OfficeException ex) {
                    throw ex;
                }
                catch (Throwable ex) {
                    String theMessage = "Unable to get values from " + aTemplate;
                
					Logger.warn(theMessage, ex, AbstractOffice.class);
                
                    throw new OfficeException(theMessage, ex);
                }
                finally {
                    this.closeFile(theDoc, theStack);
                    this.releaseStack(theStack);
                }
            }
            catch (IOException ex) {
                Logger.error("Unable to copy file '" + aTemplate + "' to '" +
						aDestFile + "'!",
					ex, AbstractOffice.class);
            }
        }

        return (false);
    }

    /**
     * Gets the comparator to sort the tokens to replace to optimize the replacing. As
     * default, the tokens are sorted descending by size to avoid replacing longer tokens
     * that have shorter ones in the names with the shorter token's value (whole name search
     * does not work for token with _ or - in them - at least in Word)
     *
     * In addition, tokens starting with "COPY_" will be moved to the beginning, so that the
     * copy-replacer will be used first before other tokens to avoid many fruitless
     * searches.
     */
    protected StringLengthComparator getTokenKeyComparator() {
        return new StringLengthComparator() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 == null && o2 == null) return 0;
                if (o1 == null) return -1;
                if (o2 == null) return 1;
                boolean c1 = ((String)o1).startsWith("COPY_");
                boolean c2 = ((String)o2).startsWith("COPY_");
                if (c1 && !c2) return -1;
                if (!c1 && c2) return 1;
                return super.compare(o1, o2);
            }
        };
    }

    /**
     * Return the version string of the office application.
     * 
     * @param    someRefs    The stack to store the references to COM objects. 
     * @return   The version string from the office application, must not be <code>null</code>.
     * @throws   Exception    If calling the COM object fails.
     */
    protected abstract String getVersion(Stack<?> someRefs) throws Exception;

    /**
     * Close the given file.
     * 
     * @param    aDoc      The document to be closed.
     * @param    someRefs  Referenes to clean up.
     */
    protected void closeFile(Object aDoc, Stack<?> someRefs) {
        try {
            this.closeDocument(aDoc, someRefs);
        }
        catch (Exception ex) {
            Logger.info("Unable to close file!", ex, AbstractOffice.class);
        }

        this.finished();
    }

    /**
     * Check, if this instance is ready for operating on files.
     * 
     * @throws    RuntimeException    If waiting for the lock on this
     *                                instance fails.
     */
    protected void isReady() throws RuntimeException {
		try {
            waitForLock();
        }
        catch (InterruptedException ex) {
			String interrupted = "Interrupted while waiting for the lock!";
			Logger.error(interrupted, ex, AbstractOffice.class);
			throw new RuntimeException(interrupted);
        }

	}

	private synchronized void waitForLock() throws InterruptedException {
		long timeout = now() + COUNTER * TIME_OUT;

		while (this.lock.booleanValue()) {
			if (now() > timeout) {
				String officeBuisy = Resources.getInstance().getString(I18NConstants.OFFICE_SUPPORT_BUZZY);
				throw new OfficeNotReadyException(officeBuisy);
			}
			this.wait(TIME_OUT);
		}

		this.lock = Boolean.TRUE;

		long theDiff = (now() - this.lastCall);

		if (theDiff < TIME_OUT) {
			this.wait(TIME_OUT - theDiff);
		}

	}

	private long now() {
		return System.currentTimeMillis();
	}

    /**
     * Signal for ending of a usage.
     */
	protected synchronized void finished() {
		this.lastCall = now();
		this.lock = Boolean.FALSE;
		this.notifyAll();
    }

    /**
     * Check, if this instance runs on a MS-Windows system.
     * 
     * This is needed to use the COM bridge.
     */
    public static void checkWindows() throws OfficeException {
        String theName = System.getProperty("os.name");

        if (!theName.startsWith("Windows")) {
            throw new OfficeException("MS-Office functionality is only " +
                                      "available in Windows (current OS is '" + 
                                      theName + "')!");
        }
    }

    /**
     * Allow access to the mapping from Internal objects to Strings
     */
	public abstract Mapping getStringMapping();

	/** 
	 * Add a token replacer.
	 * 
	 * @param aToken	the token. If a replacer exists for this token it will be replaced ;-)
	 * @param aReplacer	the replacer
	 */
	public void addTokenReplacer(String aToken, TokenReplacer aReplacer) {
		this.tokenReplacers.put(aToken, aReplacer);
	}
	
    /**
     * Compare Strings by their length descending (then by normal {@link String#compareTo(String)})
     * 
     * @author    <a href=mailto:klaus.halfmann@top-logic.com>Klaus Halfmann</a>
     */
    public static class StringLengthComparator implements Comparator {
        @Override
		public int compare(Object o1, Object o2) {
        	if (o1 == null) {
        		if (o2 == null) {
        			return 0;
        		}
        		return 1;
        	}
        	
        	if (o2 == null) {
        		return -1;
        	}
        	
        	String s1 = (String) o1;
            String s2 = (String) o2;
        	
        	int len1 = s1.length();
        	int len2 = s2.length();
        	
        	if (len1 == len2) {
        		return -s1.compareTo(s2);
        	}
        	
        	return len2 - len1;
        }
    }

    /**
	 * Replace tokens in Word documents with something useful.
	 * 
	 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
	 */
	public interface TokenReplacer {
		
		/** 
		 * Replace a token in a Word document.
		 * 
		 * @param aToken			the token to be replaced
		 * @param aReplacement		the replacement
		 * @param anApplication		the Application
		 * @param aDocument			the Document
		 * @param aReferencesStack	the references to COM objects
		 * @return true if a replacement has been made
		 * @throws Exception		if something goes wrong in Word
		 */
		public boolean replaceToken(String aToken, Object aReplacement, Object anApplication, Object aDocument, Stack aReferencesStack) throws Exception;
	}
	
	/**
	 * Replace all form fields in one go.
	 * 
	 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
	 */
	public class DefaultReplacer implements TokenReplacer {
		
		private Map fields;

		/** 
		 * Replace all form fields in one go.
		 * 
		 * The replacement has to be a Map of filed name / field value pairs.
		 * The field values can be Strings for text fields or Object[][]s for
		 * table fields.
		 *  
		 * If the replacement is not a List an IllegalArgumentException will be thrown.
		 * 
		 * @see AbstractOffice.TokenReplacer#replaceToken(String, Object, Object, Object, Stack)
		 */
		@Override
		public boolean replaceToken(String aToken, Object aReplacement, Object anApplication, Object aDocument, Stack aReferencesStack) throws Exception {
			try{			
				Object theObject = AbstractOffice.this.getSuitableFields(this.getFields(aDocument, aReferencesStack),aToken);
				return AbstractOffice.this.setResult(anApplication, aDocument, theObject, aToken, aReplacement, aReferencesStack);
			}
			catch(Exception e){
				Logger.warn("Problem setting values: "+aReplacement,e,AbstractOffice.class);
				return false;
			}
		}
		
		private Map getFields(Object aDocument, Stack aReferencesStack) throws Exception {
			if (this.fields == null) {
				this.fields = AbstractOffice.this.getFields(aDocument, aReferencesStack);
			}
			
			return this.fields;
		}
	}

	/**
	 * Data class for ImageReplacer that contains an image file name (fully qualified)
	 * and an alignment info.
	 * 
	 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
	 */
	public static class ImageReplacerData {

		/** Horizontal alignment constants. */
		public static final long HALIGN_LEFT   = 0x10000000;
		public static final long HALIGN_CENTER = 0x20000000;
		public static final long HALIGN_RIGHT  = 0x30000000;
	    public static final long HALIGN_NONE   = 0x40000000;
	    public static final long HALIGN_FIT    = 0x50000000;
	    
	    /** Vertical alignment constants. */
		public static final long VALIGN_TOP    = 0x01000000;
		public static final long VALIGN_CENTER = 0x02000000;
		public static final long VALIGN_BOTTOM = 0x03000000;
		public static final long VALIGN_NONE   = 0x04000000;
	    public static final long VALIGN_FIT    = 0x05000000;
	
	    /** Alignment, size, lock ratio fields. */
	    public long    hAlignment;
	    public long    vAlignment;
	    public float   hSize;
	    public float   vSize;
	    public boolean lockRatio;
	    public File    imgFile;
				
		/** 
		 * Create a new ImageReplacerData without alignment and resizing
		 * 
		 * @param aFile the file. The file must exist.
		 */
		public ImageReplacerData(File aFile) {
			this(aFile, HALIGN_NONE, VALIGN_NONE, 0, 0, true);
		}
		
		/** 
		 * Create a new ImageReplacerData
		 * 
		 * @param aFile   		the file. The file must exist.
		 * @param anHAlignment	the horiz. alignment (one of HALIGN_* above)
		 * @param aVAlignment	the vertical alignment (one of VALIGN_* above)
		 * @param anHSize		the horizontal size in pixels. No resize if <= 1
		 * @param aVSize		the vetical size in pixels. No resize if <= 1
		 * @param aLockRatio	if true aspect ratio will be kept when resizing
		 */
		public ImageReplacerData(File aFile, long anHAlignment, long aVAlignment, float anHSize, float aVSize, boolean aLockRatio) {
			this.imgFile    = aFile;
			
			this.hAlignment = anHAlignment;
			this.vAlignment = aVAlignment;
			
			this.hSize      = anHSize;
			this.vSize      = aVSize;
			
			this.lockRatio  = aLockRatio;
	
		}
	}
	
	/**
	 * Information holder for fixed tables in powerpoint. Allows to set the data for the fixed
	 * table, the heights of the rows and the flag whether to delete unfilled rows or not.
	 */
	public static class FixedTableInformation {

		private Object[] _tableValues;

		private List<Integer> _tableRowHeights;

		private boolean _deleteUnfilledRows;

		/**
		 * Creates a new {@link FixedTableInformation}.
		 */
		public FixedTableInformation(Object[] tableValues, List<Integer> tableRowSizes) {
			this(tableValues, tableRowSizes, false);
		}

		/**
		 * Creates a new {@link FixedTableInformation}.
		 */
		public FixedTableInformation(Object[] tableValues, boolean deleteEmptyRows) {
			this(tableValues, Collections.<Integer> emptyList(), deleteEmptyRows);
		}

		/**
		 * Creates a new {@link FixedTableInformation}.
		 */
		public FixedTableInformation(Object[] tableValues, List<Integer> tableRowSizes, boolean deleteEmptyRows) {
			_tableValues = tableValues;
			_tableRowHeights = tableRowSizes;
			_deleteUnfilledRows = deleteEmptyRows;
		}

		/**
		 * Returns the {@link Object}[] containing the table values.
		 */
		public Object[] getTableValues() {
			return _tableValues;
		}

		/**
		 * Returns the {@link List} of table row heights.
		 */
		public List<Integer> getTableRowHeights() {
			return _tableRowHeights;
		}

		/**
		 * Returns <code>true</code> or <code>false</code> depending on whether removing of empty
		 * rows is activated or not.
		 */
		public boolean getDeleteUnfilledRows() {
			return _deleteUnfilledRows;
		}

	}

	/** 
	 * Get suitable fields that need token replacement
	 * 
	 * @param fields	the fields (key, list of fields mapping)
	 * @param token		the current token
	 * @return the suitable fields
	 */
	protected Object getSuitableFields(Map fields, String token) {
		return fields.get(token);
	}
}


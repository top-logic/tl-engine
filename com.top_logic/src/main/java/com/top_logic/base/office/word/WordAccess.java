/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.word;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.top_logic.base.office.AbstractOffice;
import com.top_logic.base.office.OfficeException;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;


/**
 * {@link ManagedClass} for access to MS-Word documents.
 * 
 * This class provides high level methods for reading and writing 
 * Word files. High level means that values from Word can be read 
 * and written by passing  {@link java.util.Map maps}.
 * 
 * The maps are expected to be the following:
 * 
 * <ul>
 *     <li>getValues(): <em>key</em>: The name of the field. 
 *                      <em>value</em>: The content of the field in the template.</li>
 *     <li>setValues(): <em>key</em>: The name of the field. 
 *                      <em>value</em>: The new content of the field.</li>
 * </ul>
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class WordAccess extends AbstractOffice {

    /** File extension for MS-Word documents. */
    public static final String DOC_EXT = ".doc";

    /** File extension for MS-Word templates. */
    public static final String TEMP_EXT = ".dot";

    /**
     * Protected CTor for my subclasses
     */
    protected WordAccess() {
        super();
    }
    
	/**
	 * Creates an {@link WordAccess} from the given configuration.
	 * 
	 * @param context
	 *        context to instantiate inner configurations.
	 * @param config
	 *        configuration of this {@link WordAccess}
	 */
	protected WordAccess(InstantiationContext context, ServiceConfiguration<?> config) {
		super(context, config);
	}

    /**
     * Return the file extension of word documents (is {@link #DOC_EXT}).
     * 
     * @return    {@link #DOC_EXT} (is ".doc").
     * @see       com.top_logic.base.office.AbstractOffice#getExtension()
     */
    @Override
	public String getExtension() {
        return (DOC_EXT);
    }

    /**
     * Extract Text from tables in given file-
     * 
     * The returned map will contain a unique key, representing the position 
     * of the found text in the tables. The key is the the table name (or number) 
     * the colum and row divied by '_'. The value will be the found value. 
     * Examples of keys are "1_1_1" or "3_14_18". Joined (or merged) cells
     * are not supported an will result in an error as of NOW.
     * 
     * TODO KHA/JCO/FMA/CDO/MGA fix access to Tables with meregd cells.
     *      Think about using POI ..
     * 
     * @param    aSource    The file to be parsed.
     * @return   The map of found texts from that file null when aSource was null
     *           or did not use the expected extension.
     * @throws   OfficeException    If parsing fails for a reason.
     */
	public Map getTableValues(BinaryData aSource) throws OfficeException {
        if ((aSource != null) && aSource.getName().endsWith(this.getExtension())) {
            Object theDoc   = null;
            Stack  theStack = new Stack();
            theStack.ensureCapacity(256); // these tend to become large ...

            try {
                this.isReady();

				theDoc = this.getDocument(aSource, this.createApplication(theStack), theStack);

                return this.getTableValues(theDoc, theStack);
            }
            catch (OfficeException ex) {
                throw ex;
            }
            catch (Throwable ex) {
                String theMessage = "Unable to get values from " + aSource;
            
				Logger.warn(theMessage, ex, WordAccess.class);
            
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
     * Extract the table values and return them as Map.
     * 
     * The separete values are identified by the table number (starting with 1)
     * and the coordinate (row, column), i.e. the first value of the first 
     * table has the key 1_1_1.
     * 
     * @param    aDoc    Document containing the tables
     * 
     * @return   null when there are not tables int the document at all.
     */
    protected abstract Map getTableValues(Object aDoc, Stack someRefs) throws Exception;

    /**
	 * Return a Map of Table obejctz index by String of first cell.
	 * 
	 * KHA this code works but may not make sense when duplicate strings
	 * are found in the tables.
	 * 
	 * @param aDoc   The document to extract the values from.
	 * @return a Map of table objects indexed by content of first cell.
	 */
	protected abstract Map getTables(Object aDoc, Stack someRefs);

    /**
     * Return the only instance of this class.
     * 
     * @return    The only instance of this class.
     */
	public static WordAccess getInstance() {
		return Module.INSTANCE.getImplementationInstance();
    }

    @Override
    protected StringLengthComparator getTokenKeyComparator() {
        return new WordAccessTokenkeyComparator(CollectionUtil.toList(tokenReplacers.keySet()));
    }

    /**
     * Comparator sorting token keys by replacer type first and by string length afterwards.
     *
     * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
     */
    protected class WordAccessTokenkeyComparator extends StringLengthComparator {

    	private List<String> replacerKeys;

    	/**
    	 * Creates a new instance of this class.
    	 */
    	public WordAccessTokenkeyComparator(List<String> replacerKeys) {
    		this.replacerKeys = replacerKeys;
    	}

        @Override
        public int compare(Object o1, Object o2) {
            if (o1 == null && o2 == null) return 0;
            if (o1 == null) return -1;
            if (o2 == null) return 1;
            int result = priorityValue(o1) - priorityValue(o2);
            return result == 0 ? super.compare(o1, o2) : result;
        }

        private int priorityValue(Object o) {
        	String s = (String)o;
        	int length = replacerKeys.size();
        	for (int i = 0; i < length; i++) {
        		if (s.startsWith(replacerKeys.get(i))) return i;
        	}
        	return length;
        }

    }
    
	/**
	 * Service module to provide the application unique instance of {@link WordAccess}
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<WordAccess> {

		/** Sole {@link Module} instance */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton instance
		}

		@Override
		public Class<WordAccess> getImplementation() {
			return WordAccess.class;
		}

	}

}

/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.model.DescriptiveParsePosition;
import com.top_logic.util.Resources;

/**
 * The StringTokenFormat separates strings into tokens. The 
 * {@link #parseObject(String, ParsePosition)} and
 * {@link #format(Object, StringBuffer, FieldPosition)} methods delegates the opertions
 * to the intern {@link #format} object. 
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class StringTokenFormat extends Format {

    /** The format which is used for the string tokens. */
    private Format  format;
    /** The parse delimiters (e.g. ',; '). */
    private String  parseDelimiters;
    /** The format delimiter (e.g. ',').  */
    private String  formatDelimiter;
    /** Indicate whether the spaces before and after a token are removed. */
    private boolean removeSpaces;
    
    /** 
     * Creates a {@link StringTokenFormat} with the
     * given parameters.
     * 
     * @param aFormat             The format which is used for the separated tokens.
     * @param someParseDelimiters The delimiters for separating tokens.
     */
    public StringTokenFormat(Format aFormat, String someParseDelimiters) {
        this(aFormat, someParseDelimiters, null, false);
    }
    
    /** 
     * Creates a {@link StringTokenFormat} with the
     * given parameters.
     * 
     * @param aFormat The format which is used for the separated tokens.
     */
    public StringTokenFormat(Format aFormat, String someParseDelimiters, String aFormatDelimiter, 
                             boolean isRemoveSpaces) {
    	assert someParseDelimiters != null : "Parse delimiter list may not be null.";

    	this.format          = aFormat;
        this.removeSpaces    = isRemoveSpaces;
        this.parseDelimiters = someParseDelimiters;

        setFormatDelimiter(aFormatDelimiter);
    }

	/** 
     * This method parse the given string with the intern {@link #format} 
     * and returns a {@link List} of objects.
     * 
     * @param  aSource A source.
     * @param  aPos    A parse position.
     * @return Returns a {@link List} of objects or an {@link Collections#EMPTY_LIST}.
     * 
     * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
     */
    @Override
	public Object parseObject(String aSource, ParsePosition aPos) {
        if (aPos == null) {
            throw new NullPointerException();
        }
        if (StringServices.isEmpty(aSource)) {
            return Collections.EMPTY_LIST;
        }
        
        int    start = aPos.getIndex();
        String str   = null;
        if (removeSpaces) {
            str = aSource.substring(start).trim();
        } else {
            str = aSource.substring(start);
        }
        
        ArrayList<Object>       list      = new ArrayList<>();
        boolean         hasErrors = false;
        StringTokenizer tokenizer = new StringTokenizer(str, this.parseDelimiters);
        while (tokenizer.hasMoreTokens()) {
            String element = tokenizer.nextToken();
            if (removeSpaces) {
                element = element.trim();
            }
            DescriptiveParsePosition elementPos = new DescriptiveParsePosition(0);
            Object                   result     = this.format.parseObject(element, elementPos);
            if ((elementPos.getErrorIndex() < 0) && (elementPos.getIndex() < element.length())) {
                elementPos.setErrorIndex(elementPos.getIndex());
            } 
            
            if (elementPos.getErrorIndex() >= 0) {
                hasErrors = true;
                if (aPos instanceof DescriptiveParsePosition) {
                    DescriptiveParsePosition desPos       = (DescriptiveParsePosition) aPos;
                    String                   errorMessage = elementPos.getErrorMessage();
                    if (StringServices.isEmpty(errorMessage)) {
						errorMessage =
							Resources.getInstance().getString(I18NConstants.INVALID_TOKEN__AT.fill(String.valueOf(list.size() + 1)));
                    } 
                    desPos.setErrorDescription(errorMessage,
                                               elementPos.getErrorDetail());
                    desPos.setErrorIndex(elementPos.getErrorIndex());
                    desPos.setIndex(elementPos.getIndex());
                }
                break;
            } else {
                list.add(result);
            }
        }
        
        if (!hasErrors) {aPos.setIndex(aSource.length());}
        return list;
    }

    /** 
     * This method formats the given list with the intern {@link #format} and adds 
     * the result to the given {@link StringBuffer}.
     * 
     * @param  aObj        A {@link List}.
     * @param  aToAppendTo A {@link StringBuffer}
     * @param  aPos        A {@link FieldPosition}.
     * @return Returns the given string buffer with the results.
     * 
     * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
     */
    @Override
	public StringBuffer format(Object aObj, StringBuffer aToAppendTo, FieldPosition aPos) {
        if (aToAppendTo == null || aPos == null) {
            throw new NullPointerException();
        }
        if (!(aObj instanceof List<?>)) {
            throw new IllegalArgumentException();
        }
        
        List<?>     list     = (List<?>)aObj;
        Iterator<?> iterator = list.iterator();
        while(iterator.hasNext()) {
            Object obj = iterator.next();
            if (aToAppendTo.length() > 0) {
                aToAppendTo.append(this.formatDelimiter);
            }
            this.format.format(obj, aToAppendTo, aPos);
        }
        return aToAppendTo;
    }

    /**
     * This method returns the format.
     * 
     * @return Returns the format.
     */
    public Format getFormat() {
        return this.format;
    }
    
    /**
     * This method sets the format.
     *
     * @param aFormat The format to set.
     */
    public void setFormat(Format aFormat) {
        this.format = aFormat;
    }
    
    /**
     * This method returns the formatDelimiter.
     * 
     * @return Returns the formatDelimiter.
     */
    public String getFormatDelimiter() {
        return this.formatDelimiter;
    }
    
    /**
	 * This method sets the formatDelimiter.
	 * 
	 * @param aFormatDelimiter
	 *     The formatDelimiter to set. <code>null</code> means to
	 *     choose a delimiter automatically based on the current parse
	 *     delimiters.
	 */
	public final void setFormatDelimiter(String aFormatDelimiter) {
		if (aFormatDelimiter != null) {
        	this.formatDelimiter = aFormatDelimiter;
        } else {
        	// Compute a visually most attractive legal format delimiter. 
            char theFirstParseDelimiter = parseDelimiters.charAt(0);
            if (theFirstParseDelimiter == ' ') {
            	if (parseDelimiters.length() > 1) {
                	this.formatDelimiter = parseDelimiters.charAt(1) + " ";
            	} else {
            		this.formatDelimiter = " ";
            	}
            } else {
            	if (parseDelimiters.indexOf(' ') >= 0) {
            		this.formatDelimiter = theFirstParseDelimiter + " ";
            	} else {
            		this.formatDelimiter = String.valueOf(theFirstParseDelimiter);
            	}
            }
        }

		assert isLegalFormatDelimiter(parseDelimiters, formatDelimiter);
	}

	/**
	 * Checks, whether the given format delimiter is legal to format a list
	 * value that can be parsed later on with the given parse delimiters.
	 */
    private static boolean isLegalFormatDelimiter(String someParseDelimiters, String aFormatDelimiter) {
    	for (int cnt = aFormatDelimiter.length(), n = 0; n < cnt; n++) {
    		if (someParseDelimiters.indexOf(aFormatDelimiter.charAt(n)) < 0)
    			return false;
    	}
		return true;
	}

    /**
     * This method returns the parseDelimiters.
     * 
     * @return Returns the parseDelimiters.
     */
    public String getParseDelimiters() {
        return this.parseDelimiters;
    }
    
    /**
	 * This method sets the parseDelimiters and sets a default format delimiter.
	 * 
	 * @param aParseDelimiters
	 *     The parseDelimiters to set.
	 */
    public void setParseDelimiters(String aParseDelimiters) {
        this.parseDelimiters = aParseDelimiters;
        setFormatDelimiter(null);
    }
    
    /**
     * This method returns the removeSpaces.
     * 
     * @return Returns the removeSpaces.
     */
    public boolean isRemoveSpaces() {
        return this.removeSpaces;
    }
    
    /**
     * This method sets the removeSpaces.
     *
     * @param aRemoveSpaces The removeSpaces to set.
     */
    public void setRemoveSpaces(boolean aRemoveSpaces) {
        this.removeSpaces = aRemoveSpaces;
    }
}


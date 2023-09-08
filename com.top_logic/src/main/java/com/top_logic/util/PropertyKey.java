/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Special key for keys, which contain same key but different parameters.
 * 
 * A call can be: <code>theKey = new PropertyKey("myKey;param1=a;param2=b;param3=c");</code>.
 * Where <code>theKey.getKey()</code> returns "myKey" and <code>theKey.getProperty("param1")</code> 
 * returns "a".
 * 
 * <p>This is been used by <a href="http://www.ietf.org/rfc/rfc2425.txt">RFC2425</a>
 * and <a href="http://www.ietf.org/rfc/rfc2445.txt">RFC2445}</a>.</p>
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class PropertyKey implements Comparable {

    /** The key of this key. */
    private String key;

    /** The specific parameters of this key. */
    private Properties props;

    private String string;

    /** 
     * Create a new property based key.
     * 
     * @param    aKey    The key to extract the values from.
     */
    public PropertyKey(String aKey) {
        this(aKey, ';');
    }

    /** 
     * Create a new property based key.
     * 
     * @param    aKey    The key to extract the values from.
     * @param    aDel    The delimiter for the values.
     */
    public PropertyKey(String aKey, char aDel) {
        int    theDiv    = aKey.indexOf(aDel);
        String theParams = aKey.substring(theDiv + 1);

        this.string = aKey;
        this.props  = new Properties();

        if (theDiv > 0) {
            this.key = aKey.substring(0, theDiv);

            for (StringTokenizer theToken = new StringTokenizer(theParams, new String(new char[] {aDel})); theToken.hasMoreTokens(); ) {
                String theValue = theToken.nextToken();
    
                theDiv = theValue.indexOf('=');
    
                this.props.put(theValue.substring(0, theDiv), theValue.substring(theDiv + 1));
            }
        }
        else {
            this.key = aKey;
        }
    }

    /** 
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " [" + this.toStringValues() + ']');
    }

    /** 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
	public int compareTo(Object anObject) {
        if (anObject instanceof PropertyKey) {
            return (this.key.compareTo(((PropertyKey) anObject).key));
        }
        else {
            return (-1);
        }
    }

    /** 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals(Object anObject) {
		if (anObject == this) {
			return true;
		}
		// TODO #19482: FindBugs(EQ_CHECK_FOR_OPERAND_NOT_COMPATIBLE_WITH_THIS)
        if (anObject instanceof String) {
            return (this.string.equals(anObject));
        }
        else if (anObject instanceof PropertyKey) {
            PropertyKey theKey = (PropertyKey) anObject;

            if (theKey.key.equals(this.key)) {
                Set thePKeys = theKey.props.keySet();
                Set theKeys  = this.props.keySet();

                if ((thePKeys.size() == this.props.size()) && thePKeys.containsAll(theKeys)) {
                    for (Iterator theIt = theKeys.iterator(); theIt.hasNext(); ) {
                        Object theCurr  = theIt.next();
                        Object theValue = theKey.props.get(theCurr);

                        if (!this.props.get(theCurr).equals(theValue)) {
                            return (false);
                        }
                    }

                    return (true);
                }
            }
        }

        return (false);
    }

    /** 
     * @see java.lang.Object#hashCode()
     */
    @Override
	public int hashCode() {
        return (this.string.hashCode());
    }

    /** 
     * Return the main key of this instance.
     * 
     * @return    The main key of this key.
     */
    public String getKey() {
        return (this.key);
    }

    /** 
     * Return the property stored for the given key.
     * 
     * @param    aKey    The key to get the property for.
     * @return   The requested value.
     */
    public String getProperty(String aKey) {
        return (this.props.getProperty(aKey));
    }

    /** 
     * The original string representation of this key.
     * 
     * @return    The requested representation.
     */
    public String getString() {
        return (this.string);
    }

    /** 
     * Debugging output used for {@link #toString()}.
     * 
     * @return    Values of this instance for debugging.
     */
    protected String toStringValues() {
        return ("key: '" + this.key + "', props: " + this.props);
    }
}
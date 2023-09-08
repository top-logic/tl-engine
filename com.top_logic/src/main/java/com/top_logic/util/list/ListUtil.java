/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.list;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;


/**
 * Supporting class for managing lists (i.e. FastList).
 *
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class ListUtil {

	/** TL Yes/No list constants */
	public static final String TLYESNO_LIST = "tl.yesno";
	public static final String TLYESNO_YES  = "tl.yes";
	public static final String TLYESNO_NO   = "tl.no";

    /** The only instance of this class. */
    private static ListUtil singleton;

    /**
     * Default constructor for singleton pattern.
     */
    private ListUtil() {
        super();
    }

	/**
	 * Check is the given object is or contains the tl.yes list element
	 *
	 * @param aTLYesNo	a FastListElement or a List containing a FastListElement
	 * @return true if the list contains the tl.yes element
	 */
	public boolean isTrue(Object aTLYesNo) {
		Boolean theBool = getAsBoolean(aTLYesNo);

		return theBool != null && theBool.booleanValue();
	}

    /**
	 * Check is the given object is or contains the tl.no list element
	 *
	 * @param aTLYesNo	a FastListElement or a List containing a FastListElement
	 * @return true if the list contains the tl.no element
	 */
	public boolean isFalse(Object aTLYesNo) {
		Boolean theBool = getAsBoolean(aTLYesNo);

		return theBool != null && !theBool.booleanValue();
	}

	/**
	 * Get the value of the (List containing a) tl.yesno fast list element
	 * as Boolean
	 *
	 * @param aTLYesNo	a FastListElement or a List containing a FastListElement
	 * @return the value of the (List containing a) tl.yesno fast list element
	 * as Boolean
	 */
	public Boolean getAsBoolean(Object aTLYesNo) {
		if (aTLYesNo == null) {
			return null;
		}

		Object theO = null;
		if (aTLYesNo instanceof Collection) {
			if (((Collection) aTLYesNo).isEmpty()) {
				return null;
			}
			else {
				Iterator theIt = ((Collection) aTLYesNo).iterator();
				theO = theIt.next();
			}
		}
		else {
			theO = aTLYesNo;
		}

		if (!(theO instanceof FastListElement)) {
			if (theO instanceof Boolean) {
				return (Boolean) theO;
			}

			return null;
		}

		if (TLYESNO_YES.equals(((FastListElement) theO).getName())) {
			return Boolean.TRUE;
		}

		if (TLYESNO_NO.equals(((FastListElement) theO).getName())) {
			return Boolean.FALSE;
		}

		return null;
	}

	public FastListElement getElementOfList(String aListName, String anElementName) {
		try {
			FastList theList = FastList.getFastList(aListName);
			if (theList == null) {
				theList = FastList.getFastList(aListName);
			}
			if (theList != null) {
				return theList.getElementByName(anElementName);
			}
		}
		catch (Exception ex) {
			Logger.warn("Failed to find element '" + anElementName + "' in list '" + aListName + "'", ex, this);
		}

		return null;
	}

	/**
     * Return the only instance of this class.
     *
     * @return    The only instance of this class.
     */
    public static ListUtil getInstance() {
        if (ListUtil.singleton == null) {
            ListUtil.singleton = new ListUtil();
        }

        return (ListUtil.singleton);
    }

    /** A simple value object for list entries. */
    public static class ListEntry {

        private String name;
        private Map    otherAttributes;

        // Constructors

        /** Creates a {@link ListEntry}. */
        public ListEntry(String aName) {
            this.name            = aName;
            this.otherAttributes = new HashMap();
        }

        // Methods

        /** Returns the unique name of the list element. */
        public String getName() {
            return this.name;
        }

        /**
         * Returns the attribute value for the given key. Never null but maybe
         * an empty string.
         */
        public String getAttribute(String anAttributeKey) {
            return (String) this.otherAttributes.get(anAttributeKey);
        }

        /**
         * This method adds for a given key an string value.
         *
         * @param anAttributeKey
         *        The attribute key. Must not be <code>null</code>.
         * @param anAttributeValue
         *        The attribute value. Null values are replaced by empty
         *        strings.
         */
        public void addAttributes(String anAttributeKey, String anAttributeValue) {
            if (StringServices.isEmpty(anAttributeValue)) {
                anAttributeValue = "";
            }

            this.otherAttributes.put(anAttributeKey, anAttributeValue);
        }



    }

    /**
	 * This method returns the {@link FastListElement}s in a list.
	 */
    public static final List getFastListElements(FastList fastList) {
		return fastList.elements();
    }

}

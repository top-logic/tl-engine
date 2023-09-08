/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.table.filter;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Filter;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;


/**
 * This Filter is configured with an attribute name of a wrapper attribute and
 * can be parameterized with a String.
 * The Filter checks if the attribute value of a given Wrapper starts with the parameterized string!
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class StartsWithFilter implements Filter {

    /** Name of the Attrubute to filter */
    protected String attributeName;
    
    /** 
     * Filter will return true when upper case value of attributeName starts with this string.
     * 
     * This string will always be uppercase only.
     * When null the filter will always return null.
     */
    protected String startsWith;

    /**
     * Create a new StartsWithFilter for given anAttributeName.
     */
    public StartsWithFilter(String anAttributeName) {
        if (anAttributeName == null) 
            throw new IllegalArgumentException ("The attribute name must not be null");
        attributeName = anAttributeName;
    }
    
    
    public StartsWithFilter(String anAttributeName, String aStartswith) {
        if (anAttributeName == null) 
            throw new IllegalArgumentException ("The attribute name must not be null");
        attributeName = anAttributeName;
        if (aStartswith != null) {
            startsWith    = aStartswith.toUpperCase();
        }
    }

    /**
     * Acessor for {@link #startsWith} 
     */
    public String getStartsWith() {
        return startsWith;
    }
    
    /**
     * Acessor for {@link #startsWith} 
     */
    public void setStartsWith (String aLetter) {
        if (aLetter !=  null) {
            startsWith = aLetter.toUpperCase();
        }
        else {
            startsWith = null;
        }
    }

    /**
     * Acessor for {@link #attributeName} 
     */
    public String getAttributeName() {
        return attributeName;
    }
    
    /**
     * If no letter is configured we return <code>true</code>! 
     * 
     * TODO TL 5.2 KHA remove special Handling for Person, create PersonResitserComponent.
     * 
     * @see com.top_logic.basic.col.Filter#accept(java.lang.Object)
     */
    @Override
	public boolean accept (Object anObject) {
        if (startsWith == null) {
            return true;                    
        }
    	String name ="";
        if(anObject instanceof Person){
			name = ((Person) anObject).getLastName();
        }else if (anObject instanceof Wrapper) {
            name = (String)((Wrapper)anObject).getValue(attributeName);
        }
        return !StringServices.isEmpty(name) 
             && name.toUpperCase().startsWith(startsWith);
   }
}

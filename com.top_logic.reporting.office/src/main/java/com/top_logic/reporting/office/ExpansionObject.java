/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.office;


/**
 * The ExpansionObject is the representation of the symbols respectively the
 * expanded object resulting from the symbol. The symbol is expanded by an
 * {@link com.top_logic.reporting.office.ExpansionEngine} in a given
 * {@link com.top_logic.reporting.office.ExpansionContext}. The ExpansionObject
 * stores the type of the symbol and all relevant information to allow replacing
 * the symbol.
 * 
 * @author <a href=mailto:jco@top-logic.com>jco</a>
 */
public class ExpansionObject {
    
    public static final String STATIC = "static";
    
    /** reference to the key to find the expansion in the original document */
    private String fieldKey;
    
    /** in some cases the original fieldContent is important so ... */
    private String fieldContent;
    
    /** the symbol as it appears in the original content, this is needed for exact replacement */
    private String symbol;
    
    /** the symbol type determines what kind of expansion methodoloqy to use. At the moment static and script are supported */ 
    private String symbolType;
    
    /** the content to expand */
    private String symbolContent;
    
    /** the styles to use in the expanded content */
    private String contentStyle;
    
    /** if <code>true</code> the expansion object is no longer a symbol but an expanded object! */
    private boolean isExpanded;
    
    /** the result of the expansion process is stored here */
    private Object expandedObject;
    
    
    /**
     * The constructor needs a reference for the symbol.
     * @param aFieldKey a key to refer to thesource the expansion object originates from.
     */
    public ExpansionObject (String aFieldKey) {
        super();
        isExpanded = false;
        fieldKey   = aFieldKey;
    }

    /** 
     * Return some usefull String for debugging.
     * 
     */
    @Override
	public String toString() {
        return this.getClass().getName() + ": " + fieldKey;
    }
    
    /**
     * Only return the expanded object is the isExpanded flag is <code>true</code>.
     * @return the expanded object
     * @throws IllegalStateException if the ExpandedObject is not expanded.
     */
    public Object getExpandedObject () {
        if (isExpanded()) {
            return expandedObject;
        } else {
            throw new IllegalStateException ("The expansion object must be expanded to get the expanded object");
        }
    }
    
    /**
     * <code>true</code> if and only if the expanded object is not
     *         <code>null</code> and the expanded flag is set to
     *         <code>true</code>.
     */ 
    public boolean isExpanded () {
        return isExpanded && expandedObject != null;
    }
    
    /**
     * Expand the symbol by using the given engine and the given context. Set
     * the {@link #isExpanded} flag if a result different from <code>null</code> of the
     * expansion process was returned
     * 
     * @param anEngine the expansion engine to use
     * @param aContext the expansion context to deliver the necessary
     *            information
     */
    public void expand (ExpansionEngine anEngine, ExpansionContext aContext) {
        try {
            expandedObject = anEngine.expandSymbol (aContext,this);      
            isExpanded = (expandedObject != null);
        } catch (Exception exp) {
            isExpanded = false;
        }
    }
    
    /** Accessor to member */
    public String getSymbol() {
        return this.symbol;
    }

    
    /** Accessor to member */
    public void setSymbol(String someSymbol) {
        this.symbol = someSymbol;
    }

    
    /** Accessor to member */
    public String getSymbolContent() {
        return this.symbolContent;
    }

    
    /** Accessor to member */
    public void setSymbolContent(String someSymbolContent) {
        this.symbolContent = someSymbolContent;
    }

    
    /** Accessor to member */
    public String getSymbolType() {
        return this.symbolType;
    }

    
    /** Accessor to member */
    public void setSymbolType(String someSymbolType) {
        this.symbolType = someSymbolType;
    }

    
    /** Accessor to member */
    public String getFieldKey() {
        return this.fieldKey;
    }
    
    
    /** Accessor to member */
    public String getContentStyle() {
        return this.contentStyle;
    }

    
    /** Accessor to member */
    public void setContentStyle(String someContentStyle) {
        this.contentStyle = someContentStyle;
    }

    /** Accessor to member */
    public String getFieldContent() {
        return this.fieldContent;
    }

    
   /** Accessor to member */
   public void setFieldContent(String someFieldContent) {
        this.fieldContent = someFieldContent;
    }
}

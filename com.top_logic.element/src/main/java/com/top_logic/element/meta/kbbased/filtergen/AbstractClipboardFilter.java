/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.Collection;

import com.top_logic.basic.Logger;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.knowledge.wrap.Clipboard;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLObject;

/**
 * This class implements an {@link com.top_logic.element.meta.kbbased.filtergen.AttributedValueFilter}
 * that accepts some currently selected objects and the objects currently in the clipboard
 * as long as they are of a certain type.
 * 
 * @author    <a href="mailto:tsa@top-logic.de">Theo Sattler</a>
 *
 */
@Deprecated
public abstract class AbstractClipboardFilter extends
        AbstractAttributedValueFilter {

    /** constructor */
    public AbstractClipboardFilter() {
        super();
    }
    
    /** the class the filtered objects must be subclass of */
    protected abstract Class getInstanceOfClass();
    
    /** the currently selected objects, i.e. the objects to accept beside the content of the clipboard */
	protected abstract Collection getCurrentObjects(TLObject anAttributed, EditContext editContext);

    /** 
     * Accept all currently selected objects and the objects in the clipboard
     * if they are form the requested type.
     * 
     * @return true if the object is a contract, is in the clipboard and is not the same as the given attribute
     * 
     * @see com.top_logic.element.meta.kbbased.filtergen.AttributedValueFilter#accept(Object, EditContext)
     */
    @Override
	public boolean accept(Object anObject, EditContext editContext) {
    	boolean isOfPropperCass =  this.getInstanceOfClass().isAssignableFrom(anObject.getClass()) ;
    	boolean result = false;

		TLObject object = editContext.getObject();
		if (isOfPropperCass && anObject != object) {
    		try{
				Collection theRelatedContracts = this.getCurrentObjects(object, editContext);
                if (theRelatedContracts != null) {
                    if (theRelatedContracts.contains(anObject)) {
                        result = true;
                    }
                }
                if (!result) {
        	        Wrapper wrap = (Wrapper)anObject;
    	    		result = Clipboard.getInstance().contains(wrap);
                }
    		}catch(Exception e){
    			Logger.error("Unable to check clipboard for contract",e,this);
    		}
    	}
    	return result;
    }


}

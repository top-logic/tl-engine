/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.provider;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.layout.LabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.util.Resources;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class MetaElementLabelProvider implements LabelProvider {

	private TLClass me;
	private final LabelProvider base;

	private ResKey prefix;
	private Resources res;
	
	public MetaElementLabelProvider(LabelProvider aBase, TLClass aME, ResKey thePrefix) {
        this.base   = aBase;
        this.me = aME;
		this.prefix = thePrefix;
        this.res = Resources.getInstance();
    }
	 
	/**
	 * Try so use object as MetaAttributeName.
	 * 
	 * In case object is not a String or {@link #me} has no such attribute fall back to
	 * {@link #base}.
	 * 
	 * @see com.top_logic.layout.LabelProvider#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object object) {
		if(object instanceof String) {
		    String theMAName = (String) object;
			if (MetaElementUtil.hasMetaAttribute(me, theMAName)) {
    			try {
					TLStructuredTypePart theMA = MetaElementUtil.getMetaAttribute(this.me, theMAName);
					ResKey theRealName = TLModelI18N.getI18NKey(theMA);
    		        return this.res.getString(theRealName);
    	        }
    	        catch (NoSuchAttributeException e) {
					Logger.warn("The type " + this.me.getName() + " has no attribute " + theMAName + ".", e, this);
    		        return "attribute.not.found";
    	        }
    		}
		}
		
		String baseLabel = this.base.getLabel(object);
		return this.res.getString(this.prefix.suffix(baseLabel));
	}
}

/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.resources;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.WrapperResourceProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * Translates {@link TLStructuredType}s into his internationalized label in the locale of the
 * current user.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TLTypeResourceProvider extends WrapperResourceProvider {
	
	/**
	 * {@link ResourceProvider} for {@link TLStructuredType}s.
	 * 
	 * @see TLTypeResourceProvider
	 */
    public TLTypeResourceProvider() {
        super();
    }
    
    /** 
     * @see com.top_logic.layout.LabelProvider#getLabel(java.lang.Object)
     */
    @Override
	public String getLabel(Object object) {
		if (object instanceof TLStructuredType) {
	        try {
				return getMetaElementLabel((TLStructuredType) object);
	        }
	        catch (Exception ex) {
				Logger.error("Cannot compue label for type '" + object + "'.", ex, TLTypeResourceProvider.class);
	            return super.getLabel(object);
	        }
    	}
    	
    	return super.getLabel(object);
    }

    @Override
	protected ResKey getTooltipNonNull(Object object) {
		TLType type = (TLType) object;
		return com.top_logic.model.visit.I18NConstants.TYPE_TOOLTIP.fill(
			quote(type),
			quote(TLModelUtil.type(object)),
			quote(type.getModule()),
			quote(type.getName()));
    }
}

/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.provider;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLClass;
import com.top_logic.layout.LabelProvider;
import com.top_logic.reporting.layout.flexreporting.component.ChartConfigurationComponent.AdditionalReportingOptionProvider;
import com.top_logic.util.Resources;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public class ResourcedMetaLabelProvider implements LabelProvider {
    private final LabelProvider base;

    private final String mePrefix;
    
    private TLClass me;
    
    private AdditionalReportingOptionProvider provider;


    public ResourcedMetaLabelProvider(LabelProvider aBase, TLClass aME) {
        this(aBase, aME, null);
    }
    
    public ResourcedMetaLabelProvider(LabelProvider aBase, TLClass aME, AdditionalReportingOptionProvider anOptionProvider) {
    	this.base     = aBase;
    	this.mePrefix = aME.getName();
    	this.me       = aME;
    	this.provider = anOptionProvider;
    }

	@Override
	public String getLabel(Object object) {
		String theLabel = this.base.getLabel(object);
		try {
			TLStructuredTypePart theMA = null;
			if(this.provider != null && this.provider.hasOption(theLabel)) {
				theMA = this.provider.getMetaAttributeForReportingOption(theLabel);
				return Resources.getInstance().getString(ResKey.legacy(theLabel));
			}
			else {
				theMA = MetaElementUtil.getMetaAttribute(this.me, theLabel);
			}
			theLabel = AttributeOperations.getMetaElement(theMA).getName() + '.' + theLabel;
		}
		catch (NoSuchAttributeException ex) {
			Logger.error("Requested attribute " + theLabel + " does not exist for " + this.mePrefix, ex, this.getClass());
		}
		return Resources.getInstance().getString(ResKey.legacy(theLabel));
	}
}

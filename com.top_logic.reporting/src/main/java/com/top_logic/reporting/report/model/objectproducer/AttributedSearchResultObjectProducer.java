/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.objectproducer;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSetAware;
import com.top_logic.mig.html.layout.ComponentNameFormat;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.BoundHelper;

/**
 * @author    <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public class AttributedSearchResultObjectProducer extends AbstractObjectProducer {
	public static final String OBJECT_TYPE = "Wrapper";
	
	private Collection objects;

	public AttributedSearchResultObjectProducer(InstantiationContext aContext,  AttributedSearchResultProducerConfiguration aConfig) {
        super(aContext, aConfig);
    }
	
	/**
	 * @see com.top_logic.reporting.report.model.objectproducer.AbstractObjectProducer#_getObjectsInternal()
	 */
	@Override
	protected Collection _getObjectsInternal() {
		// IGNORE FindBugs(UWF_UNWRITTEN_FIELD): Deprecated class. Is removed when class removed.
		if(this.objects != null) {
			return this.objects;
		}
		else {
			AttributedSearchResultSetAware theComp = ((AttributedSearchResultProducerConfiguration)this.getConfiguration()).getLayoutComponent();
			if (theComp != null) {
				return theComp.getSearchResult().getResultObjects();
			}
			else {
				return Collections.EMPTY_LIST;
			}
		}
	}
    
    public interface AttributedSearchResultProducerConfiguration extends ObjectProducerConfiguration {
    	@Override
		@ClassDefault(AttributedSearchResultObjectProducer.class)
    	Class getImplementationClass();
    	
    	@Override
		@StringDefault("defaultValue")
    	String getObjectType();
    	
		public static final class MyValueFormat extends AbstractConfigurationValueProvider<LayoutComponent> {
    		
    		public static final MyValueFormat INSTANCE = new MyValueFormat();
			
			private MyValueFormat() {
				super(AttributedSearchResultSetAware.class);
			}

			@Override
			protected LayoutComponent getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
				return ((MainLayout) BoundHelper.getInstance().getRootChecker())
					.getComponentByName(ComponentNameFormat.INSTANCE.getValue(propertyName, propertyValue.toString()));
			}

			@Override
			protected String getSpecificationNonNull(LayoutComponent configValue) {
				return ComponentNameFormat.INSTANCE.getSpecification(configValue.getName());
			}
		}
    	
    	@Format(MyValueFormat.class)
    	AttributedSearchResultSetAware getLayoutComponent();
 	    void setLayoutComponent(AttributedSearchResultSetAware aComponent);
    }
}

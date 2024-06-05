/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout;

import java.util.Collection;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ListModelBuilder} that retrieves all {@link Wrapper}s of a configured
 * {@link MetaObject} type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KOInstanceTableModelBuilder extends AbstractConfiguredInstance<KOInstanceTableModelBuilder.Config>
		implements ListModelBuilder {
	
	public interface Config extends PolymorphicConfiguration<ListModelBuilder> {
		@Name(META_OBJECT_PROPERTY)
		@Mandatory
		String getMetaObject();
	}

	/**
	 * Property for configuring {@link #metaObjectName}.
	 */
	private static final String META_OBJECT_PROPERTY = "metaObject";
	
	/**
	 * The name of the structure to retrieve elements from.
	 */
	private String metaObjectName;

	/**
	 * Creates a {@link KOInstanceTableModelBuilder}.
	 *
	 * @param config The configuration.
	 */
	public KOInstanceTableModelBuilder(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		this.metaObjectName = config.getMetaObject();
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anObject) {
		return null;
	}

	@Override
	public boolean supportsListElement(LayoutComponent aComponent, Object anObject) {
		return (anObject instanceof Wrapper) && ((Wrapper) anObject).tTable().getName().equals(metaObjectName);
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		// IGNORE FindBugs(RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT): For better portability.
		return WrapperFactory.getWrappersByType(metaObjectName, PersistencyLayer.getKnowledgeBase());
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null;
	}

}

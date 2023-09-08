/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.list;

import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.config.annotation.EnumScope;

/**
 * {@link ModelBuilder} that shows all {@link FastList}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ClassificationTableModelBuilder extends AbstractConfiguredInstance<ClassificationTableModelBuilder.Config>
		implements ListModelBuilder {

	/**
	 * Configuration options for {@link ClassificationTableModelBuilder}.
	 */
	public interface Config extends PolymorphicConfiguration<ListModelBuilder> {

		/**
		 * The scope of classifications to display.
		 * 
		 * <p>
		 * No value means to display all classifications.
		 * </p>
		 * 
		 * @see EnumScope#getValue()
		 */
		@Name(TYPE_PROPERTY)
		@Nullable
		String getType();

	}

	/**
	 * {@link FastList#getClassificationType()} of classifications to retrieve.
	 * <code>null</code> means all classifications.
	 */
	protected final String type;

	/**
	 * Property for configuring {@link #type}
	 */
	private static final String TYPE_PROPERTY = "type";
	
	/**
	 * Creates a {@link ClassificationTableModelBuilder}.
	 *
	 * @param config The configuration.
	 */
	public ClassificationTableModelBuilder(InstantiationContext context, Config config) {
		super(context, config);
		this.type = StringServices.nonEmpty(StringServices.nonEmpty(config.getType()));
	}
	
	@Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anObject) {
		return null;
	}

	@Override
	public boolean supportsListElement(LayoutComponent aComponent, Object anObject) {
		if (! (anObject instanceof FastList)) {
			return false;
		}

		FastList classification = (FastList) anObject;
    	if (type != null) {
			if (! type.equals(classification.getClassificationType())) {
    			return false;
    		}
    	}
    	
		return true;
	}

	@Override
	public List<FastList> getModel(Object businessModel, LayoutComponent aComponent) {
		List<FastList> result;
    	if (type == null) {
			result = FastList.getAllLists();
    	} else {
    		result = FastList.getListsForType(type);
    	}
		return result;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null;
	}

}

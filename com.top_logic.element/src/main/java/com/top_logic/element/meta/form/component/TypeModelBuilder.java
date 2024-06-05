/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.component;

import java.util.ArrayList;
import java.util.Collection;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;

/**
 * The {@link TypeModelBuilder} creates a list of all Objects in a given table.
 *
 * @author <a href=mailto:CBR@top-logic.com>CBR</a>
 */
public class TypeModelBuilder extends AbstractConfiguredInstance<TypeModelBuilder.Config> implements ListModelBuilder {

	/**
	 * Configuration of a {@link TypeModelBuilder}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<TypeModelBuilder> {

		/** Configuration name for {@link #getKoType()}. */
		String XML_CONF_KEY_KOTYPE = "koType";
		/**
		 * Name of the table to get instances from.
		 */
		@Name(XML_CONF_KEY_KOTYPE)
		String getKoType();

	}

    private String koType;

    /**
	 * Creates a {@link TypeModelBuilder}.
	 */
	public TypeModelBuilder(InstantiationContext context, TypeModelBuilder.Config config) {
		super(context, config);
		this.koType = StringServices.nonEmpty(config.getKoType());
    }

    @Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
        if (koType != null) {
			// IGNORE FindBugs(RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT): For better portability.
            return WrapperFactory.getWrappersByType(koType,
			        PersistencyLayer.getKnowledgeBase());
        }
        return new ArrayList();
    }

    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        return aModel == null;
    }

    @Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object aAnObject) {
        return null;
    }

    @Override
	public boolean supportsListElement(LayoutComponent aComponent, Object aObject) {
        try {
			return aObject instanceof TLObject
				&& ((TLObject) aObject).tTable().getName().equals(this.koType);
        }
        catch (Exception e) {
            return false;
        }
    }

}

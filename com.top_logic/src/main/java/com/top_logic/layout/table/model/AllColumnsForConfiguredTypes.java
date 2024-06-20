/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.func.Function0;
import com.top_logic.layout.editor.config.ColumnsTemplateParameters;
import com.top_logic.layout.editor.config.TypeTemplateParameters;
import com.top_logic.layout.editor.config.TypesTemplateParameters;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.table.provider.AllColumnOptions;
import com.top_logic.layout.table.provider.ColumnOption;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Provides all {@link ColumnOption columns} for the configured
 * {@link TypesTemplateParameters#getTypes() types} given by its
 * {@link DeclarativeFormBuilder#FORM_MODEL form model}.
 * 
 * <p>
 * Used as option provider for inapp dialogs to determine the basic set of columns for table like
 * structures. Highly connected to {@link TypesTemplateParameters#getTypes()}.
 * </p>
 * 
 * @see TypesTemplateParameters
 * @see AllVisibleColumnsProvider
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class AllColumnsForConfiguredTypes extends Function0<Collection<ColumnOption>> {

	ConfigurationItem _formModel;

	/**
	 * Creates a {@link AllColumnsForConfiguredTypes}.
	 */
	@CalledByReflection
	public AllColumnsForConfiguredTypes(DeclarativeFormOptions options) {
		_formModel = options.get(DeclarativeFormBuilder.FORM_MODEL);
	}

	@Override
	public Collection<ColumnOption> apply() {
		return AllColumnOptions.INSTANCE.apply(getTypeRefs(), getProviders(), null);
	}

	private Collection<TLModelPartRef> getTypeRefs() {
		PropertyDescriptor typeProperty = _formModel.descriptor().getProperty(TypeTemplateParameters.TYPE);

		if (typeProperty != null) {
			return createModelPartRefs(CollectionUtil.asSet(_formModel.value(typeProperty)));
		}

		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	private Collection<PolymorphicConfiguration<? extends TableConfigurationProvider>> getProviders() {
		PropertyDescriptor typeProperty =
			_formModel.descriptor().getProperty(ColumnsTemplateParameters.CONFIGURATION_PROVIDERS);
		return (Collection<PolymorphicConfiguration<? extends TableConfigurationProvider>>) _formModel
			.value(typeProperty);
	}

	private Collection<TLModelPartRef> createModelPartRefs(Collection<?> types) {
		Set<TLModelPartRef> typeRefs = new HashSet<>();

		for (Object type : types) {
			if (type instanceof TLModelPartRef) {
				typeRefs.add((TLModelPartRef) type);
			} else if (type instanceof String) {
				typeRefs.add(TLModelPartRef.ref((String) type));
			} else if (type instanceof TLModelPart) {
				typeRefs.add(TLModelPartRef.ref((TLModelPart) type));
			} else {
				Logger.error("Could not create a reference for type " + type, AllColumnsForConfiguredTypes.class);
			}
		}

		return typeRefs;
	}

}

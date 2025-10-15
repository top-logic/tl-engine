/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.definition;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.layout.scripting.recorder.ref.AbstractGlobalModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ConfigurationItemNaming;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.model.util.TLModelPartRef;

/**
 * {@link ModelNamingScheme} for {@link FormDefinition}s.
 * 
 * <p>
 * Since those require a special context type, {@link ConfigurationItemNaming} is not applicable.
 * </p>
 */
public class FormDefinitionNaming extends AbstractGlobalModelNamingScheme<FormDefinition, FormDefinitionNaming.Name> {

	/**
	 * {@link ModelName} for a {@link ConfigurationItem}.
	 */
	public interface Name extends ModelName, FormContextDefinition {

		/**
		 * The {@link ConfigurationItem} this is a name for.
		 */
		@Mandatory
		FormDefinition getItem();

		/**
		 * Setter for {@link #getItem()}.
		 */
		void setItem(FormDefinition item);

		@Override
		@NullDefault
		TLModelPartRef getFormContextType();

	}

	/**
	 * Creates a {@link ConfigurationItemNaming}.
	 * 
	 * @see AbstractGlobalModelNamingScheme#AbstractGlobalModelNamingScheme(Class, Class)
	 */
	public FormDefinitionNaming() {
		super(FormDefinition.class, Name.class);
	}

	@Override
	protected void initName(Name name, FormDefinition model) {
		// Note: FormDefinition are config parts and cannot be linked to multiple containers.
		name.setItem(TypedConfiguration.copy(model));
	}

	@Override
	public FormDefinition locateModel(ActionContext context, Name name) {
		// Note: FormDefinition are config parts and cannot be linked to multiple containers.
		return TypedConfiguration.copy(name.getItem());
	}

}


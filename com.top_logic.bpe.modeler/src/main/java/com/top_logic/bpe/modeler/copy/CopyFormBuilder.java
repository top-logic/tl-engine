/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.copy;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;

/**
 * {@link DeclarativeFormBuilder} filling a {@link Form}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CopyFormBuilder extends DeclarativeFormBuilder<Collaboration, CopyFormBuilder.Form> {

	/**
	 * Form type with a single new name field.
	 *
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface Form extends ConfigurationItem {

		/**
		 * The name of the new version.
		 */
		@Mandatory
		String getNewName();

		/**
		 * @see #getNewName()
		 */
		void setNewName(String value);

	}

	/**
	 * Creates a {@link CopyFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CopyFormBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Class<? extends Collaboration> getModelType() {
		return Collaboration.class;
	}

	@Override
	protected Class<? extends Form> getFormType(Object contextModel) {
		return Form.class;
	}

	@Override
	protected void fillFormModel(Form formModel, Collaboration businessModel) {
		formModel.setNewName(businessModel.getName());
	}

}

/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.base.config.i18n.Internationalized;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.meta.MetaElementTreeModelBuilder.ModuleContainer;
import com.top_logic.element.layout.meta.ModuleContainerFormBuilder.EditModel;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.model.util.TLModelNamingConvention;

/**
 * {@link DeclarativeFormBuilder} builds the {@link FormContext} to display a
 * {@link ModuleContainer}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class ModuleContainerFormBuilder extends DeclarativeFormBuilder<ModuleContainer, EditModel> {

	/**
	 * Model of the displayed form.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	/**
	 * Model of the displayed form.
	 */
	@DisplayOrder({
		EditModel.NAME_ATTRIBUTE,
		EditModel.LABEL,
		EditModel.DESCRIPTION
	})
	public interface EditModel extends Internationalized, NamedConfiguration {

		// No additionals.

	}

	/**
	 * Creates a {@link ModuleContainerFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ModuleContainerFormBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Class<? extends ModuleContainer> getModelType() {
		return ModuleContainer.class;
	}

	@Override
	protected Class<? extends EditModel> getFormType(Object contextModel) {
		return EditModel.class;
	}

	@Override
	protected void fillFormModel(EditModel formModel, ModuleContainer businessModel) {
		String name = businessModel.getName();
		formModel.setName(name);
		ResKey key = ResKey.fallback(TLModelNamingConvention.modelPartNameKey(name), ResKey.text(name));
		formModel.setLabel(key);
		formModel.setDescription(key.tooltip());
	}

}

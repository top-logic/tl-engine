/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved
 */
package com.top_logic.element.layout.meta;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.layout.meta.MetaElementTreeModelBuilder.ModuleContainer;
import com.top_logic.element.layout.meta.ModuleContainerFormBuilder.EditModel;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.declarative.DeclarativeApplyHandler;
import com.top_logic.model.util.TLModelNamingConvention;

/**
 * Applies changes of the underlying {@link ModuleContainer}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class ModuleContainerApplyHandler extends DeclarativeApplyHandler<EditModel, ModuleContainer> {

	/**
	 * Creates a {@link ModuleContainerApplyHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ModuleContainerApplyHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void afterCommit(FormComponent form, EditModel editModel, ModuleContainer container) {
		try (ResourceTransaction tx = ResourcesModule.getInstance().startResourceTransaction()) {
			ResKey key = TLModelNamingConvention.modelPartNameKey(container.getName());

			TLMetaModelUtil.saveI18N(editModel, key, container.getLabel(), tx);

			tx.commit();
		}

		super.afterCommit(form, editModel, container);

		form.getMaster().invalidate();
	}

	@Override
	protected void storeValues(FormComponent formHandler, EditModel editModel, ModuleContainer container) {
		// Nothing.
	}

}

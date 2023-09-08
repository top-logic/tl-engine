/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Map;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.layout.meta.TLModuleFormBuilder.EditModel;
import com.top_logic.element.model.ModelResolver;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.AbstractCreateCommandHandler;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.util.model.ModelService;

/**
 * The {@link TLModuleCreateHandler} handles the creation of a new {@link TLModule}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLModuleCreateHandler extends AbstractCreateCommandHandler {

	/**
	 * Creates a {@link TLModuleCreateHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLModuleCreateHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
			Map<String, Object> arguments) {
		EditModel editModel = (EditModel) EditorFactory.getModel(formContainer);

		return createTLModule(editModel);
	}

	private TLModule createTLModule(EditModel editModel) {
		TLModel model = ModelService.getApplicationModel();

		Protocol log = new BufferingProtocol();
		ModelResolver resolver = new ModelResolver(log, model, ModelService.getInstance().getFactory());
		TLModule module = resolver.createModule(editModel);
		resolver.complete();

		try (ResourceTransaction tx = ResourcesModule.getInstance().startResourceTransaction()) {
			TLMetaModelUtil.saveI18NForPartNoLabelHeuristic(module, editModel, tx);

			tx.commit();
		}

		return module;
	}

}

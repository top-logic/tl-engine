/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.List;

import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.config.annotation.TLSingletons;
import com.top_logic.element.layout.meta.TLModuleFormBuilder.EditModel;
import com.top_logic.element.model.diff.apply.ApplyModelPatch;
import com.top_logic.element.model.diff.compare.CreateModelPatch;
import com.top_logic.element.model.diff.config.DiffElement;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.declarative.DeclarativeApplyHandler;
import com.top_logic.model.TLModule;
import com.top_logic.model.annotate.security.TLRoleDefinitions;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Applies changes to a {@link TLModule} model element.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLModuleApplyHandler extends DeclarativeApplyHandler<EditModel, TLModule> {

	/**
	 * Creates a {@link TLModuleApplyHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLModuleApplyHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void afterCommit(FormComponent formHandler, EditModel editModel, TLModule module) {
		try (ResourceTransaction tx = ResourcesModule.getInstance().startResourceTransaction()) {
			TLMetaModelUtil.saveI18NForPart(module, editModel, tx);

			tx.commit();
		}

		super.afterCommit(formHandler, editModel, module);
	}

	@Override
	protected void storeValues(FormComponent formHandler, EditModel editModel, TLModule module) {
		module.setName(editModel.getName());

		TLSingletons singletonsOld = module.getAnnotation(TLSingletons.class);
		TLSingletons singletonsNew = editModel.getAnnotation(TLSingletons.class);

		TLRoleDefinitions rolesOld = module.getAnnotation(TLRoleDefinitions.class);
		TLRoleDefinitions rolesNew = editModel.getAnnotation(TLRoleDefinitions.class);

		TLModelUtil.updateAnnotations(module, editModel.getAnnotations());

		CreateModelPatch analyzer = new CreateModelPatch();
		analyzer.addSingletonsPatch(module, singletonsOld, singletonsNew);
		analyzer.addRolesPatch(module, rolesOld, rolesNew);
		List<DiffElement> patch = analyzer.getPatch();

		if (!patch.isEmpty()) {
			Protocol log = new LogProtocol(TLModuleApplyHandler.class);
			ApplyModelPatch.applyPatch(log, module.getModel(), ModelService.getInstance().getFactory(), patch);
			log.checkErrors();
		}

		// If only the I18N has changed, the attributes has to been touched. To enforce an update of
		// other components displaying this attribute the module must be touched.
		module.tTouch();
	}

}

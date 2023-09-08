/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.layout.meta.TLStructuredTypeFormBuilder.EditModel;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.declarative.DeclarativeApplyHandler;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelUtil;

/**
 * Applies changes to a {@link TLClass} model element.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLStructuredTypeApplyHandler extends DeclarativeApplyHandler<EditModel, TLClass> {

	/**
	 * Creates a {@link TLStructuredTypeApplyHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TLStructuredTypeApplyHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void storeValues(FormComponent formHandler, EditModel editModel, TLClass typedModel) {
		typedModel.setAbstract(editModel.isAbstract());
		typedModel.setFinal(editModel.isFinal());
		updateGeneralizations(typedModel, editModel.getGeneralizationTypes());
		TLModelUtil.updateAnnotations(typedModel, editModel.getAnnotations());

		// If only the I18N has changed, the attributes has to be touched to enforce an update of
		// other components displaying this attribute.
		typedModel.tHandle().touch();
	}

	private void updateGeneralizations(TLClass type, List<TLClass> generalizations) {
		List<TLClass> current = type.getGeneralizations();
		generalizations = TLStructuredTypeCreateHandler.ensureTLObjectSuperType(type, generalizations);
		if (current.equals(generalizations)) {
			return;
		}
		current.clear();
		current.addAll(generalizations);
	}

	@Override
	protected void afterCommit(FormComponent formHandler, EditModel editModel, TLClass typedModel) {
		try (ResourceTransaction tx = ResourcesModule.getInstance().startResourceTransaction()) {
			TLMetaModelUtil.saveI18NForPart(typedModel, editModel, tx);

			tx.commit();
		}

		super.afterCommit(formHandler, editModel, typedModel);
	}

}

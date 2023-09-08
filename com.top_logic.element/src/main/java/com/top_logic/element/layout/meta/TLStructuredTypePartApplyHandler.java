/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.layout.meta.TLStructuredTypePartFormBuilder.EditModel;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.declarative.DeclarativeApplyHandler;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * Applies changes to a {@link TLStructuredTypePart} model element.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLStructuredTypePartApplyHandler extends DeclarativeApplyHandler<EditModel, TLStructuredTypePart> {

	/**
	 * Creates a {@link TLStructuredTypePartApplyHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TLStructuredTypePartApplyHandler(InstantiationContext context, Config config) {
		super(context, config);
	}
    
	@Override
	protected void storeValues(FormComponent formHandler, EditModel editModel, TLStructuredTypePart typedModel) {
		typedModel.setMandatory(editModel.getPartModel().getMandatory());
		TLModelUtil.updateAnnotations(typedModel, editModel.getPartModel().getAnnotations());
		
		// If only the I18N has changed, the attributes has to be touched to enforce an update of
		// other components displaying this attribute.
		typedModel.tHandle().touch();
	}

	@Override
	protected void afterCommit(FormComponent formHandler, EditModel editModel, TLStructuredTypePart typedModel) {
		try (ResourceTransaction tx = ResourcesModule.getInstance().startResourceTransaction()) {
			TLMetaModelUtil.saveI18NForPart(typedModel, editModel.getPartModel(), tx);

			tx.commit();
		}

		super.afterCommit(formHandler, editModel, typedModel);
	}

}

/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.layout.meta.TLStructuredTypeFormBuilder.EditModel;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.AbstractCreateCommandHandler;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.util.TLModelUtil;

/**
 * The {@link TLStructuredTypeCreateHandler} handles the creation of a new {@link TLClass}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLStructuredTypeCreateHandler extends AbstractCreateCommandHandler {

	/**
	 * Creates a {@link TLStructuredTypeCreateHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TLStructuredTypeCreateHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
			Map<String, Object> arguments) {
		EditModel editModel = (EditModel) EditorFactory.getModel(formContainer);

		TLClass result = createType(editModel);

		return result;
	}

	/**
	 * @param editModel
	 *        The configuration of the new type.
	 * @return The newly created type.
	 */
	protected TLClass createType(EditModel editModel) {
		TLModule module = editModel.getModule();
		TLClass newType = TLModelUtil.addClass(module, editModel.getName());

		newType.setAbstract(editModel.isAbstract());
		newType.setFinal(editModel.isFinal());
		List<TLClass> generalizationTypes = editModel.getGeneralizationTypes();
		generalizationTypes = ensureTLObjectSuperType(newType, generalizationTypes);
		newType.getGeneralizations().addAll(generalizationTypes);

		TLModelUtil.updateAnnotations(newType, editModel.getAnnotations());
		try (ResourceTransaction tx = ResourcesModule.getInstance().startResourceTransaction()) {
			TLMetaModelUtil.saveI18NForPart(newType, editModel, tx);

			tx.commit();
		}

		return newType;
	}

	static List<TLClass> ensureTLObjectSuperType(TLClass model, List<TLClass> generalizationTypes) {
		switch (generalizationTypes.size()) {
			case 0: {
				TLClass objectType = TLModelUtil.tlObjectType(model.getModel());
				return model.equals(objectType) ? Collections.emptyList() : Collections.singletonList(objectType);
			}
			case 1:
				return generalizationTypes;
			default: {
				TLClass objectType = TLModelUtil.tlObjectType(model.getModel());
				generalizationTypes.remove(objectType);
				return generalizationTypes;
			}
		}
	}

}

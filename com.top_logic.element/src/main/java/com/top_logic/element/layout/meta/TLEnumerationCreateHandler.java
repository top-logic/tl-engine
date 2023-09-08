/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.layout.meta.TLEnumerationFormBuilder.ClassifierModel;
import com.top_logic.element.layout.meta.TLEnumerationFormBuilder.EditModel;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.AbstractCreateCommandHandler;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.util.TLModelUtil;

/**
 * The {@link TLEnumerationCreateHandler} handles the creation of a new {@link TLEnumeration}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLEnumerationCreateHandler extends AbstractCreateCommandHandler {

	/**
	 * Creates a {@link TLEnumerationCreateHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLEnumerationCreateHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
			Map<String, Object> arguments) {
		EditModel editModel = (EditModel) EditorFactory.getModel(formContainer);

		TLEnumeration enumeration = createTLEnumeration(editModel);

		return enumeration;
	}

	private TLEnumeration createTLEnumeration(EditModel editModel) {
		TLEnumeration enumeration = TLModelUtil.addEnumeration(editModel.getModule(), editModel.getName());

		TLModelUtil.updateAnnotations(enumeration, editModel.getAnnotations());
		try (ResourceTransaction tx = ResourcesModule.getInstance().startResourceTransaction()) {
			TLMetaModelUtil.saveI18NForPart(enumeration, editModel, tx);
			createClassifiers(tx, editModel, enumeration);

			tx.commit();
		}
		return enumeration;
	}

	private void createClassifiers(ResourceTransaction tx, EditModel editModel, TLEnumeration enumeration) {
		for (ClassifierModel classifierModel : editModel.getI18NClassifiersConfig()) {
			createClassifier(tx, enumeration, classifierModel);
		}
	}

	private void createClassifier(ResourceTransaction tx, TLEnumeration enumeration, ClassifierModel classifierModel) {
		TLClassifier classifier = TLModelUtil.addClassifier(enumeration, classifierModel.getName());

		classifier.setDefault(classifierModel.isDefault());

		TLModelUtil.updateAnnotations(classifier, classifierModel.getAnnotations());
		TLMetaModelUtil.saveI18NForPart(classifier, classifierModel, tx);
	}

}

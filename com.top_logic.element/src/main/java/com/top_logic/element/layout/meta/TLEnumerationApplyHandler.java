/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.layout.meta.TLEnumerationFormBuilder.ClassifierModel;
import com.top_logic.element.layout.meta.TLEnumerationFormBuilder.EditModel;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.declarative.DeclarativeApplyHandler;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.util.TLModelUtil;

/**
 * Applies changes to a {@link TLEnumeration} model element.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TLEnumerationApplyHandler extends DeclarativeApplyHandler<EditModel, TLEnumeration> {

	private static final String CLASSIFIER_NOT_FOUND = "Classifier with name: %s not found.";

	/**
	 * Creates a {@link TLEnumerationApplyHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLEnumerationApplyHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void storeValues(FormComponent formHandler, EditModel editModel, TLEnumeration enumeration) {
		enumeration.setName(editModel.getName());

		TLModelUtil.updateAnnotations(enumeration, editModel.getAnnotations());

		List<ClassifierModel> classifierConfigs = editModel.getI18NClassifiersConfig();
		storeClassifiers(enumeration, classifierConfigs);

		// If only the I18N has changed, the attributes has to be touched to enforce an update of
		// other components displaying this attribute.
		enumeration.tHandle().touch();
	}

	private void storeClassifiers(TLEnumeration enumeration, List<ClassifierModel> classifierModels) {
		Map<String, TLClassifier> oldClassifiers = indexClassifiers(enumeration);

		List<TLClassifier> order = new ArrayList<>();
		for (ClassifierModel classifierModel : classifierModels) {
			String name = classifierModel.getName();

			TLClassifier newClassifier = takeOrCreateClassifier(oldClassifiers, enumeration, name);
			
			order.add(newClassifier);
			newClassifier.setDefault(classifierModel.isDefault());
			TLModelUtil.updateAnnotations(newClassifier, classifierModel.getAnnotations());
		}

		updateOrder(enumeration, order);
		KBUtils.deleteAll(oldClassifiers.values());
	}

	private Map<String, TLClassifier> indexClassifiers(TLEnumeration enumeration) {
		Map<String, TLClassifier> oldClassifiers = new HashMap<>();
		for (TLClassifier classifier : enumeration.getClassifiers()) {
			oldClassifiers.put(classifier.getName(), classifier);
		}
		return oldClassifiers;
	}

	private TLClassifier takeOrCreateClassifier(Map<String, TLClassifier> oldClassifiers, TLEnumeration enumeration,
			String name) {
		TLClassifier oldClassifier = oldClassifiers.remove(name);
		if (oldClassifier == null) {
			return TLModelUtil.addClassifier(enumeration, name);
		} else {
			return oldClassifier;
		}
	}

	private void updateOrder(TLEnumeration enumeration, List<TLClassifier> order) {
		List<TLClassifier> classifiers = enumeration.getClassifiers();
		classifiers.clear();
		classifiers.addAll(order);
	}

	@Override
	protected void afterCommit(FormComponent formHandler, EditModel editModel, TLEnumeration enumeration) {
		try (ResourceTransaction tx = ResourcesModule.getInstance().startResourceTransaction()) {
			TLMetaModelUtil.saveI18NForPart(enumeration, editModel, tx);

			for (ClassifierModel classifierModel : editModel.getI18NClassifiersConfig()) {
				saveI18NForClassifier(enumeration, classifierModel, tx);
			}

			tx.commit();
		}

		super.afterCommit(formHandler, editModel, enumeration);
	}

	private void saveI18NForClassifier(TLEnumeration enumeration, ClassifierModel classifierModel,
			ResourceTransaction tx) {
		String name = classifierModel.getName();
		TLClassifier classifier = enumeration.getClassifier(name);

		if (classifier == null) {
			Logger.error(String.format(CLASSIFIER_NOT_FOUND, name), this);
		}

		TLMetaModelUtil.saveI18NForPart(classifier, classifierModel, tx);
	}

}

/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.util.Utils;
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

		Set<TLClassifier> existingClassifiers = storeClassifiers(classifierConfigs, enumeration);

		deleteRemovedClassifiers(enumeration, existingClassifiers);

		updateClassifierOrder(classifierConfigs, enumeration);

		// If only the I18N has changed, the attributes has to be touched to enforce an update of
		// other components displaying this attribute.
		enumeration.tHandle().touch();
	}

	private void updateClassifierOrder(List<ClassifierModel> classifierModels, TLEnumeration enumeration) {
		int size = classifierModels.size();
		List<TLClassifier> classifiers = enumeration.getClassifiers();
		assert size == classifiers.size();

		int unchangedOrderLength = 0;
		while (true) {
			if (unchangedOrderLength == size) {
				// No order change;
				return;
			}
			if (!Utils.equals(classifiers.get(unchangedOrderLength).getName(),
				classifierModels.get(unchangedOrderLength).getName())) {
				break;
			}
			unchangedOrderLength++;
		}

		TLClassifier[] ordered = order(unchangedOrderLength, size, classifierModels, classifiers);
		classifiers.subList(unchangedOrderLength, size).clear();
		classifiers.addAll(unchangedOrderLength, Arrays.asList(ordered));
	}

	/**
	 * Orders the {@link TLClassifier}s from <code>from</code> (incl.) to <code>to</code> (excl.) in
	 * <code>classifiers</code> such that it has the same (name) order as the
	 * {@link ClassifierModel}s in the corresponding <code>classifierModels</code>.
	 * 
	 * <p>
	 * It is expected that the elements have the same names.
	 * </p>
	 */
	private TLClassifier[] order(int from, int to, List<ClassifierModel> classifierModels,
			List<TLClassifier> classifiers) {
		Map<String, Integer> indexMap = new HashMap<>();
		for (int i = from; i < to; i++) {
			indexMap.put(classifierModels.get(i).getName(), i - from);
		}
		TLClassifier[] orderedClassifier = new TLClassifier[to - from];
		for (int i = from; i < to; i++) {
			TLClassifier classifier = classifiers.get(i);
			orderedClassifier[indexMap.get(classifier.getName())] = classifier;
		}
		return orderedClassifier;
	}

	private Set<TLClassifier> storeClassifiers(List<ClassifierModel> classifierModels, TLEnumeration enumeration) {
		Set<TLClassifier> existingClassifiers = new HashSet<>();

		for (ClassifierModel classifierModel : classifierModels) {
			TLClassifier classifier = storeClassifier(enumeration, classifierModel);

			existingClassifiers.add(classifier);
		}

		return existingClassifiers;
	}

	private TLClassifier storeClassifier(TLEnumeration enumeration, ClassifierModel classifierModel) {
		String name = classifierModel.getName();
		TLClassifier classifier = enumeration.getClassifier(name);

		if (classifier == null) {
			classifier = TLModelUtil.addClassifier(enumeration, name);
		}

		classifier.setDefault(classifierModel.isDefault());

		TLModelUtil.updateAnnotations(classifier, classifierModel.getAnnotations());

		return classifier;
	}

	private void deleteRemovedClassifiers(TLEnumeration enumeration, Set<TLClassifier> existingClassifiers) {
		KBUtils.deleteAll(CollectionUtil.difference(new HashSet<>(enumeration.getClassifiers()), existingClassifiers));
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

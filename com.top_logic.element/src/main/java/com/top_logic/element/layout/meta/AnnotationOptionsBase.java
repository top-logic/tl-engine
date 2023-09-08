/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * Base class for option provider functions selecting compatible annotations based on the
 * {@link TLTypeKind}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public abstract class AnnotationOptionsBase extends Function0<OptionModel<Class<?>>> {

	private final AnnotationCustomizations _customizations;

	private final TLTypeKind _targetType;

	private final Class<? extends TLAnnotation> _annotationType;

	private final Predicate<Class<?>> _hasValidClassifier;

	/**
	 * Creates a {@link AnnotationOptionsBase}.
	 */
	public AnnotationOptionsBase(DeclarativeFormOptions options, TLTypeKind targetType,
			Class<? extends TLAnnotation> annotationType, Predicate<Class<?>> hasValidClassifier) {
		_targetType = targetType;
		_customizations = options.getCustomizations();
		_annotationType = annotationType;
		_hasValidClassifier = hasValidClassifier;
	}

	@Override
	public OptionModel<Class<?>> apply() {
		Collection<Class<?>> annotationTypes =
			TypeIndex.getInstance().getSpecializations(_annotationType, true, true, false);
		ArrayList<Class<?>> result = new ArrayList<>();
		for (Class<?> annotationType : annotationTypes) {
			InApp inapp = _customizations.getAnnotation(annotationType, InApp.class);
			if (inapp == null || !inapp.value() || !_hasValidClassifier.test(annotationType)) {
				continue;
			}
			Hidden hidden = _customizations.getAnnotation(annotationType, Hidden.class);
			if (hidden != null && hidden.value()) {
				continue;
			}
			TargetType targetAnnotation = annotationType.getAnnotation(TargetType.class);
			if (targetAnnotation != null) {
				if (ArrayUtil.indexOf(_targetType, targetAnnotation.value()) < 0) {
					continue;
				}
			}
			result.add(annotationType);
		}
		return new DefaultListOptionModel<>(result);
	}

}

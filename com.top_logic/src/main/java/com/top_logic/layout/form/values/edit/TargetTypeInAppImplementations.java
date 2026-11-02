/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.func.Function2;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.model.utility.TargetTypeFilter;
import com.top_logic.layout.form.model.utility.TypeKindFilter;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.AllInAppImplementations.PrioritizedListOptionModel;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link Options} function offering the {@link InApp} implementations of the annotated property's
 * type (see {@link AllInAppImplementations}) that are compatible with a given model type.
 *
 * <p>
 * The function takes the model type and its {@link TLTypeKind} as arguments. An implementation
 * without a {@link TargetType} annotation is always offered. An implementation with a
 * {@link TargetType} annotation is offered as follows:
 * </p>
 * <ul>
 * <li>If both the type and the kind are given, the implementation is offered if one of its
 * {@link TargetType#value() kinds} is the given kind and, if it names
 * {@link TargetType#name() types}, the given type is compatible with one of them (see
 * {@link TargetTypeFilter}).</li>
 * <li>If only the kind is given, the implementation is offered if one of its kinds is the given
 * kind and it names no types, because the compatibility with a named type cannot be decided
 * without a concrete type (see {@link TypeKindFilter}).</li>
 * <li>If the kind is not given, the implementation is not offered, because its restriction cannot
 * be checked at all.</li>
 * </ul>
 *
 * <p>
 * The option model selects the implementation with the highest {@link InApp#priority()} as
 * default.
 * </p>
 *
 * @see TargetTypeFilter
 * @see TypeKindFilter
 */
public class TargetTypeInAppImplementations extends Function2<ListOptionModel<Class<?>>, TLType, TLTypeKind>
		implements OptionMappingProvider {

	private final AllInAppImplementations _inApp;

	private final AnnotationCustomizations _customizations;

	/**
	 * Creates a {@link TargetTypeInAppImplementations}.
	 */
	@CalledByReflection
	public TargetTypeInAppImplementations(DeclarativeFormOptions options) {
		_inApp = new AllInAppImplementations(options);
		_customizations = options.getCustomizations();
	}

	@Override
	public ListOptionModel<Class<?>> apply(TLType type, TLTypeKind kind) {
		Predicate<Class<?>> filter = createFilter(type, kind);
		List<? extends Class<?>> options =
			_inApp.acceptableImplementations().filter(filter).collect(Collectors.toList());
		return new PrioritizedListOptionModel(_customizations, options);
	}

	private Predicate<Class<?>> createFilter(TLType type, TLTypeKind kind) {
		if (kind == null) {
			return this::isUnrestricted;
		}
		if (type == null) {
			return new TypeKindFilter(_customizations, kind);
		}
		return new TargetTypeFilter(_customizations, type, kind);
	}

	private boolean isUnrestricted(Class<?> option) {
		return _customizations.getAnnotation(option, TargetType.class) == null;
	}

	@Override
	public OptionMapping getOptionMapping() {
		return _inApp.getOptionMapping();
	}

}

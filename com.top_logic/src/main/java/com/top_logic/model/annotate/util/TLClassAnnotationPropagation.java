/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.util;

import static com.top_logic.model.util.TLModelUtil.*;
import static java.util.Objects.*;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.graph.DAGPropertyPropagation;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.model.TLClass;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Calculates the effective annotation of the given {@link TLTypeAnnotation} type for the overrides
 * of the given {@link TLClass}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class TLClassAnnotationPropagation extends DAGPropertyPropagation<TLClass, TLTypeAnnotation> {

	private final Class<? extends TLTypeAnnotation> _annotationType;

	/**
	 * Creates a {@link TLClassAnnotationPropagation}.
	 * 
	 * @param annotationType
	 *        Is not allowed to be null.
	 */
	public TLClassAnnotationPropagation(Class<? extends TLTypeAnnotation> annotationType) {
		super(TLModelUtil.getAllGlobalClasses(ModelService.getApplicationModel()));
		_annotationType = requireNonNull(annotationType);
	}

	@Override
	protected Collection<TLClass> getSuccessors(TLClass type) {
		return type.getSpecializations();
	}

	@Override
	protected Collection<TLClass> getPredecessors(TLClass type) {
		return type.getGeneralizations();
	}

	@Override
	protected TLTypeAnnotation getLocalProperty(TLClass type) {
		return type.getAnnotation(_annotationType);
	}

	@Override
	protected TLTypeAnnotation mergeProperties(TLClass type, TLTypeAnnotation local, List<TLTypeAnnotation> inherited) {
		return mergeAnnotations(qualifiedName(type), local, inherited, _annotationType);
	}

	static <T extends TLAnnotation> T mergeAnnotations(String modelPart, T local, List<T> inherited,
			Class<? extends TLAnnotation> annotationType) {
		if (local != null) {
			return local;
		}
		if (inherited.isEmpty()) {
			return null;
		}
		if (inherited.size() == 1) {
			return inherited.get(0);
		}
		/* Optimization: First check with the normal equality, as it is much faster, than check with
		 * ConfigEquality: */
		if (CollectionUtilShared.isEverythingEqual(inherited)) {
			return inherited.get(0);
		}
		if (CollectionUtilShared.isEverythingEqual(inherited, ConfigEquality.INSTANCE_ALL_BUT_DERIVED::equals)) {
			return inherited.get(0);
		}
		throw new ConfigurationError(
			I18NConstants.TL_ANNOTATION_PROPAGATION_UNRESOLVED_CONFLICT.fill(annotationType, modelPart));
	}

}

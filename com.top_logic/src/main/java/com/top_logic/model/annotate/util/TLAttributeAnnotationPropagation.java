/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.util;

import static com.top_logic.model.util.TLModelUtil.*;
import static java.util.Collections.*;
import static java.util.Objects.*;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.graph.DAGPropertyPropagation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.util.TLModelUtil;

/**
 * Calculates the effective annotation of the given {@link TLAttributeAnnotation} type for the
 * overrides of the given {@link TLClassPart}.
 * <p>
 * This class uses a {@link Pair} of {@link TLClass} and {@link String} (name) instead of
 * {@link TLClassPart}, as there are no {@link TLClassPart}s for inherited attributes which are not
 * overrides: The {@link TLClassPart} of the definition is used instead. But that has the "wrong"
 * owner, as it is the owner of the definition, not of the inheriting {@link TLClass}, of course.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class TLAttributeAnnotationPropagation
		extends DAGPropertyPropagation<Pair<TLClass, String>, TLAttributeAnnotation> {

	private final Class<? extends TLAttributeAnnotation> _annotationType;

	/**
	 * Creates a {@link TLAttributeAnnotationPropagation}.
	 * 
	 * @param annotationType
	 *        Is not allowed to be null.
	 * @param attribute
	 *        Is not allowed to be null.
	 */
	public TLAttributeAnnotationPropagation(Class<? extends TLAttributeAnnotation> annotationType,
			TLClassPart attribute) {
		super(singleton(requireNonNull(TLModelUtil.toPair(attribute))));
		_annotationType = requireNonNull(annotationType);
	}

	@Override
	protected Collection<Pair<TLClass, String>> getSuccessors(Pair<TLClass, String> attribute) {
		return TLModelUtil.getSuccessors(attribute);
	}

	@Override
	protected Collection<Pair<TLClass, String>> getPredecessors(Pair<TLClass, String> attribute) {
		return TLModelUtil.getPredecessors(attribute);
	}

	@Override
	protected TLAttributeAnnotation getLocalProperty(Pair<TLClass, String> attribute) {
		return TLModelUtil.toClassPart(attribute)
			.map(classPart -> classPart.getAnnotation(_annotationType)).orElse(null);
	}

	@Override
	protected TLAttributeAnnotation mergeProperties(Pair<TLClass, String> attribute, TLAttributeAnnotation local,
			List<TLAttributeAnnotation> inherited) {
		String qualifiedAttributeName =
			qualifiedName(attribute.getFirst()) + TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR + attribute.getSecond();
		return TLClassAnnotationPropagation.mergeAnnotations(qualifiedAttributeName, local, inherited, _annotationType);
	}

}

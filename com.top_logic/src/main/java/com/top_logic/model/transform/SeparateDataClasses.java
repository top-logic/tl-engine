/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.xref.AllClasses;

/**
 * {@link SeparateDataClasses} eliminates all inheritances within the "tl5.data"
 * module.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SeparateDataClasses extends ModelTransformation {

	/**
	 * Creates a {@link SeparateDataClasses} transformation.
	 *
	 * @param log
	 *        See {@link ModelTransformation#ModelTransformation(Protocol, TLModel)}.
	 * @param index
	 *        See {@link ModelTransformation#ModelTransformation(Protocol, TLModel)}.
	 */
	public SeparateDataClasses(Protocol log, TLModel index) {
		super(log, index);
	}

	@Override
	public void transform() {
		final TLModule dataModule = index.getModule("tl5.data");
		List<TLClass> allDataClasses = new ArrayList<>(AllClasses.findAllClasses(dataModule));
		allDataClasses = CollectionUtil.topsort(TLModelUtil.GET_GENERALIZATIONS, allDataClasses, true);
		FilterUtil.filterInline(new Filter<TLClass>() {
			@Override
			public boolean accept(TLClass anObject) {
				return anObject.getScope() == dataModule;
			}
		}, allDataClasses);
		Collections.reverse(allDataClasses);
		
		// Copy all local class parts of all classes to all concrete specializations.
		for (TLClass clazz : allDataClasses) {
			for (TLClass specialization : TLModelUtil.getTransitiveSpecializations(TLModelUtil.IS_CONCRETE, clazz)) {
				if (specialization.getScope() != dataModule) {
					continue;
				}
				copyProperties(clazz, specialization);
			}
		}
		
		
		// Make all data classes top-level.
		// Remove all inheritance relations between data classes. Otherwise, sum
		// types expand along the inheritance hierarchy of data classes.
		for (TLClass clazz : allDataClasses) {
			// Drop all super classes from data module.
			for (Iterator<TLClass> it = clazz.getGeneralizations().iterator(); it.hasNext(); ) {
				TLClass generalization = it.next();
				if (generalization.getScope() == dataModule) {
					it.remove();
				}
			}
		}
	}

	private void copyProperties(TLStructuredType from, TLStructuredType to) {
		for (TLProperty orig : TLModelUtil.getLocalProperties(from)) {
			copyProperty(orig, to);
		}
	}

	private TLProperty copyProperty(TLProperty orig, TLStructuredType to) {
		TLProperty copy = TLModelUtil.addProperty(to, orig.getName(), orig.getType());
		copy.setDerived(orig.isDerived());
		copyAnnotations(orig, copy);
		
		return copy;
	}

	private void copyAnnotations(TLModelPart from, TLModelPart to) {
		for (TLAnnotation annotation : from.getAnnotations()) {
			to.setAnnotation(annotation);
		}
	}


}


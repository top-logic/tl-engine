/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.xref;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.visit.DefaultDescendingTLModelVisitor;

/**
 * Builder for a relation that points to all direct extensions of a given class.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HierarchyBuilder extends DefaultDescendingTLModelVisitor<Map<TLClass, Set<TLClass>>> {
	
	/**
	 * Singleton {@link HierarchyBuilder} instance.
	 */
	public static final HierarchyBuilder INSTANCE = new HierarchyBuilder();

	private HierarchyBuilder() {
		// Singleton constructor.
	}

	/**
	 * Builds a mapping from {@link TLClass} to their usage in
	 * {@link TLClass#getGeneralizations()} properties of other classes.
	 * 
	 * @param model
	 *        The model node from which the specializations relation should be
	 *        built.
	 * @return The computed mapping.
	 */
	public static Map<TLClass, Set<TLClass>> buildSpecializations(TLModelPart model) {
		HashMap<TLClass, Set<TLClass>> specializations = new HashMap<>();
		model.visit(INSTANCE, specializations);
		return specializations;
	}
	
	@Override
	public Void visitClass(TLClass model, Map<TLClass, Set<TLClass>> specializations) {
		for (TLClass generalization : model.getGeneralizations()) {
			MultiMaps.add(specializations, generalization, model);
		}
		return super.visitClass(model, specializations);
	}

}

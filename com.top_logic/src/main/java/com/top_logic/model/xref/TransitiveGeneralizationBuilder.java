/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.xref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;

/**
 * Builder for the transitive (non reflexive) hull of the
 * {@link TLClass#getGeneralizations()} relation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TransitiveGeneralizationBuilder {

	private TransitiveGeneralizationBuilder() {
		// Singleton constructor.
	}

	/**
	 * Build the transitive (non-reflexive) hull of the
	 * {@link TLClass#getGeneralizations()} relation.
	 * 
	 * @param model
	 *        The part in the model to start the search.
	 * 
	 * @return The computed relation.
	 */
	public static Map<TLClass, Set<TLClass>> buildTransitiveGeneralizations(TLModelPart model) {
		Map<TLClass, Set<TLClass>> generalizations = new HashMap<>();
		Set<TLClass> done = new HashSet<>();
		
		List<TLClass> workPool = new ArrayList<>();
		model.visit(AllClasses.INSTANCE, workPool);
		
		for (TLClass clazz : workPool) {
			process(generalizations, done, clazz);
		}
		
		return generalizations;
	}

	private static void process(Map<TLClass, Set<TLClass>> generalizations, Set<TLClass> done, TLClass clazz) {
		if (done.contains(clazz)) {
			return;
		}

		// Add before recursive descending to super classes to prevent endless
		// recursion in case of cyclic inheritance hierarchy.
		done.add(clazz);
		
		MultiMaps.addAll(generalizations, clazz, clazz.getGeneralizations());
		
		for (TLClass generalization : clazz.getGeneralizations()) {
			// Make sure, all generalizations of all super classes have been
			// found.
			process(generalizations, done, generalization);

			Set<TLClass> generalizationsOfSuper = generalizations.get(generalization);
			if (generalizationsOfSuper != null && (! generalizationsOfSuper.isEmpty())) {
				MultiMaps.addAll(generalizations, clazz, generalizationsOfSuper);
			}
		}
	}

}

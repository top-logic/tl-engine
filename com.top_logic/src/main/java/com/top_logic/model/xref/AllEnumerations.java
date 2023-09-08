/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.xref;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModelVisitor;
import com.top_logic.model.visit.DefaultDescendingTLModelVisitor;

/**
 * {@link TLModelVisitor} that finds all {@link TLEnumeration} definitions in a
 * {@link TLModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AllEnumerations extends DefaultDescendingTLModelVisitor<Collection<TLEnumeration>> {
	
	private final Filter<? super TLEnumeration> filter;
	
	/**
	 * Singleton {@link AllEnumerations} instance.
	 */
	public static final AllEnumerations INSTANCE = new AllEnumerations(FilterFactory.trueFilter());

	private AllEnumerations(Filter<? super TLEnumeration> filter) {
		this.filter = filter;
	}

	/**
	 * Builds a set of all associations defined in the given model node matching a
	 * given filter.
	 * 
	 * @param model
	 *        The model node to search {@link TLEnumeration}s in.
	 * @param filter
	 *        The filter that selects classes.
	 * @return The set of found {@link TLEnumeration}s matching the given filter.
	 */
	public static Set<TLEnumeration> findAllEnumerations(TLModelPart model, Filter<? super TLEnumeration> filter) {
		HashSet<TLEnumeration> allEnumerations = new HashSet<>();
		model.visit(new AllEnumerations(filter), allEnumerations);
		return allEnumerations;
	}
	
	/**
	 * Builds a set of all classes defined in the given model node.
	 * 
	 * @param model
	 *        The model node to search {@link TLEnumeration}s in.
	 * @return The set of found {@link TLEnumeration}s.
	 */
	public static Set<TLEnumeration> findAllEnumerations(TLModelPart model) {
		HashSet<TLEnumeration> allEnumerations = new HashSet<>();
		model.visit(INSTANCE, allEnumerations);
		return allEnumerations;
	}
	
	@Override
	public Void visitEnumeration(TLEnumeration model, Collection<TLEnumeration> allEnumerations) {
		if (filter.accept(model)) {
			allEnumerations.add(model);
		}
		return super.visitEnumeration(model, allEnumerations);
	}

}

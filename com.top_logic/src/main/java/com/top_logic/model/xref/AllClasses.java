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
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModelVisitor;
import com.top_logic.model.visit.DefaultDescendingTLModelVisitor;

/**
 * {@link TLModelVisitor} that finds all {@link TLClass} definitions in a
 * {@link TLModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AllClasses extends DefaultDescendingTLModelVisitor<Collection<TLClass>> {
	
	private final Filter<? super TLClass> filter;
	
	/**
	 * Singleton {@link AllClasses} instance.
	 */
	public static final AllClasses INSTANCE = new AllClasses(FilterFactory.trueFilter());

	private AllClasses(Filter<? super TLClass> filter) {
		this.filter = filter;
	}

	/**
	 * Builds a set of all classes defined in the given model node matching a
	 * given filter.
	 * 
	 * @param model
	 *        The model node to search {@link TLClass}es in.
	 * @param filter
	 *        The filter that selects classes.
	 * @return The set of found {@link TLClass}es matching the given filter.
	 */
	public static Set<TLClass> findAllClasses(TLModelPart model, Filter<? super TLClass> filter) {
		HashSet<TLClass> allClasses = new HashSet<>();
		model.visit(new AllClasses(filter), allClasses);
		return allClasses;
	}
	
	/**
	 * Builds a set of all classes defined in the given model node.
	 * 
	 * @param model
	 *        The model node to search {@link TLClass}es in.
	 * @return The set of found {@link TLClass}es.
	 */
	public static Set<TLClass> findAllClasses(TLModelPart model) {
		HashSet<TLClass> allClasses = new HashSet<>();
		model.visit(INSTANCE, allClasses);
		return allClasses;
	}
	
	@Override
	public Void visitClass(TLClass model, Collection<TLClass> allClasses) {
		if (filter.accept(model)) {
			allClasses.add(model);
		}
		return super.visitClass(model, allClasses);
	}

}

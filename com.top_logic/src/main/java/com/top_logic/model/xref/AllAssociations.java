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
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModelVisitor;
import com.top_logic.model.visit.DefaultDescendingTLModelVisitor;

/**
 * {@link TLModelVisitor} that finds all {@link TLAssociation} definitions in a
 * {@link TLModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AllAssociations extends DefaultDescendingTLModelVisitor<Collection<TLAssociation>> {
	
	private final Filter<? super TLAssociation> filter;
	
	/**
	 * Singleton {@link AllAssociations} instance.
	 */
	public static final AllAssociations INSTANCE = new AllAssociations(FilterFactory.trueFilter());

	private AllAssociations(Filter<? super TLAssociation> filter) {
		this.filter = filter;
	}

	/**
	 * Builds a set of all associations defined in the given model node matching a
	 * given filter.
	 * 
	 * @param model
	 *        The model node to search {@link TLAssociation}s in.
	 * @param filter
	 *        The filter that selects classes.
	 * @return The set of found {@link TLAssociation}s matching the given filter.
	 */
	public static Set<TLAssociation> findAllAssociations(TLModelPart model, Filter<? super TLAssociation> filter) {
		HashSet<TLAssociation> allAssociations = new HashSet<>();
		model.visit(new AllAssociations(filter), allAssociations);
		return allAssociations;
	}
	
	/**
	 * Builds a set of all classes defined in the given model node.
	 * 
	 * @param model
	 *        The model node to search {@link TLAssociation}s in.
	 * @return The set of found {@link TLAssociation}s.
	 */
	public static Set<TLAssociation> findAllAssociations(TLModelPart model) {
		HashSet<TLAssociation> allAssociations = new HashSet<>();
		model.visit(INSTANCE, allAssociations);
		return allAssociations;
	}
	
	@Override
	public Void visitAssociation(TLAssociation model, Collection<TLAssociation> allAssociations) {
		if (filter.accept(model)) {
			allAssociations.add(model);
		}
		return super.visitAssociation(model, allAssociations);
	}

}

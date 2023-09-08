/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.visit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLNamedPart;

/**
 * Visitor that descends through all parts of a {@link TLModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultDescendingTLModelVisitor<A> extends DefaultTLModelVisitor<Void, A> {

	private static final Void none = null;

	/**
	 * Whether list of {@link TLNamedPart}s should be traversed in alphabetical
	 * order of their names.
	 */
	private final boolean ordered;

	/**
	 * Creates a {@link DefaultDescendingTLModelVisitor} that does a plain
	 * descend without sorting nodes.
	 */
	public DefaultDescendingTLModelVisitor() {
		this(false);
	}

	/**
	 * Creates a {@link DefaultDescendingTLModelVisitor}.
	 * 
	 * @param ordered
	 *        Whether to descend {@link Collection}s of {@link TLNamedPart}s in
	 *        alphabetical order.
	 */
	public DefaultDescendingTLModelVisitor(boolean ordered) {
		this.ordered = ordered;
	}
	
	@Override
	public Void visitModel(TLModel model, A arg) {
		super.visitModel(model, arg);
		descend(sort(model.getModules()), arg);
		return none; 
	}
	
	private <P extends TLNamedPart> Iterable<P> sort(Iterable<P> parts) {
		if (! ordered) {
			return parts;
		}
		
		ArrayList<P> result = CollectionUtil.toListIterable(parts);
		Collections.sort(result, new Comparator<P>() {

			@Override
			public int compare(P o1, P o2) {
				String o1Name = o1.getName();
				String o2Name = o2.getName();
				if (o1Name == null) {
					if (o2Name == null) {
						return 0;
					}
					
					return 1;
				} else {
					if (o2Name == null) {
						return -1;
					}
				}
				
				return o1Name.compareTo(o2Name);
			}
		});
		return result;
	}

	@Override
	public Void visitModule(TLModule model, A arg) {
		super.visitModule(model, arg);
		descend(sort(model.getTypes()), arg);
		return none;
	}

	@Override
	public Void visitAssociation(TLAssociation model, A arg) {
		super.visitAssociation(model, arg);
		descend(model.getAssociationParts(), arg);
		return none;
	}

	@Override
	public Void visitClass(TLClass model, A arg) {
		super.visitClass(model, arg);
		descend(model.getLocalClassParts(), arg);
		return none;
	}

	@Override
	public Void visitEnumeration(TLEnumeration model, A arg) {
		super.visitEnumeration(model, arg);
		descend(model.getClassifiers(), arg);
		return none;
	}
	
	protected Void descend(Iterable<? extends TLModelPart> parts, A arg) {
		for (TLModelPart part : parts) {
			descend(part, arg);
		}
		return none;
	}

	protected void descend(TLModelPart part, A arg) {
		part.visit(this, arg);
	}

}

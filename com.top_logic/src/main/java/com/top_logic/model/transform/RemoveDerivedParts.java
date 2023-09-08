/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.Protocol;
import com.top_logic.model.DerivedTLTypePart;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.visit.DefaultDescendingTLModelVisitor;

/**
 * {@link ModelTransformation} that removes derived {@link TLStructuredTypePart}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RemoveDerivedParts extends ModelTransformation {

	/**
	 * Creates a {@link RemoveDerivedParts} transformation.
	 * 
	 * @param log
	 *        See {@link ModelTransformation#ModelTransformation(Protocol, TLModel)}.
	 * @param index
	 *        See {@link ModelTransformation#ModelTransformation(Protocol, TLModel)}.
	 */
	public RemoveDerivedParts(Protocol log, TLModel index) {
		super(log, index);
	}

	@Override
	public void transform() {
		Collection<String> excludeNames = getExcludes();
			
		final Set<TLTypePart> excludes = TLModelUtil.findParts(log, index, excludeNames);
		
		Collection<TLTypePart> toRemove = new ArrayList<>();
		index.visit(new DefaultDescendingTLModelVisitor<Collection<TLTypePart>>() {

			@Override
			public Void visitProperty(TLProperty model, Collection<TLTypePart> arg) {
				handleDerivedTLTypePart(model, arg);
				return super.visitProperty(model, arg);
			}

			@Override
			public Void visitReference(TLReference model, Collection<TLTypePart> arg) {
				handleDerivedTLTypePart(model, arg);
				return super.visitReference(model, arg);
			}

			private void handleDerivedTLTypePart(DerivedTLTypePart model, Collection<TLTypePart> arg) {
				if (model.isDerived() && (! excludes.contains(model))) {
					arg.add(model);
				}
			}
		}, toRemove);
		
		for (TLTypePart part : toRemove) {
			removePart(part);
		}
	}

	/**
	 * The part names that should be excluded from removal.
	 * 
	 * TODO: Make things configurable
	 */
	protected Collection<String> getExcludes() {
		return Collections.emptyList();
	}

}

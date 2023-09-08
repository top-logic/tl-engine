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
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModelVisitor;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.visit.DefaultDescendingTLModelVisitor;

/**
 * {@link TLModelVisitor} that builds up a mapping from {@link TLType}s to their
 * usages in {@link TLTypePart}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TypeUsage extends DefaultDescendingTLModelVisitor<Map<TLType, Set<TLTypePart>>> {
	
	/**
	 * Singleton {@link TypeUsage} instance.
	 */
	public static final TypeUsage INSTANCE = new TypeUsage();

	private TypeUsage() {
		// Singleton constructor.
	}

	/**
	 * Builds the usage map that maps {@link TLType}s to threir usage in
	 * {@link TLTypePart}s.
	 * 
	 * @param model
	 *        The model node from which usage information should be computed.
	 * @return The computed type usage mapping.
	 */
	public static Map<TLType, Set<TLTypePart>> buildTypeUsage(TLModelPart model) {
		HashMap<TLType, Set<TLTypePart>> specializations = new HashMap<>();
		model.visit(INSTANCE, specializations);
		return specializations;
	}
	
	@Override
	protected Void visitTypePart(TLTypePart model, Map<TLType, Set<TLTypePart>> usage) {
		MultiMaps.add(usage, model.getType(), model);
		return super.visitTypePart(model, usage);
	}

}

/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

/**
 * Visitor for {@link BranchIdType}.
 * 
 * @see BranchIdType#visit(BranchIdTypeVisitor, Object)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface BranchIdTypeVisitor<R, A> {

	/**
	 * Visit case for {@link BranchIdType}.
	 */
	R visitBranchIdType(BranchIdType self, A arg);

	/**
	 * Visit case for {@link Module}.
	 */
	R visitModule(Module self, A arg);

	/**
	 * Visit case for {@link Type}.
	 */
	R visitType(Type self, A arg);

	/**
	 * Visit case for {@link TypePart}.
	 */
	R visitTypePart(TypePart self, A arg);

}


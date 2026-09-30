/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.schema;

import com.top_logic.model.instance.importer.UnresolvedRef;

/**
 * Visitor interface for the {@link ValueConf} hierarchy.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ValueVisitor<R, A> {

	/**
	 * Visit case for {@link InstanceRefConf}.
	 */
	R visit(InstanceRefConf ref, A arg) throws UnresolvedRef;

	/**
	 * Visit case for {@link ModelRefConf}.
	 */
	R visit(ModelRefConf ref, A arg);

	/**
	 * Visit case for {@link GlobalRefConf}.
	 */
	R visit(GlobalRefConf ref, A arg);

	/**
	 * Visit case for {@link PrimitiveValueConf}.
	 */
	R visit(PrimitiveValueConf value, A arg);

	/**
	 * Visit case for {@link ObjectConf}.
	 */
	R visit(ObjectConf obj, A arg);

	/**
	 * Visit case for {@link CustomValueConf} sub-configurations.
	 */
	R visit(CustomValueConf value, A arg);

}

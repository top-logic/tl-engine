/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import java.util.Collection;

import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.impl.generated.TLModelBase;

/**
 * Root of a <i>TopLogic</i> runtime model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLModel extends TLModelFactory, TLModelBase {

	/**
	 * All {@link TLModule}s of this model.
	 */
	@Name(MODULES_ATTR)
	@Key(TLModule.NAME_ATTRIBUTE)
	Collection<TLModule> getModules();

	/**
	 * Find the {@link TLModule} with the given name.
	 * 
	 * @return The {@link TLModule} of this model with the given
	 *         {@link TLModule#getName() name}, or <code>null</code>, if this
	 *         model has no module with the given name.
	 */
	@Indexed(collection = MODULES_ATTR)
	TLModule getModule(String name);
	
	/**
	 * Implementation of the given query interface type.
	 * 
	 * @param queryInterface
	 *        The query interface.
	 * @return The query resolver implementation.
	 * @throws UnsupportedOperationException
	 *         If this model does not support the requested query resolver.
	 */
	<T extends TLQuery> T getQuery(Class<T> queryInterface) throws UnsupportedOperationException;

	/**
	 * {@link TLModel} is the root of the model hierarchy.
	 * 
	 * @see com.top_logic.model.TLModelPart#getModel()
	 */
	@Override
	default TLModel getModel() {
		return this;
	}

	@Override
	default ModelKind getModelKind() {
		return ModelKind.MODEL;
	}

	@Override
	default <R, A> R visit(TLModelVisitor<R, A> v, A arg) {
		return v.visitModel(this, arg);
	}

}

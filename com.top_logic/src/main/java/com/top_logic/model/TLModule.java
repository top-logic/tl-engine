/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import java.util.Collection;

import com.top_logic.model.impl.generated.TLModuleBase;

/**
 * A module of a {@link TLModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLModule extends TLModuleBase {

	/**
	 * The default name for a modules (primary) singleton.
	 */
	String DEFAULT_SINGLETON_NAME = "ROOT";

	/**
	 * The singletons of this module.
	 */
	default Collection<TLModuleSingleton> getSingletons() {
		return getModel().getQuery(TLModuleSingletons.class).getSingletons(this);
	}

	/**
	 * The singleton with the given name.
	 * 
	 * @see TLModuleSingletons#getSingleton(TLModule, String)
	 * @see #DEFAULT_SINGLETON_NAME
	 */
	default TLObject getSingleton(String singletonName) {
		return getModel().getQuery(TLModuleSingletons.class).getSingleton(this, singletonName);
	}
	
	/**
	 * Adds a singleton to this {@link TLModule}.
	 * 
	 * @param singletonName
	 *        The name of the singleton.
	 * @param newSingleton
	 *        The new singleton object.
	 * @return The old singleton formerly registered at this module under the given name, or
	 *         <code>null</code> if no such singleton existed before.
	 * 
	 * @see TLModuleSingletons#getSingleton(TLModule, String)
	 */
	default TLObject addSingleton(String singletonName, TLObject newSingleton) {
		return getModel().getQuery(TLModuleSingletons.class).addSingleton(this, singletonName, newSingleton);
	}

	/**
	 * Removes the singleton with the given name from this {@link TLModule}.
	 * 
	 * @return The former singleton or <code>null</code> if no singleton with the given name existed
	 *         before.
	 * 
	 * @see TLModuleSingletons#getSingleton(TLModule, String)
	 */
	default TLObject removeSingleton(String singletonName) {
		return getModel().getQuery(TLModuleSingletons.class).removeSingleton(this, singletonName);
	}

	/**
	 * The name of the given singleton.
	 * 
	 * @see TLModuleSingletons#getSingletonName(TLModule, TLObject)
	 */
	default String getSingletonName(TLObject singleton) {
		return getModel().getQuery(TLModuleSingletons.class).getSingletonName(this, singleton);
	}

	@Override
	default ModelKind getModelKind() {
		return ModelKind.MODULE;
	}

	@Override
	default <R, A> R visit(TLModelVisitor<R, A> v, A arg) {
		return v.visitModule(this, arg);
	}

}

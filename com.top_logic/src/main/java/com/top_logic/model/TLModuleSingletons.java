/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import java.util.Collection;

/**
 * Built-in queries to the singleton assignment storage.
 * 
 * @see TLModule#getSingleton(String)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLModuleSingletons extends TLQuery {

	/**
	 * Retrieves all singleton assignments in the model.
	 */
	Collection<TLModuleSingleton> getAllSingletons();

	/**
	 * Retrieves all singleton assignments in the given module.
	 */
	Collection<TLModuleSingleton> getSingletons(TLModule module);

	/**
	 * The singleton object with the given (technical) name in the given module.
	 * 
	 * @param module
	 *        The module to search in.
	 * @param name
	 *        The name of the singleton to search.
	 * @return The identifies singleton object, or <code>null</code> if there is no singleton with
	 *         the given name in the given module.
	 */
	TLObject getSingleton(TLModule module, String name);

	/**
	 * Removes the singleton with the given name from the given {@link TLModule}.
	 *
	 * @param module
	 *        The module to remove a singleton from.
	 * @param name
	 *        The name of the singleton to remove.
	 * @return The former singleton object, or <code>null</code> if no singleton with the given name
	 *         existed before.
	 */
	TLObject removeSingleton(TLModule module, String name);

	/**
	 * Adds a new singleton to the given {@link TLModule}.
	 *
	 * @param module
	 *        The module to add a singleton to.
	 * @param name
	 *        The name of the new singleton.
	 * @param newSingleton
	 *        The new singleton object to add to the given module.
	 * @return The former singleton object, or <code>null</code> if no singleton with the given name
	 *         existed before.
	 */
	TLObject addSingleton(TLModule module, String name, TLObject newSingleton);

	/**
	 * The name of the given singleton in the given module.
	 * 
	 * @param module
	 *        The module to search in.
	 * @param singleton
	 *        The singleton to check.
	 * @return The name of the given singleton in the given module, or <code>null</code>, if the
	 *         given object is no singleton of the given module.
	 */
	String getSingletonName(TLModule module, TLObject singleton);

	/**
	 * The singleton assignment link of the given object, if it is a singleton in any module.
	 * 
	 * @param singleton
	 *        The object to check.
	 * @return The singleton assignment link, or <code>null</code> if the given object is not a
	 *         singleton in any module of the context model.
	 */
	TLModuleSingleton getModuleAndName(TLObject singleton);

}

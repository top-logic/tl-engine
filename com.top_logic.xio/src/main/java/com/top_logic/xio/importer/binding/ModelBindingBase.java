/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.binding;

import javax.xml.stream.Location;

import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.xio.importer.handlers.Handler;
import com.top_logic.xio.importer.handlers.ImportPart;

/**
 * Access to the application model to which data is imported.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ModelBindingBase {

	/**
	 * Resolves the object with the given ID.
	 * 
	 * <p>
	 * The object must not necessarily be defined before this call. In that case, a future for the
	 * object is returned.
	 * </p>
	 * 
	 * @param handler
	 *        The {@link Handler} that requests the object (for potential error reporting).
	 * @param location
	 *        The location in the source XML that contains the reference.
	 * @param id
	 *        The ID of the object to resolve.
	 * 
	 * @return The object or future for the object with the given ID.
	 */
	Object resolveObject(ImportPart handler, Location location, String id);

	/**
	 * Creates a new object of the given type and assigns the given import-local ID to it.
	 * 
	 * @param handler
	 *        The {@link Handler} that requests the object (for potential error reporting).
	 * @param location
	 *        The location in the source XML that created the object.
	 * @param modelType
	 *        The (fully-qualified) name of the type to instantiate.
	 * @param id
	 *        The ID to assign to the newly created object, see
	 *        {@link #resolveObject(ImportPart, Location, String)}.
	 *
	 * @return The newly created object.
	 */
	Object createObject(ImportPart handler, Location location, String modelType, String id);

	/**
	 * Assigns an (additional) import-local ID to the given value.
	 *
	 * @param value
	 *        The object to assign an ID to.
	 * @param id
	 *        The new/additional ID of the given value, see
	 *        {@link #resolveObject(ImportPart, Location, String)}.
	 */
	void assignId(Object value, String id);

	/**
	 * Assigns a model property.
	 * 
	 * @param handler
	 *        The handler that requested this operation (for error reporting).
	 * @param obj
	 *        The object whose property is set.
	 * @param name
	 *        The name of the property.
	 * @param value
	 *        The value to set the property to.
	 */
	void setProperty(ImportPart handler, Object obj, String name, Object value);

	/**
	 * Assigns a reference-valued attribute.
	 * 
	 * @param handler
	 *        The handler that requested this operation (for error reporting).
	 * @param obj
	 *        The object whose reference is set.
	 * @param name
	 *        The name of the reference.
	 * @param value
	 *        The value to set the reference to.
	 */
	void setReference(ImportPart handler, Object obj, String name, Object value);

	/**
	 * Adds another value to a (multi-valued) model reference.
	 * 
	 * @param handler
	 *        The handler that requested this operation (for error reporting).
	 * @param obj
	 *        The object whose reference is set.
	 * @param part
	 *        The name of the reference.
	 * @param element
	 *        The new element to add to the given multi-reference.
	 */
	void addValue(ImportPart handler, Object obj, String part, Object element);

	/**
	 * Evaluates the given search in the context of the current import model.
	 * 
	 * @param expr
	 *        The search to evaluate.
	 * @param args
	 *        Arguments to the given search expression.
	 * @return The value returned from the search expression.
	 */
	Object eval(Expr expr, Object... args);

}

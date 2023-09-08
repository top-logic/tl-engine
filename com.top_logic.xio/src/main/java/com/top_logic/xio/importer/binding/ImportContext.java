/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.binding;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.xml.log.XMLStreamLog;
import com.top_logic.xio.importer.handlers.Handler;
import com.top_logic.xio.importer.handlers.ImportPart;

/**
 * Common context for all {@link Handler}s.
 * 
 * @see Handler#importXml(ImportContext, XMLStreamReader)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ImportContext extends ModelBindingBase, XMLStreamLog, TypedAnnotatable, AutoCloseable {

	/**
	 * Callback interface for awaiting the import of a potentially forwards-referenced value.
	 */
	interface Resolution {

		/**
		 * Hook being called, when an object referenced by an ID becomes available.
		 *
		 * @param target
		 *        The concrete imported object.
		 */
		void resolve(Object target);

	}

	/**
	 * Implicit variable assigned to the outer object by an inner object creation.
	 * 
	 * @see #THIS_VAR
	 */
	String SCOPE_VAR = "scope";

	/**
	 * Implicit variable assigned with the new object by an object creation.
	 * 
	 * @see #SCOPE_VAR
	 */
	String THIS_VAR = "this";

	/**
	 * Conditional import, deciding whether a given object is of some given type.
	 *
	 * @param obj
	 *        The value to test.
	 * @param type
	 *        The (fully-qualified) name of the type to check.
	 * @param yes
	 *        The branch of the import to take, if the given value is of the given type.
	 * @param no
	 *        The branch of the import to take, if the given value is not of the given type.
	 * 
	 * @see ModelBinding#isInstanceOf(Object, String)
	 */
	void isInstanceOf(Object obj, String type, Runnable yes, Runnable no);

	/**
	 * Invokes the given {@link Handler} in the context of the given variable name assigned to a
	 * given value.
	 *
	 * @param var
	 *        The name of the variable to assign.
	 * @param value
	 *        The value to assign to the given variable name.
	 * @param in
	 *        The current state of the import passed to the given {@link Handler}.
	 * @param inner
	 *        The inner {@link Handler} to invoke.
	 * @return See {@link Handler#importXml(ImportContext, XMLStreamReader)}.
	 * @throws XMLStreamException
	 *         The failure reported from {@link Handler#importXml(ImportContext, XMLStreamReader)}.
	 */
	Object withVar(String var, Object value, XMLStreamReader in, Handler inner) throws XMLStreamException;

	/**
	 * The value of the variable with the given name.
	 *
	 * @param name
	 *        The variable name of the variable to resolve.
	 * @return The value of the variable.
	 * 
	 * @throws IllegalArgumentException
	 *         If the variable with the given name is not defined.
	 * 
	 * @see #withVar(String, Object, XMLStreamReader, Handler)
	 */
	Object getVar(String name) throws IllegalArgumentException;

	/**
	 * Same as {@link #getVar(String)} but returns <code>null</code>, if the variable is not
	 * defined.
	 */
	Object getVarOrNull(String name);

	@Override
	void close();

	/**
	 * Resolves the object with the given ID, if it was created before.
	 * 
	 * @param handler
	 *        The {@link Handler} that requests the object (for potential error reporting).
	 * @param location
	 *        The location in the source XML that requested the resolution.
	 * @param id
	 *        The ID of the object to resolve.
	 *
	 * @return The object already created with the given ID, or <code>null</code> if no such object
	 *         was created before.
	 * 
	 * @see #resolveObject(ImportPart, Location, String)
	 */
	Object resolveObjectBackwards(ImportPart handler, Location location, String id);

	/**
	 * Calls the given {@link Resolution} callback when the given object has been imported.
	 *
	 * @param obj
	 *        The (potentially forwards-referenced) import value, see
	 *        {@link #resolveObject(ImportPart, Location, String)}.
	 * @param fun
	 *        The function to call with the imported object, when it has been imported.
	 */
	void deref(Object obj, Resolution fun);

	/**
	 * Assigns an unbound object reference with a concrete value.
	 *
	 * @param ref
	 *        The unbound object reference.
	 * @param value
	 *        The concrete value.
	 * @return Whether the assignment was successful (the given value was acutally an unbound
	 *         reference).
	 */
	boolean fillForward(Object ref, Object value);

	/**
	 * Delegate the import of the given input to the given handler.
	 * 
	 * @throws XMLStreamException
	 *         If the XML cannot be read.
	 */
	default Object importXml(Handler handler, XMLStreamReader in) throws XMLStreamException {
		return handler.importXml(this, in);
	}

}

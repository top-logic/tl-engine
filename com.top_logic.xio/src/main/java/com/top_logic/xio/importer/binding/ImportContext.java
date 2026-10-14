/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.binding;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
	 * Invokes the given {@link Handler} with the given {@link IdLookup} installed.
	 * 
	 * <p>
	 * Within the given {@link Handler}, an ID that does not resolve to an object of the import is
	 * looked up with the given {@link IdLookup}. When lookups are nested, the innermost one is asked
	 * first. An object found by a lookup is registered with its ID, see
	 * {@link #assignId(Object, String)}.
	 * </p>
	 *
	 * @param lookup
	 *        The {@link IdLookup} to install.
	 * @param in
	 *        The current state of the import passed to the given {@link Handler}.
	 * @param inner
	 *        The inner {@link Handler} to invoke.
	 * @return See {@link Handler#importXml(ImportContext, XMLStreamReader)}.
	 * @throws XMLStreamException
	 *         The failure reported from {@link Handler#importXml(ImportContext, XMLStreamReader)}.
	 * 
	 * @see #resolveObjectBackwards(ImportPart, Location, String)
	 */
	Object withIdLookup(IdLookup lookup, XMLStreamReader in, Handler inner) throws XMLStreamException;

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
	 * Resolves the object with the given ID, whose resolution is required.
	 * 
	 * @see #resolveObject(ImportPart, Location, String, boolean)
	 */
	@Override
	default Object resolveObject(ImportPart handler, Location location, String id) {
		return resolveObject(handler, location, id, true);
	}

	/**
	 * Resolves the object with the given ID, potentially forwards.
	 * 
	 * <p>
	 * If the object is not yet known, a reference is returned that is filled when the object with
	 * the given ID is imported later on, see {@link #deref(Object, Resolution)}.
	 * </p>
	 * 
	 * @param handler
	 *        The {@link Handler} that requests the object (for potential error reporting).
	 * @param location
	 *        The location in the source XML that contains the reference.
	 * @param id
	 *        The ID of the object to resolve.
	 * @param required
	 *        Whether an ID that is still unresolved at the end of the import is reported as error.
	 *        If no requester of an ID requires its resolution, an unresolved ID is ignored, and all
	 *        operations waiting for the object are dropped.
	 * @return The object or future for the object with the given ID.
	 */
	Object resolveObject(ImportPart handler, Location location, String id, boolean required);

	/**
	 * Resolves the object with the given ID, if it was created before or is found by an
	 * {@link IdLookup} installed with {@link #withIdLookup(IdLookup, XMLStreamReader, Handler)}.
	 * 
	 * @param handler
	 *        The {@link Handler} that requests the object (for potential error reporting).
	 * @param location
	 *        The location in the source XML that requested the resolution.
	 * @param id
	 *        The ID of the object to resolve.
	 *
	 * @return The object already created with the given ID, the object found by an installed
	 *         {@link IdLookup}, or <code>null</code> if neither knows the ID.
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
	 * Calls the given {@link Resolution} callback when the given object has been imported, or the
	 * given fallback, if the object is still not imported at the end of the import.
	 *
	 * @param obj
	 *        The (potentially forwards-referenced) import value, see
	 *        {@link #resolveObject(ImportPart, Location, String)}.
	 * @param fun
	 *        The function to call with the imported object, when it has been imported.
	 * @param ifUnresolved
	 *        The function to call at the end of the import, if the object has not been imported
	 *        (before an unresolved ID is reported). <code>null</code> for no callback.
	 */
	void deref(Object obj, Resolution fun, Runnable ifUnresolved);

	/**
	 * Calls the given callback with the imported objects of the given values, when all of them
	 * have been imported.
	 * 
	 * <p>
	 * Values that are still not imported at the end of the import are left out: In that case, the
	 * given callback is called at the end of the import with the imported objects only. The order of
	 * the values is preserved.
	 * </p>
	 *
	 * @param values
	 *        The (potentially forwards-referenced) import values, see
	 *        {@link #resolveObject(ImportPart, Location, String)}.
	 * @param fun
	 *        The function to call with the list of imported objects.
	 */
	default void derefAll(List<?> values, Consumer<List<Object>> fun) {
		int size = values.size();
		Object[] targets = new Object[size];
		boolean[] resolved = new boolean[size];
		int[] pending = { size };
		Runnable done = () -> {
			if (--pending[0] == 0) {
				List<Object> result = new ArrayList<>(size);
				for (int n = 0; n < size; n++) {
					if (resolved[n]) {
						result.add(targets[n]);
					}
				}
				fun.accept(result);
			}
		};
		if (size == 0) {
			fun.accept(new ArrayList<>());
			return;
		}
		for (int n = 0; n < size; n++) {
			int index = n;
			deref(values.get(n), target -> {
				targets[index] = target;
				resolved[index] = true;
				done.run();
			}, done);
		}
	}

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

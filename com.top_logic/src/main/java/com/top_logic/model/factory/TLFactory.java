/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.factory;

import com.top_logic.knowledge.wrap.ValueProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.provider.DefaultProvider;

/**
 * Factory interface for generic object creation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLFactory {

	/**
	 * Creates a new object in the context of a given object (e.g. its parent object in a
	 * structure).
	 * 
	 * @param type
	 *        The type of object to create.
	 * @param context
	 *        The context in which the new object is created.
	 * @param initialValues
	 *        A provider for initial values that are required at object creation time.
	 * 
	 * @return The newly created object
	 */
	TLObject createObject(TLClass type, TLObject context, ValueProvider initialValues);

	/**
	 * Creates a new object in the context of a given object (e.g. its parent object in a
	 * structure).
	 * 
	 * @param type
	 *        The type of object to create.
	 * @param context
	 *        The context in which the new object is created.
	 * @return The newly created object
	 */
	default TLObject createObject(TLClass type, TLObject context) {
		return createObject(type, context, null);
	}

	/**
	 * Creates a new object without context.
	 * 
	 * @param type
	 *        The type of object to create.
	 * @return The newly created object
	 */
	default TLObject createObject(TLClass type) {
		return createObject(type, null);
	}

	/**
	 * Checks that the given type is not {@link TLClass#isAbstract() abstract}. If the given type is
	 * abstract, an {@link IllegalArgumentException} is thrown.
	 * 
	 * @param type
	 *        The type to check.
	 * @return Given Type for chaining.
	 * 
	 * @throws IllegalArgumentException
	 *         iff the given type is {@link TLClass#isAbstract()}.
	 */
	static TLClass failIfAbstract(TLClass type) {
		if (type.isAbstract()) {
			throw new IllegalArgumentException("Can not create an element for abstract type '" + type + "'.");
		}
		return type;
	}

	/**
	 * Sets the default values for the given {@link TLObject}.
	 * 
	 * @param createContext
	 *        The context in which the new wrapper is created. May be <code>null</code>.
	 * @param obj
	 *        The {@link TLObject} to set values for.
	 * @param type
	 *        The type of the wrapper, i.e. value of {@link TLObject#tType()} of the given
	 *        <code>newWrapper</code>.
	 */
	static void setupDefaultValues(Object createContext, TLObject obj, TLStructuredType type) {
		for (TLStructuredTypePart part : type.getAllParts()) {
			if (part.isDerived()) {
				// For safety reasons, ignore default value annotations on derived attributes.
				continue;
			}
			DefaultProvider defaultProvider = DisplayAnnotations.getDefaultProvider(part);
			if (defaultProvider == null) {
				continue;
			}
			// A transient object can never create a default that is allocated lately
			// during commit, since there is no commit for creating a transient object. When
			// creating e.g. a transient version of on object that defines an attribute with a
			// sequence number default, this attribute must not be filled in the transient version
			// to prevent an auto-rollback of the transaction started when the default is filled.
			boolean forUI = obj.tTransient();
			obj.tUpdate(part, defaultProvider.createDefault(createContext, part, forUI));
		}
	}

}

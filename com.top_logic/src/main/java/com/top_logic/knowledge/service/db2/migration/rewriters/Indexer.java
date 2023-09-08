/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.List;

import com.top_logic.basic.col.Mapping;
import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;

/**
 * Indexing object to find values of an object not identified by its {@link ObjectBranchId} but the
 * values of some key attributes.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Indexer {

	/**
	 * Attribute constant to use to not identify an attribute value of the object but the object
	 * itself.
	 */
	String SELF_ATTRIBUTE = "_self";

	/**
	 * Index for a combination of type name, key attributes, and value attributes to find desired
	 * values.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Index {

		/**
		 * Returns the value for the value attribute of the object identified by the given key
		 * values.
		 * 
		 * <p>
		 * It is expected that the index was created with exactly one value attribute and that the
		 * given key values identifying one (or zero) objects.
		 * </p>
		 * 
		 * @param keyValues
		 *        The values of the key attribute uniquely identifying the object.
		 * 
		 * @return The value for the formerly given "value attribute" or <code>null</code>, when the
		 *         key values does not identify an object.
		 * 
		 * @see #getValues(Object...)
		 */
		Object getValue(Object... keyValues);

		/**
		 * Returns the values for the value attributes in the corresponding order of the object
		 * identified by the given key values.
		 * 
		 * <p>
		 * It is expected that the given key values identifying one (or zero) objects.
		 * </p>
		 * 
		 * @param keyValues
		 *        The values of the key attribute uniquely identifying the object.
		 * 
		 * @return The value for the formerly given "value attributes" or <code>null</code>, when
		 *         the key values does not identify an object.
		 * 
		 * @see #getValue(Object...)
		 */
		Object[] getValues(Object... keyValues);

		/**
		 * Returns the list of the values for the value attributes of the objects identified by the
		 * given key values.
		 * 
		 * @param keyValues
		 *        The values of the key attributes identifying the objects.
		 * 
		 * @return The value for the formerly given "value attributes" for all objects identified by
		 *         the given key values. May be empty when there is no object identified by the key
		 *         values.
		 */
		List<Object[]> getMultiValues(Object... keyValues);

	}

	/**
	 * Registers for a given type the key attributes and the value attributes, with identity key
	 * value mappings.
	 * 
	 * @see #register(String, List, List, List)
	 */
	Index register(String typeName, List<String> keyAttributes, List<String> valueAttributes);

	/**
	 * Registers for a given type the key attributes and the value attributes.
	 * 
	 * <p>
	 * Later the desired values for the "value attributes" can be retrieved by using
	 * {@link Index#getValues(Object...)} with the values of the key attributes.
	 * </p>
	 * 
	 * @param typeName
	 *        Name of the {@link MetaObject}.
	 * @param keyValueMapping
	 *        Mappings for the key values. The list must have the same size as
	 *        <code>keyAttributes</code>. The value for the n^th key attribute is mapped by the n^th
	 *        key value mapping.
	 * @param keyAttributes
	 *        The names of the attributes whose values are later given to find the corresponding
	 *        values of the given value attributes.
	 * @param valueAttributes
	 *        The names of the value attributes whose values are needed.
	 */
	Index register(String typeName, List<? extends Mapping<Object, ?>> keyValueMapping, List<String> keyAttributes,
			List<String> valueAttributes);

}

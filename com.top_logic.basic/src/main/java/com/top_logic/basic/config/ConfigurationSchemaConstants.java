/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * Constants for serializing {@link ConfigurationItem} to XML.
 * 
 * <p>
 * The attributes of namespace {@link #CONFIG_NS} control how a source updates the configuration
 * of earlier sources. An entry of a list valued property with key property is matched by its key
 * value: An entry with the key of an existing entry updates it, an entry with a different key is
 * added ({@link #LIST_OPERATION}, {@link #LIST_POSITION}, {@link #LIST_REFERENCE_ATTR_NAME}). An
 * entry without key value is anonymous: It is added, matches no existing entry, and cannot be
 * referenced; any number of anonymous entries may occur. {@link #LIST_INDEX} addresses an entry
 * by its position, also in lists without key property and for anonymous entries.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConfigurationSchemaConstants {

	/**
	 * The namespace URI that is used for type annotation attributes.
	 */
	public static final String CONFIG_NS = "http://www.top-logic.com/ns/config/6.0";
	
	/**
	 * The default prefix for {@link #CONFIG_NS}.
	 */
	public static final String CONFIG_NS_PREFIX = "config";

	/**
	 * The attribute of namespace {@link #CONFIG_NS} that stores the concrete
	 * configuration interface name, in case it cannot be derived neither from
	 * the context nor from the implementation class of the concrete
	 * {@link PolymorphicConfiguration} item.
	 */
	public static final String CONFIG_INTERFACE_ATTR = "interface";
	
	/**
	 * Name of the attribute which determines whether a property must be updated
	 * or completely replaced
	 */
	public static final String CONFIG_OVERLOADING_OVERRIDE = "override";

	/**
	 * Name of the attribute which states that the configuration happens for an abstract class.
	 * <p>
	 * Abstract classes are not instantiated, but only used for pre-configuration.
	 * </p>
	 */
	public static final String CONFIG_ABSTRACT = "abstract";

	/**
	 * this is used as default if no value for attribute
	 * {@link #CONFIG_OVERLOADING_OVERRIDE} is given.
	 * 
	 * @see #CONFIG_OVERLOADING_OVERRIDE
	 */
	public static final boolean OVERRIDE_DEFAULT = false;

	/**
	 * Name of the attribute which describes the operation to execute when a
	 * list value is updated. Possible values are the
	 * {@link ListOperation#getExternalName() external names} of the
	 * {@link ListOperation}.
	 */
	public static final String LIST_OPERATION = "operation";

	/**
	 * Default operation to execute when a list valued property must be updated.
	 * 
	 * @see #LIST_OPERATION
	 */
	public static final ListOperation LIST_OPERATION_DEFAULT = ListOperation.ADD_OR_UPDATE;

	/**
	 * Name of the attribute which describes the position to insert or move an
	 * object (depending whether {@link #LIST_OPERATION operation} was
	 * {@link ListOperation#ADD}, {@link ListOperation#UPDATE}, or
	 * {@link ListOperation#ADD_OR_UPDATE}). Possible values are the
	 * {@link Position#getExternalName() external names} of the {@link Position}
	 * .
	 */
	public static final String LIST_POSITION = "position";

	/**
	 * Name of the attribute used to describe the {@link ConfigurationItem}
	 * which serves as reference in {@link ListOperation#UPDATE},
	 * {@link ListOperation#ADD}, and {@link ListOperation#ADD_OR_UPDATE} and
	 * there is a relative {@link Position} given
	 * 
	 * <p>
	 * The value is the key value of the referenced entry. A reference that names no key (an
	 * anonymous reference) is an error, since anonymous entries cannot be referenced.
	 * </p>
	 * 
	 * @see ListOperation#UPDATE
	 * @see ListOperation#ADD_OR_UPDATE
	 * @see ListOperation#ADD
	 * @see Position#BEFORE
	 * @see Position#AFTER
	 */
	public static final String LIST_REFERENCE_ATTR_NAME = "reference";

	/**
	 * Name of the attribute which addresses an entry of a list valued property by its position.
	 * 
	 * <p>
	 * The value is the zero-based position of the entry in the list as read so far: the entries of
	 * all earlier sources, changed by the entries of the current source read before the addressing
	 * entry. In the first (or only) source, this is the list of the entries read before the
	 * addressing entry. The addressed entry is updated as by a {@link ListOperation#UPDATE} of an
	 * entry addressed by its key: Its properties are merged, or it is replaced if
	 * {@link #CONFIG_OVERLOADING_OVERRIDE} is set. It keeps its position, unless
	 * {@link #LIST_POSITION} moves it. The attribute works for lists with and without key
	 * property, and it is the only way to address an anonymous entry of a keyed list (an entry
	 * without key value, see {@link ConfigUtil#isAnonymousKey(Object)}).
	 * </p>
	 * 
	 * <p>
	 * The updating entry may set the key property: The addressed entry then has this key and can
	 * be addressed by it afterwards, provided no other entry of the list has the same key.
	 * </p>
	 * 
	 * <p>
	 * A position out of range, a negative or non-numeric value, and a combination with the
	 * {@link #LIST_OPERATION operation} {@link ListOperation#ADD} or {@link ListOperation#REMOVE}
	 * are errors.
	 * </p>
	 */
	public static final String LIST_INDEX = "index";

	/**
	 * Name of the attribute which describes the operation to execute when a map
	 * value is updated. Possible values are the
	 * {@link MapOperation#getExternalName() external names} of the
	 * {@link MapOperation}.
	 */
	public static final String MAP_OPERATION = "operation";

	/**
	 * Default operation to execute when a map valued property must be updated.
	 * 
	 * @see #MAP_OPERATION
	 */
	public static final MapOperation MAP_OPERATION_DEFAULT = MapOperation.ADD_OR_UPDATE;

}

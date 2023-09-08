/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * Constants for serializing {@link ConfigurationItem} to XML.
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
	 * @see ListOperation#UPDATE
	 * @see ListOperation#ADD_OR_UPDATE
	 * @see ListOperation#ADD
	 * @see Position#BEFORE
	 * @see Position#AFTER
	 */
	public static final String LIST_REFERENCE_ATTR_NAME = "reference";

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

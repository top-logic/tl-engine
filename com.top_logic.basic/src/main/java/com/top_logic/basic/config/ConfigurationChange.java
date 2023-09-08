/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * Event object sent to {@link ConfigurationListener}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConfigurationChange {

	/**
	 * Kind of the change.
	 */
	public enum Kind {
		/**
		 * A atomic property (of kind {@link PropertyKind#PLAIN}, {@link PropertyKind#COMPLEX}, or
		 * {@link PropertyKind#ITEM}) was updated.
		 * 
		 * <p>
		 * Note: A collection-valued (({@link PropertyKind#LIST}, or {@link PropertyKind#MAP})
		 * property is never updated as a whole.
		 * </p>
		 */
		SET,

		/**
		 * An element was added to a collection-valued ({@link PropertyKind#LIST}, or
		 * {@link PropertyKind#MAP}) property.
		 */
		ADD,

		/**
		 * An element was removed from a collection-valued ({@link PropertyKind#LIST}, or
		 * {@link PropertyKind#MAP}) property.
		 */
		REMOVE
	}

	/**
	 * Type of change.
	 */
	Kind getKind();

	/**
	 * The {@link ConfigurationItem} that was modified.
	 */
	ConfigurationItem getModel();

	/**
	 * The property that was changed.
	 */
	PropertyDescriptor getProperty();

	/**
	 * The value the property had before the change, in case of a {@link Kind#SET} change, or that
	 * has been removed in a {@link Kind#REMOVE} operation.
	 */
	Object getOldValue();

	/**
	 * The value that was set or added to a property.
	 */
	Object getNewValue();

	/**
	 * The index of the addition or removal for a {@link Kind#ADD} or {@link Kind#REMOVE} change on
	 * a {@link PropertyKind#LIST} property.
	 */
	int getIndex();

}

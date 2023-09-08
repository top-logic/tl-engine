/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

/**
 * Common base class for all {@link AttributeValueLocator} implementations.
 * 
 * <p>
 * Note: Custom implementations should use {@link CustomAttributeValueLocator} as base class.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractAttributeValueLocator implements AttributeValueLocator {

	/**
	 * Name of the most general type.
	 */
	protected static final String ANY_TYPE = "tl.util:Any";

	/**
	 * The type name of the value computed.
	 */
	protected abstract String getValueTypeSpec();

	/**
	 * Whether this {@link AttributeValueLocator} implements a backwards reference.
	 * 
	 * @see #getReverseEndSpec()
	 */
	protected abstract boolean isBackReference();

	/**
	 * Whether more than one value is returned.
	 */
	protected abstract boolean isCollection();

	/**
	 * If {@link #isBackReference()} the name of the reference that is navigated in reverse order, or
	 * <code>null</code>, if this {@link AttributeValueLocator} does not implement a reverse
	 * navigation.
	 */
	protected abstract String getReverseEndSpec();

	/**
	 * Access to {@link #isBackReference()} if given locator implements it.
	 */
	public static boolean isBackReferenceLocator(AttributeValueLocator locator) {
		if (locator instanceof AbstractAttributeValueLocator) {
			return ((AbstractAttributeValueLocator) locator).isBackReference();
		}
		return false;
	}

	/**
	 * Access to {@link #getValueTypeSpec()} if given locator implements it.
	 */
	public static String getLocatorValueTypeSpec(AttributeValueLocator locator) {
		if (locator instanceof AbstractAttributeValueLocator) {
			return ((AbstractAttributeValueLocator) locator).getValueTypeSpec();
		}
		return ANY_TYPE;
	}

	/**
	 * Access to {@link #isCollection()} if given locator implements it.
	 */
	public static boolean isCollectionLocator(AttributeValueLocator locator) {
		if (locator instanceof AbstractAttributeValueLocator) {
			return ((AbstractAttributeValueLocator) locator).isCollection();
		}
		return false;
	}

	/**
	 * Access to {@link #getReverseEndSpec()} if given locator implements it.
	 */
	public static String getLocatorReverseEndSpec(AttributeValueLocator locator) {
		if (locator instanceof AbstractAttributeValueLocator) {
			return ((AbstractAttributeValueLocator) locator).getReverseEndSpec();
		}
		return null;
	}

}

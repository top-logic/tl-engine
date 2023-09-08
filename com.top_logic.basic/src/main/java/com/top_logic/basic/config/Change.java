/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * For working with changes on a {@link ConfigurationItem}.
 * <p>
 * Implementation note: The methods declared in this interface all require the
 * {@link PropertyDescriptor} of the affected property as first parameter. The same
 * {@link PropertyDescriptor} has already been passed in
 * {@link AbstractConfigItem#onChange(PropertyDescriptor)} when the {@link Change} is being given
 * out. But as the {@link AbstractConfigItem} itself sometimes acts as {@link Change}, the affected
 * property can not be stored in those cases and therefore has to be passed again when the methods
 * declared here are called.
 * </p>
 * 
 * @author <a href=mailto:Jan Stolzenburg@top-logic.com>Jan Stolzenburg</a>
 */
interface Change {

	public abstract void update(PropertyDescriptor property, Object oldValue, Object newValue);

	public abstract void add(PropertyDescriptor property, int index, Object element);

	public abstract void remove(PropertyDescriptor property, int index, Object element);

}
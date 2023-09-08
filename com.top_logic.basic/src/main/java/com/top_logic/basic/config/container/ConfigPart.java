/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.container;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.annotation.Container;

/**
 * A {@link ConfigurationItem} which is part of an {@link ConfigurationItem} tree and has a
 * {@link #container() reference} to its parent in this tree.
 * 
 * <p>
 * It is recommend to define an explicit property which is annotated with {@link Container}.
 * </p>
 * 
 * @see Container
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface ConfigPart extends ConfigurationItem {

	/**
	 * The configuration context, in which this {@link ConfigurationItem} lives.
	 * <p>
	 * <em>It is not allowed to build a cycle by putting a container into one of its (indirect)
	 * parts.</em> This is not checked though, for performance reasons. <br/>
	 * A configuration item lives in the context of another configuration, if it is assigned to a
	 * containment reference (of kind {@link PropertyKind#ITEM}, {@link PropertyKind#ARRAY},
	 * {@link PropertyKind#LIST}, or {@link PropertyKind#MAP}) of the other configuration.
	 * </p>
	 */
	ConfigurationItem container();

}

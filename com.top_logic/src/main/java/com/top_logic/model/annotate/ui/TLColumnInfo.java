/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.layout.table.model.ColumnBaseConfig;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAnnotation} specifying {@link ColumnBaseConfig column settings} for an attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("column-info")
@InApp
@DisplayInherited(DisplayStrategy.PREPEND)
public interface TLColumnInfo extends TLAttributeAnnotation, TLTypeAnnotation, ColumnBaseConfig {

	/** Name for the configuration option {@link #isOverrideDefaults()}. */
	String OVERRIDE_DEFAULTS = "override-defaults";

	/**
	 * Controls how unset properties in this annotation are handled.
	 *
	 * <p>
	 * Properties that are explicitly set in this annotation are always used. For properties that
	 * are <b>not</b> explicitly configured in this annotation:
	 * </p>
	 * <ul>
	 * <li>If <code>false</code> (default): The default column setting for that property is used
	 * (derived from attribute type, global table settings, or other configuration sources).</li>
	 * <li>If <code>true</code>: No value is used for that property (the property remains unset).
	 * </li>
	 * </ul>
	 *
	 * @return Whether unset properties should remain unset (<code>true</code>) or fall back to
	 *         defaults (<code>false</code>).
	 */
	@Name(OVERRIDE_DEFAULTS)
	boolean isOverrideDefaults();
}

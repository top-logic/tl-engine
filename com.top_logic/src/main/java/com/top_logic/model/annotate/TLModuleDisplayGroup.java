/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.config.TLModuleAnnotation;

/**
 * <p>
 * {@link TLAnnotation} to define where this module should be grouped in the type tree of the model
 * editor, which resembles the view of a package explorer of the commonly used code editors.
 * </p>
 * 
 * <p>
 * The value of this field is a path with <code>.</code> as separators.
 * </p>
 * 
 * <p>
 * For example, <code>com.foo.bar</code> specifies that this module is added as a child of the
 * <code>com.foo.bar</code> group.
 * </p>
 * 
 * <p>
 * By default, if the module is not annotated, the grouping of the module is determined by its
 * technical name.
 * </p>
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@InApp
@TagName(TLModuleDisplayGroup.TAG_NAME)
public interface TLModuleDisplayGroup extends TLModuleAnnotation, StringAnnotation {

	/**
	 * Custom tag to create a {@link TLModuleDisplayGroup} annotation.
	 */
	String TAG_NAME = "display-group";

}

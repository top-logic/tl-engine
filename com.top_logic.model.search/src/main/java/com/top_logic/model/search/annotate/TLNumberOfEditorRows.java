/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.annotate;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link TLAnnotation} that defines the number of displayed rows for Expr attributes.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
@TagName("editor-rows")
@TargetType(value = TLTypeKind.CUSTOM, name = "tl.model.search:Expr")
public interface TLNumberOfEditorRows extends TLAttributeAnnotation {

	/**
	 * Configuration option name for {@link #min()}
	 */
	String MIN = "min";

	/**
	 * Configuration option name for {@link #max()}
	 */
	String MAX = "max";

	/**
	 * Minimum number of editor rows that are displayed.
	 */
	@Name(MIN)
	int min();

	/**
	 * Maximum number of editor rows that are displayed.
	 */
	@Name(MAX)
	int max();

}

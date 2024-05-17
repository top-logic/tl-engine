/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.persistency;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * Name of the link table in which a composite reference stores the links.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("composition-link")
@InApp
@TargetType(value = { TLTypeKind.COMPOSITION })
public interface CompositionLinkTable extends TLAttributeAnnotation {

	/**
	 * The name of the database table which is used to store the link objects between the container
	 * and the part of the annotated composition reference.
	 */
	@Options(fun = LinkTables.class)
	String getTable();

}

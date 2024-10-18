/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.display;

import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.TLAttributeAnnotation;

/**
 * DO NOT USE!
 * 
 * @deprecated Legacy annotation no longer in use. Kept for easier migration.
 */
@TagName("template-builder")
@Deprecated
public interface TemplateBuilder extends TLAttributeAnnotation {

	/**
	 * DO NOT USE!
	 */
	@InstanceFormat
	@Subtypes({})
	Object getFunction();
}

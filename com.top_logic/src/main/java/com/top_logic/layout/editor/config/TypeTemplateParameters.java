/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.config;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Base interface for layout template parameter definitions to provide a type parameter.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@Abstract
public interface TypeTemplateParameters extends OptionalTypeTemplateParameters {

	@Override
	@Mandatory
	TLModelPartRef getType();

}

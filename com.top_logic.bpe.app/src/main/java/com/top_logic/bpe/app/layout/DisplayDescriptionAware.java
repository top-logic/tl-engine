/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout;

import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.definition.FormDefinition;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface DisplayDescriptionAware {

	@Hidden
	public FormDefinition getDisplayDescription();

	TLObject getContextModel();
}

/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.layout.form.template.util.EmbeddedResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.search.ui.model.structure.InheritedContextType;
import com.top_logic.model.search.ui.model.structure.RightHandSide;

/**
 * {@link RightHandSide} that selects the compare object dynamically with a nested query.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(EmbeddedResourceDisplay.class)
public interface SubQuery extends RightHandSide, FilterContainer, InheritedContextType {

	// Pure sum interface.

}

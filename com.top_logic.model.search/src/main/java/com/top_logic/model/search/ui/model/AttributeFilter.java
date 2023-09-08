/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.form.template.util.ResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.ui.model.function.part.PartMultiplicity;
import com.top_logic.model.search.ui.model.options.AllParts;

/**
 * An {@link AttributeBased} {@link NavigatingFilter}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(ResourceDisplay.class)
public interface AttributeFilter extends AttributeBased, NavigatingFilter {

	@Override
	@Mandatory
	@Options(fun = AllParts.class, args = {
		@Ref(CONTEXT_TYPE),
		@Ref(CONFIG_NAME) })
	TLStructuredTypePart getAttribute();

	@Override
	@Derived(fun = PartMultiplicity.class, args = @Ref(ATTRIBUTE))
	boolean getValueMultiplicity();

}

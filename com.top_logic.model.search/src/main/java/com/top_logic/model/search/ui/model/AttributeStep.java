/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.form.template.util.SpanResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.ui.model.function.part.CombinedPartMultiplicity;
import com.top_logic.model.search.ui.model.options.ReferencesOrCompatibleParts;

/**
 * An {@link AttributeBased} {@link NavigationStep} expression.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(SpanResourceDisplay.class)
public interface AttributeStep extends AttributeBased, NavigationStep {

	@Override
	@Options(fun = ReferencesOrCompatibleParts.class, args = {
		@Ref(CONTEXT_TYPE),
		@Ref(EXPECTED_TYPE),
		@Ref(EXPECTED_MULTIPLICITY),
		@Ref(CONFIG_NAME) })
	@Mandatory
	TLStructuredTypePart getAttribute();

	@Override
	@Derived(fun = CombinedPartMultiplicity.class, args = {
		@Ref(ATTRIBUTE),
		@Ref(CONTEXT_MULTIPLICITY) })
	boolean getValueMultiplicity();

}

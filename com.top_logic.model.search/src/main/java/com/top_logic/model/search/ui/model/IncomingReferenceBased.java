/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.form.template.util.ResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLType;
import com.top_logic.model.format.TLObjectFormat;
import com.top_logic.model.search.ui.model.function.part.PartOwner;
import com.top_logic.model.search.ui.model.misc.Multiplicity;
import com.top_logic.model.search.ui.model.options.ReferenceOptions;
import com.top_logic.model.search.ui.model.structure.SearchPart;
import com.top_logic.model.search.ui.model.structure.WithValueContext;
import com.top_logic.model.search.ui.model.ui.StructuredTypePartWithOwnerType;

/**
 * A {@link SearchPart} that is based on using an incoming {@link TLReference}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(ResourceDisplay.class)
public interface IncomingReferenceBased extends ValueContext, WithValueContext {

	/**
	 * Property name of {@link #getReference()}.
	 */
	String REFERENCE = "reference";

	/**
	 * the {@link TLReference} that is navigated backwards.
	 */
	@Name(REFERENCE)
	@Format(TLObjectFormat.class)
	@Mandatory
	@Options(fun = ReferenceOptions.class, args = {
		@Ref(CONTEXT_TYPE),
		@Ref(CONFIG_NAME) })
	@OptionLabels(StructuredTypePartWithOwnerType.class)
	TLReference getReference();

	@Override
	@Derived(fun = PartOwner.class, args = @Ref(REFERENCE))
	TLType getValueType();

	@Override
	@Derived(fun = Multiplicity.CollectionValue.class, args = {})
	boolean getValueMultiplicity();

}

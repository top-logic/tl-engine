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
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.format.TLObjectFormat;
import com.top_logic.model.search.ui.model.function.part.PartOwner;
import com.top_logic.model.search.ui.model.misc.Multiplicity;
import com.top_logic.model.search.ui.model.options.PartOptions;
import com.top_logic.model.search.ui.model.structure.RightHandSide;
import com.top_logic.model.search.ui.model.ui.StructuredTypePartWithOwnerType;

/**
 * Value set retrieved from evaluating a property of all result objects of a sub-query.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(ResourceDisplay.class)
public interface QueryValue extends RightHandSide, FilterContainer {

	/**
	 * Property name of {@link #getPart()}.
	 */
	String PART = "part";

	/**
	 * The property to evaluate on the result set.
	 * 
	 * <p>
	 * All objects are in the result set that are of the owning type of the {@link #getPart()} and
	 * match the {@link #getFilters()}.
	 * </p>
	 */
	@Name(PART)
	@Mandatory
	@Options(fun = PartOptions.class, args = {
		@Ref(CONTEXT_TYPE),
		@Ref(CONTEXT_MULTIPLICITY),
		@Ref(CONFIG_NAME) })
	@OptionLabels(StructuredTypePartWithOwnerType.class)
	@Format(TLObjectFormat.class)
	TLStructuredTypePart getPart();

	@Override
	@Derived(fun = PartOwner.class, args = @Ref(PART))
	TLType getValueType();

	@Override
	@Derived(fun = Multiplicity.CollectionValue.class, args = {})
	boolean getValueMultiplicity();

}

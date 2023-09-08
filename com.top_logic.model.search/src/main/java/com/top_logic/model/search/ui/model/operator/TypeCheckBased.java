/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.form.template.util.ResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.format.TLObjectFormat;
import com.top_logic.model.search.ui.model.ValueContext;
import com.top_logic.model.search.ui.model.options.TypeCastOptions;
import com.top_logic.model.search.ui.model.structure.SearchPart;
import com.top_logic.model.search.ui.model.structure.WithValueContext;

/**
 * A {@link SearchPart} for casting an expression.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
@UseTemplate(ResourceDisplay.class)
public interface TypeCheckBased extends ValueContext, WithValueContext {

	/**
	 * Property name of {@link #getTypeCast()}
	 */
	String TYPE_CAST = "type-cast";

	/**
	 * The type to which the {@link #getContext()} is cast.
	 */
	@Name(TYPE_CAST)
	@Format(TLObjectFormat.class)
	@Options(fun = TypeCastOptions.class, args = {
		@Ref(CONTEXT_TYPE),
		@Ref(CONFIG_NAME) })
	@Mandatory
	TLClass getTypeCast();

	@Override
	@DerivedRef(TYPE_CAST)
	TLType getValueType();

	@Override
	@DerivedRef(CONTEXT_MULTIPLICITY)
	boolean getValueMultiplicity();

}

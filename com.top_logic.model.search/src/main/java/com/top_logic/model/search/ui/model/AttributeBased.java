/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.format.TLObjectFormat;
import com.top_logic.model.search.ui.model.function.part.PartType;
import com.top_logic.model.search.ui.model.structure.SearchPart;
import com.top_logic.model.search.ui.model.structure.WithValueContext;

/**
 * A {@link SearchPart} that is based on using an attribute, i.e. a {@link TLClassPart}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@Abstract
public interface AttributeBased extends ValueContext, WithValueContext {

	/**
	 * Property name of {@link #getAttribute()}.
	 */
	String ATTRIBUTE = "attribute";

	/**
	 * The {@link TLStructuredTypePart} this {@link AttributeBased} is about.
	 */
	@Name(ATTRIBUTE)
	@Abstract
	@Format(TLObjectFormat.class)
	TLStructuredTypePart getAttribute();

	@Override
	@Derived(fun = PartType.class, args = @Ref(ATTRIBUTE))
	TLType getValueType();

	@Override
	@Abstract
	boolean getValueMultiplicity();

}

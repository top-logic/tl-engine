/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.literal;

import java.util.Collection;

import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.form.template.util.SpanResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.format.TLObjectsFormat;
import com.top_logic.model.search.ui.model.options.InstanceOptions;
import com.top_logic.model.search.ui.model.structure.RightHandSide;

/**
 * {@link RightHandSide} describing a literal set of compare objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(SpanResourceDisplay.class)
public interface LiteralObjectSet extends RightHandSide {

	/**
	 * Property name of {@link #getObjects()}.
	 */
	String OBJECTS = "objects";

	/**
	 * The literal example set.
	 */
	@Name(OBJECTS)
	@Format(TLObjectsFormat.class)
	@Options(fun = InstanceOptions.class, args = { @Ref(CONTEXT_TYPE) })
	Collection<Object> getObjects();

}

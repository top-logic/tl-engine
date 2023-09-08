/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.literal;

import static com.top_logic.layout.form.template.model.Templates.*;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.func.misc.AlwaysTrue;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.form.values.edit.TemplateProvider;
import com.top_logic.layout.form.values.edit.annotation.DynamicMandatory;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.search.annotate.TLSearchOptions;
import com.top_logic.model.search.ui.model.structure.RightHandSide;

/**
 * {@link RightHandSide} describing a literal primitive value.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@UseTemplate(LiteralPrimitiveValue.Display.class)
@Abstract
public interface LiteralPrimitiveValue extends RightHandSide {

	/**
	 * {@link TemplateProvider} for {@link LiteralPrimitiveValue}.
	 */
	public class Display extends TemplateProvider {

		@Override
		public HTMLTemplateFragment get(ConfigurationItem model) {
			return embedd(member(COMPARE_VALUE));
		}

	}

	/**
	 * Property name of {@link #getCompareValue()}.
	 */
	String COMPARE_VALUE = "compare-value";

	/**
	 * The actual value to compare with.
	 * 
	 * <p>
	 * Note: The property cannot be declared statically {@link Mandatory} because this would prevent
	 * configuring template values in e.g. y {@link TLSearchOptions} with non-set values.
	 * </p>
	 */
	@DynamicMandatory(fun = AlwaysTrue.class)
	@Name(COMPARE_VALUE)
	@Abstract
	Object getCompareValue();

}

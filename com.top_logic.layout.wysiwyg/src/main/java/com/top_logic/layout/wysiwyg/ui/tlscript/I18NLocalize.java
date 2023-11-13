/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.tlscript;

import java.util.Locale;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.string.Localize;

/**
 * {@link Localize} that additionally localizes {@link I18NStructuredText}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NLocalize extends Localize {

	/**
	 * Creates a new {@link I18NLocalize}.
	 */
	protected I18NLocalize(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new I18NLocalize(getName(), self, arguments);
	}

	@Override
	public Object evalDirect(EvalContext definitions, Object base, Locale param) {
		if (base instanceof StructuredText) {
			return base;
		}
		if (base instanceof I18NStructuredText) {
			I18NStructuredText i18nText = (I18NStructuredText) base;
			return i18nText.localize(param);
		}
		return super.evalDirect(definitions, base, param);
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link Localize} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<I18NLocalize> {

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return Localize.Builder.DESCRIPTOR;
		}

		@Override
		public I18NLocalize build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			return new I18NLocalize(getConfig().getName(), self, args);
		}

	}

}
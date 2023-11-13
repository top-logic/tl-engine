/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.layout.Resource;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelUtil;

/**
 * Generic method creating {@link Resource}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ResourceBuilder extends GenericMethod {

	/**
	 * Creates a new {@link ResourceBuilder}.
	 */
	protected ResourceBuilder(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new ResourceBuilder(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.RESOURCE_TYPE);
	}

	@Override
	protected Object eval(Object self, Object[] arguments, EvalContext definitions) {
		int numberArgs = arguments.length;

		Object userObject = null;
		ResKey label = null;
		ThemeImage image = null;
		ResKey tooltip = null;
		String link = null;
		String type = null;
		String cssClass = null;

		for (int n = 0; n < numberArgs; n++) {
			switch (n) {
				case 0:
					// userObject
					userObject = arguments[n];
					break;
				case 1:
					// image
					image = asImageOptional(arguments[n]);
					break;
				case 2:
					// label
					Object labelArg = arguments[n];
					if (labelArg instanceof ResKey) {
						label = (ResKey) labelArg;
					} else if (labelArg != null) {
						label = ResKey.text(asString(labelArg));
					}
					break;
				case 3:
					// tooltip
					Object tooltipArg = arguments[n];
					if (tooltipArg instanceof ResKey) {
						tooltip = (ResKey) tooltipArg;
					} else if (tooltipArg != null) {
						tooltip = ResKey.text(asString(tooltipArg));
					}
					break;
				case 4:
					// cssClass
					cssClass = asString(arguments[n], null);
					break;
				case 5:
					// cssClass
					link = asString(arguments[n], null);
					break;
				case 6:
					type = asString(arguments[n], null);
					break;
			}
		}

		if (tooltip == null && label != null) {
			tooltip = label.tooltip();
		}

		return Resource.resourceFor(userObject, label, image, tooltip, link, type, cssClass);
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link ResourceBuilder}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ResourceBuilder> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.optional("userObject")
			.optional("icon")
			.optional("label")
			.optional("tooltip")
			.optional("cssClass")
			.optional("url")
			.optional("type")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public ResourceBuilder build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			return new ResourceBuilder(getConfig().getName(), self, args);
		}

	}

}


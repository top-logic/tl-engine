/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.icon.IconProvider;
import com.top_logic.model.search.expr.ToString;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link IconProvider} that can be parameterized with a TL-Script expression.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class IconProviderByExpression extends AbstractConfiguredInstance<IconProviderByExpression.Config>
		implements IconProvider {

	private QueryExecutor _iconFunction;

	/**
	 * Configuration options for {@link IconProviderByExpression}.
	 */
	@TagName("icon-by-expression")
	public interface Config extends PolymorphicConfiguration<IconProviderByExpression> {
		/**
		 * Function computing the icon for a given object.
		 * 
		 * <p>
		 * The function expects the object the icon is requested for as single argument. The
		 * function result must be either the icon itself or a string representing the icon
		 * resource.
		 * </p>
		 * 
		 * <p>
		 * A icon reference can be retrieved from a model attribute of type
		 * <code>tl.core:Icon</code>. A valid icon resource would be "css:fas fa-car-side".
		 * </p>
		 * 
		 * <p>
		 * If the function returns <code>null</code>, the static icon of the type is used that is
		 * defined for the object's type.
		 * </p>
		 */
		@Mandatory
		Expr getIcon();
	}

	/**
	 * Creates a {@link IconProviderByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public IconProviderByExpression(InstantiationContext context, Config config) {
		super(context, config);

		_iconFunction = QueryExecutor.compile(config.getIcon());
	}

	@Override
	public ThemeImage getIcon(Object object, Flavor flavor) {
		Object result = _iconFunction.execute(object);
		if (result instanceof ThemeImage) {
			return (ThemeImage) result;
		}
		if (result == null) {
			return null;
		}
		String iconKey = ToString.toString(result);
		return ThemeImage.internalDecode(iconKey);
	}

}

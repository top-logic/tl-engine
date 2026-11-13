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
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.icon.IconProvider;
import com.top_logic.model.search.expr.SearchExpression;
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
		 * function result must be either the icon itself, a string representing the icon
		 * resource, or image data (a value of type <code>tl.core:Binary</code> with an image
		 * content type).
		 * </p>
		 * 
		 * <p>
		 * A icon reference can be retrieved from a model attribute of type
		 * <code>tl.core:Icon</code>. A valid icon resource would be "css:fas fa-car-side".
		 * </p>
		 * 
		 * <p>
		 * Image data is embedded into the page. Image data that is larger than 32 KB is not
		 * displayed. An application stores a scaled-down version of a large image in a separate
		 * attribute (e.g. computed with <code>binaryScaleImage(...)</code> when the image is
		 * stored) and returns that version from this function.
		 * </p>
		 * 
		 * <p>
		 * If the function returns <code>null</code>, data that is no image, or image data that is
		 * too large, the static icon of the type is used that is defined for the object's type.
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
		return toIcon(_iconFunction.execute(object));
	}

	/**
	 * Converts the result of an icon function to the icon it denotes.
	 *
	 * @param result
	 *        The function result.
	 * @return The result itself, if it is a {@link ThemeImage}, the image a {@link BinaryDataSource}
	 *         contains (see {@link ThemeImage#data(BinaryDataSource)}), the icon the text of any
	 *         other value encodes (e.g. "css:fas fa-car-side"), {@code null} for a {@code null}
	 *         result and for binary data that is no image or is too large.
	 */
	public static ThemeImage toIcon(Object result) {
		if (result instanceof ThemeImage image) {
			return image;
		}
		if (result == null) {
			return null;
		}
		if (result instanceof BinaryDataSource data) {
			return ThemeImage.data(data);
		}
		String iconKey = SearchExpression.asString(result);
		return ThemeImage.internalDecode(iconKey);
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.awt.Color;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.annotate.ui.AnnotationValueColorProvider;
import com.top_logic.model.annotate.ui.TLColor;
import com.top_logic.model.annotate.ui.ValueColor;
import com.top_logic.model.annotate.ui.ValueColorProvider;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ValueColorProvider} that can be implemented with a <i>TL-Script</i> expression.
 */
@Label("Color via TL-Script")
@InApp
public class ColorByExpression extends AbstractConfiguredInstance<ColorByExpression.Config>
		implements ValueColorProvider {

	/**
	 * Configuration options for {@link ColorByExpression}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<ColorByExpression> {

		/** Name of the tag defining a {@link ColorByExpression}. */
		String TAG_NAME = "color-by-expression";

		/** Configuration name of {@link #getColor()}. */
		String COLOR = "color";

		/**
		 * Function computing the color of a given object.
		 * 
		 * <p>
		 * The function expects the object the color is requested for as single argument. The
		 * result is either a color value or an enumeration literal carrying a color annotation.
		 * For any other result, and for <code>null</code>, the object is displayed without a
		 * color.
		 * </p>
		 */
		@Name(COLOR)
		@Mandatory
		Expr getColor();

	}

	private final QueryExecutor _colorExpr;

	/**
	 * Creates a {@link ColorByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ColorByExpression(InstantiationContext context, Config config) {
		super(context, config);

		_colorExpr = QueryExecutor.compile(config.getColor());
	}

	/**
	 * Evaluates the configured {@link Config#getColor() color expression} with the given object and
	 * resolves its result: a {@link Color} value is the color itself, a {@link TLClassifier} is
	 * colored by its {@link TLColor} annotation, anything else has no color.
	 */
	@Override
	public ValueColor colorOf(Object value) {
		Object result = _colorExpr.execute(value);
		if (result instanceof Color || result instanceof TLClassifier) {
			return AnnotationValueColorProvider.INSTANCE.colorOf(result);
		}
		return null;
	}

}

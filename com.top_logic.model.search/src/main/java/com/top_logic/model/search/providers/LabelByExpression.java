/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.knowledge.gui.WrapperResourceProvider;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.ProxyResourceProvider;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ProxyResourceProvider} which computes the label and tooltip from the base object using an
 * {@link Expr}. Other methods are delegated to a configured {@link ResourceProvider}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class LabelByExpression extends ProxyResourceProvider
		implements ConfiguredInstance<LabelByExpression.Config<?>> {

	/**
	 * Configuration options for {@link LabelByExpression}.
	 */
	@TagName("label-by-expression")
	public interface Config<I extends LabelByExpression> extends PolymorphicConfiguration<I> {

		/**
		 * The expression that is executed with the object for which the label is required.
		 * 
		 * <p>
		 * It is expected that the {@link Expr} uses one argument.
		 * </p>
		 */
		Expr getLabel();

		/**
		 * The expression that is executed with the object for which the tooltip is required.
		 * 
		 * <p>
		 * It is expected that the {@link Expr} uses one argument.
		 * </p>
		 */
		Expr getTooltip();

		/**
		 * A constant image that is used for all objects. If not set, the value from the
		 * {@link #getInner() inner provider} is used.
		 */
		ThemeImage getImage();

		/**
		 * CSS class to return for each object. If not set, the value from the {@link #getInner()
		 * inner provider} is used.
		 */
		String getCSSClass();

		/**
		 * Configuration of the {@link ResourceProvider} to get other properties than label and
		 * tooltip from.
		 * 
		 * @see ResourceProvider#getLabel(Object)
		 * @see ResourceProvider#getTooltip(Object)
		 */
		@ItemDefault(WrapperResourceProvider.class)
		PolymorphicConfiguration<ResourceProvider> getInner();

	}

	private QueryExecutor _labelExpr;

	private QueryExecutor _tooltipExpr;

	private final Config<?> _config;

	/**
	 * Creates a new {@link LabelByExpression}.
	 */
	public LabelByExpression(InstantiationContext context, Config<?> config) {
		super(context.getInstance(config.getInner()));
		_config = config;
		_labelExpr = QueryExecutor.compileOptional(config.getLabel());
		_tooltipExpr = QueryExecutor.compileOptional(config.getTooltip());
	}

	/**
	 * Evaluates the configured {@link Expr label expression} with the given object and routes the
	 * result through the inner {@link ResourceProvider}.
	 */
	@Override
	public String getLabel(Object object) {
		if (_labelExpr == null) {
			return super.getLabel(object);
		}
		if (object == null) {
			return null;
		}
		return super.getLabel(_labelExpr.execute(object));
	}

	/**
	 * Evaluates the configured {@link Expr tooltip expression} with the given object and routes the
	 * result through the inner {@link ResourceProvider}.
	 */
	@Override
	public String getTooltip(Object object) {
		if (_tooltipExpr == null) {
			return super.getTooltip(object);
		}
		if (object == null) {
			return null;
		}
		return super.getTooltip(_tooltipExpr.execute(object));
	}

	@Override
	public ThemeImage getImage(Object anObject, Flavor aFlavor) {
		ThemeImage staticImage = getConfig().getImage();
		if (staticImage != null) {
			return staticImage;
		}
		return super.getImage(anObject, aFlavor);
	}

	@Override
	public String getCssClass(Object anObject) {
		String cssClass = getConfig().getCSSClass();
		if (!cssClass.isEmpty()) {
			return cssClass;
		}
		return super.getCssClass(anObject);
	}

	@Override
	public Config<?> getConfig() {
		return _config;
	}

}

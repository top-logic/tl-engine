/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.table;

import static java.util.Collections.*;

import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.table.model.AdditionalHeaderControlModel;
import com.top_logic.layout.table.model.ColumnBaseConfig;
import com.top_logic.layout.table.model.SimpleAdditionalHeaderControl;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * The {@link ControlProvider} for {@link AdditionalHeaderControlByExpression}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@InApp
public class AdditionalHeaderControlProviderByExpression
		extends AbstractConfiguredInstance<AdditionalHeaderControlProviderByExpression.Config> implements ControlProvider {

	/** {@link ConfigurationItem} for the {@link AdditionalHeaderControlProviderByExpression}. */
	@TagName(Config.TAG_NAME)
	@DisplayOrder({
		Config.RES_KEY,
		Config.LABEL,
		Config.TOOLTIP,
		Config.USE_ROW_OBJECTS,
		ColumnBaseConfig.CSS_CLASS
	})
	public interface Config extends PolymorphicConfiguration<AdditionalHeaderControlProviderByExpression> {

		/** The {@link TagName} for this {@link ConfigurationItem}. */
		String TAG_NAME = "byExpression";

		/** Property name of {@link #getLabel()}. */
		String LABEL = "label";

		/** Property name of {@link #getTooltip()}. */
		String TOOLTIP = "tooltip";

		/** Property name of {@link #getResKey()}. */
		String RES_KEY = "resKey";

		/** Property name of {@link #getUseRowObjects()}. */
		String USE_ROW_OBJECTS = "useRowObjects";

		/**
		 * The TL-Script expression for the label.
		 *
		 * <p>
		 * This script is expected take two arguments, the {@link List} of
		 * {@link AdditionalHeaderControlModel#getValues() column values} as first argument and the
		 * current component model as second argument.
		 * </p>
		 * <p>
		 * The result of the script is converted to a {@link String} with the
		 * {@link MetaLabelProvider}.
		 * </p>
		 * <p>
		 * If {@link #getResKey()} is also given, the dynamic label is passed as single argument to
		 * the configured {@link ResKey} for embedding. The static label text should contain a
		 * placeholder <code>{0}</code> where the dynamic label is to be inserted.
		 * </p>
		 */
		@Name(LABEL)
		@Label("Dynamic Label")
		Expr getLabel();

		/**
		 * The TL-Script expression for the tooltip.
		 * <p>
		 * This script is expected take two arguments, the {@link List} of
		 * {@link AdditionalHeaderControlModel#getValues() column values} as first argument and the
		 * current component model as second argument.
		 * </p>
		 * <p>
		 * The result of the script is converted to a {@link String} with the
		 * {@link MetaLabelProvider}.
		 * </p>
		 * 
		 */
		@Name(TOOLTIP)
		Expr getTooltip();

		/**
		 * The {@link ResKey} for the static label.
		 *
		 * <p>
		 * If provided along with {@link #getLabel()}, the static label text should contain a
		 * placeholder <code>{0}</code> where the dynamic content will be inserted. If only the
		 * static label is given, it will be displayed as is.
		 * </p>
		 *
		 * <p>
		 * Note: The tooltip {@link ResKey} is derived by appending <code>.tooltip</code> to this
		 * key. Both translations (with and without suffix) must exist when using this key.
		 * </p>
		 *
		 * @see SimpleAdditionalHeaderControl#composeStaticWithDynamicPart(ResKey, Object)
		 */
		@Name(RES_KEY)
		@Label("Static Label")
		ResKey getResKey();

		/**
		 * Additional CSS class for the rendered control.
		 * 
		 * @see AdditionalHeaderControlByExpression#getCssClass()
		 */
		@Name(ColumnBaseConfig.CSS_CLASS)
		String getCssClass();

		/**
		 * Whether the {@link #getLabel()} and {@link #getTooltip()} scripts should get the row
		 * objects instead of the column values as input.
		 * 
		 * @see AdditionalHeaderControlByExpression#getUseRowObjects()
		 */
		@Name(USE_ROW_OBJECTS)
		boolean getUseRowObjects();

	}

	private LayoutComponent _component;

	private final QueryExecutor _labelScript;

	private final QueryExecutor _tooltipScript;

	private final ResKey _resKey;

	private final String _cssClass;

	private final boolean _useRowObjects;

	/** {@link TypedConfiguration} constructor for {@link AdditionalHeaderControlProviderByExpression}. */
	public AdditionalHeaderControlProviderByExpression(InstantiationContext context, Config config) {
		super(context, config);

		if (!(context instanceof SimpleInstantiationContext)) {
			context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, c -> _component = c);
		}
		_labelScript = QueryExecutor.compileOptional(config.getLabel());
		_tooltipScript = QueryExecutor.compileOptional(config.getTooltip());
		_resKey = config.getResKey();
		_cssClass = config.getCssClass();
		_useRowObjects = config.getUseRowObjects();
	}

	@Override
	public Control createControl(Object model, String style) {
		return new AdditionalHeaderControlByExpression((AdditionalHeaderControlModel) model,
			_labelScript, _tooltipScript, _resKey, _cssClass, _useRowObjects, _component, emptyMap());
	}

}

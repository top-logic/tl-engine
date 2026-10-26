/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.table.ReactResourceCellControl;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.InputsOf;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.layout.view.table.AbstractColumnDeclaration;
import com.top_logic.layout.view.table.ValueAsConfig;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.providers.IconProviderByExpression;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.util.Resources;

/**
 * Display of an object by its icon and its label, each of which - and its CSS class and its tooltip
 * - computed by a TL-Script expression over the object.
 *
 * <p>
 * The display serves wherever an object is shown by a {@link ReactControlProvider}: as the
 * <code>nodeContent</code> of a <code>&lt;tree&gt;</code> and as the <code>display</code> of a
 * table column. Whatever the configuration does not compute, the object shows as its type
 * registers it with the {@link MetaResourceProvider}, so a display computing only the label keeps
 * the icon of the object.
 * </p>
 *
 * <p>
 * The display follows the object it shows: a change of the object resolves label, icon, CSS class
 * and tooltip again.
 * </p>
 *
 * <p>
 * Example showing a device as <i>Taster (1.1.3)</i>, with its own icon and a class marking an
 * inactive one:
 * </p>
 *
 * <pre>
 * &lt;display class="com.top_logic.layout.view.element.ResourceDisplayByExpression"
 *     label="$value.get(`my:Device#name`) + ' (' + $value.get(`my:Device#address`) + ')'"
 *     css-class="$value.get(`my:Device#active`) ? null : 'tl-status-warning'"
 * /&gt;
 * </pre>
 *
 * @implNote The display is a {@link ReactResourceCellControl} reading the parts of the display from
 *           this {@link ResourceProvider}.
 */
@InApp
public class ResourceDisplayByExpression implements ReactControlProvider, ResourceProvider {

	/**
	 * Configuration of a {@link ResourceDisplayByExpression}.
	 *
	 * <p>
	 * Each expression sees the displayed object as the variable
	 * {@value ValueAsConfig#DEFAULT_VALUE_AS}, or by the name the column holding the display gives
	 * the cell value with {@value ValueAsConfig#VALUE_AS}. A display gets no channels, so the
	 * expressions see no inputs.
	 * </p>
	 */
	public interface Config extends PolymorphicConfiguration<ResourceDisplayByExpression>, ConfigPart {

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getIcon()}. */
		String ICON = "icon";

		/** Configuration name for {@link #getCssClass()}. */
		String CSS_CLASS = "css-class";

		/** Configuration name for {@link #getTooltip()}. */
		String TOOLTIP = "tooltip";

		/** Configuration name for {@link #getImage()}. */
		String IMAGE = "image";

		/** Configuration name for {@link #getLink()}. */
		String LINK = "link";

		@Override
		@ClassDefault(ResourceDisplayByExpression.class)
		Class<? extends ResourceDisplayByExpression> getImplementationClass();

		/**
		 * The label of the displayed object: an expression computing the text naming it.
		 *
		 * <p>
		 * The result is a text, an internationalized text written as
		 * <code>#('Text'@en, 'Text'@de)</code>, or any other value shown by its label. Unset, or
		 * where the expression yields nothing, the object shows its own label.
		 * </p>
		 */
		@Name(LABEL)
		@Supplies(value = ValueAsConfig.DEFAULT_VALUE_AS, renamedBy = ValueAsConfig.VALUE_AS)
		@InputsOf(Config.class)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getLabel();

		/**
		 * The icon of the displayed object: an expression computing the icon shown before the
		 * label.
		 *
		 * <p>
		 * The result is an icon - e.g. the value of an attribute of type <code>tl.core:Icon</code> -
		 * or the text of an icon resource, e.g. <code>"css:fas fa-car-side"</code>. Unset, or where
		 * the expression yields nothing, the object shows the icon of its type. No icon at all is
		 * shown when {@link #getImage()} is switched off.
		 * </p>
		 */
		@Name(ICON)
		@Supplies(value = ValueAsConfig.DEFAULT_VALUE_AS, renamedBy = ValueAsConfig.VALUE_AS)
		@InputsOf(Config.class)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getIcon();

		/**
		 * The CSS class of the display: an expression computing the class put on the display of the
		 * object.
		 *
		 * <p>
		 * The result becomes the class as the result of a column's
		 * {@link AbstractColumnDeclaration.Config#getCellClass() cell class} does: nothing or an
		 * empty string gives the display no class, a list all of its members, any other value its
		 * text. Unset, the display carries the class the type of the object gives it.
		 * </p>
		 */
		@Name(CSS_CLASS)
		@Supplies(value = ValueAsConfig.DEFAULT_VALUE_AS, renamedBy = ValueAsConfig.VALUE_AS)
		@InputsOf(Config.class)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getCssClass();

		/**
		 * The tooltip of the display: an expression computing what the user reads when pointing at
		 * the object.
		 *
		 * <p>
		 * The result is a text, an internationalized text, or any other value shown by its label;
		 * nothing at all leaves the display without a tooltip. Unset, the display shows the tooltip
		 * the type of the object gives it.
		 * </p>
		 */
		@Name(TOOLTIP)
		@Supplies(value = ValueAsConfig.DEFAULT_VALUE_AS, renamedBy = ValueAsConfig.VALUE_AS)
		@InputsOf(Config.class)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getTooltip();

		/**
		 * Whether the icon of the displayed object is shown before its label.
		 */
		@Name(IMAGE)
		@BooleanDefault(true)
		boolean getImage();

		/**
		 * Whether the display leads to the place the application shows the object at.
		 *
		 * <p>
		 * A display computing the parts of an object is mostly the object's own place in the view -
		 * a node of a tree, the tree column of a tree table - where a click selects the object, and
		 * a link leaving the view on that click is in the way. A display of an object a row merely
		 * points to switches the link on.
		 * </p>
		 */
		@Name(LINK)
		boolean getLink();

	}

	private final QueryExecutor _label;

	private final QueryExecutor _icon;

	private final QueryExecutor _cssClass;

	private final QueryExecutor _tooltip;

	private final boolean _useImage;

	private final boolean _useLink;

	/**
	 * Creates a {@link ResourceDisplayByExpression} from configuration.
	 *
	 * @param context
	 *        The context the display is instantiated in.
	 * @param config
	 *        The expressions computing the parts of the display.
	 */
	@CalledByReflection
	public ResourceDisplayByExpression(InstantiationContext context, Config config) {
		_label = InputScript.compile(config, Config.LABEL);
		_icon = InputScript.compile(config, Config.ICON);
		_cssClass = InputScript.compile(config, Config.CSS_CLASS);
		_tooltip = InputScript.compile(config, Config.TOOLTIP);
		_useImage = config.getImage();
		_useLink = config.getLink();
	}

	@Override
	public ReactControl createControl(ReactContext context, Object model) {
		return new ReactResourceCellControl(context, model, this, _useImage, true, _useLink);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The label the {@link Config#getLabel() label expression} computes, the object's own where it
	 * computes nothing.
	 * </p>
	 */
	@Override
	public String getLabel(Object object) {
		if (_label != null && object != null) {
			Object result = _label.execute(object);
			if (result != null) {
				return result instanceof ResKey key ? Resources.getInstance().getString(key)
					: SearchExpression.asString(result);
			}
		}
		return MetaResourceProvider.INSTANCE.getLabel(object);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The icon the {@link Config#getIcon() icon expression} computes, the object's own where it
	 * computes none.
	 * </p>
	 */
	@Override
	public ThemeImage getImage(Object object, Flavor flavor) {
		if (_icon != null && object != null) {
			ThemeImage icon = IconProviderByExpression.toIcon(_icon.execute(object));
			if (icon != null) {
				return icon;
			}
		}
		return MetaResourceProvider.INSTANCE.getImage(object, flavor);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The class the {@link Config#getCssClass() CSS class expression} computes, {@code null} where
	 * it computes none.
	 * </p>
	 */
	@Override
	public String getCssClass(Object object) {
		if (_cssClass == null) {
			return MetaResourceProvider.INSTANCE.getCssClass(object);
		}
		return AbstractColumnDeclaration.cssClass(_cssClass.execute(object));
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The text the {@link Config#getTooltip() tooltip expression} computes, written as HTML,
	 * {@code null} where it computes nothing.
	 * </p>
	 */
	@Override
	public String getTooltip(Object object) {
		if (_tooltip == null) {
			return MetaResourceProvider.INSTANCE.getTooltip(object);
		}
		String text = AbstractColumnDeclaration.tooltipText(_tooltip.execute(object));
		return text == null || text.isEmpty() ? null : TagUtil.encodeXML(text);
	}

	@Override
	public String getType(Object object) {
		return MetaResourceProvider.INSTANCE.getType(object);
	}

	@Override
	public String getLink(DisplayContext context, Object object) {
		return MetaResourceProvider.INSTANCE.getLink(context, object);
	}

}

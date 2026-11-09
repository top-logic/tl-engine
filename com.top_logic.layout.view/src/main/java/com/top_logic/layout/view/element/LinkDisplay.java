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
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.LinkKind;
import com.top_logic.layout.react.control.common.ReactLinkControl;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.InputsOf;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.layout.view.table.AbstractColumnDeclaration;
import com.top_logic.layout.view.table.ValueAsConfig;
import com.top_logic.model.TLObject;
import com.top_logic.model.listen.ObservedObjects;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;

/**
 * Display of a value holding an address - a web address, an e-mail address, a telephone number -
 * as a link opening that address.
 *
 * <p>
 * The display serves wherever a value is shown by a {@link ReactControlProvider}: as the
 * <code>display</code> of a table column and as the <code>nodeContent</code> of a
 * <code>&lt;tree&gt;</code>. By default the value is the address and is shown as it is written; the
 * address and the shown text can each be computed by a TL-Script expression over the value
 * instead, e.g. to show an object by its name and link it to its homepage. A value without an
 * address is shown without a link.
 * </p>
 *
 * <p>
 * The display follows the object it shows: a change of the object computes address, text and
 * tooltip again.
 * </p>
 *
 * <p>
 * Example showing the e-mail address of a contact as a link writing an e-mail:
 * </p>
 *
 * <pre>
 * &lt;column attribute="email"&gt;
 *   &lt;display class="com.top_logic.layout.view.element.LinkDisplay" kind="email"/&gt;
 * &lt;/column&gt;
 * </pre>
 *
 * @implNote The display is a {@link ReactLinkControl}, and the label it is a {@link LabelProvider}
 *           of is the text the link shows, so that a table's free-text search finds the cell by
 *           it.
 */
@InApp
public class LinkDisplay implements ReactControlProvider, LabelProvider {

	/**
	 * Configuration of a {@link LinkDisplay}.
	 *
	 * <p>
	 * Each expression sees the displayed value as the variable
	 * {@value ValueAsConfig#DEFAULT_VALUE_AS}, or by the name the column holding the display gives
	 * the cell value with {@value ValueAsConfig#VALUE_AS}. A display gets no channels, so the
	 * expressions see no inputs.
	 * </p>
	 */
	public interface Config extends PolymorphicConfiguration<LinkDisplay>, ConfigPart {

		/** Configuration name for {@link #getHref()}. */
		String HREF = "href";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getTooltip()}. */
		String TOOLTIP = "tooltip";

		/** Configuration name for {@link #getKind()}. */
		String KIND = "kind";

		/** Configuration name for {@link #getNewWindow()}. */
		String NEW_WINDOW = "new-window";

		@Override
		@ClassDefault(LinkDisplay.class)
		Class<? extends LinkDisplay> getImplementationClass();

		/**
		 * The address the link opens: an expression computing it from the displayed value.
		 *
		 * <p>
		 * The result is written as the {@link #getKind() kind} of the address takes it: a web
		 * address, an e-mail address or a telephone number, each without a scheme for the latter
		 * two. Unset, the displayed value is the address. Where the address is empty, the value is
		 * shown without a link.
		 * </p>
		 */
		@Name(HREF)
		@Label("Address")
		@Supplies(value = ValueAsConfig.DEFAULT_VALUE_AS, renamedBy = ValueAsConfig.VALUE_AS)
		@InputsOf(Config.class)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getHref();

		/**
		 * The text the link shows: an expression computing it from the displayed value.
		 *
		 * <p>
		 * The result is a text, an internationalized text written as
		 * <code>#('Text'@en, 'Text'@de)</code>, or any other value shown by its label. Unset, or
		 * where the expression yields nothing, the link shows its address as it is written.
		 * </p>
		 */
		@Name(LABEL)
		@Supplies(value = ValueAsConfig.DEFAULT_VALUE_AS, renamedBy = ValueAsConfig.VALUE_AS)
		@InputsOf(Config.class)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getLabel();

		/**
		 * The tooltip of the link: an expression computing what the user reads when pointing at it.
		 *
		 * <p>
		 * The result is a text, an internationalized text, or any other value shown by its label;
		 * nothing at all leaves the link without a tooltip. Unset, the link has none.
		 * </p>
		 */
		@Name(TOOLTIP)
		@Supplies(value = ValueAsConfig.DEFAULT_VALUE_AS, renamedBy = ValueAsConfig.VALUE_AS)
		@InputsOf(Config.class)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getTooltip();

		/**
		 * What the address is: a web address opened as it is, an e-mail address the link writes an
		 * e-mail to, or a telephone number the link calls.
		 */
		@Name(KIND)
		LinkKind getKind();

		/**
		 * Whether the link opens its address in a new browser window (or tab) instead of the window
		 * of the application.
		 */
		@Name(NEW_WINDOW)
		@BooleanDefault(true)
		boolean getNewWindow();

	}

	private final QueryExecutor _href;

	private final QueryExecutor _label;

	private final QueryExecutor _tooltip;

	private final LinkKind _kind;

	private final boolean _newWindow;

	/**
	 * Creates a {@link LinkDisplay} from configuration.
	 *
	 * @param context
	 *        The context the display is instantiated in.
	 * @param config
	 *        The configuration of the link.
	 */
	@CalledByReflection
	public LinkDisplay(InstantiationContext context, Config config) {
		_href = InputScript.compile(config, Config.HREF);
		_label = InputScript.compile(config, Config.LABEL);
		_tooltip = InputScript.compile(config, Config.TOOLTIP);
		_kind = config.getKind();
		_newWindow = config.getNewWindow();
	}

	@Override
	public ReactControl createControl(ReactContext context, Object model) {
		ReactLinkControl control = new ReactLinkControl(context, getLabel(model), getAddress(model));
		control.setTooltip(getTooltip(model));
		control.setKind(_kind);
		control.setNewWindow(_newWindow);

		ObservedObjects displayed = new ObservedObjects(event -> {
			if (model instanceof TLObject object && !object.tValid()) {
				// A deleted object has nothing to compute the link from; the display holding the
				// control drops it.
				return;
			}
			control.setLink(getLabel(model), getAddress(model), getTooltip(model));
		});
		displayed.observeValue(model);
		control.addAttachListener(() -> displayed.attach(context.getModelScope()));
		control.addDetachListener(displayed::detach);
		return control;
	}

	/**
	 * The address the link to the given value opens, as the {@link Config#getKind() kind} writes
	 * it.
	 *
	 * @return The address the {@link Config#getHref() address expression} computes, the value
	 *         itself where there is none; the empty text for a value without an address.
	 */
	public String getAddress(Object value) {
		Object address = _href != null && value != null ? _href.execute(value) : value;
		return nonNull(AbstractColumnDeclaration.tooltipText(address));
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The text the {@link Config#getLabel() label expression} computes, the {@link #getAddress(Object)
	 * address} where it computes nothing.
	 * </p>
	 */
	@Override
	public String getLabel(Object value) {
		if (_label != null && value != null) {
			String label = AbstractColumnDeclaration.tooltipText(_label.execute(value));
			if (label != null && !label.isEmpty()) {
				return label;
			}
		}
		return getAddress(value);
	}

	/**
	 * The tooltip of the link to the given value, plain text.
	 *
	 * @return The text the {@link Config#getTooltip() tooltip expression} computes, {@code null}
	 *         for none.
	 */
	public String getTooltip(Object value) {
		if (_tooltip == null || value == null) {
			return null;
		}
		String text = AbstractColumnDeclaration.tooltipText(_tooltip.execute(value));
		return text == null || text.isEmpty() ? null : text;
	}

	private static String nonNull(String text) {
		return text == null ? "" : text;
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.common.ReactDividerControl;
import com.top_logic.layout.react.control.common.ReactDividerControl.Inset;
import com.top_logic.layout.react.control.common.ReactDividerControl.LabelPosition;
import com.top_logic.layout.react.control.common.ReactDividerControl.Orientation;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.util.Resources;

/**
 * {@link UIElement} drawing a line that separates the content before it from the content after it,
 * in a stack, in the columns of a row, or in a card.
 *
 * <p>
 * A {@link Orientation#HORIZONTAL horizontal} line runs across, between content stacked one above
 * the other, and may carry a short text in the line. A {@link Orientation#VERTICAL vertical} line
 * stands in a row (a {@code <stack direction="row">}) and stretches to its height. The direction is
 * always the configured one; it is not derived from the element around the line.
 * </p>
 *
 * <p>
 * Example: a heading line between two groups of fields, and a vertical line between two buttons.
 * </p>
 *
 * <pre>
 * &lt;divider label-position="start"&gt;
 *   &lt;label&gt;&lt;en&gt;Address&lt;/en&gt;&lt;de&gt;Adresse&lt;/de&gt;&lt;/label&gt;
 * &lt;/divider&gt;
 *
 * &lt;stack direction="row"&gt;
 *   ...
 *   &lt;divider orientation="vertical"/&gt;
 *   ...
 * &lt;/stack&gt;
 * </pre>
 *
 * @implNote The control is a {@link ReactDividerControl}, which draws the part {@code Divider}.
 */
@InApp
public class DividerElement implements UIElement {

	/**
	 * Configuration for {@link DividerElement}.
	 */
	@TagName("divider")
	@TypeIcon("css:bi bi-hr")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getOrientation()}. */
		String ORIENTATION = "orientation";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getLabelPosition()}. */
		String LABEL_POSITION = "label-position";

		/** Configuration name for {@link #getInset()}. */
		String INSET = "inset";

		@Override
		@ClassDefault(DividerElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The direction of the line: {@link Orientation#HORIZONTAL across} (the default), between
		 * content stacked one above the other, or {@link Orientation#VERTICAL standing} in a row and
		 * stretching to its height.
		 */
		@Name(ORIENTATION)
		Orientation getOrientation();

		/**
		 * A short text shown in the line, e.g. the name of the section below it.
		 *
		 * <p>
		 * Only a horizontal line carries a text. Without one, the line is plain.
		 * </p>
		 */
		@Name(LABEL)
		@Nullable
		ResKey getLabel();

		/**
		 * Where the {@link #getLabel() text} stands in the line: at the {@link LabelPosition#START
		 * start}, in the {@link LabelPosition#CENTER middle} (the default), or at the
		 * {@link LabelPosition#END end}.
		 */
		@Name(LABEL_POSITION)
		@FormattedDefault("center")
		LabelPosition getLabelPosition();

		/**
		 * Where the line begins: across the {@link Inset#NONE whole width} (the default), or
		 * {@link Inset#START shortened at its start} by the indent of an entry of a list, so that it
		 * separates the entries of a list without cutting through its leading edge.
		 */
		@Name(INSET)
		Inset getInset();
	}

	private final Orientation _orientation;

	private final ResKey _label;

	private final LabelPosition _labelPosition;

	private final Inset _inset;

	private final String _cssClass;

	/**
	 * Creates a new {@link DividerElement} from configuration.
	 */
	@CalledByReflection
	public DividerElement(InstantiationContext context, Config config) {
		_orientation = config.getOrientation();
		_label = config.getLabel();
		_labelPosition = config.getLabelPosition();
		_inset = config.getInset();
		_cssClass = config.getCssClass();

		if (_label != null && _orientation == Orientation.VERTICAL) {
			context.error("A <divider> with '" + Config.ORIENTATION + "=\"" + Orientation.VERTICAL.getExternalName()
				+ "\"' carries no '" + Config.LABEL + "': only a horizontal line shows a text.");
		}
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		String label = _label == null ? null : Resources.getInstance().getString(_label);
		ReactDividerControl control = new ReactDividerControl(context, _orientation, label, _labelPosition, _inset);
		control.setCssClass(_cssClass);
		return control;
	}
}

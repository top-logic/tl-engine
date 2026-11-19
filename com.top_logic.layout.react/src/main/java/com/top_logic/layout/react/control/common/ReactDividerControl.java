/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import java.util.Set;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.state.DividerState;

/**
 * A read-only control drawing a line that separates the content before it from the content after
 * it, via the {@code TLDivider} React component, which draws the part {@code Divider}.
 *
 * <p>
 * A horizontal line may carry a short text, shown in the line at its {@link LabelPosition}. A
 * vertical line stands in a row and stretches to its height; it carries no text.
 * </p>
 *
 * <p>
 * The state is described by {@link DividerState}.
 * </p>
 */
public class ReactDividerControl extends ReactControl {

	private static final String REACT_MODULE = "TLDivider";

	/**
	 * The direction of a divider.
	 */
	public enum Orientation implements ExternallyNamed {

		/** A line across, between content stacked one above the other. */
		HORIZONTAL("horizontal"),

		/** A line standing in a row and stretching to its height. */
		VERTICAL("vertical");

		private final String _externalName;

		Orientation(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * Where the text of a divider stands in its line.
	 */
	public enum LabelPosition implements ExternallyNamed {

		/** At the start of the line. */
		START("start"),

		/** In the middle, with a line on either side. */
		CENTER("center"),

		/** At the end of the line. */
		END("end");

		private final String _externalName;

		LabelPosition(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * Where a divider begins.
	 */
	public enum Inset implements ExternallyNamed {

		/** The line spans the whole width. */
		NONE("none"),

		/** The line is shortened at its start by the indent of an entry of a list. */
		START("start");

		private final String _externalName;

		Inset(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * Creates a {@link ReactDividerControl}.
	 *
	 * @param context
	 *        The {@link ReactContext} for ID allocation and SSE registration.
	 * @param orientation
	 *        The direction of the line.
	 * @param label
	 *        The text shown in the line, or {@code null} for a plain line. Only a
	 *        {@link Orientation#HORIZONTAL horizontal} line carries a text.
	 * @param labelPosition
	 *        Where the text stands in the line.
	 * @param inset
	 *        Where the line begins.
	 */
	public ReactDividerControl(ReactContext context, Orientation orientation, String label,
			LabelPosition labelPosition, Inset inset) {
		super(context, null, REACT_MODULE);
		if (label != null && orientation == Orientation.VERTICAL) {
			throw new IllegalArgumentException("A vertical divider carries no text.");
		}
		putState(DividerState.ORIENTATION__PROP, orientation.getExternalName());
		putState(DividerState.LABEL__PROP, label);
		putState(DividerState.LABEL_POSITION__PROP, labelPosition.getExternalName());
		putState(DividerState.INSET__PROP, inset.getExternalName());
	}

	/**
	 * Rendering-only state keys, omitted from the headless projection.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), DividerState.ORIENTATION__PROP,
			DividerState.LABEL_POSITION__PROP, DividerState.INSET__PROP);
	}
}

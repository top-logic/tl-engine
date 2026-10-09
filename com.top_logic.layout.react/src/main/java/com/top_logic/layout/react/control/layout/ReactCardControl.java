/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.state.CardState;

/**
 * A {@link ReactControl} that renders an elevated content container via the {@code TLCard} React
 * component.
 *
 * <p>
 * A card is lighter than {@link ReactPanelControl} - it provides visual grouping with an optional
 * header but no minimize/maximize/pop-out behavior.
 * </p>
 *
 * <p>
 * The state is described by {@link CardState}.
 * </p>
 */
public class ReactCardControl extends ReactControl {

	private static final String REACT_MODULE = "TLCard";

	/**
	 * Visual variant of a card.
	 */
	public enum CardVariant implements ExternallyNamed {

		/** Card with a thin border. */
		OUTLINED("outlined"),

		/** Card with a drop shadow. */
		ELEVATED("elevated");

		private final String _externalName;

		CardVariant(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * Content padding of a card.
	 */
	public enum CardPadding implements ExternallyNamed {

		/** Standard padding. */
		DEFAULT("default"),

		/** Reduced padding. */
		COMPACT("compact"),

		/** No padding. */
		NONE("none");

		private final String _externalName;

		CardPadding(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	private final ReactControl _child;

	private final List<ReactControl> _headerActions;

	/**
	 * Creates a card with full configuration.
	 *
	 * @param title
	 *        The header text, or {@code null} for no header.
	 * @param variant
	 *        The visual variant.
	 * @param padding
	 *        The content padding.
	 * @param headerActions
	 *        Optional action buttons in the header.
	 * @param child
	 *        The content child control.
	 */
	public ReactCardControl(ReactContext context, String title, CardVariant variant, CardPadding padding,
			List<? extends ReactControl> headerActions, ReactControl child) {
		super(context, null, REACT_MODULE);
		_child = child;
		_headerActions = new ArrayList<>(headerActions);
		setTitle(title);
		setVariant(variant);
		setPadding(padding);
		putState(CardState.HEADER_ACTIONS__PROP, _headerActions);
		putState(CardState.CHILD__PROP, child);
	}

	/**
	 * Creates an outlined card with default padding and no header actions.
	 *
	 * @param title
	 *        The header text, or {@code null} for no header.
	 * @param child
	 *        The content child control.
	 */
	public ReactCardControl(ReactContext context, String title, ReactControl child) {
		this(context, title, CardVariant.OUTLINED, CardPadding.DEFAULT, List.of(), child);
	}

	/**
	 * Updates the card title.
	 *
	 * @param title
	 *        The new title, or {@code null} to remove the header.
	 */
	public void setTitle(String title) {
		putState(CardState.TITLE__PROP, title);
	}

	/**
	 * Sets the visual variant.
	 */
	public void setVariant(CardVariant variant) {
		putState(CardState.VARIANT__PROP, variant.getExternalName());
	}

	/**
	 * Sets the content padding.
	 */
	public void setPadding(CardPadding padding) {
		putState(CardState.PADDING__PROP, padding.getExternalName());
	}

	/**
	 * Rendering-only state keys, omitted from the headless projection.
	 */
	@Override
	protected java.util.Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), CardState.PADDING__PROP,
			CardState.VARIANT__PROP);
	}
}

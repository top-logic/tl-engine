/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.GlobalKey;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.react.control.IReactControl;

/**
 * Base interface for all view elements.
 *
 * <p>
 * A UIElement is a stateless factory for {@link IReactControl}. The UIElement tree is shared across
 * all sessions (parsed once from configuration). Each session gets its own control tree by calling
 * {@link #createControl(ViewContext)}.
 * </p>
 */
public interface UIElement {

	/**
	 * Configuration interface for {@link UIElement}.
	 *
	 * <p>
	 * Every concrete UIElement has a corresponding Config sub-interface. Because this extends
	 * {@link PolymorphicConfiguration}, new element types can be registered in any module.
	 * </p>
	 */
	interface Config extends PolymorphicConfiguration<UIElement> {

		/** Configuration name for {@link #getId()}. */
		String ID = "id";

		/** Configuration name for {@link #getPersonalizationKey()}. */
		String PERSONALIZATION_KEY = "personalization-key";

		/** Configuration name for {@link #getCssClass()}. */
		String CSS_CLASS = "css-class";

		/**
		 * Optional override for the auto-derived personalization key.
		 *
		 * <p>
		 * If set, stateful elements use this key instead of the auto-derived path from the view
		 * context.
		 * </p>
		 *
		 * @see ViewContext#getPersonalizationKey()
		 */
		@com.top_logic.basic.config.annotation.Name(PERSONALIZATION_KEY)
		String getPersonalizationKey();

		/**
		 * The name an overlay of the view addresses this element by.
		 *
		 * <p>
		 * A view is extended by a view of the same path in another module. Such an overlay
		 * addresses the element it updates, replaces, removes, places another element beside or
		 * adds content to by its id, with a referencing element written anywhere above it:
		 * {@code <tab-bar config:ref="device-tabs">}. An element without id is anonymous: an
		 * overlay adds elements beside it, but cannot address the element itself.
		 * </p>
		 *
		 * <p>
		 * The id is unique in the whole view: among all elements, tabs, panes, cases, sidebar items
		 * and menu groups of the view file, its overlays included. A view a {@code <view-ref>}
		 * embeds is a view of its own with ids of its own.
		 * </p>
		 *
		 * @implNote The element lists of the view elements are keyed by this property, which is a
		 *           {@link GlobalKey}: the configuration reader resolves a {@code config:ref} among
		 *           the entries of all such lists below the enclosing element and rejects two
		 *           entries with the same id. An anonymous element is appended in order and
		 *           matched by no overlay entry; an overlay reaches it only by its position with
		 *           {@code config:index}.
		 */
		@com.top_logic.basic.config.annotation.Name(ID)
		@GlobalKey
		String getId();

		/**
		 * CSS class the element is displayed with, beside the classes its kind brings itself.
		 *
		 * <p>
		 * The class is written on the element the display of this element is rooted in, so that a
		 * stylesheet of the application styles this one element - a card, a picture, a piece of
		 * text - without touching every element of its kind.
		 * </p>
		 *
		 * <p>
		 * Several classes are written separated by spaces, as in HTML.
		 * </p>
		 */
		@com.top_logic.basic.config.annotation.Name(CSS_CLASS)
		@Nullable
		String getCssClass();
	}

	/**
	 * Creates a {@link IReactControl} for this element in the given session context.
	 *
	 * @param context
	 *        The view context providing session-scoped infrastructure.
	 * @return A control for the current session. Typically a
	 *         {@link com.top_logic.layout.react.control.ReactControl}.
	 */
	IReactControl createControl(ViewContext context);

	/**
	 * The content this element holds according to its configuration, grouped as its container
	 * addresses it.
	 *
	 * <p>
	 * Answered without a session, so that the element tree of a view can be walked before any
	 * control exists. Content a container only learns at runtime (a frame pushed onto a tile stack,
	 * a row rendered per object) is not part of the answer.
	 * </p>
	 *
	 * @return The groups, in configuration order. Empty for an element that holds no content of its
	 *         own.
	 *
	 * @see ChildGroup
	 */
	default List<ChildGroup> getChildGroups() {
		return List.of();
	}
}

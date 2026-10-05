/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.security.AccessChecks;
import com.top_logic.layout.view.security.AccessControl;
import com.top_logic.layout.view.security.SecurityScope;
import com.top_logic.util.Resources;

/**
 * A configured {@link ContentSectionConfig part with content of its own}, as far as it is the same
 * for every session.
 *
 * <p>
 * The label stays a {@link ResKey}: an element is parsed once and shared by every session, so a
 * text resolved at parse time would be the one language whichever session loaded the view first
 * happened to ask in.
 * </p>
 *
 * @implNote {@link #label()} resolves the label for the session being served.
 */
public final class ContentSection {

	private final String _id;

	private final ResKey _label;

	private final String _icon;

	private final AccessControl _accessControl;

	private final List<UIElement> _children;

	private ContentSection(String id, ResKey label, String icon, AccessControl accessControl,
			List<UIElement> children) {
		_id = id;
		_label = label;
		_icon = icon;
		_accessControl = accessControl;
		_children = children;
	}

	/**
	 * Creates a {@link ContentSection} from its configuration.
	 *
	 * @param context
	 *        The context instantiating the content elements.
	 * @param config
	 *        The configuration of the part.
	 */
	public static ContentSection of(InstantiationContext context, ContentSectionConfig config) {
		String icon = config.getIcon();
		return new ContentSection(config.getId(), config.getLabel(), StringServices.isEmpty(icon) ? null : icon,
			config.getAccessControl(),
			config.getChildren().stream()
				.map(context::getInstance)
				.collect(Collectors.toList()));
	}

	/**
	 * The identifier of this part, unique within its container.
	 */
	public String getId() {
		return _id;
	}

	/**
	 * The encoded icon shown next to the label, or {@code null} for none.
	 */
	public String getIcon() {
		return _icon;
	}

	/**
	 * The content elements of this part.
	 */
	public List<UIElement> getChildren() {
		return _children;
	}

	/**
	 * Whether the current user may see this part.
	 */
	public boolean isAccessible() {
		return AccessChecks.isAccessible(_accessControl);
	}

	/**
	 * The label to display, in the language of the session being served.
	 *
	 * <p>
	 * Falls back to the {@link #getId() ID} while no label is configured, so that the part is
	 * visible and can be selected instead of rendering blank.
	 * </p>
	 */
	public String label() {
		String label = Resources.getInstance().getString(_label, null);
		return StringServices.isEmpty(label) ? _id : label;
	}

	/**
	 * The context the content of this part is created in.
	 *
	 * <p>
	 * Extends the personalization key with the given segment and the slot path with the
	 * {@link #getId() ID}, so that same-named {@code <slot-content>} in two parts route into
	 * independent positions. Channels are not forked: only a {@code <view>} declares channels, so
	 * the content shares the enclosing view's channels. A part needing its own channel namespace
	 * embeds a {@code <view-ref>} to a separate {@code <view>} and binds across that boundary. The
	 * security scope of the part's access control becomes the scope command rules in the content
	 * default to.
	 * </p>
	 *
	 * @param context
	 *        The context of the container, already locating the part.
	 * @param segment
	 *        The personalization key segment naming the kind of part.
	 */
	public ViewContext contentContext(ViewContext context, String segment) {
		ViewContext baseContext = context.childContext(segment).withChildSlotPath(_id);
		SecurityScope scope = AccessChecks.resolveScope(_accessControl);
		return scope != null ? baseContext.withScope(SecurityScope.class, scope) : baseContext;
	}

	/**
	 * Creates the control displaying the content of this part.
	 *
	 * @param contentContext
	 *        The context derived by {@link #contentContext(ViewContext, String)}.
	 */
	public ReactControl createContent(ViewContext contentContext) {
		return ContentControls.toControl(_children, contentContext);
	}

}

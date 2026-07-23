/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.model.TLObject;

/**
 * A {@link ReactControl} that wraps a single child control in a scroll anchor identified by a model
 * object, via the {@code TLAnchor} React component.
 *
 * <p>
 * The anchor renders a wrapper carrying its key as a {@code data-tl-anchor} attribute. The key is
 * either an object's {@link #anchorId(Object) anchor id} (for object-keyed anchors) or a literal
 * name (for fixed regions). A {@link ScrollLinkControl} or a {@code <scroll-to-anchor>} action
 * targeting the same key scrolls the browser here; object-keyed anchors and links derive the key
 * from the same {@link #anchorId(Object)} helper, so they cannot drift.
 * </p>
 */
public class AnchorControl extends ReactControl {

	private static final String REACT_MODULE = "TLAnchor";

	/** State key for the wrapped child control. */
	private static final String CHILD = "child";

	/** State key for the anchor key ({@code data-tl-anchor} value), or {@code null} for no anchor. */
	private static final String ANCHOR = "anchor";

	private final ReactControl _child;

	/**
	 * Creates a new {@link AnchorControl}.
	 *
	 * @param context
	 *        The {@link ReactContext} for ID allocation and SSE registration.
	 * @param child
	 *        The wrapped content control.
	 * @param key
	 *        The anchor key ({@code data-tl-anchor} value), or {@code null} for no anchor. Use
	 *        {@link #anchorId(Object)} for an object-keyed anchor.
	 */
	public AnchorControl(ReactContext context, ReactControl child, String key) {
		super(context, null, REACT_MODULE);
		_child = child;
		putState(CHILD, child);
		setKey(key);
	}

	/**
	 * Updates the anchor key ({@code data-tl-anchor} value).
	 *
	 * @param key
	 *        The new key, or {@code null} for no anchor.
	 */
	public void setKey(String key) {
		putState(ANCHOR, key);
	}

	@Override
	protected void cleanupChildren() {
		_child.cleanupTree();
	}

	@Override
	protected void propagateAttach() {
		_child.attach();
	}

	@Override
	protected void propagateDetach() {
		_child.detach();
	}

	/**
	 * The stable, DOM-safe anchor id for a model object: the external form of its persistent
	 * identifier, or {@code null} when the value is not a persistent {@link TLObject}.
	 *
	 * <p>
	 * The same identity the model observers use for keyed reuse. Shared by {@link AnchorControl}
	 * (which renders it) and {@link ScrollLinkControl} (which scrolls to it).
	 * </p>
	 */
	public static String anchorId(Object value) {
		if (!(value instanceof TLObject object)) {
			return null;
		}
		return IdentifierUtil.toExternalForm(KBUtils.getWrappedObjectName(object));
	}

}

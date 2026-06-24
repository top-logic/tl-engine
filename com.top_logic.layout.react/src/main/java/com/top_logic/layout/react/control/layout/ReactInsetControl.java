/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactControl} that wraps a single content control in a padded box via the
 * {@code TLInset} React component.
 *
 * <p>
 * In the spacing model, containers are flush and content owns its inset. This element provides an
 * explicit inset where a view places content (e.g. a stack of cards) that would otherwise glue to
 * the container border.
 * </p>
 */
public class ReactInsetControl extends ReactControl {

	private static final String REACT_MODULE = "TLInset";

	private static final String CHILD = "child";

	private final ReactControl _child;

	/**
	 * Creates a new {@link ReactInsetControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param child
	 *        The content control to inset.
	 */
	public ReactInsetControl(ReactContext context, ReactControl child) {
		super(context, null, REACT_MODULE);
		_child = child;
		putState(CHILD, child);
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
	 * Structural: this control is a padding/inset wrapper and is elided from the headless agent projection.
	 */
	@Override
	public boolean agentTransparent() {
		return true;
	}
}

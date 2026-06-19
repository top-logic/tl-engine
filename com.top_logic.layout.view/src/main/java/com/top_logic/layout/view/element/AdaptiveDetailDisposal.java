/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.util.TLContextManager;

/**
 * Coordinates deferred disposal of adaptive-detail presentations within a subsession.
 *
 * <p>
 * Clearing a selection channel (e.g. breadcrumb navigation) makes the affected
 * {@link ReactAdaptiveDetailControl} rebuild and dispose its old presentation. That rebuild runs
 * inside the channel's listener notification, where the old presentation's children (a detail form,
 * a dependent table) are still pending in the channel's listener snapshot - disposing them
 * synchronously would let them run on a torn-down control. The disposal happens in whichever control
 * owns the cleared channel's level, which is not necessarily the one that initiated the clear (the
 * outermost coordinator clears deeper levels' channels during breadcrumb navigation), so a per-control
 * deferral flag does not suffice.
 * </p>
 *
 * <p>
 * This holder is shared by all adaptive-detail controls of a subsession. While {@link #run(Runnable)}
 * executes a channel-mutating action, every control defers its old presentation here; once the
 * action (and all the notifications it triggered) has unwound, the deferred presentations are
 * disposed together - now safely outside any notification.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class AdaptiveDetailDisposal {

	private static final Property<AdaptiveDetailDisposal> PROPERTY =
		TypedAnnotatable.property(AdaptiveDetailDisposal.class, "adaptiveDetailDisposal");

	private int _depth;

	private final List<ReactControl> _pending = new ArrayList<>();

	/**
	 * The {@link AdaptiveDetailDisposal} of the current subsession, creating it on first access.
	 *
	 * @return The shared coordinator, never {@code null} (a transient instance if no subsession).
	 */
	public static AdaptiveDetailDisposal forCurrentSubSession() {
		SubSessionContext subSession = TLContextManager.getSubSession();
		if (subSession == null) {
			return new AdaptiveDetailDisposal();
		}
		synchronized (subSession) {
			AdaptiveDetailDisposal disposal = subSession.get(PROPERTY);
			if (disposal == null) {
				disposal = new AdaptiveDetailDisposal();
				subSession.set(PROPERTY, disposal);
			}
			return disposal;
		}
	}

	/**
	 * Whether a {@link #run(Runnable) deferring action} is currently in progress.
	 */
	public boolean isDeferring() {
		return _depth > 0;
	}

	/**
	 * Defers disposal of the given control until the enclosing {@link #run(Runnable)} completes.
	 *
	 * @param control
	 *        The presentation to dispose later.
	 */
	public void defer(ReactControl control) {
		_pending.add(control);
	}

	/**
	 * Runs a channel-mutating action with deferral active, then disposes everything deferred during
	 * it (once the outermost such action completes).
	 *
	 * @param action
	 *        The action to run (e.g. clearing selection channels).
	 */
	public void run(Runnable action) {
		_depth++;
		try {
			action.run();
		} finally {
			_depth--;
			if (_depth == 0 && !_pending.isEmpty()) {
				List<ReactControl> toDispose = new ArrayList<>(_pending);
				_pending.clear();
				for (ReactControl control : toDispose) {
					control.cleanupTree();
				}
			}
		}
	}

}

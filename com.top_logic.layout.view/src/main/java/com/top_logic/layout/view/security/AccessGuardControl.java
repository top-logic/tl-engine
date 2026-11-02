/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactAlertControl;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl.Variant;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.element.ContentControls;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.layout.view.security.UnitAccess.Display;
import com.top_logic.util.Resources;

/**
 * Control displaying the content of a UI unit with an {@link AccessControl} as the access check
 * decides for the current value of the unit's {@link SecuredObject}.
 *
 * <p>
 * Displays the content while the operation is allowed, nothing while there is no object, and a
 * "no access" placeholder while the operation is refused on a checked object (see
 * {@link UnitAccess#decide(SecuredObject)}). The decision is made anew when an input channel of the
 * unit takes a new value, and when an object an input holds is changed (its roles may depend on
 * it). Content that is no longer displayed is {@link ContentControls#retire(ReactControl) retired}:
 * it is detached at once and disposed after the channel notification in progress, since it may
 * itself listen to the channel that caused the change. While the decision stays the same, the
 * displayed content stays as it is.
 * </p>
 *
 * <p>
 * The placeholder is shown for a unit {@link DeniedUnitDisplay#HIDE hidden} while denied as well:
 * its container leaves the unit out (see {@link UnitHiding}), but where the user still sees the
 * content - the open page of a navigation item - the placeholder explains it.
 * </p>
 *
 * <p>
 * The React component {@code TLDeckPane} renders the single {@code activeChild}.
 * </p>
 */
public class AccessGuardControl extends ReactControl {

	private static final String REACT_MODULE = "TLDeckPane";

	private static final String ACTIVE_CHILD = "activeChild";

	private final ViewContext _context;

	private final UnitAccess _access;

	private final SecuredObject _secured;

	private final Supplier<ReactControl> _content;

	private final ChannelObjectObserver _observer;

	private Display _display;

	private ReactControl _current;

	private boolean _disposed;

	/**
	 * Creates a {@link AccessGuardControl}.
	 *
	 * @param context
	 *        The context of the content.
	 * @param access
	 *        The access check of the unit.
	 * @param secured
	 *        The object of the unit, bound to the channels of the session.
	 * @param content
	 *        Creates the content of the unit, called each time the content becomes accessible
	 *        again.
	 */
	public AccessGuardControl(ViewContext context, UnitAccess access, SecuredObject secured,
			Supplier<ReactControl> content) {
		super(context, null, REACT_MODULE);
		_context = context;
		_access = access;
		_secured = secured;
		_content = content;

		_observer = new ChannelObjectObserver(secured.getInputs(), Collections.emptySet(), this::update);
		Registration registration = secured.observe(this::update);
		addCleanupAction(registration::dispose);
		addAttachListener(() -> _observer.attach(_context.getModelScope()));
		addDetachListener(_observer::detach);

		update();
	}

	/**
	 * How the content is currently displayed.
	 */
	public Display getDisplay() {
		return _display;
	}

	/**
	 * The control currently displayed: the content, an empty content, or the placeholder.
	 */
	public ReactControl getActive() {
		return _current;
	}

	private void update() {
		if (_disposed) {
			return;
		}
		Display display = _access.decide(_secured);
		if (display == _display && _current != null) {
			return;
		}
		_display = display;

		ReactControl built = build(display);
		ReactControl old = _current;
		_current = built;

		Object token = beginUpdate();
		putState(ACTIVE_CHILD, built);
		commitUpdate(token);

		if (isAttached()) {
			built.attach();
		}
		if (old != null && old != built) {
			ContentControls.retire(old);
		}
	}

	private ReactControl build(Display display) {
		switch (display) {
			case CONTENT:
				return _content.get();
			case DENIED:
				return placeholder();
			case EMPTY:
			case HIDDEN:
				return ContentControls.toControl(List.of(), _context);
		}
		throw new IllegalArgumentException("No such display: " + display);
	}

	private ReactControl placeholder() {
		Resources resources = Resources.getInstance();
		ReactAlertControl result = new ReactAlertControl(_context);
		result.show(Variant.WARNING, resources.getString(I18NConstants.NO_ACCESS_TITLE),
			resources.getString(_access.deniedReason()));
		return result;
	}

	@Override
	protected void onCleanup() {
		_disposed = true;
		super.onCleanup();
	}

}

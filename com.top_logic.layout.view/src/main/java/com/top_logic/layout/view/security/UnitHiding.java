/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import java.util.Collections;
import java.util.function.Consumer;

import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.layout.view.security.UnitAccess.Display;

/**
 * Whether a UI unit with {@link DeniedUnitDisplay#HIDE} is currently not displayed, because its
 * operation is refused on the object it checks.
 *
 * <p>
 * The container of the unit (tab bar, accordion, sidebar, dashboard) asks for the state when it
 * builds the entry of the unit ({@link #isHidden()}), and {@link #bind(ReactControl, Consumer)
 * binds} to its own control to follow the state for as long as that control lives: the state is
 * decided anew when an input channel of the unit takes a new value, and when an object an input
 * holds is changed while the container is attached. The container draws the entry or leaves it
 * out; it neither removes nor rebuilds the unit.
 * </p>
 *
 * @see AccessChecks#hiding(UnitAccess, ViewContext)
 */
public final class UnitHiding {

	private final UnitAccess _access;

	private final SecuredObject _secured;

	private final ViewContext _context;

	private boolean _hidden;

	private Consumer<Boolean> _onChange;

	/**
	 * Creates a {@link UnitHiding}.
	 *
	 * @param access
	 *        The access check of the unit.
	 * @param secured
	 *        The object of the unit, bound to the channels of the session.
	 * @param context
	 *        The context the unit is displayed in, providing the model scope to observe the objects
	 *        in.
	 */
	UnitHiding(UnitAccess access, SecuredObject secured, ViewContext context) {
		_access = access;
		_secured = secured;
		_context = context;
		_hidden = decide();
	}

	/**
	 * Whether the unit is currently not displayed.
	 */
	public boolean isHidden() {
		return _hidden;
	}

	/**
	 * Follows the state for the lifetime of the given control of the container.
	 *
	 * @param container
	 *        The control displaying the entry of the unit. The objects of the inputs are observed
	 *        while it is attached; the observation ends when it is cleaned up.
	 * @param onChange
	 *        Called with the new state whenever it changes.
	 */
	public void bind(ReactControl container, Consumer<Boolean> onChange) {
		_onChange = onChange;
		Registration registration = _secured.observe(this::update);
		container.addCleanupAction(registration::dispose);

		ChannelObjectObserver observer =
			new ChannelObjectObserver(_secured.getInputs(), Collections.emptySet(), this::update);
		container.addAttachListener(() -> observer.attach(_context.getModelScope()));
		container.addDetachListener(observer::detach);
		container.addCleanupAction(observer::detach);
	}

	private void update() {
		boolean hidden = decide();
		if (hidden == _hidden) {
			return;
		}
		_hidden = hidden;
		if (_onChange != null) {
			_onChange.accept(Boolean.valueOf(hidden));
		}
	}

	private boolean decide() {
		return _access.decide(_secured) == Display.DENIED;
	}

}

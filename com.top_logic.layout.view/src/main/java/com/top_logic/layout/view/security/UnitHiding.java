/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelCondition;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.layout.view.security.UnitAccess.Display;

/**
 * Whether a UI unit is currently left out of its container.
 *
 * <p>
 * A unit is left out while its visibility condition does not hold, or while its operation is
 * refused on the object it checks and it is configured to be {@link DeniedUnitDisplay#HIDE hidden}
 * then. While the visibility condition does not hold, the access check is not consulted: the
 * objects the unit is not meant for are not checked.
 * </p>
 *
 * <p>
 * The container of the unit (tab bar, accordion, sidebar, dashboard) asks for the state when it
 * builds the entry of the unit ({@link #isHidden()}), and {@link #bind(ReactControl, Consumer)
 * binds} to its own control to follow the state for as long as that control lives: the state is
 * decided anew when an input channel of the condition or of the access check takes a new value,
 * and when an object such an input holds is changed while the container is attached. The container
 * draws the entry or leaves it out; it neither removes nor rebuilds the unit.
 * </p>
 *
 * @see AccessChecks#hiding(UnitAccess, ChannelCondition, ViewContext)
 */
public final class UnitHiding {

	private final ChannelCondition _visible;

	private final UnitAccess _access;

	private final SecuredObject _secured;

	private final ViewContext _context;

	private boolean _hidden;

	private Consumer<Boolean> _onChange;

	/**
	 * Creates a {@link UnitHiding}.
	 *
	 * @param visible
	 *        The condition under which the unit is shown, {@code null} for a unit shown
	 *        independent of its inputs.
	 * @param access
	 *        The access check of the unit, {@code null} when the unit is not hidden while denied.
	 * @param secured
	 *        The object of the unit, bound to the channels of the session; {@code null} exactly
	 *        when the access check is.
	 * @param context
	 *        The context the unit is displayed in, providing the model scope to observe the objects
	 *        in.
	 */
	UnitHiding(ChannelCondition visible, UnitAccess access, SecuredObject secured, ViewContext context) {
		_visible = visible;
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
		Registration registration = observe(_visible, _secured, this::update);
		container.addCleanupAction(registration::dispose);

		ChannelObjectObserver observer =
			new ChannelObjectObserver(channels(_visible, _secured), Collections.emptySet(), this::update);
		container.addAttachListener(() -> observer.attach(_context.getModelScope()));
		container.addDetachListener(observer::detach);
		container.addCleanupAction(observer::detach);
	}

	/**
	 * Runs the given callback whenever an input channel of the visibility condition or of the
	 * access check of a unit takes a new value.
	 *
	 * @param visible
	 *        The visibility condition of the unit, {@code null} for none.
	 * @param secured
	 *        The object of the access check of the unit, {@code null} for none.
	 * @return The registration that stops the observation.
	 */
	static Registration observe(ChannelCondition visible, SecuredObject secured, Runnable onChange) {
		Registration condition =
			visible == null ? Registration.NONE : ChannelCondition.observe(List.of(visible), onChange);
		Registration access = secured == null ? Registration.NONE : secured.observe(onChange);
		return Registration.onDispose(() -> {
			condition.dispose();
			access.dispose();
		});
	}

	/**
	 * The channels a unit is displayed by: those of its visibility condition and those of its
	 * access check, each once.
	 *
	 * @param visible
	 *        The visibility condition of the unit, {@code null} for none.
	 * @param secured
	 *        The object of the access check of the unit, {@code null} for none.
	 */
	static List<ViewChannel> channels(ChannelCondition visible, SecuredObject secured) {
		return Stream.concat(
			visible == null ? Stream.empty() : visible.channels().stream(),
			secured == null ? Stream.empty() : secured.getInputs().stream())
			.distinct()
			.toList();
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
		if (_visible != null && !_visible.holds()) {
			return true;
		}
		return _access != null && _access.decide(_secured) == Display.DENIED;
	}

}

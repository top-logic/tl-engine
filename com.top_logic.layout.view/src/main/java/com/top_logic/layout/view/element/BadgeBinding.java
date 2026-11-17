/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.BadgeDisplay;
import com.top_logic.layout.react.control.common.BadgeRole;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.util.Resources;

/**
 * The badge of an element declared {@link WithBadge}: what its channel holds, as a
 * {@link BadgeDisplay}, kept up to date on the control showing it.
 *
 * <p>
 * The badge follows a new value on the channel for as long as the control exists, and a change of
 * the object the value points to while the control is attached. A control attached again after a
 * detach shows the object as it is then. Disposing the control ends both.
 * </p>
 */
public class BadgeBinding {

	/** The largest count a badge shows with its digits; a larger one is shown as {@link #OVERFLOW}. */
	public static final int MAX_COUNT = 99;

	/** What a badge shows for a count above {@link #MAX_COUNT}. */
	public static final String OVERFLOW = MAX_COUNT + "+";

	private final ViewContext _context;

	private final ViewChannel _channel;

	private final BadgeRole _role;

	private final String _label;

	private BadgeBinding(ViewContext context, ViewChannel channel, BadgeRole role, String label) {
		_context = context;
		_channel = channel;
		_role = role;
		_label = label;
	}

	/**
	 * The badge declared by the given configuration.
	 *
	 * @param context
	 *        The context the element carrying the badge is built in, resolving its channel.
	 * @param config
	 *        The declaration of the badge.
	 * @return The binding of the badge, {@code null} for an element declared without a badge.
	 */
	public static BadgeBinding create(ViewContext context, WithBadge config) {
		ChannelRef ref = config.getBadge();
		if (ref == null) {
			return null;
		}
		ResKey label = config.getBadgeLabel();
		return new BadgeBinding(context, context.resolveChannel(ref), config.getBadgeRole(),
			label == null ? null : Resources.getInstance().getString(label));
	}

	/**
	 * The badge for the current value of the channel, {@code null} for none.
	 */
	public BadgeDisplay current() {
		return display(_channel.get(), _role, _label);
	}

	/**
	 * Keeps the badge shown by the given control up to date.
	 *
	 * @param control
	 *        The control showing the badge; its attachment decides whether changes of the object on
	 *        the channel are followed, its disposal ends the binding.
	 * @param show
	 *        Receives the badge to show, {@code null} for none, whenever it may have changed.
	 */
	public void bind(ReactControl control, Consumer<? super BadgeDisplay> show) {
		Runnable update = () -> show.accept(current());

		Registration registration = _channel.addListener((sender, oldValue, newValue) -> update.run());
		control.addCleanupAction(registration::dispose);

		ChannelObjectObserver observer = new ChannelObjectObserver(List.of(_channel), Set.of(), update);
		control.addAttachListener(() -> observer.attach(_context.getModelScope()));
		control.addDetachListener(observer::detach);
	}

	/**
	 * The badge showing the given value.
	 *
	 * <p>
	 * The number 0 shows no badge, a number above {@link #MAX_COUNT} shows {@link #OVERFLOW}.
	 * {@link Boolean#TRUE} shows a dot; {@code null}, {@link Boolean#FALSE} and an empty text show no
	 * badge. A text is shown as it is, any other value by its {@link ValueLabel label}.
	 * </p>
	 *
	 * @param value
	 *        The value of the badge channel.
	 * @param role
	 *        The color role of the badge.
	 * @param label
	 *        The accessible name of the badge, {@code null} for none.
	 * @return The badge to show, {@code null} for none.
	 */
	public static BadgeDisplay display(Object value, BadgeRole role, String label) {
		if (value == null || Boolean.FALSE.equals(value)) {
			return null;
		}
		if (Boolean.TRUE.equals(value)) {
			return new BadgeDisplay(null, true, role, label);
		}
		if (value instanceof Number number) {
			double count = number.doubleValue();
			if (count == 0) {
				return null;
			}
			if (count > MAX_COUNT) {
				return new BadgeDisplay(OVERFLOW, false, role, label);
			}
		}
		String text = ValueLabel.label(value);
		if (text == null || text.isEmpty()) {
			return null;
		}
		return new BadgeDisplay(text, false, role, label);
	}

}

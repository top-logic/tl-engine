/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.slot;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.layout.react.servlet.DeliveryScope;

/**
 * Per-view-tree registry that routes {@link SlotContribution} entries to their nearest matching
 * {@link SlotPlaceholder} by tree distance.
 *
 * <p>
 * For each contribution, the placeholder with the same name that is closest in tree distance
 * (smallest sum of depths from the lowest common ancestor) wins. Ties are broken by registration
 * order. All contributions routed to a placeholder are rendered there in registration order.
 * </p>
 *
 * <p>
 * The routing is computed once the {@link DeliveryScope#afterInteraction(Runnable) interaction}
 * that changed the display has completed, not on every register/unregister: a container switching
 * its content displays the new content before it retires the old one, or the other way round, and
 * routing in between would show a slot holding both contributions or none for a moment. The
 * registry recomputes the assignment of every contribution to its current nearest placeholder and
 * pushes the resulting per-placeholder list via {@link SlotPlaceholder#setRoutedContributions(List)}.
 * Placeholders are responsible for updating their displayed state from that list. A placeholder
 * about to be written asks for the routing with {@link #settleRouting()} first, so that it is
 * written with the contributions routed to it - a page is written with its slots filled. Outside
 * an interaction, the routing is recomputed at once.
 * </p>
 *
 * <p>
 * A contribution that reaches no placeholder is displayed nowhere, which is most likely a mistake
 * in the name of the slot or in the place of the contribution. The registry checks the
 * contributions that reach no placeholder when the interaction displaying them
 * {@link DeliveryScope#afterInteraction(Runnable) has completed} - a placeholder attached later in
 * the same interaction still catches its contribution - and marks each one still without placeholder
 * as {@link SlotContribution#setUnrouted(boolean) unrouted}, logging a warning that names the slot
 * and the {@link SlotContribution#getSource() declaration}. The warning is logged once when the
 * contribution is marked; a contribution that stays unrouted across later interactions, or that is
 * displayed again without having been routed in between, is not reported again. Routed to a
 * placeholder, the contribution loses its mark, and is reported anew when it reaches no placeholder
 * again. An {@link SlotContribution#isOptional() optional} contribution is never reported.
 * </p>
 *
 * <p>
 * Outside an {@link DeliveryScope#isOpen() interaction}, nothing is checked: there the display has
 * no point at which it is complete - a session that ends disposes its trees without one, and its
 * slots leave the display before the contributions routed to them.
 * </p>
 */
public class SlotRegistry {

	private final List<SlotPlaceholder> _placeholders = new ArrayList<>();

	private final List<SlotContribution> _contributions = new ArrayList<>();

	/**
	 * Whether a placeholder or a contribution was registered or unregistered since the routing was
	 * last computed.
	 */
	private boolean _routingPending;

	/**
	 * Whether {@link #completeInteraction()} is registered to run when the current interaction
	 * completes.
	 */
	private boolean _completionPending;

	/**
	 * Whether the routing is being computed, i.e. pushed to the placeholders.
	 *
	 * <p>
	 * Pushing the routing serializes the routed controls, which attaches them - and a placeholder or
	 * contribution among them registers here in turn. Such a registration only marks the routing as
	 * {@link #_routingPending pending}; the running {@link #settleRouting()} computes it once more.
	 * </p>
	 */
	private boolean _routing;

	/**
	 * Registers a placeholder. Returns a handle that removes it on {@link SlotHandle#close()}.
	 */
	public synchronized SlotHandle registerPlaceholder(SlotPlaceholder placeholder) {
		_placeholders.add(placeholder);
		changed();
		return () -> {
			synchronized (SlotRegistry.this) {
				_placeholders.remove(placeholder);
				changed();
			}
		};
	}

	/**
	 * Registers a contribution. Returns a handle that removes it on {@link SlotHandle#close()}.
	 */
	public synchronized SlotHandle registerContribution(SlotContribution contribution) {
		_contributions.add(contribution);
		changed();
		return () -> {
			synchronized (SlotRegistry.this) {
				_contributions.remove(contribution);
				changed();
			}
		};
	}

	/**
	 * Marks the routing as pending and lets it be computed when the current interaction has
	 * completed - at once outside an interaction.
	 */
	private void changed() {
		_routingPending = true;
		if (!_completionPending) {
			_completionPending = true;
			DeliveryScope.afterInteraction(this::completeInteraction);
		}
	}

	/**
	 * Computes the routing the display has settled on, and reports the contributions that reach no
	 * placeholder.
	 */
	private synchronized void completeInteraction() {
		_completionPending = false;
		settleRouting();
		if (DeliveryScope.isOpen()) {
			checkUnrouted();
		}
	}

	/**
	 * Computes the routing now, if a registration has changed since it was last computed.
	 *
	 * <p>
	 * Called by a placeholder about to be written, so that it is written with the contributions
	 * routed to it rather than with the routing of the display before the running interaction. Every
	 * other placeholder receives its routing as an update. Called while the routing is being
	 * computed, the call returns at once: the running computation repeats itself for the
	 * registrations made meanwhile.
	 * </p>
	 */
	public synchronized void settleRouting() {
		if (_routing) {
			return;
		}
		_routing = true;
		try {
			while (_routingPending) {
				_routingPending = false;
				reroute();
			}
		} finally {
			_routing = false;
		}
	}

	private void reroute() {
		Map<SlotPlaceholder, List<SlotContribution>> assignment = new IdentityHashMap<>();
		for (SlotPlaceholder placeholder : _placeholders) {
			assignment.put(placeholder, new ArrayList<>());
		}

		for (SlotContribution contribution : _contributions) {
			SlotPlaceholder nearest = findNearest(contribution);
			if (nearest != null) {
				assignment.get(nearest).add(contribution);
				if (contribution.isUnrouted()) {
					contribution.setUnrouted(false);
				}
			}
		}

		for (Map.Entry<SlotPlaceholder, List<SlotContribution>> e : assignment.entrySet()) {
			e.getKey().setRoutedContributions(e.getValue());
		}
	}

	/**
	 * Whether the given contribution, which reaches no placeholder, is to be reported for that.
	 */
	private static boolean isUnreported(SlotContribution contribution) {
		return !contribution.isOptional() && !contribution.isUnrouted();
	}

	/**
	 * Reports the contributions registered now that reach no placeholder.
	 */
	private void checkUnrouted() {
		for (SlotContribution contribution : _contributions) {
			if (isUnreported(contribution) && findNearest(contribution) == null) {
				contribution.setUnrouted(true);
				Logger.warn("The content for the slot '" + contribution.getSlotName() + "' declared at "
					+ contribution.getSource() + " is displayed nowhere: no slot of that name is displayed.",
					SlotRegistry.class);
			}
		}
	}

	private SlotPlaceholder findNearest(SlotContribution contribution) {
		SlotPlaceholder best = null;
		int bestDistance = Integer.MAX_VALUE;
		for (SlotPlaceholder placeholder : _placeholders) {
			if (!placeholder.getSlotName().equals(contribution.getSlotName())) {
				continue;
			}
			int distance =
				SlotPath.distance(placeholder.getSlotPath(), contribution.getSlotPath());
			if (distance < bestDistance) {
				bestDistance = distance;
				best = placeholder;
			}
		}
		return best;
	}

	/**
	 * Read-only view of all currently registered placeholders. For diagnostics / tests.
	 */
	public synchronized List<SlotPlaceholder> getPlaceholders() {
		return new ArrayList<>(_placeholders);
	}

	/**
	 * Read-only view of all currently registered contributions. For diagnostics / tests.
	 */
	public synchronized List<SlotContribution> getContributions() {
		return new ArrayList<>(_contributions);
	}
}

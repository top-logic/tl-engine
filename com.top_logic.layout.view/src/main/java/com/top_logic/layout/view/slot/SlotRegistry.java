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
 * Re-routing happens on every register/unregister: the registry recomputes the assignment of
 * every contribution to its current nearest placeholder and pushes the resulting per-placeholder
 * list via {@link SlotPlaceholder#setRoutedContributions(List)}. Placeholders are responsible for
 * updating their displayed state from that list.
 * </p>
 */
public class SlotRegistry {

	private final List<SlotPlaceholder> _placeholders = new ArrayList<>();

	private final List<SlotContribution> _contributions = new ArrayList<>();

	/**
	 * Registers a placeholder. Returns a handle that removes it on {@link SlotHandle#close()}.
	 */
	public synchronized SlotHandle registerPlaceholder(SlotPlaceholder placeholder) {
		_placeholders.add(placeholder);
		reroute();
		return () -> {
			synchronized (SlotRegistry.this) {
				_placeholders.remove(placeholder);
				reroute();
			}
		};
	}

	/**
	 * Registers a contribution. Returns a handle that removes it on {@link SlotHandle#close()}.
	 */
	public synchronized SlotHandle registerContribution(SlotContribution contribution) {
		_contributions.add(contribution);
		reroute();
		return () -> {
			synchronized (SlotRegistry.this) {
				_contributions.remove(contribution);
				reroute();
			}
		};
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
			}
		}

		for (Map.Entry<SlotPlaceholder, List<SlotContribution>> e : assignment.entrySet()) {
			e.getKey().setRoutedContributions(e.getValue());
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

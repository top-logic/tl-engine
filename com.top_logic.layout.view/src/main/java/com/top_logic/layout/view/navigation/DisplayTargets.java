/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.top_logic.basic.Log;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * The display targets of an application, answering where an object of a given type is displayed.
 *
 * <p>
 * A type is answered by the targets declared for it and by those declared for its generalizations,
 * best match first:
 * </p>
 * <ol>
 * <li>The target declared for the most special type: one declared for the object's own type
 * precedes one declared for a generalization, and a near generalization precedes a distant
 * one.</li>
 * <li>Among targets of equally special types, the one displayed nearest to where the request comes
 * from: the mount of the target's {@link DisplayTarget#first() first view} sharing the longest
 * prefix with the caller's own mount. A first view displayed as a dialog is at the caller by
 * definition; one that is nowhere displayed and no dialog ranks last, because reaching it means
 * displaying it anew.</li>
 * <li>Among equally near targets, the {@link DisplayTarget#isDefault() default} one.</li>
 * <li>Among equally preferred targets, the one declared first.</li>
 * </ol>
 */
public final class DisplayTargets {

	/**
	 * The nearness of a target whose first view is displayed as a dialog: at the caller, wherever
	 * that is.
	 */
	private static final int DIALOG_NEARNESS = 0;

	/**
	 * The nearness of a target whose first view is displayed nowhere: behind every displayed one.
	 */
	private static final int UNMOUNTED_NEARNESS = -1;

	private final List<DisplayTarget> _targets;

	private final Map<TLType, List<Integer>> _byType;

	private final Supplier<ViewMounts> _mounts;

	/**
	 * Creates the display targets of an application.
	 *
	 * @param targets
	 *        The declared targets, in declaration order.
	 * @param mounts
	 *        Access to the places the application's views are displayed at, asked whenever several
	 *        targets compete for a type.
	 */
	public DisplayTargets(List<DisplayTarget> targets, Supplier<ViewMounts> mounts) {
		_targets = List.copyOf(targets);
		_mounts = mounts;

		Map<TLType, List<Integer>> byType = new LinkedHashMap<>();
		for (int n = 0, size = _targets.size(); n < size; n++) {
			byType.computeIfAbsent(_targets.get(n).type(), key -> new ArrayList<>()).add(Integer.valueOf(n));
		}
		byType.replaceAll((type, indices) -> List.copyOf(indices));
		_byType = Collections.unmodifiableMap(byType);
	}

	/**
	 * All declared targets, in declaration order.
	 */
	public List<DisplayTarget> getTargets() {
		return _targets;
	}

	/**
	 * The places the views are displayed at, as the ranking of the targets sees them.
	 *
	 * @return The scan, or {@code null} if nothing says where the views are displayed.
	 */
	public ViewMounts getMounts() {
		return _mounts.get();
	}

	/**
	 * Whether an object of the given type can be displayed.
	 *
	 * @param type
	 *        The type of the object to display, may be {@code null}.
	 * @return Whether {@link #resolve(TLType, MountPath)} answers at least one target.
	 */
	public boolean hasTarget(TLType type) {
		return !candidates(type).isEmpty();
	}

	/**
	 * The targets displaying an object of the given type, best first.
	 *
	 * @param type
	 *        The type of the object to display, may be {@code null}.
	 * @param nearest
	 *        Where the request to display the object comes from, or {@code null} when it comes from
	 *        nowhere in particular.
	 * @return The candidates in preference order, empty if the type cannot be displayed.
	 */
	public List<DisplayTarget> resolve(TLType type, MountPath nearest) {
		List<Candidate> candidates = candidates(type);
		if (candidates.size() < 2) {
			return candidates.stream().map(candidate -> _targets.get(candidate.index())).toList();
		}

		ViewMounts mounts = _mounts.get();
		Map<Integer, Integer> nearness = new LinkedHashMap<>();
		for (Candidate candidate : candidates) {
			nearness.put(Integer.valueOf(candidate.index()),
				Integer.valueOf(nearness(_targets.get(candidate.index()), nearest, mounts)));
		}

		List<Candidate> ranked = new ArrayList<>(candidates);
		ranked.sort(Comparator
			.comparingInt(Candidate::distance)
			.thenComparing(Comparator.comparingInt(
				(Candidate candidate) -> nearness.get(Integer.valueOf(candidate.index())).intValue()).reversed())
			.thenComparing(Comparator.comparing(
				(Candidate candidate) -> Boolean.valueOf(_targets.get(candidate.index()).isDefault())).reversed())
			.thenComparingInt(Candidate::index));

		return ranked.stream().map(candidate -> _targets.get(candidate.index())).toList();
	}

	/**
	 * The target displaying an object of the given type.
	 *
	 * @param type
	 *        The type of the object to display, may be {@code null}.
	 * @param nearest
	 *        Where the request to display the object comes from, or {@code null} when it comes from
	 *        nowhere in particular.
	 * @return The best of the {@link #resolve(TLType, MountPath) candidates}, or {@code null} if
	 *         the type cannot be displayed.
	 */
	public DisplayTarget resolveBest(TLType type, MountPath nearest) {
		List<DisplayTarget> candidates = resolve(type, nearest);
		return candidates.isEmpty() ? null : candidates.get(0);
	}

	/**
	 * Reports every binding addressing a channel that the view it is written to does not declare.
	 *
	 * @param log
	 *        Where a binding that cannot arrive is reported to.
	 * @param channelsOfView
	 *        The channel names a view declares, by the path of its view file. Answers {@code null}
	 *        for a view whose declarations are unavailable, whose bindings are then left unchecked.
	 */
	public void checkBindings(Log log, Function<String, Set<String>> channelsOfView) {
		for (DisplayTarget target : _targets) {
			for (ShowStep show : target.shows()) {
				if (show.bindings().isEmpty()) {
					continue;
				}
				Set<String> declared = channelsOfView.apply(show.viewRef());
				if (declared == null) {
					continue;
				}
				for (Binding binding : show.bindings()) {
					if (!declared.contains(binding.channel())) {
						log.error("The display target for type '" + TLModelUtil.qualifiedName(target.type())
							+ "' binds the channel '" + binding.channel() + "', which the view '" + show.viewRef()
							+ "' does not declare.");
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + _targets;
	}

	/**
	 * The declared targets answering the given type, each with the number of generalization steps
	 * from that type to the type the target is declared for.
	 */
	private List<Candidate> candidates(TLType type) {
		if (type == null || _byType.isEmpty()) {
			return List.of();
		}

		List<Candidate> result = new ArrayList<>();
		Set<TLType> visited = new HashSet<>();
		List<TLType> level = List.of(type);
		for (int distance = 0; !level.isEmpty(); distance++) {
			List<TLType> next = new ArrayList<>();
			for (TLType current : level) {
				if (!visited.add(current)) {
					continue;
				}
				for (Integer index : _byType.getOrDefault(current, List.of())) {
					result.add(new Candidate(index.intValue(), distance));
				}
				if (current instanceof TLClass clazz) {
					next.addAll(clazz.getGeneralizations());
				}
			}
			level = next;
		}
		return result;
	}

	/**
	 * How near the given target is displayed to the place a request comes from.
	 */
	private static int nearness(DisplayTarget target, MountPath nearest, ViewMounts mounts) {
		ShowStep first = target.first();
		if (first.dialog()) {
			return DIALOG_NEARNESS;
		}
		List<MountPath> paths = mounts == null ? List.of() : mounts.getMounts(first.viewRef());
		if (paths.isEmpty()) {
			return UNMOUNTED_NEARNESS;
		}
		List<MountStep> hint = nearest == null ? List.of() : nearest.steps();
		int result = 0;
		for (MountPath path : paths) {
			result = Math.max(result, commonPrefixLength(path.steps(), hint));
		}
		return result;
	}

	/**
	 * The number of leading steps the two paths have in common.
	 */
	private static int commonPrefixLength(List<MountStep> left, List<MountStep> right) {
		int limit = Math.min(left.size(), right.size());
		int result = 0;
		while (result < limit && left.get(result).equals(right.get(result))) {
			result++;
		}
		return result;
	}

	/**
	 * A declared target answering a type.
	 *
	 * @param index
	 *        Position of the target in the declaration order.
	 * @param distance
	 *        The number of generalization steps from the type being displayed to the type the
	 *        target is declared for.
	 */
	private record Candidate(int index, int distance) {
		// Pure data.
	}
}

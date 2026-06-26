/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.contribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Registry of {@link Contribution}s that extend named extension points declared by upstream views.
 *
 * <p>
 * The service configuration is a {@link Key keyed} list of contributions. Because keyed lists merge
 * additively across module configuration fragments, any module appends contributions to an upstream
 * view's extension point without that view's module (or any other) depending on the contributor -
 * the same cross-module mechanism that lets modules register label providers or command handlers.
 * </p>
 *
 * <p>
 * Contributions are indexed by {@link Contribution#getTarget() target} and, within a target, ordered
 * by {@link Contribution#getRank() rank} (ties broken by {@link Contribution#getId() id}). A
 * container exposing an extension point queries {@link #getContributions(String)} to obtain the
 * entries to append.
 * </p>
 */
public class ViewContributionService extends ConfiguredManagedClass<ViewContributionService.Config> {

	/**
	 * Configuration of the {@link ViewContributionService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<ViewContributionService> {

		/** Configuration name for {@link #getContributions()}. */
		String CONTRIBUTIONS = "contributions";

		/**
		 * All registered view contributions, merged across modules by {@link Contribution#getId()
		 * id}.
		 */
		@Name(CONTRIBUTIONS)
		@Key(Contribution.ID)
		List<Contribution> getContributions();
	}

	private final Map<String, List<Contribution>> _byTarget;

	/**
	 * Creates a new {@link ViewContributionService} from configuration.
	 */
	@CalledByReflection
	public ViewContributionService(InstantiationContext context, Config config) {
		super(context, config);
		_byTarget = indexByTarget(config.getContributions());
	}

	private static Map<String, List<Contribution>> indexByTarget(List<Contribution> contributions) {
		Map<String, List<Contribution>> result = new HashMap<>();
		for (Contribution contribution : contributions) {
			result.computeIfAbsent(contribution.getTarget(), target -> new ArrayList<>()).add(contribution);
		}
		Comparator<Contribution> order =
			Comparator.comparingInt(Contribution::getRank).thenComparing(Contribution::getId);
		for (List<Contribution> group : result.values()) {
			group.sort(order);
		}
		return result;
	}

	/**
	 * The contributions targeting the given extension point, ordered by {@link Contribution#getRank()
	 * rank}.
	 *
	 * @param target
	 *        The extension-point id (e.g. the {@code extension-id} of a {@code <tab-bar>}).
	 * @return The matching contributions in display order, empty if none.
	 */
	public List<Contribution> getContributions(String target) {
		return _byTarget.getOrDefault(target, Collections.emptyList());
	}

	/**
	 * The singleton {@link ViewContributionService} instance.
	 */
	public static ViewContributionService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton holder for the {@link ViewContributionService}.
	 */
	public static final class Module extends TypedRuntimeModule<ViewContributionService> {

		/** Singleton {@link ViewContributionService.Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<ViewContributionService> getImplementation() {
			return ViewContributionService.class;
		}
	}
}

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
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.layout.view.UIElement;

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
 * by {@link Contribution#getRank() rank} (ties broken by {@link Contribution#getId() id}).
 * </p>
 *
 * <p>
 * In addition to the flat {@link Contribution} list, the service holds {@link Extension}s: each binds
 * the content of a named extension point (rendered by an {@code <extension-point id="..."/>}) to a
 * single {@link UIElement}. Because {@link Extension}s merge across modules by id and their content
 * merges recursively (keyed child lists, positionable via {@code config:position}), a module refines
 * an upstream extension point - adding, positioning or overriding parts - purely through the standard
 * typed-configuration override, with no rank arithmetic and no per-container extension code.
 * </p>
 *
 * @implNote A container exposing a flat extension point queries {@link #getContributions(String)} to
 *           obtain the entries to append; an {@code <extension-point>} queries
 *           {@link #getExtensionContent(String)} for its content.
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

		/** Configuration name for {@link #getExtensions()}. */
		String EXTENSIONS = "extensions";

		/**
		 * All registered extension-point content bindings, merged across modules by
		 * {@link Extension#getId() id}.
		 */
		@Name(EXTENSIONS)
		@Key(Extension.ID)
		List<Extension> getExtensions();
	}

	/**
	 * Binds the content of a named extension point to a single {@link UIElement}.
	 *
	 * <p>
	 * Entries with the same {@link Extension#getId() id} merge across module configuration fragments:
	 * the owning module registers the base content, depending modules refine it through the standard
	 * typed-configuration override (recursive item merge, keyed child lists, {@code config:position}).
	 * </p>
	 */
	public interface Extension extends ConfigurationItem {

		/** Configuration name for {@link #getId()}. */
		String ID = "id";

		/** Configuration name for {@link #getContent()}. */
		String CONTENT = "content";

		/**
		 * The extension-point id, matching an {@code <extension-point id="...">} in some view.
		 */
		@Name(ID)
		@Mandatory
		String getId();

		/**
		 * The single element rendered at the extension point.
		 */
		@Name(CONTENT)
		@DefaultContainer
		PolymorphicConfiguration<? extends UIElement> getContent();
	}

	private final Map<String, List<Contribution>> _byTarget;

	private final Map<String, PolymorphicConfiguration<? extends UIElement>> _extensionContent;

	/**
	 * Creates a new {@link ViewContributionService} from configuration.
	 */
	@CalledByReflection
	public ViewContributionService(InstantiationContext context, Config config) {
		super(context, config);
		_byTarget = indexByTarget(config.getContributions());
		_extensionContent = indexExtensions(config.getExtensions());
	}

	private static Map<String, PolymorphicConfiguration<? extends UIElement>> indexExtensions(
			List<Extension> extensions) {
		Map<String, PolymorphicConfiguration<? extends UIElement>> result = new HashMap<>();
		for (Extension extension : extensions) {
			result.put(extension.getId(), extension.getContent());
		}
		return result;
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
	 * The content registered for the extension point with the given id, or {@code null} if no module
	 * registered any.
	 *
	 * @param id
	 *        The extension-point id (the {@code id} of an {@code <extension-point>}).
	 * @return The single element configuration to render, or {@code null}.
	 */
	public PolymorphicConfiguration<? extends UIElement> getExtensionContent(String id) {
		return _extensionContent.get(id);
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

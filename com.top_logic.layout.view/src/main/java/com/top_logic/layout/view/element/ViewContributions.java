/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.ReferenceElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.contribution.Contribution;
import com.top_logic.layout.view.contribution.ViewContributionService;
import com.top_logic.layout.view.contribution.WithExtensionPoint;
import com.top_logic.util.Resources;

/**
 * Shared seam that turns the {@link Contribution}s of an extension point into neutral, navigable
 * {@link Section}s.
 *
 * <p>
 * Every container exposing an {@link WithExtensionPoint#getExtensionId() extension id} goes through
 * here: it queries the {@link ViewContributionService} for the contributions targeting the point
 * and turns each into a {@link Section} whose content is a {@code <view-ref>} to the contributed
 * view. The container then renders each section as its own kind of entry (a tab, a navigation
 * item, ...), so only that last, genuinely container-specific hop lives in the container.
 * </p>
 */
public final class ViewContributions {

	private ViewContributions() {
		// Static utility.
	}

	/**
	 * A navigable section produced from a {@link Contribution}.
	 *
	 * <p>
	 * Sections carry no access control of their own: a contributed section's visibility is governed
	 * by the extension point's host and the contributed view's own command security.
	 * </p>
	 *
	 * @param id
	 *        Stable id of the section (the contribution id).
	 * @param label
	 *        Resolved display label.
	 * @param route
	 *        Configured route segment, or empty to default to the id.
	 * @param icon
	 *        CSS icon class, or empty for no icon.
	 * @param content
	 *        Content elements shown when the section is selected.
	 */
	public record Section(String id, String label, String route, String icon, List<UIElement> content) {
		// Neutral carrier between the contribution registry and a concrete container.
	}

	/**
	 * The sections contributed to the given extension point, ordered by rank.
	 *
	 * @param context
	 *        Context used to instantiate the contributed views.
	 * @param extensionId
	 *        The extension-point id, or empty for a container without an extension point.
	 * @return The contributed sections in display order, empty if none.
	 */
	public static List<Section> sections(InstantiationContext context, String extensionId) {
		if (extensionId == null || extensionId.isEmpty()) {
			return List.of();
		}
		List<Section> result = new ArrayList<>();
		for (Contribution contribution : ViewContributionService.getInstance().getContributions(extensionId)) {
			result.add(toSection(context, contribution));
		}
		return result;
	}

	private static Section toSection(InstantiationContext context, Contribution contribution) {
		ReferenceElement.Config refConfig = TypedConfiguration.newConfigItem(ReferenceElement.Config.class);
		refConfig.update(refConfig.descriptor().getProperty(ReferenceElement.Config.VIEW), contribution.getView());
		UIElement content = context.getInstance(refConfig);

		ResKey labelKey = contribution.getLabel();
		String label = labelKey != null ? Resources.getInstance().getString(labelKey) : contribution.getId();

		return new Section(contribution.getId(), label, contribution.getRoute(), contribution.getIcon(),
			List.of(content));
	}
}

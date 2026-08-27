/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.RoleToolAccess;
import com.top_logic.tl.ai.core.security.ToolAuthorization;
import com.top_logic.tl.ai.core.security.ToolIdentity;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

/**
 * The rows of the Werkzeug-Katalog: every registered tool, joined with the policy in effect.
 *
 * <p>
 * The overview the raw policy editor cannot give. That editor shows the <em>names</em> a policy
 * refers to - a group name, eighty tool names in a flat picker - and nowhere the relation between
 * them, so granting {@code data.read} is granting blind. This table is that relation.
 * </p>
 *
 * <p>
 * Reads the running service, and yields no row while it is down: a catalog that showed tools without
 * their policy would answer the one question it exists for with a blank column.
 * </p>
 *
 * @see ToolCatalogAccessor
 */
public class ToolCatalogModelBuilder implements ListModelBuilder {

	/** Singleton {@link ToolCatalogModelBuilder} instance. */
	public static final ToolCatalogModelBuilder INSTANCE = new ToolCatalogModelBuilder();

	private ToolCatalogModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent component) {
		if (!AiToolAccessService.Module.INSTANCE.isActive()
			|| !ToolRegistryService.Module.INSTANCE.isActive()) {
			return List.of();
		}
		AiToolAccessService service = AiToolAccessService.getInstance();
		RoleToolAccess access = service.getAccess();
		ToolAuthorization.Identities identities = AiToolAccessService.identities();

		List<ToolCatalogEntry> rows = new ArrayList<>();
		for (Tool tool : ToolRegistryService.getInstance().getTools()) {
			ToolIdentity identity = identities.identify(tool);
			rows.add(new ToolCatalogEntry(
				identity.displayName(),
				tool.description() == null ? "" : tool.description(),
				access.groupOf(identity),
				access.groupSourceOf(identity),
				service.rolesReaching(identity),
				service.baselineReaches(identity)));
		}
		// Ungrouped tools first: they are the state an administrator has to act on, and a warning that
		// needs scrolling to is a warning nobody sees. The rest by group, then by name, so that "which
		// tools are in data.read" is one glance rather than a filter.
		rows.sort(Comparator
			.comparing((ToolCatalogEntry row) -> row.isUnclassified() ? 0 : 1)
			.thenComparing(row -> row.group() == null ? "" : row.group())
			.thenComparing(ToolCatalogEntry::qualifiedName));
		return rows;
	}

	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		// The rows come from the running service; the component has no model of its own.
		return model == null;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent component, Object listElement) {
		// The table has no model of its own; the rows are computed from the running service.
		return null;
	}

	/** Called by reflection from the layout configuration. */
	public static ToolCatalogModelBuilder getInstance() {
		return INSTANCE;
	}

}

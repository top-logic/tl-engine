/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy;

/*
 * The class comment below is exported to META-INF/messages_*.properties by the build's doclet and kept
 * short for that reason - TestConfigPresentation enforces a length limit on everything under this
 * package prefix.
 *
 * The whole reason this class exists rather than a plain TableComponent: the rows join the tool registry
 * with the *saved* policy, and the policy tab next door writes that policy. A table that kept the rows
 * it built when it was first opened would answer the question "which roles reach this tool" with the
 * answer from before the change the reader has just made.
 *
 * Conditional on the policy having actually changed, and by identity: reloading the application
 * configuration replaces the configuration item, so a different instance means somebody wrote it.
 * Invalidating on every tab switch would work too and would throw away the reader's sorting, filtering
 * and column configuration each time - on a table of ninety rows whose whole point is being filtered.
 */

/**
 * The tools of this application, with what the saved policy says about each of them.
 *
 * @see ToolCatalogModelBuilder
 */
public class ToolCatalogComponent extends TableComponent {

	/**
	 * Configuration options for {@link ToolCatalogComponent}.
	 */
	public interface Config extends TableComponent.Config {

		@Override
		@ClassDefault(ToolCatalogComponent.class)
		Class<? extends LayoutComponent> getImplementationClass();
	}

	/** The policy the rows were built from, to notice that it has been written since. */
	private ToolAccessPolicy _shownPolicy;

	/**
	 * Creates a {@link ToolCatalogComponent} from configuration.
	 */
	@CalledByReflection
	public ToolCatalogComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected void becomingVisible() {
		super.becomingVisible();
		ToolAccessPolicy saved = AiToolAccessService.savedPolicy();
		if (_shownPolicy != saved) {
			_shownPolicy = saved;
			invalidate();
		}
	}

}

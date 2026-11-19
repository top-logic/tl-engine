/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.discovery;

import java.io.IOException;
import java.util.List;

import com.top_logic.base.discovery.catalog.AiCatalog;
import com.top_logic.base.discovery.catalog.CatalogEntry;
import com.top_logic.base.discovery.catalog.CatalogHost;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.RuntimeCategory;
import com.top_logic.basic.version.Version;

/**
 * Describes the application to agents (machine clients) that know only its URL.
 *
 * <p>
 * Serves, through the {@link AgentDiscoveryServlet}, two documents:
 * </p>
 * <ul>
 * <li><code>llms.txt</code> ({@link AgentDiscoveryServlet#LLMS_TXT_PATH}): Markdown in the shape
 * of <a href="https://llmstxt.org/">llmstxt.org</a>.</li>
 * <li>The AI Catalog ({@link AgentDiscoveryServlet#AI_CATALOG_PATH}).</li>
 * </ul>
 *
 * <p>
 * The content comes from the configured {@link Config#getContributions() contributions}. Without
 * an active contribution, both documents name only the application.
 * </p>
 *
 * @see #writeLlmsTxt(DiscoveryContext, Appendable)
 * @see #catalog(DiscoveryContext)
 */
@ServiceCategory.Of(RuntimeCategory.class)
public class AgentDiscoveryService extends ConfiguredManagedClass<AgentDiscoveryService.Config> {

	/**
	 * The {@link AiCatalog#getSpecVersion() version} of the AI Catalog specification of the
	 * {@link #catalog(DiscoveryContext) catalog}.
	 */
	public static final String AI_CATALOG_SPEC_VERSION = "1.0";

	/**
	 * Configuration of the {@link AgentDiscoveryService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<AgentDiscoveryService> {

		/** Configuration name of {@link #getDisplayName()}. */
		String DISPLAY_NAME = "display-name";

		/** Configuration name of {@link #getSummary()}. */
		String SUMMARY = "summary";

		/** Configuration name of {@link #getContributions()}. */
		String CONTRIBUTIONS = "contributions";

		/** Tag name of an entry in {@link #getContributions()}. */
		String CONTRIBUTION = "contribution";

		/**
		 * The name of the application shown to agents.
		 *
		 * <p>
		 * If not set, the name of the application from its version information is used.
		 * </p>
		 */
		@Name(DISPLAY_NAME)
		@Nullable
		String getDisplayName();

		/**
		 * A one-line English summary of what the application is for.
		 *
		 * <p>
		 * Written as the quoted line below the heading of <code>llms.txt</code>. If not set, the
		 * line is left out.
		 * </p>
		 */
		@Name(SUMMARY)
		@Nullable
		String getSummary();

		/**
		 * The parts the application's self-description is assembled from.
		 *
		 * <p>
		 * A module that publishes a machine endpoint adds its contribution here by a configuration
		 * overlay.
		 * </p>
		 */
		@Name(CONTRIBUTIONS)
		@EntryTag(CONTRIBUTION)
		List<PolymorphicConfiguration<? extends AgentDiscoveryContribution>> getContributions();

	}

	private final List<AgentDiscoveryContribution> _contributions;

	/**
	 * Creates a {@link AgentDiscoveryService} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public AgentDiscoveryService(InstantiationContext context, Config config) {
		super(context, config);
		_contributions = TypedConfiguration.getInstanceList(context, config.getContributions());
	}

	/**
	 * The name of the application shown to agents.
	 */
	public String displayName() {
		String configured = getConfig().getDisplayName();
		if (!StringServices.isEmpty(configured)) {
			return configured;
		}
		return Version.getApplicationVersion().getName();
	}

	/**
	 * Writes the <code>llms.txt</code> document of the application.
	 *
	 * @param context
	 *        The context providing the absolute URLs of the application.
	 * @param out
	 *        The output to write the Markdown document to.
	 */
	public void writeLlmsTxt(DiscoveryContext context, Appendable out) throws IOException {
		out.append("# ").append(singleLine(displayName())).append('\n');

		String summary = getConfig().getSummary();
		if (!StringServices.isEmpty(summary)) {
			out.append('\n').append("> ").append(singleLine(summary)).append('\n');
		}

		out.append('\n');
		out.append("The machine-readable list of the agent-facing endpoints of this application is its AI Catalog: ");
		out.append(context.url(AgentDiscoveryServlet.AI_CATALOG_PATH));
		out.append('\n');

		for (AgentDiscoveryContribution contribution : activeContributions()) {
			for (LlmsTxtSection section : contribution.llmsTxtSections(context)) {
				out.append('\n').append("## ").append(singleLine(section.title())).append('\n');
				out.append('\n').append(section.markdown().strip()).append('\n');
			}
		}
	}

	/**
	 * Builds the AI Catalog of the application.
	 *
	 * <p>
	 * An entry that has no identifier or type, or not exactly one of URL and inline data, is left
	 * out and logged as an error.
	 * </p>
	 *
	 * @param context
	 *        The context providing the absolute URLs of the application.
	 */
	public AiCatalog catalog(DiscoveryContext context) {
		AiCatalog catalog = AiCatalog.create()
			.setSpecVersion(AI_CATALOG_SPEC_VERSION)
			.setHost(CatalogHost.create()
				.setDisplayName(displayName())
				.setDocumentationUrl(context.url(AgentDiscoveryServlet.LLMS_TXT_PATH)));
		for (AgentDiscoveryContribution contribution : activeContributions()) {
			for (CatalogEntry entry : contribution.catalogEntries(context)) {
				if (isValid(entry)) {
					catalog.addEntry(entry);
				} else {
					Logger.error("Invalid AI Catalog entry of " + contribution.getClass().getName()
						+ " left out (identifier and type are required, and exactly one of url and data): " + entry,
						AgentDiscoveryService.class);
				}
			}
		}
		return catalog;
	}

	private static boolean isValid(CatalogEntry entry) {
		return !StringServices.isEmpty(entry.getIdentifier())
			&& !StringServices.isEmpty(entry.getType())
			&& entry.hasUrl() != entry.hasData();
	}

	private List<AgentDiscoveryContribution> activeContributions() {
		return _contributions.stream().filter(AgentDiscoveryContribution::isActive).toList();
	}

	private static String singleLine(String text) {
		return text.strip().replaceAll("\\s*[\\r\\n]+\\s*", " ");
	}

	/**
	 * The running {@link AgentDiscoveryService}, or <code>null</code> if the service is not
	 * active.
	 */
	public static AgentDiscoveryService getInstanceOrNull() {
		Module module = Module.INSTANCE;
		return module.isActive() ? module.getImplementationInstance() : null;
	}

	/**
	 * Module for instantiation of the {@link AgentDiscoveryService}.
	 */
	public static class Module extends TypedRuntimeModule<AgentDiscoveryService> {

		/** Singleton for this module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<AgentDiscoveryService> getImplementation() {
			return AgentDiscoveryService.class;
		}
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.xml.TagWriter;

/**
 * Registry of all client-side resources of the React UI.
 *
 * <p>
 * Feature modules contribute {@link ResourceConfig} entries. At page rendering time the registry
 * emits the corresponding head references through a {@link ClientResourceProvider}. Resources are
 * emitted in topological order of their {@link ResourceConfig} dependencies.
 * </p>
 */
@Label("Client resources")
public class ClientResources extends ConfiguredManagedClass<ClientResources.Config> {

	/**
	 * Configuration of {@link ClientResources}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<ClientResources> {

		/**
		 * The registered client resources, contributed across modules.
		 */
		@Name("resources")
		@Subtypes({
			@Subtype(tag = ModuleScriptConfig.TAG_NAME, type = ModuleScriptConfig.class),
			@Subtype(tag = ScriptConfig.TAG_NAME, type = ScriptConfig.class),
			@Subtype(tag = StyleSheetConfig.TAG_NAME, type = StyleSheetConfig.class),
		})
		List<ResourceConfig> getResources();

	}

	private final List<ResourceConfig> _ordered;

	private final ResourceResolver _resolver = new DefaultResourceResolver();

	/**
	 * Creates a {@link ClientResources} service from configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The service configuration.
	 */
	@CalledByReflection
	public ClientResources(InstantiationContext context, Config config) {
		super(context, config);
		_ordered = order(context, config.getResources());
	}

	/**
	 * Emits the import map and module script references.
	 *
	 * <p>
	 * Must be placed before any module script on the page, including those emitted by other
	 * mechanisms, since the import map has to precede every module script that imports a registered
	 * specifier.
	 * </p>
	 *
	 * @param out
	 *        The writer of the HTML {@code <head>}.
	 * @param contextPath
	 *        The web application context path.
	 * @throws IOException
	 *         If writing fails.
	 */
	public void writeScriptRefs(TagWriter out, String contextPath) throws IOException {
		provider().writeScriptRefs(out, contextPath);
	}

	/**
	 * Emits the stylesheet references.
	 *
	 * <p>
	 * Must be placed after the design-token block so that the registered stylesheets, which
	 * reference the tokens, form the overriding cascade layer.
	 * </p>
	 *
	 * @param out
	 *        The writer of the HTML {@code <head>}.
	 * @param contextPath
	 *        The web application context path.
	 * @throws IOException
	 *         If writing fails.
	 */
	public void writeStyleRefs(TagWriter out, String contextPath) throws IOException {
		provider().writeStyleRefs(out, contextPath);
	}

	private ClientResourceProvider provider() {
		return new UnbundledResourceProvider(_ordered, _resolver);
	}

	private static List<ResourceConfig> order(Log log, List<ResourceConfig> resources) {
		Map<String, ResourceConfig> byName = new LinkedHashMap<>();
		for (ResourceConfig resource : resources) {
			byName.put(resource.getName(), resource);
		}
		List<ResourceConfig> result = new ArrayList<>(resources.size());
		Set<String> done = new HashSet<>();
		Set<String> active = new HashSet<>();
		for (ResourceConfig resource : resources) {
			visit(log, resource, byName, done, active, result);
		}
		return result;
	}

	private static void visit(Log log, ResourceConfig resource, Map<String, ResourceConfig> byName,
			Set<String> done, Set<String> active, List<ResourceConfig> result) {
		String name = resource.getName();
		if (done.contains(name)) {
			return;
		}
		if (!active.add(name)) {
			log.error("Cyclic 'requires' dependency at client resource '" + name + "'.");
			return;
		}
		for (String dependency : resource.getRequires()) {
			ResourceConfig required = byName.get(dependency);
			if (required == null) {
				log.error("Client resource '" + name + "' requires unknown resource '" + dependency + "'.");
				continue;
			}
			visit(log, required, byName, done, active, result);
		}
		active.remove(name);
		done.add(name);
		result.add(resource);
	}

	/**
	 * The singleton {@link ClientResources} service instance.
	 */
	public static ClientResources getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for {@link ClientResources}.
	 */
	public static final class Module extends TypedRuntimeModule<ClientResources> {

		/** Singleton instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton.
		}

		@Override
		public Class<ClientResources> getImplementation() {
			return ClientResources.class;
		}

	}

}

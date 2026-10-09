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
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Registry of all client-side resources of the React UI.
 *
 * <p>
 * Feature modules contribute {@link ResourceConfig} entries. At page rendering time the registry
 * emits the corresponding head references through a {@link ClientResourceProvider}. Resources are
 * emitted in topological order of their {@link ResourceConfig} dependencies.
 * </p>
 *
 * <p>
 * The styles of the page are ordered by CSS cascade layers, independent of their position in the
 * page head: the rules of a later one of the {@link Config#getLayers()} win against the rules of an
 * earlier one, and unlayered rules win against all layered rules. The engine's stylesheets are in
 * the layer {@value #ENGINE_LAYER}, the rules of a component library rendering the UI (Material
 * UI) in the layer {@value #COMPONENT_LIBRARY_LAYER}, and the stylesheets of the application are
 * unlayered: engine &lt; component library &lt; application.
 * </p>
 */
@Label("Client resources")
public class ClientResources extends ConfiguredManagedClass<ClientResources.Config> {

	/**
	 * The CSS cascade layer of the engine's styles: its stylesheets, the libraries it bundles, and
	 * the design tokens of the UI themes.
	 */
	public static final String ENGINE_LAYER = "tl";

	/**
	 * The CSS cascade layer of the styles of a component library rendering the UI, e.g. the rules
	 * Material UI writes into the page.
	 */
	public static final String COMPONENT_LIBRARY_LAYER = "mui";

	/**
	 * Default of {@link Config#getLayers()}.
	 */
	public static final String DEFAULT_LAYERS = ENGINE_LAYER + ", " + COMPONENT_LIBRARY_LAYER;

	/**
	 * Configuration of {@link ClientResources}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<ClientResources> {

		/** Configuration name for {@link #getLayers()}. */
		String LAYERS = "layers";

		/**
		 * The CSS cascade layers of the page, from the lowest to the highest priority.
		 *
		 * <p>
		 * The rules of a later layer win against the rules of an earlier one, whatever their
		 * specificity; unlayered rules win against all of them. The default puts the engine's
		 * styles below the styles of a component library. An application may insert a layer of its
		 * own, e.g. {@code tl, app-base, mui} for a global base stylesheet (resets, element
		 * selectors) that overrides the engine but not the component library, and name it as the
		 * {@link StyleSheetConfig#getLayer()} of that stylesheet.
		 * </p>
		 */
		@Name(LAYERS)
		@Format(CommaSeparatedStrings.class)
		@FormattedDefault(DEFAULT_LAYERS)
		List<String> getLayers();

		/**
		 * The registered client resources, contributed across modules.
		 *
		 * <p>
		 * Entries are keyed by {@link ResourceConfig#getName() name}, so that a later module may
		 * replace or remove a resource by naming it with a {@code config:operation} of
		 * {@code update} or {@code remove}. A resource another entry still requires must not be
		 * removed; the registry reports that as an error.
		 * </p>
		 */
		@Name("resources")
		@Key(NamedConfiguration.NAME_ATTRIBUTE)
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
		checkLayers(context, config);
	}

	private static void checkLayers(Log log, Config config) {
		List<String> layers = config.getLayers();
		for (ResourceConfig resource : config.getResources()) {
			if (resource instanceof StyleSheetConfig stylesheet) {
				String layer = stylesheet.getLayer();
				if (!StringServices.isEmpty(layer) && !layers.contains(layer)) {
					log.error("Stylesheet '" + resource.getName() + "' names the CSS cascade layer '" + layer
						+ "', which is not one of the layers " + layers + ".");
				}
			}
		}
	}

	/**
	 * Emits the statement establishing the order of the CSS cascade {@link Config#getLayers()
	 * layers}.
	 *
	 * <p>
	 * Must be placed before any style of the page, since the first mention of a layer fixes its
	 * position in the order.
	 * </p>
	 *
	 * @param out
	 *        The writer of the HTML {@code <head>}.
	 * @throws IOException
	 *         If writing fails.
	 */
	public void writeLayerOrder(TagWriter out) throws IOException {
		List<String> layers = getConfig().getLayers();
		if (layers.isEmpty()) {
			return;
		}
		out.beginBeginTag(HTMLConstants.STYLE_ELEMENT);
		out.endBeginTag();
		out.writeContent("@layer ");
		out.writeContent(String.join(", ", layers));
		out.writeContent(";");
		out.endTag(HTMLConstants.STYLE_ELEMENT);
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
	 * Must be placed after the {@link #writeLayerOrder(TagWriter) layer order} and after the
	 * design-token block, so that the registered stylesheets of the layer {@value #ENGINE_LAYER},
	 * which reference the tokens, follow the tokens within that layer.
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

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.resource;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.gui.Theme;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;

/**
 * {@link ClientResourceProvider} that emits an individual head reference for every registered
 * resource (no runtime bundling).
 *
 * <p>
 * Emission order is: all stylesheets (cascade order), then the aggregated import map, then all
 * module scripts (evaluation order). The import map precedes the module scripts as required by the
 * browser. The same {@link ResourceRef} is used for a module's script tag and its import map entry,
 * so both reference an identical URL and the module is instantiated only once.
 * </p>
 */
public class UnbundledResourceProvider implements ClientResourceProvider {

	private static final String MODULE_TYPE = "module";

	private static final String IMPORTMAP_TYPE = "importmap";

	private final List<? extends ResourceConfig> _ordered;

	private final ResourceResolver _resolver;

	/**
	 * Creates a {@link UnbundledResourceProvider}.
	 *
	 * @param ordered
	 *        The registered resources in topologically sorted emission order.
	 * @param resolver
	 *        The resolver mapping a declaration to concrete references.
	 */
	public UnbundledResourceProvider(List<? extends ResourceConfig> ordered, ResourceResolver resolver) {
		_ordered = ordered;
		_resolver = resolver;
	}

	@Override
	public void writeScriptRefs(TagWriter out, String contextPath, Theme theme) throws IOException {
		writeImportMap(out, contextPath, theme);

		for (ResourceConfig resource : _ordered) {
			if (resource instanceof ModuleScriptConfig script && !script.isExternal()) {
				for (ResourceRef ref : _resolver.resolve(resource, theme)) {
					HTMLUtil.writeJavaScriptRef(out, contextPath, ref.url(), ref.version(), MODULE_TYPE);
				}
			}
		}
	}

	@Override
	public void writeStyleRefs(TagWriter out, String contextPath, Theme theme) throws IOException {
		for (ResourceConfig resource : _ordered) {
			if (resource instanceof StyleSheetConfig) {
				for (ResourceRef ref : _resolver.resolve(resource, theme)) {
					HTMLUtil.writeStylesheetRef(out, contextPath, ref.full());
				}
			}
		}
	}

	private void writeImportMap(TagWriter out, String contextPath, Theme theme) throws IOException {
		Map<String, String> imports = new LinkedHashMap<>();
		for (ResourceConfig resource : _ordered) {
			if (resource instanceof ModuleScriptConfig script) {
				String specifier = script.getSpecifier();
				if (!StringServices.isEmpty(specifier)) {
					List<ResourceRef> refs = _resolver.resolve(resource, theme);
					if (!refs.isEmpty()) {
						imports.put(specifier, contextPath + refs.get(0).full());
					}
				}
			}
		}
		if (imports.isEmpty()) {
			return;
		}

		out.beginBeginTag(HTMLConstants.SCRIPT);
		out.writeAttribute(HTMLConstants.TYPE_ATTR, IMPORTMAP_TYPE);
		out.endBeginTag();

		// Not closed deliberately, to avoid closing the underlying writer.
		@SuppressWarnings("resource")
		JsonWriter json = new JsonWriter(out);
		json.beginObject();
		json.name("imports");
		json.beginObject();
		for (Map.Entry<String, String> entry : imports.entrySet()) {
			json.name(entry.getKey());
			json.value(entry.getValue());
		}
		json.endObject();
		json.endObject();

		out.endTag(HTMLConstants.SCRIPT);
	}

}

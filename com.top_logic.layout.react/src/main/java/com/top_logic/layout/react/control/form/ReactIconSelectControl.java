/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.common.json.adapt.ReaderR;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.control.IconBundle;
import com.top_logic.layout.form.control.IconDescription;
import com.top_logic.layout.form.control.IconInputControl;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Icon select control that renders via the {@code TLIconSelect} React component.
 *
 * <p>
 * Displays the currently selected icon as a preview swatch and opens a popup picker to browse and
 * search icons from the configured icon libraries (e.g. Font Awesome).
 * </p>
 *
 * <p>
 * Icons are loaded lazily on first popup open via the {@code loadIcons} command to avoid sending
 * the full icon library on page load.
 * </p>
 */
public class ReactIconSelectControl extends ReactFormFieldControl {

	private static final String ICONS = "icons";

	private static final String ICONS_LOADED = "iconsLoaded";

	/**
	 * Creates a new {@link ReactIconSelectControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        The field model whose value is an encoded {@link ThemeImage} string.
	 */
	public ReactIconSelectControl(ReactContext context, FieldModel model) {
		super(context, model, "TLIconSelect");
		putState(ICONS_LOADED, Boolean.FALSE);
	}

	/**
	 * Lazily loads icon metadata from configured icon libraries and sends it to the client.
	 */
	@ReactCommand("loadIcons")
	HandlerResult handleLoadIcons() {
		try {
			List<Map<String, Object>> iconData = buildIconData();
			putState(ICONS, iconData);
			putState(ICONS_LOADED, Boolean.TRUE);
		} catch (Exception ex) {
			Logger.error("Failed to load icon data.", ex, ReactIconSelectControl.class);
			return HandlerResult.error(I18NConstants.JS_ICON_SELECT_LOAD_ERROR);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	private List<Map<String, Object>> buildIconData() {
		List<IconBundle> bundles = loadIconBundles();
		List<Map<String, Object>> result = new ArrayList<>();

		for (IconBundle bundle : bundles) {
			for (IconDescription icon : bundle.getIcons()) {
				List<Map<String, Object>> variants = new ArrayList<>();
				for (ThemeImage img : icon.getImages()) {
					Map<String, Object> variant = new HashMap<>();
					variant.put("encoded", img.toEncodedForm());
					variants.add(variant);
				}

				if (!variants.isEmpty()) {
					Map<String, Object> entry = new HashMap<>();
					entry.put("prefix", icon.getPrefix());
					entry.put("label", icon.getLabel());
					entry.put("terms", icon.getTerms());
					entry.put("variants", variants);
					result.add(entry);
				}
			}
		}

		return result;
	}

	/**
	 * Loads icon bundles from the configured icon library resources.
	 *
	 * <p>
	 * Reuses the same {@link IconInputControl.Config} configuration that the legacy icon chooser
	 * uses, reading JSON metadata files and applying style-to-CSS mappings.
	 * </p>
	 */
	static List<IconBundle> loadIconBundles() {
		List<IconBundle> resourceList = new ArrayList<>();
		FileManager fm = FileManager.getInstance();

		IconInputControl.Config config =
			ApplicationConfig.getInstance().getConfig(IconInputControl.Config.class);
		List<IconInputControl.ThemeImageMetaData> resources = config.getResources();

		for (IconInputControl.ThemeImageMetaData resource : resources) {
			Map<String, String> prefixMapping = resource.getIconMappings();
			String resourceName = resource.getResource();
			try {
				try (InputStream stream = fm.getStream(resourceName)) {
					Reader reader = new InputStreamReader(stream, StringServices.UTF8);
					JsonReader json = new JsonReader(new ReaderR(reader));
					resourceList.add(IconBundle.read(json, prefixMapping));
				}
			} catch (IOException ex) {
				Logger.warn(
					"Cannot read icon definition '" + resourceName + "'.",
					ex, ReactIconSelectControl.class);
			}
		}

		return resourceList;
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.theme;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.model.annotate.ui.ValueColor;

/**
 * The fill color of each {@link ValueColor color role}: the tinted surface the design system draws
 * the role with, in its light default mode.
 *
 * <p>
 * Where a role cannot be drawn by the stylesheet of the design system but needs a color value - an
 * Excel export fills its cells, and a workbook has no dark mode -, it is filled with this color.
 * The values are not kept here: the build of the design system writes them from its token
 * definitions into the resource {@value #RESOURCE}, the same source its stylesheets are generated
 * from. A role the design system draws without a tint ({@link ValueColor#NEUTRAL}) has no fill.
 * </p>
 */
public final class RoleFills {

	/**
	 * The resource holding the fill of each role, one line <code>role=#rrggbb</code> per role, keyed
	 * by the {@link ValueColor#getExternalName() external name} of the role.
	 */
	public static final String RESOURCE = "/style/tl-design-system/role-fills.properties";

	/** The fills, read on first use. */
	private static volatile Map<ValueColor, Color> _fills;

	private RoleFills() {
		// Static utilities only.
	}

	/**
	 * The fill color of the given role, {@code null} for a role without a fill.
	 */
	public static Color fill(ValueColor role) {
		return fills().get(role);
	}

	/**
	 * The fills of all roles that have one.
	 */
	public static Map<ValueColor, Color> fills() {
		Map<ValueColor, Color> result = _fills;
		if (result == null) {
			result = Collections.unmodifiableMap(load());
			_fills = result;
		}
		return result;
	}

	private static Map<ValueColor, Color> load() {
		Map<ValueColor, Color> result = new EnumMap<>(ValueColor.class);
		Properties properties = new Properties();
		try (InputStream in = FileManager.getInstance().getStream(RESOURCE);
				Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
			properties.load(reader);
		} catch (IOException ex) {
			Logger.error("The fills of the color roles cannot be read from '" + RESOURCE + "'.", ex,
				RoleFills.class);
			return result;
		}
		for (String name : properties.stringPropertyNames()) {
			ValueColor role = ValueColor.byExternalName(name);
			if (role == null) {
				Logger.warn("The design system fills a color role '" + name + "' that is no role of "
					+ ValueColor.class.getName() + ".", RoleFills.class);
				continue;
			}
			result.put(role, Color.decode(properties.getProperty(name).trim()));
		}
		return result;
	}

}

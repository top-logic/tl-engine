/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.version;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.version.model.VersionInfo;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;

/**
 * {@link CellRenderer} writing the {@link VersionInfo} name column.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DependencyNameCellRenderer extends AbstractCellRenderer {

	/**
	 * Singleton {@link DependencyNameCellRenderer} instance.
	 */
	public static final DependencyNameCellRenderer INSTANCE = new DependencyNameCellRenderer();

	private DependencyNameCellRenderer() {
		// Singleton constructor.
	}

	@Override
	public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		VersionInfo version = (VersionInfo) cell.getRowObject();
		String name = (String) DependencyNameAccessor.INSTANCE.getValue(version, cell.getColumnName());

		try (TagWriter tooltip = new TagWriter()) {
			tooltip.beginTag(H3);
			tooltip.append(version.getGroupId());
			tooltip.append(":");
			tooltip.append(version.getArtifactId());
			tooltip.append(" ");
			tooltip.append(version.getVersion());
			tooltip.endTag(H3);

			String description = version.getDescription();
			if (description != null && !description.trim().isEmpty()) {
				tooltip.beginTag(PARAGRAPH);
				tooltip.append(description);
				tooltip.endTag(PARAGRAPH);
			}

			String homepage = version.getUrl();
			if (homepage != null && !homepage.trim().isEmpty()) {
				tooltip.beginTag(PARAGRAPH);
				tooltip.append(homepage);
				tooltip.endTag(PARAGRAPH);
			}

			out.beginBeginTag(SPAN);
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out,
				ResKey.text(tooltip.toString()));
			out.endBeginTag();

			URL url = validateUrl(version);
			if (url != null) {
				out.beginBeginTag(ANCHOR);
				out.writeAttribute(HREF_ATTR, url.toExternalForm());
				out.writeAttribute(TARGET_ATTR, BLANK_VALUE);
				out.endBeginTag();
				{
					out.append(name);
				}
				out.endTag(ANCHOR);
			} else {
				out.append(name);
			}

			out.endTag(SPAN);
		}
	}

	private URL validateUrl(VersionInfo version) {
		String url = version.getUrl();
		if (url == null) {
			return null;
		}
		try {
			return new URL(url);
		} catch (MalformedURLException ex) {
			return null;
		}
	}

}

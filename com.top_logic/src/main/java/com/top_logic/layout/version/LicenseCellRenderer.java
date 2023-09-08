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

import com.top_logic.basic.version.model.License;
import com.top_logic.basic.version.model.VersionInfo;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;

/**
 * {@link CellRenderer} writing the {@link License}s of a {@link VersionInfo} row.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LicenseCellRenderer extends AbstractCellRenderer {

	/**
	 * Singleton {@link LicenseCellRenderer} instance.
	 */
	public static final LicenseCellRenderer INSTANCE = new LicenseCellRenderer();

	private LicenseCellRenderer() {
		// Singleton constructor.
	}

	@Override
	public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		VersionInfo version = (VersionInfo) cell.getRowObject();

		boolean first = true;
		for (License license : version.getLicenses()) {
			if (first) {
				first = false;
			} else {
				out.append(", ");
			}

			String urlSpec = license.getUrl();
			if (urlSpec != null) {
				try {
					URL url = new URL(urlSpec);

					out.beginBeginTag(ANCHOR);
					out.writeAttribute(HREF_ATTR, url.toExternalForm());
					out.writeAttribute(TARGET_ATTR, BLANK_VALUE);
					out.endBeginTag();
					{
						out.append(license.getName());
					}
					out.endTag(ANCHOR);
				} catch (MalformedURLException ex) {
					out.append(license.getName());
				}
			} else {
				out.append(license.getName());
			}
		}
	}

}

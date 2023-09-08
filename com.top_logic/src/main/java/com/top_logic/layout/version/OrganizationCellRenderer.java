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

import com.top_logic.basic.version.model.Organisation;
import com.top_logic.basic.version.model.VersionInfo;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;

/**
 * {@link CellRenderer} writing the {@link VersionInfo} name column.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OrganizationCellRenderer extends AbstractCellRenderer {

	/**
	 * Singleton {@link OrganizationCellRenderer} instance.
	 */
	public static final OrganizationCellRenderer INSTANCE = new OrganizationCellRenderer();

	private OrganizationCellRenderer() {
		// Singleton constructor.
	}

	@Override
	public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		VersionInfo version = (VersionInfo) cell.getRowObject();

		Organisation organization = version.getOrganization();
		String name = (String) OrganizationAccessor.INSTANCE.getValue(version, cell.getColumnName());

		URL url = validateUrl(organization);
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
	}

	private URL validateUrl(Organisation organization) {
		if (organization == null) {
			return null;
		}
		String url = organization.getUrl();
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

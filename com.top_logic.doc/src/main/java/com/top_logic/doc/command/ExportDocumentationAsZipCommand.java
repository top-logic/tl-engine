/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.command;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.Settings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.doc.model.Page;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Delivers the selected part of the documentation as ZIP download.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExportDocumentationAsZipCommand extends AbstractExportDocumentationCommand {

	/**
	 * Creates a {@link ExportDocumentationAsZipCommand}.
	 */
	public ExportDocumentationAsZipCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		try {
			tryExport(context, (Page) model);
		} catch (IOException ex) {
			throw new IOError(ex);
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	private void tryExport(DisplayContext context, Page model) throws IOException {
		File tmp = Settings.getInstance().getTempDir();
		File zipfile = File.createTempFile("export", ".zip", tmp);
		zipfile.delete();
		Map<String, String> env = new HashMap<>();
		env.put("create", "true");
		try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + zipfile.toPath().toUri()), env, null)) {
			export(fs.getRootDirectories().iterator().next(), model, new ExportArgs().setExportUnknownSource(true));
		}

		String name = pageName(model) + "-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".zip";
		context.getWindowScope()
			.deliverContent(BinaryDataFactory.createBinaryData(zipfile, "application/zip", name));
	}

	private String pageName(Page page) {
		if (page.isRoot()) {
			return "All";
		}
		StringBuilder result = new StringBuilder();
		appendPageName(result, page);
		return result.toString();
	}

	private void appendPageName(StringBuilder result, Page page) {
		Page parent = page.getParent();
		if (!parent.isRoot()) {
			appendPageName(result, parent);
			result.append("-");
		}
		result.append(toFileName(page.getName()));
	}

}

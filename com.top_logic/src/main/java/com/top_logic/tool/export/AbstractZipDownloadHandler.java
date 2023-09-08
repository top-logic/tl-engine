/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.Settings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Zipper;

/**
 * Handler to download a zip file.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class AbstractZipDownloadHandler extends AbstractCommandHandler {

	/**
	 * Creates a {@link AbstractZipDownloadHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public AbstractZipDownloadHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		try {
			deliverContent(context, component, createZipTmpFile(component, arguments));
		} catch (IOException exception) {
			throw new IOError(exception);
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	private File createZipTmpFile(LayoutComponent component, Map<String, Object> arguments) throws IOException {
		File tmpZipFile = createRawZipTmpFile(component);

		addZipEntries(component, arguments, tmpZipFile);

		return tmpZipFile;
	}

	private void addZipEntries(LayoutComponent component, Map<String, Object> arguments, File tmpZipFile) throws IOException {
		try (FileOutputStream fileOutStream = new FileOutputStream(tmpZipFile)) {
			try (Zipper zipper = new Zipper(fileOutStream)) {
				addFilesToZip(zipper, component, arguments);
			}
		}
	}

	private void deliverContent(DisplayContext context, LayoutComponent component, File tempZipFile) {
		context.getWindowScope().deliverContent(createFileBasedBinaryData(component, tempZipFile));
	}

	private BinaryData createFileBasedBinaryData(LayoutComponent component, File tempZipFile) {
		return BinaryDataFactory.createBinaryDataWithContentType(tempZipFile, getZipFilename(component));
	}

	private String getZipFilename(LayoutComponent component) {
		return getZipName(component) + FileUtilities.ZIP_FILE_ENDING;
	}

	private File createRawZipTmpFile(LayoutComponent component) throws IOException {
		return File.createTempFile(getZipName(component), FileUtilities.ZIP_FILE_ENDING, getTempDirectory());
	}

	private File getTempDirectory() {
		return Settings.getInstance().getTempDir();
	}

	/**
	 * Name of the created zip.
	 */
	protected abstract String getZipName(LayoutComponent component);

	/**
	 * Use the given {@link Zipper} to add content to the zip file.
	 */
	protected abstract void addFilesToZip(Zipper zipper, LayoutComponent component, Map<String, Object> arguments);
}

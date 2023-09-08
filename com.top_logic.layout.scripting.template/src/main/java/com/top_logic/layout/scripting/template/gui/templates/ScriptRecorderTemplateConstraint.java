/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.template.xml.TemplateXMLParser;
import com.top_logic.template.xml.source.TemplateFilesystemSource;
import com.top_logic.util.Resources;

/**
 * Checks if the given data is a valid collection of ScriptRecorder templates.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ScriptRecorderTemplateConstraint extends AbstractConstraint {

	private final String TMP_TEMPLATE_FILE_PREFIX = "checkScriptTemplate";

	private final TemplateXMLParser _templateXMLParser;

	/**
	 * Creates the a constraints that checks if the files are correct ScriptRecorder templates.
	 */
	public ScriptRecorderTemplateConstraint() {
		_templateXMLParser = new TemplateXMLParser();
	}

	private File createTmpTemplateFile() throws IOError {
		try {
			File tmpDir = Settings.getInstance().getTempDir();

			return File.createTempFile(TMP_TEMPLATE_FILE_PREFIX, StringServices.EMPTY_STRING, tmpDir);
		} catch (IOException exception) {
			throw new IOError(exception);
		}
	}

	@Override
	public boolean check(Object value) throws CheckException {
		List<BinaryData> items = DataField.toItems(value);

		for (BinaryData item : items) {
			checkItem(item);
		}

		return true;
	}

	private void checkItem(BinaryData data) throws CheckException {
		try (InputStream stream = data.getStream()) {
			if (ImportScriptRecorderTemplateCommand.ZIP_COMPRESSED_CONTENT_TYPE.equals(data.getContentType())) {
				checkZipFile(stream);
			} else {
				checkSingleScriptRecorderTemplate(stream);
			}
		} catch (IOException exception) {
			throw new IOError(exception);
		}
	}

	private void checkZipFile(InputStream stream) throws IOException, CheckException {
		try (BufferedInputStream bufferedInputStream = new BufferedInputStream(stream)) {
			try (ZipInputStream zipStream = new ZipInputStream(bufferedInputStream)) {
				ZipEntry zipEntry = zipStream.getNextEntry();

				while (zipEntry != null) {
					checkValidZipEntry(zipStream, zipEntry);

					zipEntry = zipStream.getNextEntry();
				}
			}
		}
	}

	private void checkValidZipEntry(ZipInputStream zipStream, ZipEntry zipEntry) throws IOException, CheckException {
		if (!zipEntry.isDirectory()) {
			checkSingleScriptRecorderTemplate(zipStream);
		}
	}

	private void checkSingleScriptRecorderTemplate(InputStream stream) throws IOError, IOException, CheckException {
		File tmpFile = createTmpTemplateFile();
		FileUtilities.copyToFile(stream, tmpFile);

		checkScriptRecorderTemplateFile(tmpFile);
	}

	private void checkScriptRecorderTemplateFile(File possibleTemplateFile) throws CheckException {
		try {
			_templateXMLParser.parse(new TemplateFilesystemSource(possibleTemplateFile));
		} catch (Exception exception) {
			ResKey noValidTemplateResKey = I18NConstants.NO_VALID_SCRIPT_RECORDER_TEMPLATE_FILE;

			throw new CheckException(Resources.getInstance().getString(noValidTemplateResKey));
		}
	}
}

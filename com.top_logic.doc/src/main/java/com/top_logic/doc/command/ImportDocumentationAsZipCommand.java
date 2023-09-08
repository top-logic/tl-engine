/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.command;

import static com.top_logic.layout.basic.fragments.Fragments.*;
import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.doc.export.DocumentationImporter;
import com.top_logic.doc.export.TLDocExportImportConstants;
import com.top_logic.doc.model.Page;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.values.edit.annotation.AcceptedTypes;
import com.top_logic.layout.messagebox.AbstractFormDialogBase;
import com.top_logic.layout.messagebox.CreateConfigurationDialog;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.NullModelDisabled;
import com.top_logic.util.error.TopLogicException;

/**
 * Command to import a Zip export of the docummentation.
 * 
 * @see ExportDocumentationAsZipCommand
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ImportDocumentationAsZipCommand extends AbstractImportExportDocumentationCommand {

	/**
	 * Configuration of the import data.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ImportSettings extends ConfigurationItem {
		/**
		 * @see #getImportData()
		 */
		String IMPORT_DATA = "import-data";

		/**
		 * The data to import.
		 */
		@AcceptedTypes({ "application/zip", })
		@Name(IMPORT_DATA)
		@Mandatory
		BinaryData getImportData();
	}

	/** {@link ConfigurationItem} for the {@link ImportDocumentationAsZipCommand}. */
	public interface Config extends AbstractImportExportDocumentationCommand.Config {

		/** Menu group for import buttons */
		String IMPORT_BUTTONS = "importButtons";

		@Override
		@StringDefault(IMPORT_BUTTONS)
		String getClique();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.CREATE_NAME)
		CommandGroupReference getGroup();

	}

	/** {@link TypedConfiguration} constructor for {@link ImportDocumentationAsZipCommand}. */
	public ImportDocumentationAsZipCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), NullModelDisabled.INSTANCE);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		if (!(model instanceof Page)) {
			return HandlerResult.error(com.top_logic.doc.command.I18NConstants.SELECTION_IS_NO_PAGE);
		}
		Page page = (Page) model;
		AbstractFormDialogBase importDialog =
			new CreateConfigurationDialog<>(ImportSettings.class,
				settings -> processImport(settings, page),
				I18NConstants.IMPORT_DIALOG.key("title"),
				DisplayDimension.px(300),
				DisplayDimension.px(150));
		return importDialog.open(aContext);
	}

	private HandlerResult processImport(ImportSettings settings, Page target) {
		File importRoot;
		try (InputStream input = settings.getImportData().getStream()) {
			importRoot = unzipToTmpFolder(input);
		} catch (IOException ex) {
			throw new TopLogicException(I18NConstants.UNZIP_FAILED, ex);
		}
		String directImportRootFile = FileManager.markDirect(importRoot.getAbsolutePath());
		DocumentationImporter importer = new DocumentationImporter(directImportRootFile);
		importer.setMissingDocumentationHandler(
			locale -> showNoImportFilesFoundMessage(locale, settings.getImportData().getName()));
		BufferingProtocol protocol = new BufferingProtocol();
		importer.doImport(protocol, target);
		protocol.checkErrors();
		FileUtilities.deleteR(importRoot);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Shows an info message that contains an explanation about the required directory structure of
	 * the uploaded zip file.
	 * 
	 * @param locale
	 *        {@link Locale} of the currently imported documentation.
	 * @param zipName
	 *        Name of the zip file that should be imported.
	 */
	private void showNoImportFilesFoundMessage(Locale locale, String zipName) {
		HTMLFragment infoText = p(message(I18NConstants.NO_IMPORT_FILES_ZIP__LANGUAGE__FILE
			.fill(locale.getDisplayLanguage(ThreadContext.getLocale()), zipName)));
		HTMLFragment structureText = p(message(I18NConstants.REQUIRED_IMPORT_STRUCTURE__FILE.fill(zipName)));

		HTMLFragment structure = new HTMLFragment() {
			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				out.beginTag(UL);
				out.beginTag(LI);
				out.append(locale.toString());
				out.endTag(LI);
				out.beginTag(UL);
				out.beginTag(LI);
				out.append(TLDocExportImportConstants.PROPERTIES_TITLE);
				out.endTag(LI);
				out.beginTag(UL);
				out.beginTag(LI);
				out.append(TLDocExportImportConstants.PROPERTIES_FILE_NAME);
				out.endTag(LI);
				out.beginTag(LI);
				out.append(TLDocExportImportConstants.CONTENT_FILE_NAME);
				out.endTag(LI);
				out.beginTag(LI);
				out.append("...");
				out.endTag(LI);
				out.endTag(UL);
				out.endTag(UL);
				out.endTag(UL);
			}
		};
		HTMLFragment message = div(infoText, structureText, structure);
		InfoService.showInfo(message);
	}

	private File unzipToTmpFolder(InputStream input) throws IOException, FileNotFoundException {
		File folder = new File(Settings.getInstance().getTempDir(), StringServices.randomUUID());
		folder.mkdir();
		unzipToFolder(input, folder);
		return folder;
	}

	private void unzipToFolder(InputStream input, File target) throws IOException, FileNotFoundException {
		try (ZipInputStream zipStream = new ZipInputStream(input)) {
			ZipEntry zipEntry;
			while ((zipEntry = zipStream.getNextEntry()) != null) {
				try {
					if (zipEntry.isDirectory()) {
						new File(target, zipEntry.getName()).mkdirs();
					} else {
						try (OutputStream out = new FileOutputStream(new File(target, zipEntry.getName()))) {
							StreamUtilities.copyStreamContents(zipStream, out);
						}
					}
				} finally {
					zipStream.closeEntry();
				}
			}

		}
	}

}

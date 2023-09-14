/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.command;

import static com.top_logic.basic.StringServices.*;
import static com.top_logic.doc.export.TLDocExportImportConstants.*;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.doc.model.Page;
import com.top_logic.doc.model.TLDocFactory;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.NullModelDisabled;
import com.top_logic.util.Resources;

/**
 * A {@link CommandHandler} that exports the documentation into the workspace.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class AbstractExportDocumentationCommand extends AbstractImportExportDocumentationCommand {

	/**
	 * Static export arguments.
	 * 
	 * @see AbstractExportDocumentationCommand#export(Path, Page, ExportArgs)
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	protected class ExportArgs {

		/** Position offset to determine a position of a page relative to its sibling. */
		public int _defaultPositionOffset = 1000;

		/**
		 * Workspace root.
		 * 
		 * <p>
		 * The value must be an initial segment of the export root to determine actual folder to
		 * export documentation to. Documentation is exported to the source stored in
		 * {@link Page#getImportSource()} if exists.
		 * </p>
		 * 
		 * <p>
		 * May be <code>null</code> in which case the value of {@link Page#getImportSource()} is
		 * ignored.
		 * </p>
		 */
		public Path _ws;

		/**
		 * Whether a page must be exported to the "default" export path, when the page has a source
		 * annotation, but the source can not be determined within {@link #_ws}.
		 * 
		 * <p>
		 * If <code>false</code>, such a page is <b>not</b> exported.
		 * </p>
		 */
		public boolean _exportUnknownSource;

		/**
		 * Creates a new {@link ExportArgs}.
		 */
		public ExportArgs() {
		}

		/**
		 * Setter for {@link #_defaultPositionOffset}.
		 */
		public ExportArgs setDefaultPositionOffset(int offset) {
			_defaultPositionOffset = offset;
			return this;
		}

		/**
		 * Setter for {@link #_ws}.
		 */
		public ExportArgs setWorkspace(Path ws) {
			_ws = ws;
			return this;
		}

		/**
		 * Setter for {@link #_exportUnknownSource}.
		 */
		public ExportArgs setExportUnknownSource(boolean b) {
			_exportUnknownSource = b;
			return this;
		}

	}

	/**
	 * Configuration of an {@link AbstractExportDocumentationCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractImportExportDocumentationCommand.Config {

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.EXPORT_NAME)
		CommandGroupReference getGroup();

	}

	/**
	 * {@link TypedConfiguration} constructor for {@link AbstractExportDocumentationCommand}.
	 */
	public AbstractExportDocumentationCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), NullModelDisabled.INSTANCE);
	}

	/**
	 * Actually writes contents to the given {@link Path}.
	 * 
	 * @param root
	 *        Root path of the documentation to export documentation to.
	 * @param model
	 *        The model the command is executed on.
	 * @param arg
	 *        Static export arguments.
	 */
	protected void export(Path root, Page model, ExportArgs arg) {
		if (arg._ws != null && !root.startsWith(arg._ws)) {
			throw new IllegalArgumentException(
				arg._ws + " is not an initial segment of " + root + ". Unable to determine source folder.");
		}
		Path documentationRoot = root;
		for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
			Resources resources = Resources.getInstance(locale);
			Path languageRootPath = documentationRoot.resolve(locale.getLanguage());

			Page rootPage = TLDocFactory.getInstance().getRootSingleton();
			if (model == rootPage) {
				for (Page child : model.getChildren()) {
					exportSubTree(resources, arg, getPath(languageRootPath, child), child, arg._defaultPositionOffset);
				}
			} else {
				Path modelPath = descendTo(languageRootPath, rootPage, model);
				exportSubTree(resources, arg, modelPath, model, arg._defaultPositionOffset);
			}
		}
	}

	private Path descendTo(Path rootPagePath, Page rootPage, Page model) {
		if (model == rootPage) {
			return rootPagePath;
		}

		rootPagePath = descendTo(rootPagePath, rootPage, model.getParent());

		return getPath(rootPagePath, model);
	}

	/**
	 * Exports the given {@link Page} and all its (indirect) children.
	 * @param resources
	 *        The language to export. Never null.
	 * @param arg
	 *        Static export arguments.
	 * @param path
	 *        Never null.
	 * @param page
	 *        Never null.
	 * @param position
	 *        Sort position of the page inside of the subtree of its parent.
	 */
	protected void exportSubTree(Resources resources, ExportArgs arg, Path path, Page page, int position) {
		exportPage(resources, arg, path, page, position);
		List<? extends Page> children = page.getChildren();

		int childPosition = arg._defaultPositionOffset;
		for (int i = 0; i < children.size(); i++) {
			Path childPath = getPath(path, children.get(i));
			exportSubTree(resources, arg, childPath, children.get(i), childPosition);
			childPosition += arg._defaultPositionOffset;
		}
	}

	private Path createDirectory(Path path) {
		try {
			return Files.createDirectories(path);
		} catch (IOException exception) {
			String message = "Failed to create the directory: '" + path + "'. Cause: " + exception.getMessage();
			throw new RuntimeException(message, exception);
		}
	}

	/**
	 * Exports the given {@link Page} to the given {@link Path}.
	 * 
	 * @param resources
	 *        The language to export. Never null.
	 * @param arg
	 *        Static export arguments.
	 * @param path
	 *        Never null.
	 * @param page
	 *        Never null.
	 * @param position
	 *        Sort position of the page inside of the subtree of its parent.
	 */
	protected void exportPage(Resources resources, ExportArgs arg, Path path, Page page, int position) {
		if (arg._ws != null) {
			path = relativize(arg, page, path);
			if (path == null) {
				// No export.
				return;
			}
		}
		boolean createdContentFile = exportContent(resources, path, page);
		if (!createdContentFile) {
			// If an element below the page has content the page has to be created even thought it
			// has no content itself. If no child has content the page will not be created.
			if (!hasChildrenWithContent(resources, page)) {
				return;
			}
			createDirectory(path);
		}

		Properties properties = new Properties();

		setPropValue(properties, PROPERTIES_UUID, stripNullsafe(page.getUuid()));
		setPropValue(properties, PROPERTIES_TITLE, stripNullsafe(resources.getString(page.getTitle())));
		setPropValue(properties, PROPERTIES_POSITION, Integer.toString(position));
		setPropValue(properties, PROPERTIES_SOURCE_BUNDLE, stripNullsafe(page.getImportSource()));

		try {
			try (OutputStream out = Files.newOutputStream(path.resolve(PROPERTIES_FILE_NAME))) {
				StreamUtilities.storeNormalized(out, properties);
			}
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	private void setPropValue(Properties properties, String property, String value) {
		if (!StringServices.isEmpty(value)) {
			properties.setProperty(property, value);
		}
	}

	/**
	 * Determines the actual path containing page files.
	 * 
	 * @param arg
	 *        Export arguments. {@link ExportArgs#_ws} is not null.
	 * @param page
	 *        Page to export.
	 * @param path
	 *        Original path desired to export page to.
	 * 
	 * @return <code>null</code> in case the page must not be exported.
	 */
	protected Path relativize(ExportArgs arg, Page page, Path path) {
		String importSource = page.getImportSource();
		if (importSource == null) {
			// No source known. export to default path.
			return path;
		}
		Path resolvedSource = arg._ws.resolve(importSource.strip());
		if (!Files.exists(resolvedSource)) {
			// source bundle does not exist.
			if (arg._exportUnknownSource) {
				return path;
			} else {
				return null;
			}
		}
		Path pathRelativeToWS = arg._ws.relativize(path);
		Path removedSourceBundle = pathRelativeToWS.subpath(1, pathRelativeToWS.getNameCount());
		return resolvedSource.resolve(removedSourceBundle);
	}

	/**
	 * Exports the content of the {@link Page} of the specified {@link Locale} into a file in the
	 * workspace.
	 * 
	 * @param resources
	 *        {@link Resources} containing the {@link Locale}. Not <code>null</code>.
	 * @param path
	 *        {@link Path} to export the file to.
	 * @param page
	 *        {@link Page} whose content will be exported.
	 * @return If a file was exported.
	 */
	private boolean exportContent(Resources resources, Path path, Page page) {
		StructuredText translatedContent = translatedContent(resources, page);
		if (translatedContent == null) {
			return false;
		}
		String translatedSourceCode = translatedContent.getSourceCode();
		if (StringServices.isEmpty(translatedSourceCode)) {
			/* No need to check for images in this case: If there is no source code, there are also
			 * no images: The source code would have to contain the <img> tag for the images. But
			 * than it would not be empty. */
			return false;
		}
		createDirectory(path);
		write(path, CONTENT_FILE_NAME, translatedSourceCode);
		Map<String, BinaryData> images = translatedContent.getImages();
		if (!CollectionUtil.isEmptyOrNull(images)) {
			write(path, images);
		}
		return true;
	}

	/**
	 * Translation of the {@link Page} content determined by the {@link Locale}.
	 * 
	 * @param resources
	 *        {@link Resources} containing the {@link Locale}. Not <code>null</code>.
	 * @param page
	 *        {@link Page} containing the {@link I18NStructuredText}. Not <code>null</code>.
	 * @return {@link StructuredText} of the {@link Locale}. <code>null</code> if the
	 *         {@link StructuredText} has no content meaning the {@link Page} has no translation for
	 *         the given {@link Locale} and therefore should not be exported.
	 */
	private StructuredText translatedContent(Resources resources, Page page) {
		I18NStructuredText content = page.getContent();
		if (content == null) {
			return null;
		}
		StructuredText translatedContent = content.localizeStrict(resources.getLocale());
		return translatedContent;
	}

	/**
	 * Whether a child {@link Page} or any of its children has content.
	 * 
	 * @param resources
	 *        {@link Resources} of the {@link Locale}.
	 * @param parent
	 *        {@link Page} whose children will be checked.
	 * @return <code>true</code> if one element below the parent has content.
	 */
	private boolean hasChildrenWithContent(Resources resources, Page parent) {
		if (parent == null || !parent.hasChildren()) {
			return false;
		}

		for (Page child : parent.getChildren()) {
			if (translatedContent(resources, child) != null) {
				return true;
			}
			if (hasChildrenWithContent(resources, child)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Writes the given content.
	 * <p>
	 * Replaces the file, if it already exists.
	 * </p>
	 */
	protected void write(Path folder, String fileName, String fileContent) {
		Path file = folder.resolve(fileName);
		write(file, fileContent);
	}

	private void write(Path path, String content) {
		try {
			Files.write(path, content.getBytes(CHARACTER_SET));
		} catch (IOException exception) {
			String message = "Failed to write the file: '" + path + "'. Cause: " + exception.getMessage();
			throw new RuntimeException(message, exception);
		}
	}

	/**
	 * Writes the given images to the specified folder.
	 * <p>
	 * Replaces the files, if they already exist.
	 * </p>
	 */
	protected void write(Path path, Map<String, BinaryData> images) {
		images.entrySet().forEach(
			entry -> write(path, entry.getKey(), entry.getValue()));
	}

	/**
	 * Writes the given image to the specified file.
	 * <p>
	 * Replaces the file, if it already exists.
	 * </p>
	 */
	protected void write(Path folder, String fileName, BinaryData image) {
		Path file = folder.resolve(fileName);
		try (InputStream inputStream = image.getStream()) {
			Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException exception) {
			String message = "Failed to write the file: '" + file + "'. Cause: " + exception.getMessage();
			throw new RuntimeException(message, exception);
		}
	}

}

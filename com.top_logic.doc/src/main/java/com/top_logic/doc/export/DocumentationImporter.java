/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.export;

import static com.top_logic.basic.StringServices.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.top_logic.basic.Environment;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.shared.function.FunctionUtil;
import com.top_logic.basic.tooling.ModuleLayout;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.dob.DataObject;
import com.top_logic.doc.model.Page;
import com.top_logic.doc.model.TLDocFactory;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextUtil;
import com.top_logic.util.TLResKeyUtil;

/**
 * Import class that updates the documentation of the application from a certain path in the
 * workspace.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DocumentationImporter {

	/** Prefix of image mime types */
	private static final String IMAGE_MIME_TYPE = "image/";

	private Consumer<Locale> _missingDocumentationHandler;

	private Iterable<Locale> _importLocales;

	private final String _docRootResourcePath;

	/**
	 * Creates a new {@link DocumentationImporter}.
	 */
	public DocumentationImporter() {
		this(TLDocExportImportConstants.ROOT_RELATIVE_PATH);
	}

	/**
	 * Creates a new {@link DocumentationImporter}.
	 * 
	 * @param docRootResourcePath
	 *        Resource path relative to {@link ModuleLayout#getWebappDir() webapp} where the import
	 *        data lie.
	 */
	public DocumentationImporter(String docRootResourcePath) {
		_docRootResourcePath = docRootResourcePath;
		setMissingDocumentationHandler(FunctionUtil.noOpConsumer());
		setImportLocales(ResourcesModule.getInstance().getSupportedLocales());
	}

	/**
	 * Consumer that is called when not data for some language can be found.
	 */
	public Consumer<Locale> getMissingDocumentationHandler() {
		return _missingDocumentationHandler;
	}

	/**
	 * Setter for {@link #getMissingDocumentationHandler()}.
	 */
	public void setMissingDocumentationHandler(Consumer<Locale> missingDocumentationHandler) {
		_missingDocumentationHandler = Objects.requireNonNull(missingDocumentationHandler);
	}

	/**
	 * Locales for which the import is executed.
	 * 
	 * <p>
	 * When nothing is set, import is executed for all available languages.
	 * </p>
	 * 
	 * @see ResourcesModule#getSupportedLocales()
	 */
	public Iterable<Locale> getImportLocales() {
		return _importLocales;
	}

	/**
	 * Setter for {@link #getImportLocales()}.
	 */
	public void setImportLocales(Iterable<Locale> importLocales) {
		_importLocales = Objects.requireNonNull(importLocales);
		
	}

	/**
	 * Setter for {@link #getImportLocales()}.
	 */
	public final void setImportLocales(Locale... locales) {
		setImportLocales(Arrays.asList(locales));
	}

	/**
	 * Root resource path for the documentation.
	 */
	public String getDocRootResourcePath() {
		return _docRootResourcePath;
	}

	/**
	 * Imports the documentation from {@link #getDocRootResourcePath()}.
	 * 
	 * @param log
	 *        {@link Log} to write infos and errors to.
	 */
	public void doImport(Log log) {
		doImport(log, TLDocFactory.getInstance().getRootSingleton());
	}

	/**
	 * Imports the documentation from {@link #getDocRootResourcePath()} as child of the given root
	 * page.
	 * 
	 * @param log
	 *        {@link Log} to write infos and errors to.
	 * @param rootPage
	 *        The page to import documentation to.
	 */
	public void doImport(Log log, Page rootPage) {
		if (StringServices.isEmpty(getDocRootResourcePath())) {
			log.info("No documentation root folder.");
			return;
		}
		importForLocales(log, rootPage);
	}

	private void importForLocales(Log log, Page rootPage) {
		for (Locale locale : _importLocales) {
			log.info("Importing data for language " + locale.getLanguage());

			Set<String> rootResourcesForLanguage = getRootResourcePathsForLanguage(locale);

			if (rootResourcesForLanguage.isEmpty()) {
				_missingDocumentationHandler.accept(locale);
				return;
			}

			List<String> orderedRootResourcesForLanguage = getResourcesSortedByFilename(rootResourcesForLanguage);
			int entryPosition = 1000;
			for (String rootResourceForLanguage : orderedRootResourcesForLanguage) {
				Page child = importSubTree(log, rootPage, locale, rootResourceForLanguage, entryPosition);
				if (child != null) {
					entryPosition += 1000;
				}
			}
			sortChildren(rootPage);
		}
	}

	private List<String> getResourcesSortedByFilename(Set<String> resourcePaths) {
		return resourcePaths.stream()
			.filter(resource -> FileManager.getInstance().isDirectory(resource))
			.sorted((resource1, resource2) -> {
				return FileUtilities.getFilenameOfResource(resource1).toLowerCase()
					.compareTo(FileUtilities.getFilenameOfResource(resource2).toLowerCase());
			})
			.collect(Collectors.toList());
	}

	private Set<String> getRootResourcePathsForLanguage(Locale locale) {
		return FileManager.getInstance().getResourcePaths(createResourcePathForLanguage(locale));
	}

	private String createResourcePathForLanguage(Locale locale) {
		return _docRootResourcePath + "/" + locale.getLanguage() + "/";
	}

	/**
	 * Imports the given folders and all its (indirect) children into {@link Page}s.
	 * 
	 * @param log
	 *        Log to write informations and errors to.
	 * @param parent
	 *        The {@link Page} to import the new {@link Page}s to. Never null.
	 * @param locale
	 *        {@link Locale} of the imported files. Never null.
	 * @param position
	 *        The position of the imported node within its sibling nodes (if not explicitly
	 *        specified by page properties).
	 * 
	 * @return The root of the imported sub-tree.
	 */
	protected Page importSubTree(Log log, Page parent, Locale locale, String resourcePath, int position) {
		Page newParent = importPage(log, parent, locale, resourcePath, position);
		log.info("Imported content from path " + resourcePath, Protocol.VERBOSE);
		if (newParent != null) {
			importContentPages(log, newParent, locale, resourcePath);
		}
		return parent;
	}

	private void importContentPages(Log log, Page parent, Locale locale, String rootPath) {
		Set<String> resourcePaths = FileManager.getInstance().getResourcePaths(rootPath);
		List<String> orderedResourcePaths = getResourcesSortedByFilename(resourcePaths);
		int entryPosition = 1000;
		for (String resourcePath : orderedResourcePaths) {
			Page child = importSubTree(log, parent, locale, resourcePath, entryPosition);
			if (child != null) {
				entryPosition += 1000;
			}
		}
		sortChildren(parent);
	}

	/**
	 * Sort children of a {@link Page} based on the position defined in the
	 * {@link TLDocExportImportConstants#PROPERTIES_FILE_NAME} file.
	 * 
	 * @param parent
	 *        {@link Page} whose children will be sorted.
	 */
	private void sortChildren(Page parent) {
		List<StructuredElement> children = parent.getChildrenModifiable();
		KBUtils.inTransaction(() -> children.sort(new Comparator<StructuredElement>() {
			@Override
			public int compare(StructuredElement o1, StructuredElement o2) {
				int position1 = getPosition(o1);
				int position2 = getPosition(o2);
				return Integer.compare(position1, position2);
			}

			private int getPosition(StructuredElement structuredElement) {
				Integer result = ((Page) structuredElement).getPosition();
				return result == null ? Integer.MAX_VALUE : result.intValue();
			}
		}));
	}

	/**
	 * Creates a new {@link Page} to import the contents of the {@link Path}.
	 * 
	 * @param log
	 *        Log to write informations and errors to.
	 * @param parent
	 *        The {@link Page} to add the new child {@link Page} to.
	 * @param locale
	 *        {@link Locale} of the imported files. Never null.
	 *        Never null.
	 * @param position
	 *        The default position to use for the created page, if not explicitly specified by page
	 *        properties.
	 * 
	 * @return The newly created {@link Page}.
	 */
	protected Page importPage(Log log, Page parent, Locale locale, String resourcePath, int position) {
		String folderName = FileUtilities.getFilenameOfResource(resourcePath);

		String uuid = null;
		String title = folderName;
		String sourceBundle = null;

		String propertyResourcePath = resourcePath + TLDocExportImportConstants.PROPERTIES_FILE_NAME;
		if (FileManager.getInstance().exists(propertyResourcePath)) {
			try {
				Properties propertiesMap = loadProperties(propertyResourcePath);
				uuid = propertiesMap.getProperty(TLDocExportImportConstants.PROPERTIES_UUID);
				title = propertiesMap.getProperty(TLDocExportImportConstants.PROPERTIES_TITLE);
				sourceBundle = deriveBundleFromFile(propertyResourcePath);
				if (sourceBundle == null) {
					sourceBundle = propertiesMap.getProperty(TLDocExportImportConstants.PROPERTIES_SOURCE_BUNDLE);
				}
				String storedPosition = propertiesMap.getProperty(TLDocExportImportConstants.PROPERTIES_POSITION);
				if (storedPosition != null) {
					position = Integer.parseInt(storedPosition);
				}
			} catch (IOException ex) {
				log.error("Unable to load properties from '" + propertyResourcePath + "'.");
			}
		}

		String contents;
		String contentResourcePath = resourcePath + TLDocExportImportConstants.CONTENT_FILE_NAME;
		if (FileManager.getInstance().exists(contentResourcePath)) {
			try {
				contents = getFileContents(contentResourcePath, StringServices.UTF8);
			} catch (IOException ex) {
				log.error("Failed to read contents of: '" + contentResourcePath + "'", ex);
				contents = null;
			}
		} else {
			contents = null;
		}

		Map<String, BinaryData> images = getImages(log, resourcePath);
		return createPage(parent, locale, folderName, title, uuid, contents, images, position, sourceBundle);
	}

	private String deriveBundleFromFile(String propertiesResource) {
		if (Environment.isDeployed()) {
			return null;
		}

		File propertiesFile = FileManager.getInstance().getIDEFileOrNull(propertiesResource);
		if (propertiesFile == null) {
			// Loaded from a JAR, cannot derive source bundle.
			return null;
		}

		Path path = propertiesFile.toPath();
		int moduleIndex = path.getNameCount() - StringServices.count(propertiesResource, '/') - 4;
		if (moduleIndex < 0) {
			// Safety.
			return null;
		}
		return path.getName(moduleIndex).toString();
	}

	/**
	 * Loads {@link Properties} from the given {@link File}.
	 */
	public static Properties loadProperties(String propertiesResource) throws IOException {
		Properties result = new Properties();
		try (InputStream in = FileManager.getInstance().getData(propertiesResource).getStream()) {
			result.load(in);
		}
		return result;
	}

	/**
	 * The {@link String} contents of a file.
	 * 
	 * @param contentResource
	 *        File to read. Must exist and be a {@link File#isFile() normal file}.
	 * @param encoding
	 *        The encoding for reading text.
	 * 
	 * @return Contents as string.
	 */
	public static String getFileContents(String contentResource, String encoding) throws IOException {
		try (InputStream in = FileManager.getInstance().getData(contentResource).getStream()) {
			return StreamUtilities.readAllFromStream(in, encoding);
		}
	}

	/**
	 * The images within the folders.
	 * 
	 * @param log
	 *        Log to write informations and errors to.
	 * @param rootResourcePath
	 *        Folder to get images from. Never null.
	 * 
	 * @return All images of the folders.
	 */
	private Map<String, BinaryData> getImages(Log log, String rootResourcePath) {
		Set<String> resourcePaths = FileManager.getInstance().getResourcePaths(rootResourcePath);
		if (resourcePaths.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, BinaryData> images = new HashMap<>();
		for (String resourcePath : resourcePaths) {
			String name = FileUtilities.getFilenameOfResource(resourcePath);
			if (FileManager.getInstance().isDirectory(resourcePath)) {
				continue;
			}
			String contentType = MimeTypes.getInstance().getMimeType(name);
			if (contentType != null && !contentType.startsWith(IMAGE_MIME_TYPE)) {
				continue;
			}
			images.put(name, FileManager.getInstance().getData(resourcePath));

		}
		return images;
	}

	/**
	 * Creates a new {@link Page} below the parent {@link Page}.
	 * 
	 * @param parent
	 *        Parent {@link Page} to add the new {@link Page} to. Never null.
	 * @param locale
	 *        {@link Locale} of the new {@link Page}. Never null.
	 * @param id
	 *        Help id of the {@link Page}. Never null.
	 * @param title
	 *        Name of the new {@link Page}.
	 * @param uuid
	 *        UUID of the new {@link Page}. If <code>null</code> a random ID will be generated
	 *        automatically.
	 * @param contents
	 *        Source code of the new {@link Page}.
	 * @param images
	 *        {@link Map} of images of the HTML page.
	 * @param position
	 *        The relative position of the {@link Page} within its siblings.
	 * @param source
	 *        Source where the page was laded from. New value of {@link Page#getImportSource()}.
	 * @return Newly created {@link Page}.
	 */
	protected Page createPage(Page parent, Locale locale, String id, String title, String uuid, String contents,
			Map<String, BinaryData> images, int position, String source) {
		String idStripped = id.strip(); // The id is not allowed to be null.
		String titleStripped = stripNullsafe(title);
		String uuidStripped = stripNullsafe(uuid);
		String contentsStripped = stripNullsafe(contents);
		String sourceStripped = stripNullsafe(source);
		KnowledgeBase kb = parent.tKnowledgeBase();
		KnowledgeItem existingPage = findExistingPage(kb, uuidStripped, idStripped);
		Page page;

		if (sourceStripped == null) {
			return null;
		}

		if (existingPage == null) {
			page = (Page) parent.createChild(idStripped, Page.PAGE_TYPE);
			if (uuidStripped != null) {
				page.setUuid(uuidStripped);
			}
		} else {
			page = existingPage.getWrapper();
			page.setName(idStripped);
		}
		page.setPosition(position);
		page.setImportSource(sourceStripped);
		TLResKeyUtil.updateTranslation(page, Page.TITLE_ATTR, locale, titleStripped);
		I18NStructuredTextUtil.updateStructuredText(page, Page.CONTENT_ATTR, locale, contentsStripped, images);
		return page;
	}

	private KnowledgeItem findExistingPage(KnowledgeBase kb, String uuid, String name) {
		DataObject dbObject;
		if (uuid != null) {
			dbObject = kb.getObjectByAttribute(TLDocFactory.KO_NAME_PAGE, Page.UUID_ATTR, uuid);
		} else if (name != null) {
			dbObject = kb.getObjectByAttribute(TLDocFactory.KO_NAME_PAGE, Page.NAME_ATTR, name);
		} else {
			dbObject = null;
		}
		return (KnowledgeItem) dbObject;
	}
}


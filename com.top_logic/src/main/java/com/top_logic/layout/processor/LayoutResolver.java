/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import static com.top_logic.basic.core.xml.DOMUtil.*;
import static com.top_logic.layout.processor.LayoutModelConstants.*;
import static com.top_logic.layout.processor.LayoutModelUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.mig.html.layout.LayoutUtils;

/**
 * {@link Operation} that allows loading layout definitions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LayoutResolver extends Operation {

	/**
	 * Prefix to layout names that allow resolving global file system references during test.
	 */
	public static final String TEST_SCHEME_PREFIX = "test:";

	private boolean _addAnnotations;

	private boolean _addSourceFile = true;

	private FileManager _fileManager;

	private Theme _theme;

	private Map<BinaryData, ConstantLayout> _templates = new HashMap<>();

	public LayoutResolver(Protocol protocol, Application application, FileManager fileManager, Theme theme) {
		super(protocol, application);

		_fileManager = fileManager;
		_theme = theme;
	}

	/**
	 * @see #getFileManager()
	 */
	public void setFileManager(FileManager fileManager) {
		_fileManager = fileManager;
	}

	public FileManager getFileManager() {
		return _fileManager;
	}

	public boolean getAddAnnotations() {
		return _addAnnotations;
	}

	/**
	 * @see #getAddAnnotations()
	 */
	public void setAddAnnotations(boolean addAnnotations) {
		_addAnnotations = addAnnotations;
	}

	/**
	 * Whether to add the {@link ConfigurationReader#DEFINITION_FILE_ANNOTATION_ATTR} to the
	 * generated files.
	 */
	public void setSourceFile(boolean value) {
		_addSourceFile = value;
	}

	public Theme getTheme() {
		return _theme;
	}

	/**
	 * @see #getTheme()
	 */
	public void setTheme(Theme theme) {
		_theme = theme;
	}

	public Document loadLayout(BinaryData layoutData) throws ResolveFailure {
		Document layoutDocument = loadXML(layoutData);

		if (_addSourceFile) {
			annotateSourceFile(layoutDocument, layoutData);
		}

		if (_addAnnotations) {
			annotateFileVersion(layoutDocument);
		}

		return layoutDocument;
	}

	public Document loadXML(BinaryData layoutData) throws ResolveFailure {
		try (InputStream stream = layoutData.getStream()) {
			return DOMUtil.getDocumentBuilder().parse(stream);
		} catch (IOException ex) {
			throw new ResolveFailure("Layout '" + layoutData.getName() + "' not found.", ex);
		} catch (SAXException ex) {
			throw new ResolveFailure("Layout parse error in '" + layoutData.getName() + "'.", ex);
		}
	}

	private void annotateSourceFile(Document layoutDocument, BinaryData layout) {
		setSourceAnnotation(layoutDocument, layout.getName());
	}

	private void annotateFileVersion(Document layoutDocument) {
		String version = null;
		for (Comment comment : comments(layoutDocument)) {
			String commentText = comment.getNodeValue();

			int revIndex = commentText.indexOf(CVS_REVISION_PREFIX);
			if (revIndex >= 0) {
				int revStart = revIndex + CVS_REVISION_PREFIX.length();
				int revEnd = commentText.indexOf("$", revStart);
				if (revEnd >= 0) {
					version = commentText.substring(revStart, revEnd);
					break;
				}
			}

			int idIndex = commentText.indexOf(CVS_ID_PREFIX);
			if (idIndex >= 0) {
				int idStart = idIndex + CVS_ID_PREFIX.length();
				int idEnd = commentText.indexOf("$", idStart);
				if (idEnd >= 0) {
					String idString = commentText.substring(idStart, idEnd).trim();

					findRevision:
					{
						int firstSpace = idString.indexOf(' ');
						if (firstSpace >= 0) {
							int secondSpace = idString.indexOf(' ', firstSpace + 1);
							if (secondSpace >= 0) {
								version = idString.substring(firstSpace + 1, secondSpace);
								break findRevision;
							}
						}
						version = idString;
					}

					break;
				}
			}
		}

		if (version != null) {
			setVersionAnnotation(layoutDocument, version);
		}
	}

	private void expandThemeVariables(Document layoutDocument) {
		expandThemeVariablesElement(layoutDocument.getDocumentElement());
	}

	private void expandThemeVariablesElement(Element element) {
		NamedNodeMap attributes = element.getAttributes();
		for (int n = 0, cnt = attributes.getLength(); n < cnt; n++) {
			Node attribute = attributes.item(n);
			String value = attribute.getNodeValue();

			if (Theme.isThemeValueReference(value)) {
				String varName = Theme.var(value);
				String themeValue = _theme.getRawValue(varName);
				if (themeValue == null) {
					error("Missing theme variable '" + varName + "' in theme '" + _theme.getName() + "' ("
						+ location(element) + ").");
				} else {
					attribute.setNodeValue(themeValue);
				}
			}
		}

		for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child instanceof Element) {
				expandThemeVariablesElement((Element) child);
			}
		}
	}

	private String location(Node node) {
		return node.getOwnerDocument().getDocumentElement().getAttributeNS(ConfigurationReader.ANNOTATION_NS,
			ConfigurationReader.DEFINITION_FILE_ANNOTATION_ATTR);
	}

	private BinaryData getData(String referencedFileName) {
		return _fileManager.getDataOrNull(referencedFileName);
	}

	/**
	 * Creates a {@link ConstantLayout} that resolves a theme-defined layout definition.
	 */
	public ConstantLayout getLayout(String layoutName) throws ResolveFailure {
		return getLayout(layoutName, resolveThemeLayoutData(LayoutUtils.normalizeLayoutName(layoutName)));
	}

	/**
	 * Creates a {@link ConstantLayout} with the global layout definition resolved in contrast to
	 * the theme-defined one.
	 * 
	 * @param layoutName
	 *        See {@link ConstantLayout#getLayoutName()}.
	 */
	public ConstantLayout getGlobalLayout(String layoutName) throws ResolveFailure {
		BinaryData layoutData = getLayoutData(layoutName, true);
		return new ConstantLayout(this, layoutName, layoutData, loadLayout(layoutData));
	}

	/**
	 * Creates a {@link ConstantLayout} based on the given layout file.
	 * 
	 * @param layoutName
	 *        See {@link LayoutResolver#getLayout(String)}.
	 */
	public ConstantLayout getLayout(String layoutName, BinaryData layoutData) throws ResolveFailure {
		ConstantLayout result = _templates.get(layoutData);
		if (result == null) {
			ConstantLayout newTemplate = new ConstantLayout(this, layoutName, layoutData, loadLayout(layoutData));
			_templates.put(layoutData, newTemplate);
			return newTemplate;
		} else {
			return result;
		}
	}

	public BinaryData getLayoutData(String layoutName, boolean global) throws ResolveFailure {
		try {
			BinaryData result = resolveLayoutData(layoutName, global);
			if (result == null) {
				throw errorLayoutNotFound(layoutName);
			}
			return result;
		} catch (IOException exception) {
			throw errorLayoutCanNotBeLoaded(layoutName, exception);
		}
	}

	public BinaryData resolveLayoutData(String layoutName, boolean global) throws IOException {
		if (layoutName.startsWith(TEST_SCHEME_PREFIX)) {
			return getData(layoutName.substring(TEST_SCHEME_PREFIX.length()));
		}

		if (global) {
			return getGlobalLayoutData(LayoutUtils.normalizeLayoutName(layoutName));
		} else {
			return getThemeOrGlobalLayoutData(_theme, LayoutUtils.normalizeLayoutName(layoutName));
		}
	}

	private BinaryData resolveThemeLayoutData(String layoutName) throws ResolveFailure {
		try {
			BinaryData result = getThemeOrGlobalLayoutData(_theme, layoutName);
			if (result == null) {
				throw errorLayoutNotFound(layoutName);
			}
			return result;
		} catch (IOException exception) {
			throw errorLayoutCanNotBeLoaded(layoutName, exception);
		}
	}

	private BinaryData getThemeOrGlobalLayoutData(Theme theme, String layoutName) throws IOException {
		BinaryData result = getThemeLayoutData(theme, layoutName, new ArrayList<>());

		if (result == null) {
			result = getGlobalLayoutData(layoutName);
		}

		return result;
	}

	private BinaryData getThemeLayoutData(Theme theme, String layoutName, List<Theme> visited) throws IOException {
		BinaryData result = getThemeLayoutData(theme, layoutName);
		if (result == null) {
			List<Theme> parentThemes = theme.getParentThemes();
			if (parentThemes != null) {
				for (Theme parent : parentThemes) {
					if (!visited.contains(parent)) {
						visited.add(parent);
						result = getThemeLayoutData(parent, layoutName, visited);

						if (result != null) {
							return result;
						}
					}
				}
			}
		}
		return result;
	}

	private BinaryData getThemeLayoutData(Theme theme, String layoutName) {
		return getGlobalLayoutData(Theme.LAYOUT_PREFIX + '/' + theme.getThemeID() + '/' + layoutName);
	}

	private BinaryData getGlobalLayoutData(String layoutName) {
		return getData(ModuleLayoutConstants.LAYOUT_RESOURCE_PREFIX + layoutName);
	}

	private ResolveFailure errorLayoutCanNotBeLoaded(String layoutName, IOException ex) {
		return new ResolveFailure("Layout '" + layoutName + "' cannot be loaded.", ex);
	}

	private ResolveFailure errorLayoutNotFound(String layoutName) {
		return new ResolveFailure("Layout '" + layoutName + "' not found.");
	}

	/**
	 * Creates a {@link LayoutResolver} for the {@link RuntimeApplication} and the current theme.
	 * 
	 * @see #newRuntimeResolver(Protocol, Theme)
	 */
	public static LayoutResolver newRuntimeResolver(Protocol protocol) {
		return newRuntimeResolver(protocol, ThemeFactory.getTheme());
	}

	/**
	 * Creates a {@link LayoutResolver} for the {@link RuntimeApplication}.
	 * 
	 * @param protocol
	 *        Value of {@link LayoutResolver#getProtocol()}.
	 * @param theme
	 *        Value of {@link LayoutResolver#getTheme()}.
	 */
	public static LayoutResolver newRuntimeResolver(Protocol protocol, Theme theme) {
		Application application = RuntimeApplication.INSTANCE;
		LayoutResolver resolver = application.createLayoutResolver(protocol, theme);
		return resolver;
	}

}
/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.theme.download;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Charsets;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.gui.MultiThemeFactory;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.gui.config.ThemeSettings;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractDownloadHandler;
import com.top_logic.util.ResourceZipper;
import com.top_logic.util.error.TopLogicException;

/**
 * Handler to download the given theme.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DownloadThemeHandler extends AbstractDownloadHandler {

	private static final String THEME_LAYOUTS_PATH = "/WEB-INF/layouts/themes";

	/**
	 * Prefix for the temporary created zip file.
	 */
	private static final String TEMP_THEME_ZIP_PREFIX_FILENAME = "theme";

	/**
	 * Creates a {@link DownloadThemeHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DownloadThemeHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Object prepareDownload(LayoutComponent component, DefaultProgressInfo progressInfo,
			Map<String, Object> arguments) throws Exception {
		File tempZipFile = createZipTempFile();

		try (ResourceZipper zipper = new ResourceZipper(tempZipFile)) {
			addThemeToZip(getThemeId(component), zipper, progressInfo);
		}

		progressInfo.setFinished(true);

		return tempZipFile;
	}

	private void addThemeToZip(String themeId, ResourceZipper zipper, DefaultProgressInfo progressInfo)
			throws IOException {
		progressInfo.setExpected(4);

		addThemeConfigurationsToZip(themeId, zipper);
		progressInfo.setCurrent(1);

		addThemeStylesToZip(themeId, zipper);
		progressInfo.setCurrent(2);

		addThemeLayoutsToZip(themeId, zipper);
		progressInfo.setCurrent(3);

		addThemeResourcesToZip(themeId, zipper);
		progressInfo.setCurrent(4);
	}

	private void addThemeLayoutsToZip(String themeId, ResourceZipper zipper) throws IOException {
		addContainingFilesToZip(zipper, getThemeLayoutsPath(themeId));
	}

	private String getThemeLayoutsPath(String themeId) {
		return THEME_LAYOUTS_PATH + FileUtilities.PATH_SEPARATOR + themeId;
	}

	private File createZipTempFile() throws IOException {
		return File.createTempFile(TEMP_THEME_ZIP_PREFIX_FILENAME, FileUtilities.ZIP_FILE_ENDING);
	}

	private void addThemeConfigurationsToZip(String themeId, ResourceZipper zipper) throws IOException {
		addThemeConfigToZip(themeId, zipper,
			FileManager.getInstance().getDataOverlays(ThemeUtil.getThemeConfigPath(themeId)));
		addThemeSettingsToZip(themeId, zipper,
			FileManager.getInstance().getDataOverlays(ThemeUtil.getThemeSettingsPath(themeId)));
	}

	private void addThemeResourcesToZip(String themeId, ResourceZipper zipper) throws IOException {
		addContainingFilesToZip(zipper, getThemeResourcesPath(themeId));
	}

	private void addContainingFilesToZip(ResourceZipper zipper, String parent) throws IOException {
		zipper.addAll(FileUtilities.getAllResourcePaths(parent));
	}

	private String getThemeResourcesPath(String themeId) {
		return FileUtilities.PATH_SEPARATOR + Theme.ICONS_DIRECTORY + FileUtilities.PATH_SEPARATOR + themeId
			+ FileUtilities.PATH_SEPARATOR;
	}

	private void addThemeStylesToZip(String themeId, ResourceZipper zipper) throws IOException {
		zipper.addAll(FileUtilities.getAllResourcePaths(ThemeUtil.getThemeStylePath(themeId)));
	}

	private void addThemeSettingsToZip(String themeId, ResourceZipper zipper,
			List<? extends Content> themeDirectories) {
		Optional<ThemeSettings.Config> settingsConfig = ThemeUtil.readThemeSettingsConfiguration(themeDirectories);

		settingsConfig.ifPresent(config -> writeThemeSettingsToZip(themeId, zipper, config));
	}

	private void writeThemeSettingsToZip(String themeId, ResourceZipper zipper, ThemeSettings.Config config) {
		writeStringToZip(zipper, ThemeUtil.getThemeSettingsPath(themeId), getThemeSettingsConfig(config));
	}

	private String getThemeSettingsConfig(ThemeSettings.Config config) {
		return createString(ThemeSettings.TOP_LEVEL_THEME_SETTINGS_CONFIG_TAG_NAME, config);
	}

	private void addThemeConfigToZip(String themeId, ResourceZipper zipper, List<? extends Content> themeConfigs) {
		Optional<ThemeConfig> themeConfig = ThemeUtil.readThemeConfig(themeConfigs);

		themeConfig.ifPresent(
			config -> writeStringToZip(zipper, ThemeUtil.getThemeConfigPath(themeId), getThemeConfig(config)));
	}

	private String getThemeConfig(ThemeConfig config) {
		return createString(MultiThemeFactory.TOP_LEVEL_THEME_CONFIG_TAG_NAME, config);
	}

	private String createString(String rootTagName, ConfigurationItem config) {
		return TypedConfiguration.toString(rootTagName, config, ThemeUtil.THEME_PRINTER_CONFIG);
	}

	private void writeStringToZip(ResourceZipper zipper, String path, String content) {
		try (InputStream targetStream = new ByteArrayInputStream(content.getBytes(Charsets.UTF_8))) {
			zipper.add(targetStream, path);
		} catch (IOException exception) {
			throw new IOError(exception);
		}
	}

	private String getThemeId(LayoutComponent aComponent) {
		TableComponent table = (TableComponent) aComponent;

		ThemeConfig themeConfig = (ThemeConfig) table.getSelected();

		return themeConfig.getId();
	}

	@Override
	public String getDownloadName(LayoutComponent aComponent, Object download) {
		Object selected = getSelectedObject(aComponent);

		if (selected instanceof ThemeConfig) {
			return getThemeDownloadName((ThemeConfig) selected);
		} else {
			throw new TopLogicException(ResKey.text("No theme configuration selected"));
		}
	}

	private Object getSelectedObject(LayoutComponent aComponent) {
		TableComponent table = (TableComponent) aComponent;

		return table.getSelected();
	}

	private String getThemeDownloadName(ThemeConfig themeConfig) {
		return TEMP_THEME_ZIP_PREFIX_FILENAME + "-" + themeConfig.getId() + FileUtilities.ZIP_FILE_ENDING;
	}

	@Override
	public BinaryDataSource getDownloadData(Object zipFile) throws Exception {
		return BinaryDataFactory.createBinaryData((File) zipFile);
	}

	@Override
	public void cleanupDownload(Object model, Object download) {
		((File) download).delete();
	}

}

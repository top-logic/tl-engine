/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.io.File;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutReference;
import com.top_logic.mig.html.layout.LayoutScopeMapper;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.LayoutTemplateCall;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.TLLayout;

/**
 * Exports the in app layout into the filesystem.
 * 
 * @see ExportLayoutCommandHandler
 * @see LayoutScopeMapper
 * @see LayoutTemplateCall
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class LayoutExporter {

	private LayoutScopeMapper _mapper;

	/**
	 * Creates a {@link LayoutExporter} transforming all {@link LayoutReference#getResource()} and
	 * {@link ComponentName} scopes according to the given {@link LayoutScopeMapper mapper}.
	 */
	public LayoutExporter(LayoutScopeMapper mapper) {
		_mapper = mapper;
	}

	/**
	 * Exports the template for the given layout identifier into the filesystem and removes it from the database.
	 */
	public void export(String layoutKey, TLLayout layout) throws ConfigurationException {
		ensureLoadingCompleted();
		exportToFilesystem(_mapper.mapScope(layoutKey), getExportedTemplate(layoutKey, layout));

		LayoutExportUtils.deletePersistentLayoutTemplates(layoutKey);
	}

	/**
	 * Exporting the layouts has to wait until loading them is completed for all themes. Otherwise,
	 * the loading process might see a mixture of old and new layouts, with the new layouts being
	 * written only partially.
	 */
	private void ensureLoadingCompleted() {
		try {
			LayoutStorage.getInstance().awaitEverythingLoaded();
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Thread is being interrupted.", exception);
		}
	}

	private LayoutTemplateCall getExportedTemplate(String layoutKey, TLLayout layout) throws ConfigurationException {
		return new LayoutTemplateCall(layout.getTemplateName(), getMappedTemplateArguments(layout), layoutKey);
	}

	private ConfigurationItem getMappedTemplateArguments(TLLayout layout) throws ConfigurationException {
		ConfigurationItem copiedArguments = TypedConfiguration.copy(layout.getArguments());

		_mapper.map(copiedArguments);

		return copiedArguments;
	}

	private void exportToFilesystem(String relativePath, TLLayout layout) throws ConfigurationException {
		File base = LayoutUtils.getCurrentTopLayoutBaseDirectory();

		if (relativePath != null) {
			removeLayoutOverlayOnFilesystem(base, relativePath);
			removeFromLayoutCache(relativePath);

			writeLayoutOnFilesystem(new File(base, relativePath), layout);
		}
	}

	private void writeLayoutOnFilesystem(File file, TLLayout layout) throws ConfigurationException {
		LayoutTemplateUtils.writeTemplate(file, layout, true);
	}

	private void removeLayoutOverlayOnFilesystem(File base, String relativeLayoutPath) {
		File overlay = new File(base, LayoutUtils.createLayoutOverlayPath(relativeLayoutPath));

		if (overlay.exists()) {
			overlay.delete();
		}
	}

	private void removeFromLayoutCache(String relativeLayoutPath) {
		LayoutStorage.getInstance().removeLayout(ThemeFactory.getTheme(), relativeLayoutPath);
	}

}

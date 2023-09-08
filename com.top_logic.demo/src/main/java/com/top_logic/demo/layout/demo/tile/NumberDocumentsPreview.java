/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.tile;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Named;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.common.folder.FolderDefinition;
import com.top_logic.knowledge.gui.layout.webfolder.WebFolderAware;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.LabelBasedPreview;

/**
 * {@link LabelBasedPreview} displaying the number folders and documents in a {@link WebFolder}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NumberDocumentsPreview extends LabelBasedPreview<NumberDocumentsPreview.Config> {

	/**
	 * Configuration of the {@link NumberDocumentsPreview}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends LabelBasedPreview.Config<NumberDocumentsPreview> {

		// No special properties here

	}

	/**
	 * Creates a new {@link NumberDocumentsPreview}.
	 */
	public NumberDocumentsPreview(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HTMLFragment descriptionContent(ComponentTile tile) {
		LayoutComponent businessComponent = tile.getTileComponent();
		WebFolder webFolder = getWebFolder(businessComponent);
		if (webFolder == null) {
			return Fragments.empty();
		}
		Collection<Named> contents = webFolder.getContents();
		AtomicInteger numberFolders = new AtomicInteger(0);
		contents.forEach(named -> {
			if (named instanceof FolderDefinition) {
				numberFolders.incrementAndGet();
			}
		});
		int folders = numberFolders.intValue();
		int documents = contents.size() - folders;
		return message(I18NConstants.NUMBER_CONTENT__FOLDERS__FILES.fill(folders, documents));

	}

	private WebFolder getWebFolder(LayoutComponent businessComponent) {
		AtomicReference<WebFolder> webFolder = new AtomicReference<>(null);
		businessComponent.acceptVisitorRecursively(new DefaultDescendingLayoutVisitor() {

			@Override
			public boolean visitLayoutComponent(LayoutComponent aComponent) {
				if (aComponent instanceof WebFolderAware) {
					webFolder.set(((WebFolderAware) aComponent).getWebFolder());
					return false;
				}
				return true;
			}

		});
		return webFolder.get();
	}

}


/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.*;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.layout.form.model.FolderField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.tree.model.TLTreeModel;

/**
 * Displays a {@link WebFolder} just by the {@link WebFolderTableUtil#getWebFolderCount(Object)
 * number} of entries.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class WebFolderSizeControl extends ConstantControl<FolderField> {

	/**
	 * The {@link ControlProvider} for the {@link WebFolderSizeControl}.
	 * <p>
	 * The model has to be a {@link FolderField}, built from a {@link WebFolder}.
	 * </p>
	 */
	public static class CP implements ControlProvider {

		@Override
		public Control createControl(Object model, String style) {
			return new WebFolderSizeControl((FolderField) model);
		}

	}

	/** Creates a {@link WebFolderSizeControl}. */
	public WebFolderSizeControl(FolderField model) {
		super(model);
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		int size = nonNull(getWebFolder().getContent()).size();
		WebFolderAttributeRenderer.writeCount(size, out);
	}

	private WebFolder getWebFolder() {
		return (WebFolder) getTreeModel().getBusinessObject(getTreeModel().getRoot());
	}

	@SuppressWarnings("unchecked")
	private <N> TLTreeModel<N> getTreeModel() {
		return getModel().getFolderData().getTreeModel();
	}

}

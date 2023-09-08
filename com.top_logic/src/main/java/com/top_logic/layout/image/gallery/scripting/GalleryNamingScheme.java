/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery.scripting;

import com.top_logic.layout.image.gallery.GalleryModel;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.table.control.FilterFormOwner;

/**
 * {@link ModelNamingScheme} for {@link GalleryDialogHandler}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class GalleryNamingScheme extends AbstractModelNamingScheme<GalleryDialogHandler, GalleryNamingScheme.Name> {

	/**
	 * {@link ModelName} for {@link GalleryDialogHandler}.
	 */
	public interface Name extends ModelName {

		/**
		 * The context gallery.
		 */
		ModelName getGalleryName();

		/**
		 * @see FilterFormOwner#getTableData()
		 */
		void setGalleryName(ModelName modelName);

	}

	@Override
	public Class<GalleryNamingScheme.Name> getNameClass() {
		return GalleryNamingScheme.Name.class;
	}

	@Override
	public Class<GalleryDialogHandler> getModelClass() {
		return GalleryDialogHandler.class;
	}

	@Override
	protected void initName(GalleryNamingScheme.Name name, GalleryDialogHandler model) {
		name.setGalleryName(model.getGalleryModel().getModelName());
	}

	@Override
	public GalleryDialogHandler locateModel(ActionContext context, GalleryNamingScheme.Name name) {
		GalleryModel galleryModel = (GalleryModel) ModelResolver.locateModel(context, name.getGalleryName());
		return galleryModel.get(GalleryDialogHandler.GALLERY_FORM_OWNER);
	}
}
/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery.scripting;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.image.gallery.GalleryModel;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;

/**
 * {@link FormHandler} for holding information of gallery management dialog.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class GalleryDialogHandler implements FormHandler {

	static final Property<GalleryDialogHandler> GALLERY_FORM_OWNER =
		TypedAnnotatable.property(GalleryDialogHandler.class, "galleryFormOwner");

	private FormContext _galleryDialogContext;
	private GalleryModel _galleryModel;

	/**
	 * Create a new {@link GalleryDialogHandler}.
	 */
	public GalleryDialogHandler(GalleryModel galleryModel, FormContext galleryDialogContext) {
		_galleryDialogContext = galleryDialogContext;
		_galleryModel = galleryModel;
	}

	@Override
	public ModelName getModelName() {
		return ModelResolver.buildModelName(this);
	}

	@Override
	public FormContext getFormContext() {
		return _galleryDialogContext;
	}

	@Override
	public boolean hasFormContext() {
		return true;
	}

	@Override
	public Command getApplyClosure() {
		return null;
	}

	@Override
	public Command getDiscardClosure() {
		return null;
	}

	GalleryModel getGalleryModel() {
		return _galleryModel;
	}

	/**
	 * Adds this {@link GalleryDialogHandler} to the underlying image gallery, so that it
	 * can be found in scripted tests.
	 */
	public void registerToGalleryModel() {
		_galleryModel.set(GALLERY_FORM_OWNER, this);
	}

	/**
	 * @see #registerToGalleryModel()
	 */
	public void deregisterFromGalleryModel() {
		_galleryModel.reset(GALLERY_FORM_OWNER);
	}
}

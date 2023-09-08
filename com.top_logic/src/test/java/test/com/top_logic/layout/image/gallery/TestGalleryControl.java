/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.image.gallery;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.TestControl;

import com.top_logic.basic.io.binary.EmptyBinaryData;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.GalleryField;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.image.gallery.GalleryControl;
import com.top_logic.layout.image.gallery.TransientGalleryImage;

/**
 * Test for the {@link GalleryControl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestGalleryControl extends TestControl {

	public void testImageListenerDeRegistration() {
		GalleryField galleryField = FormFactory.newGalleryField("testField");
		GalleryControl galleryControl =
			(GalleryControl) DefaultFormFieldControlProvider.INSTANCE.createControl(galleryField);
		writeControl(galleryControl);
		galleryField.setVisible(false);
		assertTrue(galleryControl.isRepaintRequested());
		try {
			galleryField.setImages(list(new TransientGalleryImage(EmptyBinaryData.INSTANCE)));
		} catch (AssertionError err) {
			fail("Ticket #23166: Control must not add update when it is already invalid.", err);
		}
		assertTrue(galleryControl.isRepaintRequested());
	}

	public void testModeListenerDeRegistration() {
		GalleryField galleryField = FormFactory.newGalleryField("testField");
		GalleryControl galleryControl =
			(GalleryControl) DefaultFormFieldControlProvider.INSTANCE.createControl(galleryField);
		writeControl(galleryControl);
		galleryField.setVisible(false);
		assertTrue(galleryControl.isRepaintRequested());
		try {
			// Set visible again to ensure setting immutable fires event.
			galleryField.setVisible(true);
			galleryField.setImmutable(true);
		} catch (AssertionError err) {
			fail("Ticket #23166: Control must not add update when it is already invalid.", err);
		}
		assertTrue(galleryControl.isRepaintRequested());
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestGalleryControl}.
	 */
	public static Test suite() {
		return suite(ServiceTestSetup.createSetup(TestGalleryControl.class, MimeTypes.Module.INSTANCE));
	}

}

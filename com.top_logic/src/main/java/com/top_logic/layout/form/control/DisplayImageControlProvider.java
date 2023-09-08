/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.awt.Dimension;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.image.gallery.Icons;

/**
 * {@link ControlProvider} creatinge a {@link DisplayImageControl} for a {@link FormField}. It is
 * expected that the model of the {@link FormField} is either <code>null</code> or a
 * {@link BinaryData} representing an image.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DisplayImageControlProvider implements ControlProvider {

	final Dimension _dimension;

	private BinaryData _noImage;

	/**
	 * Creates a new {@link DisplayImageControlProvider}.
	 * 
	 * @param dimension
	 *        Dimension of the displayed image.
	 */
	public DisplayImageControlProvider(Dimension dimension) {
		_dimension = dimension;
		_noImage = newNoImage();
	}

	private BinaryData newNoImage() {
		String imageKey = Icons.NO_PREVIEW_IMAGE.get().getImagePath();
		String themedPath = ThemeFactory.getTheme().getFileLink(imageKey);
		BinaryData noImageFile = FileManager.getInstance().getData(themedPath);
		return noImageFile;
	}

	@Override
	public Control createControl(Object model, String style) {
		DisplayImageControl imageControl = new DisplayImageControl(_noImage, _dimension);
		ensureValueUpdate(imageControl, (FormField) model);
		return imageControl;
	}

	private void ensureValueUpdate(final DisplayImageControl imageControl, final FormField field) {
		final ValueListener updater = new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				setImage(imageControl, newValue);
			}
		};

		imageControl.addListener(AbstractControlBase.ATTACHED_PROPERTY, new AttachedPropertyListener() {

			@Override
			public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
				if (newValue.booleanValue()) {
					field.addValueListener(updater);
					if (field.hasValue()) {
						setImage(imageControl, field.getValue());
					}
				} else {
					field.removeValueListener(updater);
				}
			}
		});
	}

	void setImage(final DisplayImageControl imageControl, Object newValue) {
		BinaryData content;
		if (newValue != null) {
			content = BinaryData.cast(newValue);
		} else {
			content = _noImage;
		}
		imageControl.setDataItem(content, _dimension);
	}
}

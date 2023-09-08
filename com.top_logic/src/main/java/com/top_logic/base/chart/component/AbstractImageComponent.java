/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.component;

import com.top_logic.base.chart.ImageComponent;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.structure.ContentLayouting;
import com.top_logic.layout.structure.LayoutControlProvider.Layouting;

/**
 * Abstract implementation of {@link ImageComponent} based on a {@link FormComponent}.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractImageComponent extends FormComponent implements ImageComponent {

	/**
	 * Configuration of an {@link AbstractImageComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends FormComponent.Config {

		/** Name of the {@link #getWriteImageToTempFile()} property. */
		String WRITE_IMAGE_TO_TEMP_FILE_NAME = "write-image-to-temp-file";

		/**
		 * Whether the image created by this {@link ImageComponent} should be written to a temporary
		 * file.
		 */
		@Name(WRITE_IMAGE_TO_TEMP_FILE_NAME)
		Decision getWriteImageToTempFile();

		@Override
		@InstanceDefault(ContentLayouting.class)
		Layouting getContentLayouting();

	}

	private final boolean _writeToTempFile;

	/**
	 * Creates a new {@link AbstractImageComponent}.
	 */
	public AbstractImageComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_writeToTempFile = config.getWriteImageToTempFile().toBoolean(false);
	}

	/**
	 * Whether the created images should be created by writing the data into a temp file and reading
	 * from this file. This may be useful when the image is extremely large such that the image can
	 * not hold in memory a long time.
	 */
	protected boolean isWriteToTempFile() {
		return _writeToTempFile;
	}

}


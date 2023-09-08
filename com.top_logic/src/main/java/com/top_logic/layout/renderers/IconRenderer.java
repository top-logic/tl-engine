/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.renderers;

import java.awt.Dimension;
import java.io.IOException;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.control.DisplayImageControl;

/**
 * {@link Renderer} rendering an {@link BinaryData}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IconRenderer extends AbstractConfiguredInstance<IconRenderer.Config> implements Renderer<BinaryData> {

	/**
	 * Configuration of an {@link IconRenderer}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<IconRenderer> {

		/**
		 * The width of the icon (in {@link DisplayUnit#PIXEL}).
		 */
		@Mandatory
		int getWidth();

		/**
		 * The height of the icon (in {@link DisplayUnit#PIXEL}).
		 */
		@Mandatory
		int getHeight();
	}

	/**
	 * Creates a new {@link IconRenderer} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link IconRenderer}.
	 */
	public IconRenderer(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void write(DisplayContext context, TagWriter out, BinaryData value) throws IOException {
		if (value == null) {
			return;
		}
		Dimension dimension = new Dimension(getConfig().getWidth(), getConfig().getHeight());
		new DisplayImageControl(value, dimension).write(context, out);
	}

}


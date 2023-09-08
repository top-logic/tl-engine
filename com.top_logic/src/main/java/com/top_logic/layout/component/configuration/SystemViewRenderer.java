/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.layout.CompositeControl;
import com.top_logic.layout.View;
import com.top_logic.layout.form.control.DefaultSimpleCompositeControlRenderer;

/**
 * Renderer for an {@link CompositeControl} that contains {@link View}s to be displayed in the
 * system area of the application.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SystemViewRenderer extends DefaultSimpleCompositeControlRenderer
		implements ConfiguredInstance<SystemViewRenderer.Config> {

	public interface Config extends PolymorphicConfiguration<SystemViewRenderer> {
		@MapBinding
		Map<String, String> getAttributes();
	}

	/** CSS-class of the DIV tag for a system message. */
	protected static final String SYSTEM_MESSAGE_DIV = "system_message";

	private Config _config;

	/**
	 * Creates a new {@link SystemViewRenderer}.
	 * 
	 * @param config
	 *        Configuration of this {@link SystemViewRenderer}.
	 */
	@CalledByReflection
	public SystemViewRenderer(InstantiationContext context, Config config) {
		super(DIV, createAttributesMap(config));
		_config = config;
	}

	private static Map<String, String> createAttributesMap(Config config) {
		HashMap<String, String> attributes = new HashMap<>();
		attributes.put(CLASS_ATTR, SYSTEM_MESSAGE_DIV);
		attributes.putAll(config.getAttributes());
		return attributes;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

}


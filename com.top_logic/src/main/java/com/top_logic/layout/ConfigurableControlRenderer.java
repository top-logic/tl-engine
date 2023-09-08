/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * {@link DefaultControlRenderer} that has a configuration.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ConfigurableControlRenderer<T extends Control, C extends ConfigurableControlRenderer.Config<?>>
		extends DefaultControlRenderer<T> implements ConfiguredInstance<C> {

	/**
	 * Typed configuration interface definition for {@link ConfigurableControlRenderer}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config<I extends ConfigurableControlRenderer<?, ?>> extends PolymorphicConfiguration<I> {

		/** @see #getControlTag() */
		String CONTROL_TAG = "controlTag";

		/** @see #getControlClass() */
		String CONTROL_CLASS = "controlClass";

		/** Name of the root tag of the {@link Control}. */
		@Name(CONTROL_TAG)
		@StringDefault(DIV)
		String getControlTag();

		/**
		 * @see #getControlTag()
		 */
		void setControlTag(String tag);

		/** CSS class(es) to add to the top-level {@link Control} tag. */
		@Name(CONTROL_CLASS)
		@Nullable
		String getControlClass();

		/**
		 * @see #getControlTag()
		 */
		void setControlClass(String tag);

	}

	@Inspectable
	private C _config;

	private final String _controlTag;

	private String _controlClass;

	/**
	 * Create a {@link ConfigurableControlRenderer}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ConfigurableControlRenderer(InstantiationContext context, C config) {
		_config = config;
		_controlTag = config.getControlTag();
		_controlClass = config.getControlClass();
	}

	@Override
	public C getConfig() {
		return _config;
	}

	@Override
	protected final String getControlTag(T control) {
		return _controlTag;
	}

	@Override
	public void appendControlCSSClasses(Appendable out, T control) throws IOException {
		super.appendControlCSSClasses(out, control);

		if (_controlClass != null) {
			out.append(_controlClass);
		}
	}

}


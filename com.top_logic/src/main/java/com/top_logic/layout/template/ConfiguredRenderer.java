/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.template;

import java.io.IOException;
import java.util.Collections;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.template.Eval;
import com.top_logic.basic.config.template.TemplateExpression;
import com.top_logic.basic.config.template.TemplateExpression.VariableAccess;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.control.AbstractButtonControl;
import com.top_logic.util.TLContext;

/**
 * {@link Renderer} using a {@link TemplateExpression} to
 * {@link #write(DisplayContext, TagWriter, Object) render} the displayed object.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfiguredRenderer<T, C extends ConfiguredRenderer.Config<?>> extends AbstractConfiguredInstance<C>
		implements Renderer<T> {

	/**
	 * Common variable name to access the current user model object from within a
	 * {@link ConfiguredRenderer} template.
	 */
	public static final String USER = "user";

	/**
	 * Configuration options for {@link ConfiguredRenderer}.
	 */
	public interface Config<I extends ConfiguredRenderer<?, ?>> extends PolymorphicConfiguration<I> {

		/**
		 * The template for rendering the {@link AbstractButtonControl}.
		 */
		TemplateExpression getTemplate();

	}

	/**
	 * Creates a {@link ConfiguredRenderer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfiguredRenderer(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public void write(DisplayContext context, TagWriter out, T value) throws IOException {
		render(context, out, getConfig().getTemplate(), value);
	}

	/**
	 * Renders a {@link TemplateExpression} in the context of a given value (this object).
	 *
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The rendering output.
	 * @param template
	 *        The {@link TemplateExpression} to expand.
	 * @param value
	 *        The model for the expansion (this object).
	 * @throws IOException
	 *         If writing fails.
	 */
	protected static <T> void render(DisplayContext context, TagWriter out, TemplateExpression template, T value)
			throws IOException {
		template.visit(new TemplateWriter(context, out),
			new Eval.EvalContext(value, Collections.emptyMap()) {
				@Override
				public Object getVariableValue(VariableAccess variable) {
					String variableName = variable.getVariableName();
					if (USER.equals(variableName)) {
						return TLContext.getContext().getCurrentPersonWrapper();
					}

					return super.getVariableValue(variable);
				}
			});
	}

}

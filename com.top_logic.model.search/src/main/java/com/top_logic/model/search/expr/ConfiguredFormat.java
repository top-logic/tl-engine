/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.text.Format;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.format.FormatDefinition;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;

/**
 * A {@link GenericMethod} implementation that creates and initializes format instances via their
 * configuration ID.
 *
 *
 * @author <a href="mailto:Jonathan.Hüsing@top-logic.com">Jonathan Hüsing</a>
 */
public class ConfiguredFormat extends GenericMethod {

	/**
	 * Creates a new {@link ConfiguredFormat}.
	 *
	 */
	protected ConfiguredFormat(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ConfiguredFormat(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	/**
	 * retrieves a format instance from the formatter service
	 * 
	 * @param arguments
	 *        args[0]: configured format ID
	 * @param definitions
	 *        evaluation context
	 * @return configured format instance or null if ID invalid/empty
	 */
	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		String configuredFormatId = asString(arguments[0]);
		if (configuredFormatId == null || configuredFormatId.isEmpty()) {
			return null;
		}

		FormatterService formatterService = FormatterService.getInstance();
		FormatDefinition<?> formatDefinition = formatterService.getFormatDefinition(configuredFormatId);

		if (formatDefinition == null) {
			// FormatDefinition with given configuredFormatId does not exist
			return null;
		}

		Format formatter = formatDefinition.newFormat(formatterService.getConfig(), ThreadContext.getTimeZone(),
			ThreadContext.getLocale());

		return formatter;
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link ConfiguredFormat} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<ConfiguredFormat> {

		/** Description of parameters for a {@link ConfiguredFormat}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("id")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public ConfiguredFormat build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new ConfiguredFormat(getConfig().getName(), args);
		}

	}

}

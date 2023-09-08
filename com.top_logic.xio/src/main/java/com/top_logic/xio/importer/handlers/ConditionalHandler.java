/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.xml.WrappedXMLStreamException;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * A case block in a {@link SwitchImportHandler}.
 */
public class ConditionalHandler<C extends ConditionalHandler.Config<?>> extends ConfiguredImportHandler<C> {

	/**
	 * Configuration options for {@link ConditionalHandler}.
	 */
	@TagName("if")
	public interface Config<I extends ConditionalHandler<?>>
			extends ConfiguredImportHandler.Config<I> {

		/**
		 * The predicate (function evaluating to a boolean expression) to evaluate on the given
		 * {@link #getArguments()}.
		 */
		Expr getPredicate();

		/**
		 * Names of variables that should be passed as arguments to the {@link #getPredicate()}.
		 */
		@Format(CommaSeparatedStrings.class)
		@FormattedDefault(ImportContext.THIS_VAR)
		List<String> getArguments();

		/**
		 * List of {@link Handler}s to execute, if the {@link #getPredicate()} evaluates to
		 * <code>true</code>.
		 */
		@DefaultContainer
		List<PolymorphicConfiguration<? extends Handler>> getContents();
	}

	private List<Handler> _contents;

	/**
	 * Creates a {@link ConditionalHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConditionalHandler(InstantiationContext context, C config) {
		super(context, config);

		_contents = TypedConfiguration.getInstanceList(context, config.getContents());
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		execIf(context, in, () -> {
			// Nothing else.
		});
		return null;
	}

	/**
	 * Performs the {@link #importXml(ImportContext, XMLStreamReader) import} if the condition
	 * holds, calls the given elseContinuation otherwise.
	 */
	protected void execIf(ImportContext context, XMLStreamReader in, Runnable elseContinuation)
			throws WrappedXMLStreamException {
		List<Object> values = getVars(context, getConfig().getArguments());
		
		derefAll(context, values, arguments -> {
			try {
				boolean test = (Boolean) context.eval(getConfig().getPredicate(), arguments.toArray());
				if (test) {
					for (Handler handler : _contents) {
						handler.importXml(context, in);
					}
				} else {
					elseContinuation.run();
				}
			} catch (XMLStreamException ex) {
				throw new WrappedXMLStreamException(ex);
			}
		});
	}

	private List<Object> getVars(ImportContext context, List<String> arguments) {
		List<Object> args = new ArrayList<>(arguments.size());
		for (String name : arguments) {
			args.add(context.getVar(name));
		}
		return args;
	}

	private void derefAll(ImportContext context, List<Object> values, Consumer<List<Object>> continuation) {
		derefAll(context, values, 0, continuation);
	}

	private void derefAll(ImportContext context, List<Object> values, int index,
			Consumer<List<Object>> continuation) {
		if (index == values.size()) {
			continuation.accept(values);
		} else {
			context.deref(values.get(index), r -> {
				values.set(index, r);
				derefAll(context, values, index + 1, continuation);
			});
		}
	}

}
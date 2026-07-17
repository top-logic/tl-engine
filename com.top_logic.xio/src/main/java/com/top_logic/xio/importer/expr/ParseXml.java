/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.expr;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.i18n.log.BufferingI18NLog;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.logging.Level;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;
import com.top_logic.xio.importer.XmlImporter;
import com.top_logic.xio.importer.binding.ApplicationModelBinding;
import com.top_logic.xio.importer.binding.ModelBinding;
import com.top_logic.xio.importer.binding.TransientModelBinding;
import com.top_logic.xio.importer.handlers.ConfiguredImportHandler;
import com.top_logic.xio.importer.handlers.DispatchingImporter;
import com.top_logic.xio.importer.handlers.Handler;

/**
 * {@link GenericMethod} parsing an XML stream into an object graph using a configured
 * {@link XmlImporter} declaration.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ParseXml extends GenericMethod {

	private final Handler _importDefinition;

	/**
	 * Creates a {@link ParseXml}.
	 *
	 * @param importDefinition
	 *        The top-level import {@link Handler} describing how to interpret the XML input.
	 */
	protected ParseXml(String name, SearchExpression[] arguments, Handler importDefinition) {
		super(name, arguments);
		_importDefinition = importDefinition;
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new ParseXml(getName(), arguments, _importDefinition);
	}

	@Override
	public Object getId() {
		return getName();
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object data = arguments[0];
		if (data == null) {
			return null;
		}
		if (!(data instanceof BinaryDataSource)) {
			throw new TopLogicException(I18NConstants.ERROR_NO_BINARY_INPUT__VALUE_EXPR.fill(data, this));
		}
		BinaryDataSource input = (BinaryDataSource) data;

		Object context = arguments[1];
		boolean transientImport = asBoolean(arguments[2]);
		boolean logCreations = asBoolean(arguments[3]);

		BufferingI18NLog errors = new BufferingI18NLog();
		I18NLog log = errors.filter(Level.ERROR).tee(
			new LogProtocol(ParseXml.class)
				.asI18NLog(Resources.getSystemInstance())
				.filter(Level.FATAL));

		XmlImporter importer = XmlImporter.newInstance(log, _importDefinition);
		importer.setLogCreations(logCreations);

		ModelBinding binding = transientImport
			? new TransientModelBinding(ModelService.getApplicationModel())
			: new ApplicationModelBinding(PersistencyLayer.getKnowledgeBase(), ModelService.getApplicationModel());

		Object result;
		try (InputStream in = input.toData().getStream()) {
			result = importer.importModel(binding, new StreamSource(in), context);
		} catch (XMLStreamException | IOException ex) {
			throw new TopLogicException(I18NConstants.ERROR_IMPORT_FAILED__EXPR.fill(this), ex);
		}

		if (errors.hasErrors()) {
			throw errors.asException(I18NConstants.ERROR_IMPORT_FAILED__EXPR.fill(this));
		}

		return result;
	}

	/**
	 * {@link MethodBuilder} creating a {@link ParseXml} function from a configured
	 * {@link XmlImporter} declaration.
	 */
	public static class Builder<C extends ParseXml.Builder.Config<?>> extends AbstractMethodBuilder<C, ParseXml> {

		/**
		 * Configuration options for {@link ParseXml.Builder}.
		 */
		public interface Config<I extends ParseXml.Builder<?>> extends AbstractMethodBuilder.Config<I> {

			/**
			 * The import declaration describing how to interpret the parsed XML.
			 *
			 * @implNote The value is the XML serialization of an import {@link DispatchingImporter}
			 *           configuration.
			 */
			@ItemDefault(DispatchingImporter.Config.class)
			@NonNullable
			@DefaultContainer
			ConfiguredImportHandler.Config<?> getHandler();

		}

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("data")
			.optional("context")
			.optional("transient", false)
			.optional("logCreations", true)
			.build();

		private final Handler _importDefinition;

		/**
		 * Creates a {@link Builder}.
		 */
		@CalledByReflection
		public Builder(InstantiationContext context, C config) {
			super(context, config);
			_importDefinition = context.getInstance(config.getHandler());
		}

		@Override
		public ParseXml build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new ParseXml(getName(), args, _importDefinition);
		}

		@Override
		public Object getId() {
			return getName();
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

	}

}

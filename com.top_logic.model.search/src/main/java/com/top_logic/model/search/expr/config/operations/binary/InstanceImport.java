/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.i18n.log.BufferingI18NLog;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.instance.importer.XMLInstanceImporter;
import com.top_logic.model.instance.importer.schema.ObjectsConf;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Parses a {@link BinaryContent} as XML instance bundle and allocates and returns the objects
 * defined in the XML file.
 * 
 * @see XMLInstanceImporter
 */
public class InstanceImport extends GenericMethod {

	/**
	 * Creates a {@link InstanceImport}.
	 */
	protected InstanceImport(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new InstanceImport(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		BinaryContent xmlData = (BinaryContent) arguments[0];
		boolean allocateTransient = asBoolean(arguments[1]);
		Object context = arguments[2];

		try {
			ObjectsConf objects = XMLInstanceImporter.loadConfig(xmlData);

			XMLInstanceImporter importer =
				new XMLInstanceImporter(ModelService.getInstance().getModel(),
					allocateTransient ? TransientObjectFactory.INSTANCE : ModelService.getInstance().getFactory());
			importer.setContext(context);

			BufferingI18NLog log = new BufferingI18NLog();
			importer.setLog(log);

			List<TLObject> result = importer.importInstances(objects);

			if (log.hasErrors()) {
				throw log.asException(I18NConstants.INSTANCE_IMPORT_FAILED__FILE.fill(xmlData.getName()));
			} else {
				log.forwardToApplicationLog(InstanceImport.class);
			}

			return result;
		} catch (ConfigurationException ex) {
			throw new TopLogicException(
				I18NConstants.INSTANCE_IMPORT_FAILED__FILE_MSG.fill(xmlData.getName(), ex.getErrorKey()), ex);
		}
	}

	/**
	 * {@link MethodBuilder} creating {@link InstanceImport} expressions.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<InstanceImport> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("xml")
			.optional("transient", false)
			.optional("context")
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
		public InstanceImport build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new InstanceImport(getConfig().getName(), args);
		}
	}

}

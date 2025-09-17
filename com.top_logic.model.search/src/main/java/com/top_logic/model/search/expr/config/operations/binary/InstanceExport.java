/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.instance.exporter.XMLInstanceExporter;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Serializes a set of objects to XML for later re-import.
 * 
 * @see XMLInstanceExporter
 */
public class InstanceExport extends GenericMethod {

	/**
	 * Creates a {@link InstanceExport}.
	 */
	protected InstanceExport(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new InstanceExport(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Collection<?> objects = asCollection(arguments[0]);
		String downloadName = asString(arguments[1], null);
		Collection<?> excludes = asCollection(arguments[2]);
		Collection<?> includes = asCollection(arguments[3]);

		XMLInstanceExporter exporter = new XMLInstanceExporter();
		for (Object modelPart : excludes) {
			exporter.addExclude((TLModelPart) modelPart);
		}
		for (Object modelPart : includes) {
			exporter.addInclude((TLModelPart) modelPart);
		}
		return exporter.exportInstances(downloadName, CollectionUtil.dynamicCastView(TLObject.class, objects));
	}

	/**
	 * {@link MethodBuilder} creating {@link InstanceExport} expressions.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<InstanceExport> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("input")
			.optional("name")
			.optional("excludes")
			.optional("includes")
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
		public InstanceExport build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new InstanceExport(getConfig().getName(), args);
		}
	}

}

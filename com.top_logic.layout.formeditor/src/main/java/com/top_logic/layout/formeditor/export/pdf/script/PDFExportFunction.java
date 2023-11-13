/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.export.pdf.script;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.formeditor.definition.PDFExportAnnotation;
import com.top_logic.layout.formeditor.export.pdf.PDFData;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLType;
import com.top_logic.model.builtin.TLCore;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.util.model.ModelService;

/**
 * TLScript function generating a PDF from an object.
 * 
 * <p>
 * The function uses the export definition for the object to render the object to PDF.
 * </p>
 * 
 * @see PDFExportAnnotation
 */
public class PDFExportFunction extends GenericMethod {

	/**
	 * Creates a {@link PDFExportFunction}.
	 */
	protected PDFExportFunction(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new PDFExportFunction(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return TLCore.getPrimitiveType(ModelService.getInstance().getModel(), Kind.BINARY);
	}

	@Override
	protected Object eval(Object self, Object[] arguments, EvalContext definitions) {
		TLObject exportObject = asTLObject(arguments[0]);
		if (exportObject == null) {
			return null;
		}
		String name = asString(arguments[1]);

		return new PDFData(name, exportObject);
	}

	/**
	 * {@link MethodBuilder} creating {@link PDFExportFunction}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<PDFExportFunction> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("dataObject")
			.optional("name", "export.pdf")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public PDFExportFunction build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			return new PDFExportFunction(getName(), self, args);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

	}

}

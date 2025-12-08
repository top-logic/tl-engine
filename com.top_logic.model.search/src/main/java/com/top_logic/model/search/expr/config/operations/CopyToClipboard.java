/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link GenericMethod} copying a String or Mimetype to the Clipboard.
 * 
 * @author <a href="mailto:simon.haneke@top-logic.com">Simon Haneke</a>
 */
public class CopyToClipboard extends GenericMethod {

	/**
	 * Creates a {@link CopyToClipboard} expression.
	 */
	protected CopyToClipboard(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new CopyToClipboard(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable data;
		data = new StringSelection((String) arguments[0]);
		cb.setContents(data, null);

		return null;
	}

	@Override
	public boolean isSideEffectFree() {
		return true;
	}


	/**
	 * {@link AbstractSimpleMethodBuilder} creating an {@link CopyToClipboard} function.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<CopyToClipboard> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("toCopy")
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
		public CopyToClipboard build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new CopyToClipboard(getConfig().getName(), args);
		}
	}
}

/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.rewrite;

import org.w3c.dom.Document;

import com.top_logic.layout.tools.LayoutRewrite;

/**
 * {@link LayoutRewrite} applying a configurable {@link DocumentRewrite} implementation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LayoutUpgrade extends LayoutRewrite {

	/**
	 * The option to pass a {@link DocumentRewrite} class to the tool.
	 */
	public static final String REWRITE_OPT = "rewrite";

	/**
	 * Main entry to the {@link LayoutUpgrade} tool.
	 */
	public static void main(String[] args) throws Exception {
		new LayoutUpgrade().runMain(args);
	}

	private DocumentRewrite _impl;

	/**
	 * Creates a {@link LayoutUpgrade}.
	 *
	 */
	public LayoutUpgrade() {
		super();
	}

	@Override
	protected void showHelpOptions() {
		super.showHelpOptions();
		info("\t--" + REWRITE_OPT + " <class name>       Class of DocumentRewrite to use.");
	}

	@Override
	protected int longOption(String option, String[] args, int i) {
		if (option.equals(REWRITE_OPT)) {
			try {
				String className = args[i++];
				Class<?> rewriteClass = Class.forName(className);
				_impl = (DocumentRewrite) rewriteClass.newInstance();
				_impl.init(this);
			} catch (IllegalAccessException | InstantiationException | ClassNotFoundException | ClassCastException ex) {
				throw new IllegalArgumentException("Cannot instantiate rewriter.", ex);
			}
		} else {
			return super.longOption(option, args, i);
		}
		return i;
	}

	@Override
	protected boolean process(Document layout) {
		if (_impl == null) {
			throw new IllegalStateException("Missing " + REWRITE_OPT + " argument.");
		}
		_impl.rewrite(layout);
		return true;
	}

}

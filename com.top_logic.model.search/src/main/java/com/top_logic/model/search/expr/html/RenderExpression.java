/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.html;

import java.io.IOError;
import java.io.IOException;
import java.util.Collection;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.mig.html.layout.tag.JSPErrorUtil;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.Args;

/**
 * Base class for {@link SearchExpression}s that produce page output as side-effect to their
 * {@link #evalWith(EvalContext, Args) evaluation}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class RenderExpression extends SearchExpression {

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		DisplayContext context = definitions.getDisplayContext();
		TagWriter out = definitions.getOut();

		if (out == null) {
			EvalContext originalContext = definitions.snapshot();
			return new HTMLFragment() {
				@Override
				public void write(DisplayContext context2, TagWriter out2) throws IOException {
					internalWrite(context2, out2, args, originalContext);
				}
			};
		}

		internalWrite(context, out, args, definitions);

		return null;
	}

	void internalWrite(DisplayContext context, TagWriter out, Args args, EvalContext definitions) {
		int tagDepth = out.getDepth();
		try {
			write(context, out, args, definitions);
		} catch (IOException ex) {
			throw new IOError(ex);
		} catch (Throwable ex) {
			try {
				JSPErrorUtil.produceErrorOutput(context, out, "Rendering dynamic contents failed.", ex,
					HtmlMacro.class);
				out.endAll(tagDepth);
			} catch (Throwable inner) {
				ex.addSuppressed(inner);
				throw ex;
			}
		}
	}

	/**
	 * Writes the macro contents as side-effect of {@link #evalWith(EvalContext, Args)}.
	 */
	protected abstract void write(DisplayContext context, TagWriter out, Args args, EvalContext definitions)
			throws IOException;

	/**
	 * Utility to write the given value to the page.
	 */
	protected static void writeValue(DisplayContext context, TagWriter out, Renderer<Object> renderer, Object value)
			throws IOException {
		if (value == null) {
			// Skip.
			return;
		}

		if (value instanceof Collection<?>) {
			for (Object entry : (Collection<?>) value) {
				writeValue(context, out, renderer, entry);
			}
		} else if (value instanceof HTMLFragment) {
			((HTMLFragment) value).write(context, out);
		} else {
			Renderer<? super Object> valueRenderer = LabelProviderService.getInstance().getRenderer(value);
			if (valueRenderer == null) {
				valueRenderer = renderer;
			}

			valueRenderer.write(context, out, value);
		}
	}

}

/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.template;

import java.io.IOException;
import java.util.Map.Entry;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.template.AbstractExpand;
import com.top_logic.basic.config.template.EmptyScope;
import com.top_logic.basic.config.template.Eval;
import com.top_logic.basic.config.template.ModelAccess;
import com.top_logic.basic.config.template.TemplateExpression;
import com.top_logic.basic.config.template.TemplateExpression.LiteralInt;
import com.top_logic.basic.config.template.TemplateExpression.LiteralText;
import com.top_logic.basic.config.template.TemplateExpression.Tag;
import com.top_logic.basic.config.template.TemplateScope;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * Creates output to a {@link TagWriter} from a {@link TemplateExpression}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateWriter implements AbstractExpand<Void, IOException> {

	private final Eval _eval;

	private MetaLabelProvider _labelProvider;

	private final Renderer<Object> _valueRenderer;

	private final TemplateScope _scope;

	private final DisplayContext _context;

	private final TagWriter _out;

	/**
	 * Creates a {@link TemplateWriter}.
	 */
	public TemplateWriter(DisplayContext context, TagWriter out) {
		this(context, out, TLModelAccess.INSTANCE, MetaLabelProvider.INSTANCE, MetaResourceProvider.DEFAULT_RENDERER);
	}

	/**
	 * Creates a {@link TemplateWriter}.
	 *
	 * @param context
	 *        The wrapped {@link DisplayContext}.
	 * @param out
	 *        The wrapped {@link TagWriter}.
	 * @param modelAccess
	 *        The {@link ModelAccess} implementation.
	 * @param labelProvider
	 *        The {@link LabelProvider} to output values in text-only context (e.g. XML attributes).
	 * @param valueRenderer
	 *        The renderer of computed values.
	 */
	public TemplateWriter(DisplayContext context, TagWriter out, TLModelAccess modelAccess,
			MetaLabelProvider labelProvider,
			Renderer<Object> valueRenderer) {
		_eval = new Eval(modelAccess);
		_scope = EmptyScope.INSTANCE;
		_labelProvider = labelProvider;
		_valueRenderer = valueRenderer;
		_context = context;
		_out = out;
	}

	@Override
	public Eval eval() {
		return _eval;
	}

	@Override
	public TemplateExpression getTemplate(String templateName) {
		return _scope.getTemplate(templateName, false);
	}

	@Override
	public Void visitTag(Tag expr, Eval.IContext arg) throws IOException {
		_out.beginBeginTag(expr.getName());
		for (Entry<String, TemplateExpression> attr : expr.getAttributes().entrySet()) {
			_out.beginAttribute(attr.getKey());
			attr.getValue().visit(this, arg);
			_out.endAttribute();
		}
		_out.endBeginTag();
		if (expr.isEmpty()) {
			_out.endEmptyTag();
		} else {
			visitSequence(expr, arg);
			_out.endTag(expr.getName());
		}
		return null;
	}

	@Override
	public Void visitLiteralText(LiteralText expr, Eval.IContext arg) throws IOException {
		_out.append(expr.getText());
		return null;
	}

	@Override
	public Void visitLiteralInt(LiteralInt expr, Eval.IContext arg) throws IOException {
		_out.writeInt(expr.getValue());
		return null;
	}

	@Override
	public void output(Object result) throws IOException {
		if (result instanceof HTMLFragment) {
			((HTMLFragment) result).write(_context, _out);
		} else {
			if (_out.getState().beginTagAllowed()) {
				_valueRenderer.write(_context, _out, result);
			} else {
				_out.append(_labelProvider.getLabel(result));
			}
		}
	}
}
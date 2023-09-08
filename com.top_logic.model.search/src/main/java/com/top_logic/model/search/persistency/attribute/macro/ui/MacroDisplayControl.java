/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.persistency.attribute.macro.ui;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;
import static com.top_logic.model.search.expr.query.QueryExecutor.*;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.AbstractFormFieldControlBase;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.model.ModelService;

/**
 * {@link Control} writing the evaluation of a {@link SearchExpression macro expression}.
 * 
 * <p>
 * A macro is a rendering search expression producing HTML output as evaluation side-effect.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MacroDisplayControl extends AbstractFormFieldControlBase {

	private TLObject _self;

	/**
	 * Creates a {@link MacroDisplayControl}.
	 */
	public MacroDisplayControl(TLObject self, FormField model) {
		super(model);
		_self = self;
	}

	@Override
	protected void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
		// Ignore, a macro display cannot be edited.
	}

	@Override
	protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
		requestRepaint();
	}

	@Override
	protected String getTypeCssClass() {
		return "cMacro";
	}

	@Override
	protected void doInternalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		{
			SearchExpression fun = (SearchExpression) getFieldModel().getValue();
			if (fun != null) {
				writeEvaluatedMacro(context, out, _self, fun);
			}
		}
		out.endTag(DIV);
	}

	/**
	 * Evaluates the given {@link SearchExpression function} together with the given context object.
	 */
	public static void writeEvaluatedMacro(DisplayContext context, TagWriter out, Object self, SearchExpression fun) {
		KnowledgeBase defaultKnowledgeBase = PersistencyLayer.getKnowledgeBase();
		TLModel defaultTLModel = ModelService.getApplicationModel();
		QueryExecutor executor = interpret(defaultKnowledgeBase, defaultTLModel, call(fun, literal(self)));
		executor.executeWith(context, out, Args.none());
	}

}

/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * {@link Constraint} for {@link FormField}s containing {@link Expr TL-Script expressions} checking
 * that all model references contained in the script can be resolved.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ModelReferenceChecker extends AbstractConstraint {

	/**
	 * Singleton {@link ModelReferenceChecker} instance.
	 */
	public static final ModelReferenceChecker INSTANCE = new ModelReferenceChecker();

	private ModelReferenceChecker() {
		// Singleton constructor.
	}

	@Override
	public boolean check(Object value) throws CheckException {
		if (value != null) {
			try {
				checkModelElements((Expr) value);
			} catch (I18NRuntimeException ex) {
				throw new CheckException(Resources.getInstance().getString(ex.getErrorKey()));
			}
		}
		return true;
	}

	/**
	 * Checks that the model elements in the given {@link Expr} are valid.
	 * 
	 * <p>
	 * Note: Directly while {@link ExprFormat parsing} a script, model references must not be
	 * resolved immediately. Parsing script contents also happens when script code is loaded by the
	 * persistency layer for old revisions of model elements. Such historic scripts may contain
	 * model references that do no longer exist in the current revision. Therefore, it must not be
	 * checked in the current revision.
	 * </p>
	 * 
	 * @param expression
	 *        The expression to analyze.
	 * 
	 * @throws I18NRuntimeException
	 *         When compiling the {@link Expr} fails.
	 */
	public static void checkModelElements(Expr expression) throws I18NRuntimeException {
		if (PersistencyLayer.Module.INSTANCE.isActive()) {
			if (ModelService.Module.INSTANCE.isActive()) {
				ModelService service = ModelService.getInstance();
				if (service.isStarted()) {
					KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
					TLModel model = service.getModel();
					QueryExecutor.compile(kb, model, expression);
				}
			}
		}
	}
}
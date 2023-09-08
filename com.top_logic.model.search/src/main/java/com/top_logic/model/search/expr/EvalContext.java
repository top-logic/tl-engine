/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.model.TLModel;
import com.top_logic.util.error.TopLogicException;

/**
 * Shared state for the evaluation of a {@link SearchExpression} model.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class EvalContext {

	private final KnowledgeBase _kb;

	/**
	 * Set of local variables bound while evaluating a {@link SearchExpression} model.
	 */
	private final Map<NamedConstant, Object> _vars = new HashMap<>();

	private final TLModel _model;

	private final DisplayContext _displayContext;

	private TagWriter _out;

	private Renderer<Object> _renderer = ResourceRenderer.INSTANCE;

	/**
	 * Creates a {@link EvalContext}.
	 * 
	 * @param kb
	 *        See {@link #getKnowledgeBase()}.
	 * @param displayContext
	 *        See {@link #getDisplayContext()}.
	 * @param out
	 *        See {@link #getOut()}.
	 */
	public EvalContext(KnowledgeBase kb, TLModel model, DisplayContext displayContext, TagWriter out) {
		_kb = kb;
		_model = model;
		_displayContext = displayContext;
		_out = out;
	}

	/**
	 * The {@link DisplayContext} for macro expansions.
	 * 
	 * <p>
	 * The value is <code>null</code> for head-less queries.
	 * </p>
	 */
	public DisplayContext getDisplayContext() {
		return _displayContext;
	}

	/**
	 * The {@link TagWriter} for macro expansions.
	 * 
	 * <p>
	 * The value is <code>null</code> for head-less queries.
	 * </p>
	 */
	public TagWriter getOut() {
		return _out;
	}

	/**
	 * Updates {@link #getOut()} value.
	 * 
	 * <p>
	 * Note this method must exclusively called with a construct
	 * <code>before = setOut(x); try {...} finally {setOut(before);}</code>
	 * </p>
	 * 
	 * @return The {@link TagWriter} set before.
	 */
	public TagWriter setOut(TagWriter out) {
		TagWriter before = _out;
		_out = out;
		return before;
	}

	/**
	 * The renderer to write simple values when expanding render expressions.
	 */
	public Renderer<Object> getRenderer() {
		return _renderer;
	}

	/**
	 * Updates the value of {@link #getRenderer()}.
	 * 
	 * <p>
	 * Note this method must exclusively called with a construct
	 * <code>before = setRenderer(r); try {...} finally {setRenderer(before);}</code>
	 * </p>
	 * 
	 * @see #getRenderer()
	 * @return The renderer that was set before.
	 */
	public Renderer<Object> setRenderer(Renderer<Object> renderer) {
		assert renderer != null;
		Renderer<Object> before = _renderer;
		_renderer = renderer;
		return before;
	}

	/**
	 * Allocates a local variable.
	 * 
	 * @param name
	 *        The key of the variable to define.
	 * @param value
	 *        The value of the variable.
	 */
	public void defineVar(NamedConstant name, Object value) {
		_vars.put(name, value);
	}

	/**
	 * Removes a previously {@link #defineVar(NamedConstant, Object) defined} variable.
	 *
	 * @param name
	 *        The key of the variable to remove.
	 * @return The value associated with the variable before, <code>null</code> if no such variable
	 *         was defined.
	 */
	public Object deleteVar(NamedConstant name) {
		return _vars.remove(name);
	}

	/**
	 * Value of the local variable with the given key.
	 * 
	 * @param key
	 *        The variable ID.
	 * @return The variable value.
	 */
	public Object getVar(NamedConstant key) {
		if (!hasVar(key)) {
			throw new TopLogicException(I18NConstants.ERROR_ACCES_TO_UNDEFINED_VARIABLE__NAME.fill(key.asString()));
		}
		return getVarOrNull(key);
	}

	/**
	 * Access to the value of a local variable, if it is defined, <code>null</code> otherwise.
	 * 
	 * @param key
	 *        The variable ID.
	 * @return The variable value.
	 */
	public Object getVarOrNull(NamedConstant key) {
		return _vars.get(key);
	}

	/**
	 * Whether a variable with the given key is {@link #defineVar(NamedConstant, Object) defined}.
	 */
	public boolean hasVar(NamedConstant key) {
		return _vars.containsKey(key);
	}

	/**
	 * The {@link KnowledgeBase} to search in.
	 */
	public KnowledgeBase getKnowledgeBase() {
		return _kb;
	}

	/**
	 * The {@link TLModel} in which the evaluation occurs.
	 */
	public TLModel getModel() {
		return _model;
	}

	/**
	 * Creates a snapshot copy of this context with copies of all variable assignments.
	 */
	@FrameworkInternal
	public final EvalContext snapshot() {
		EvalContext result = new EvalContext(_kb, _model, null, null);
		result._vars.putAll(_vars);
		result._renderer = _renderer;
		return result;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('{');
		boolean first = true;
		for (Entry<NamedConstant, Object> entry : _vars.entrySet()) {
			if (first) {
				first = false;
			} else {
				result.append("; ");
			}
			result.append(entry.getKey().asString());
			result.append('=');
			result.append(entry.getValue());
		}
		result.append('}');
		return result.toString();
	}

}

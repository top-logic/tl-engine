/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Map;
import java.util.Set;

import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.AbstractDeleteCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Delete handler for a general {@link TLModelPart}.
 * 
 * @see TLModelPartDeletionChecker
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TLModelPartDeleteHandler extends AbstractDeleteCommandHandler {

	/**
	 * The unique ID of this handler.
	 */
	public static final String COMMAND = "deleteTLModelPart";

	/**
	 * Creates a {@link AbstractDeleteCommandHandler} to delete a general {@link TLModelPart}.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLModelPartDeleteHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		Set<Pair<TLModelPart, ResKey>> conflictingParts = getConflictingParts(model);

		if (!conflictingParts.isEmpty()) {
			return createErrorResult(conflictingParts);
		}

		return super.handleCommand(context, component, model, arguments);
	}

	private Set<Pair<TLModelPart, ResKey>> getConflictingParts(Object model) {
		return TLMetaModelUtil.getDeleteConflictingModelParts((TLModelPart) model);
	}

	private HandlerResult createErrorResult(Set<Pair<TLModelPart, ResKey>> conflictingModelParts) {
		HandlerResult result = new HandlerResult();

		for (Pair<TLModelPart, ResKey> conflictingModelPart : conflictingModelParts) {
			result.addError(conflictingModelPart.getSecond());
		}

		return result;
	}

	@Override
	protected boolean deleteObject(FormContext context, Object model) throws Exception {
		TLModelUtil.deleteRecursive((TLModelPart) model);

		return true;
	}

}

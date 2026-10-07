/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Commit message of a drop performed by a drop target configured with a
 * {@link DropTargetByExpressionConfig}.
 *
 * <p>
 * The message is computed by the {@link DropTargetByExpressionConfig#getCommitMessage() configured
 * function}. Without a function, or if the function returns nothing, the message is
 * {@link I18NConstants#DROPPED__OBJECTS_COMPONENT}.
 * </p>
 */
public class DropCommitMessage {

	private final QueryExecutor _script;

	/**
	 * Creates a {@link DropCommitMessage} for the given configuration.
	 *
	 * @param config
	 *        The configuration of the drop target.
	 */
	public DropCommitMessage(DropTargetByExpressionConfig config) {
		_script = QueryExecutor.compileOptional(config.getCommitMessage());
	}

	/**
	 * Computes the commit message of a drop into the given component.
	 *
	 * @param droppedObjects
	 *        The objects being dropped.
	 * @param dropArguments
	 *        The arguments passed to the drop operation after the dropped objects.
	 * @param component
	 *        The component the objects are dropped into.
	 * @return The message to annotate to the change performed by the drop.
	 */
	public ResKey create(Collection<?> droppedObjects, Args dropArguments, LayoutComponent component) {
		return create(droppedObjects, dropArguments, component.getModel(), component.getTitleKey());
	}

	/**
	 * Computes the commit message of a drop.
	 *
	 * @param droppedObjects
	 *        The objects being dropped.
	 * @param dropArguments
	 *        The arguments passed to the drop operation after the dropped objects.
	 * @param model
	 *        The model of the component the objects are dropped into.
	 * @param componentTitle
	 *        The title of the component the objects are dropped into.
	 * @return The message to annotate to the change performed by the drop.
	 */
	public ResKey create(Collection<?> droppedObjects, Args dropArguments, Object model, ResKey componentTitle) {
		if (_script != null) {
			ResKey message = SearchExpression.asResKey(_script.executeWith(scriptArguments(droppedObjects, dropArguments, model)));
			if (message != null) {
				return message;
			}
		}
		String droppedLabels = droppedObjects.stream()
			.map(MetaLabelProvider.INSTANCE::getLabel)
			.collect(Collectors.joining(", "));
		return I18NConstants.DROPPED__OBJECTS_COMPONENT.fill(droppedLabels, componentTitle);
	}

	private static Args scriptArguments(Collection<?> droppedObjects, Args dropArguments, Object model) {
		List<Object> values = new ArrayList<>();
		values.add(droppedObjects);
		for (Args args = dropArguments; args.hasValue(); args = args.next()) {
			values.add(args.value());
		}
		values.add(model);
		return Args.some(values.toArray());
	}

}

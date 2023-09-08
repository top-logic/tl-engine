/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.graph.diagramjs.server.util.model.TLInheritance;
import com.top_logic.layout.channel.BidirectionalTransformLinking;
import com.top_logic.layout.channel.linking.impl.AbstractTransformLinking;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;

/**
 * Connecting the selection channels between the meta element tree table and the
 * {@link DiagramJSGraphComponent}.
 * 
 * <p>
 * In contrast to the meta element tree table, the {@link DiagramJSGraphComponent} contains not only
 * {@link TLType}'s and {@link TLModule}'s but also {@link TLTypePart}'s and
 * {@link TLInheritance}'s.
 * </p>
 * <p>
 * Therefore an additional mapping is necessary to determine the type for a type part and an
 * inheritance.
 * </p>
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class GraphSelectionTransformLinking extends BidirectionalTransformLinking<AbstractTransformLinking.Config> {

	/**
	 * Creates a {@link GraphSelectionTransformLinking}.
	 */
	public GraphSelectionTransformLinking(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void appendTo(StringBuilder result) {
		result.append("GraphSelectionTransform(");
		input().appendTo(result);
		result.append(")");
	}

	@Override
	protected BiFunction<Object, Object, ?> transformation() {
		return (newValue, oldValue) -> getModelScope(oldValue) == newValue ? oldValue : newValue;
	}

	@Override
	protected Function<Object, ?> inverseTransformation() {
		return diagramSelection -> getModelScope(diagramSelection);
	}

	private Object getModelScope(Object modelPart) {
		if (modelPart == null) {
			return null;
		}

		if (modelPart instanceof TLType || modelPart instanceof TLModule) {
			return modelPart;
		} else if (modelPart instanceof TLTypePart) {
			return ((TLTypePart) modelPart).getOwner();
		} else if (modelPart instanceof TLInheritance) {
			return ((TLInheritance) modelPart).getSource();
		} else {
			throw new UnsupportedOperationException(modelPart + " is not a supported element type.");
		}
	}

}

/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.model;

import com.top_logic.bpe.bpml.model.impl.ComplexGatewayBase;
import com.top_logic.bpe.bpml.model.visit.NodeVisitor;

/**
 * Interface for {@link #COMPLEX_GATEWAY_TYPE} business objects.
 * 
 * @author Automatically generated by {@link com.top_logic.element.model.generate.InterfaceTemplateGenerator}
 */
public interface ComplexGateway extends ComplexGatewayBase {

	@Override
	default <R, A, E extends Throwable> R visit(NodeVisitor<R, A, E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}

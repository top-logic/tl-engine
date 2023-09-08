/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import java.util.Collections;
import java.util.List;

import org.w3c.dom.Node;

import com.top_logic.layout.processor.Expansion.Buffer;

/**
 * The value of an unbound mandatory parameter.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UndefinedValue extends ParameterValue {

	/**
	 * Creates a {@link UndefinedValue}.
	 * 
	 * @param paramName
	 *        See {@link #getParamName()}.
	 */
	public UndefinedValue(String paramName) {
		super(paramName);
	}

	@Override
	public boolean isDefined() {
		return false;
	}

	@Override
	protected List<? extends Node> internalExpand(Buffer out) throws ElementInAttributeContextError {
		return Collections.singletonList(out.appendText("???undefined(" + getParamName() + ")"));
	}

}
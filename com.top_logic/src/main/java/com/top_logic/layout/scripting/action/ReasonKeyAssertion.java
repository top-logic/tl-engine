/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import java.util.Set;

import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;

/**
 * {@link GuiAssertion} for reason keys for a {@link CommandModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ReasonKeyAssertion extends GuiAssertion {

	/**
	 * Setter for {@link #getReasonKey()}.
	 */
	void setReasonKey(String reason);

	/**
	 * The expected error.
	 */
	String getReasonKey();

	/**
	 * Keys that are expected to be the errors of the hander result.
	 */
	@Format(CommaSeparatedStringSet.class)
	Set<String> getReasonKeys();

	/**
	 * Keys that are expected to be contained in the errors of the hander result.
	 */
	@Format(CommaSeparatedStringSet.class)
	Set<String> getContainedKeys();

}


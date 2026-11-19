/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.scriptable;

/**
 * Configuration inheriting the methods of {@link PropertyRule}.
 */
public interface PropertyRuleSub extends PropertyRule {

	/**
	 * The value of a property of the sub-interface.
	 */
	String getSubValue();

}

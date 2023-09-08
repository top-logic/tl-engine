/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

/**
 * The formula class represents a formula in Excel. This class allows to distinguish
 * normal strings from forumlas.
 *  
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class Formula {

	private String formulaAsString;

	/** 
	 * Creates a new Formula object.
	 * 
	 * @param formulaAsString The formula as {@link String}.
	 */
	public Formula(String formulaAsString) {
		super();
		this.formulaAsString = formulaAsString;
	}

	/**
	 * Returns the formulaAsString.
	 */
	public String getFormulaAsString() {
		return this.formulaAsString;
	}

	/**
	 * @param formulaAsString The formulaAsString to set.
	 */
	public void setFormulaAsString(String formulaAsString) {
		this.formulaAsString = formulaAsString;
	}
	
}

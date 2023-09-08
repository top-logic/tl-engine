/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.wrap;

/**
 * Interface to deliver the Mandator to which objects (Wrappers in general) are attached.
 * 
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public interface MandatorAware {

	/** 
	 * Get the Mandator to which this object is attached
	 * 
	 * @return the Mandator
	 */
	public Mandator getMandator();
	
	/** 
	 * Set the Mandator to which this object is attached
	 * 
	 * @param aMandator the Mandator
	 */
	public void setMandator(Mandator aMandator);
}

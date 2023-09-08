/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;

/**
 * Builder class to create {@link MOAlternative} 
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface MOAlternativeBuilder extends MOAlternative {
	
	/** 
	 * Returns the created {@link MOAlternative}
	 */
	MOAlternative createAlternative();

	/**
	 * Marks the given type as specialisation of the constructed {@link MOAlternative}.
	 * 
	 * @param specialisation
	 *        Must not be <code>null</code>.
	 */
	void registerSpecialisation(MetaObject specialisation);

}



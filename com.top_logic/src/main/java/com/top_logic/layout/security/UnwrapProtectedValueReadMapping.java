/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Maps a {@link ProtectedValue} to its value if the current user has READ
 * rights or <code>null</code> otherwise. If the given input is no
 * {@link ProtectedValue} the input is returned.
 * <p/>
 * Usage: As a 'sortKeyProvider' for a table which uses a
 * 'columnSecurityProviderClass' to enable sorting of columns with protected
 * values.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class UnwrapProtectedValueReadMapping extends AbstractProtectedValueMapping<Object> {

	public static UnwrapProtectedValueReadMapping INSTANCE = new UnwrapProtectedValueReadMapping();
	
	private UnwrapProtectedValueReadMapping() {
		super(SimpleBoundCommandGroup.READ);
	}
	
	/** 
	 * Maps the input 
	 * <ul>
	 * <li>to the protected value iff the input is a {@link ProtectedValue} and the current user has READ</li>
	 * <li>to <code>null</code> iff the the input is a {@link ProtectedValue} and the current user has no READ</li>
	 * <li> the input iff the the input is no {@link ProtectedValue}</li>
	 * </ul>
	 * 
	 * @param input an object to map, might be <code>null</code>
	 * @return the result of the mapping, might be <code>null</code>
	 */
	@Override
	public Object map(Object input) {
		return super.map(input);
	}

	@Override
	protected Object blockedValue(ProtectedValue value) {
		return null;
	}

	@Override
	protected Object handleUnprotected(Object input) {
		return input;
	}
}

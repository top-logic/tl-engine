/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.Resources;

/**
 * {@link LabelProvider} that supports {@link ProtectedValue}s.
 * 
 * <p>
 * A {@link ProtectedValue} is unwrapped if possible and the value gets its label by a given
 * {@link LabelProvider}. If there are no rights a replacement is returned.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SecurityLabelProvider extends AbstractProtectedValueMapping<String> implements LabelProvider {

	/** A {@link SecurityLabelProvider} that dispatches to the {@link MetaLabelProvider}. */
	public static final LabelProvider INSTANCE = new SecurityLabelProvider(MetaLabelProvider.INSTANCE,
		SimpleBoundCommandGroup.READ);

	private final LabelProvider _dispatch;

	/**
	 * Creates a new {@link SecurityLabelProvider}.
	 * 
	 * @param dispatch
	 *        The {@link LabelProvider} to get label from if the user has enough rights.
	 * @param requiredRight
	 *        The right a user must have to see the value.
	 */
	public SecurityLabelProvider(LabelProvider dispatch, BoundCommandGroup requiredRight) {
		super(requiredRight);
		_dispatch = dispatch;
	}

	@Override
	public String getLabel(Object object) {
		return map(object);
	}

	@Override
	protected String blockedValue(ProtectedValue value) {
		return ProtectedValueRenderer.getBlockedText(Resources.getInstance());
	}

	@Override
	protected String handleUnprotected(Object input) {
		return _dispatch.getLabel(input);
	}

}


/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;

/**
 * The state in which a {@link Task} can be.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public enum TaskState {

	/** The {@link Task} is inactive, probably waiting for the next execution. */
	INACTIVE(I18NConstants.INACTIVE, Icons.INACTIVE),

	/** The {@link Task} is running. */
	RUNNING(I18NConstants.RUNNING, Icons.RUNNING),

	/**
	 * The {@link Task} is canceling: It was running and was told to cancel, but has not canceled,
	 * yet.
	 */
	CANCELING(I18NConstants.CANCELING, Icons.CANCELING);

	private final ResKey _messageKey;

	private final ThemeImage _icon;

	private TaskState(ResKey messageKey, ThemeImage icon) {
		_messageKey = messageKey;
		_icon = icon;
	}

	/**
	 * The I18N key for displaying this state.
	 */
	public ResKey getMessageI18N() {
		return _messageKey;
	}

	/**
	 * The icon for displaying this state.
	 */
	public ThemeImage getIcon() {
		return _icon;
	}

}

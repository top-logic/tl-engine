/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Classification of {@link BoundCommandGroup}s.
 * 
 * @see BoundCommandGroup#getCommandType()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum CommandGroupType implements ExternallyNamed {

	/**
	 * Commands with a {@link BoundCommandGroup} of type {@link #READ} simply accesses objects
	 * without modify them.
	 */
	READ {
		@Override
		public String getExternalName() {
			return "read";
		}
	},

	/**
	 * Commands with a {@link BoundCommandGroup} of type {@link #WRITE} access objects and change
	 * them in any way.
	 */
	WRITE {
		@Override
		public String getExternalName() {
			return "write";
		}
	},

	/**
	 * Commands with a {@link BoundCommandGroup} of type {@link #DELETE} delete objects.
	 */
	DELETE {
		@Override
		public String getExternalName() {
			return "delete";
		}
	},
	;

	@Override
	public abstract String getExternalName();

}

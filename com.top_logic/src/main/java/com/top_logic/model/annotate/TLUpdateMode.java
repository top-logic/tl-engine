/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.util.model.ModelService.ClassificationConfig;

/**
 * {@link TLAnnotation} specifying how a model is updated, if the application configuration changes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("enum-update")
@TargetType(value = TLTypeKind.ENUMERATION)
public interface TLUpdateMode extends TLTypeAnnotation {

	/**
	 * initialize the list if not already existing, otherwise do not change the list
	 */
	String VALUE_USE = "use";

	/**
	 * initialize the list if not already existing; if there are new list elements configured, add
	 * theme to the list
	 */
	String VALUE_UPDATE = "update";

	/**
	 * @see #getValue()
	 */
	String VALUE = "value";

	/**
	 * The {@link ClassificationConfig} usage mode.
	 * 
	 * @see ClassificationConfig#getMode()
	 */
	enum Mode implements ExternallyNamed {
		/**
		 * The classification is installed, but not updated by configuration changes.
		 */
		USE(VALUE_USE),

		/**
		 * The classification is installed and updated with configuration changes.
		 */
		UPDATE(VALUE_UPDATE);

		String _externalName = null;

		private Mode(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * How the classification is used and updated.
	 */
	@Name(VALUE)
	Mode getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(Mode value);

}

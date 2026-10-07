/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.func.Function1;
import com.top_logic.layout.form.model.FieldMode;

/**
 * The kind of relation a type configures as its access parent.
 *
 * @see SecurityConfigurationService.TLClassAccessRights#getAccessParent()
 * @see SecurityConfigurationService.TLClassAccessRights#getAccessReference()
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum AccessParentKind implements ExternallyNamed {

	/**
	 * No setting of its own: the type inherits the setting of its generalizations, or else a
	 * composition part without a definition of its own delegates to its container by default.
	 */
	@Label("Automatic")
	AUTO("auto"),

	/**
	 * The access parent is the container holding the object in a composition, navigated backwards.
	 * Without an access reference, whichever composition holds the object, otherwise only the named
	 * composition.
	 */
	@Label("Container")
	CONTAINER("container"),

	/**
	 * The access parent is the object the to-one access reference of the type points to, navigated
	 * forwards.
	 */
	@Label("Reference target")
	TARGET("target"),

	/**
	 * The type decides for itself through its grants and the roles users hold on its objects: it
	 * does not delegate, neither by default nor by a setting of its generalizations.
	 */
	@Label("Own decision")
	SELF("self"),

	;

	private final String _externalName;

	private AccessParentKind(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	/**
	 * Whether a type with this setting delegates its access decision to an access parent.
	 */
	public boolean delegates() {
		return this == CONTAINER || this == TARGET;
	}

	/**
	 * Mode of the access reference: only a relation that navigates a reference names one.
	 */
	public static class ReferenceMode extends Function1<FieldMode, AccessParentKind> {

		@Override
		public FieldMode apply(AccessParentKind kind) {
			return kind != null && kind.delegates() ? FieldMode.ACTIVE : FieldMode.INVISIBLE;
		}

	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.react.control.form.InputType;

/**
 * What the address of a {@link ReactLinkControl} is, deciding the link it becomes.
 *
 * <p>
 * The kinds are those of the {@link InputType}s whose read-only text field shows its value as a
 * link, spelled as those, so that the link opens the address the field would.
 * </p>
 */
public enum LinkKind implements ExternallyNamed {

	/** A web address, the link is the address itself. */
	URL(InputType.URL),

	/** An e-mail address, the link writes an e-mail to it. */
	EMAIL(InputType.EMAIL),

	/** A telephone number, the link calls it. */
	TEL(InputType.TEL);

	private final InputType _inputType;

	LinkKind(InputType inputType) {
		_inputType = inputType;
	}

	/**
	 * The {@link InputType} of a text field holding an address of this kind.
	 */
	public InputType inputType() {
		return _inputType;
	}

	@Override
	public String getExternalName() {
		return _inputType.htmlType();
	}
}

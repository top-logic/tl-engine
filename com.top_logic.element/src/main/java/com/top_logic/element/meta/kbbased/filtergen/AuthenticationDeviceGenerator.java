/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.element.meta.form.EditContext;

/**
 * Generator that delivers the names of the {@link AuthenticationDevice}s in
 * {@link TLSecurityDeviceManager}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AuthenticationDeviceGenerator extends ListGeneratorAdaptor {

	@Override
	public List<?> generateList(EditContext editContext) {
		if (!TLSecurityDeviceManager.Module.INSTANCE.isActive()) {
			return Collections.emptyList();
		}
		TLSecurityDeviceManager instance = TLSecurityDeviceManager.getInstance();
		List<String> authenticationDevices = new ArrayList<>(instance.getConfiguredAuthenticationDeviceIDs());
		Collections.sort(authenticationDevices);
		return authenticationDevices;
	}

}

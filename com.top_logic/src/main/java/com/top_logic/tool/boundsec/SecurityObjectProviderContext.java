/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.InstantiationContextAdaptor;

/**
 * {@link InstantiationContext} for {@link SecurityObjectProviderManager} to resolve referenced
 * {@link ReferencedSecurityObjectProvider} later.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class SecurityObjectProviderContext extends InstantiationContextAdaptor {

	private List<ReferencedSecurityObjectProvider> _referenceProviders;

	private SecurityObjectProviderManager _securityProviderManager;

	public SecurityObjectProviderContext(InstantiationContext context, SecurityObjectProviderManager securityProviderManager) {
		super(context, context);
		_securityProviderManager = securityProviderManager;
	}

	public void resolveLater(ReferencedSecurityObjectProvider referenceProvider) {
		if (_referenceProviders == null) {
			_referenceProviders = new ArrayList<>();
		}
		_referenceProviders.add(referenceProvider);
	}

	public void resolveReferences() {
		if (_referenceProviders == null) {
			return;
		}
		for (ReferencedSecurityObjectProvider referenceProvider : _referenceProviders) {
			referenceProvider.resolveReference(this, _securityProviderManager);
		}
	}

}


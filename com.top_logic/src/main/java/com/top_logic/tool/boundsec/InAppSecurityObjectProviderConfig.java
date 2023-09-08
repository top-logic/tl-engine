/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.DisplayMinimized;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * {@link SecurityObjectProviderConfig} to extend by templates for "in app" components.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface InAppSecurityObjectProviderConfig extends SecurityObjectProviderConfig {

	@Override
	@Options(fun = AllInAppImplementations.class)
	@FormattedDefault(SecurityObjectProviderConfig.CompactSecurityObjectProviderFormat.SECURITY_ROOT)
	@DisplayMinimized
	PolymorphicConfiguration<? extends SecurityObjectProvider> getSecurityObject();

}


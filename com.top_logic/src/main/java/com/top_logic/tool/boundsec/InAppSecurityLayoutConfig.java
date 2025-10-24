/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.initializer.UUIDInitializer;
import com.top_logic.layout.help.UseHelpEditor;
import com.top_logic.model.config.TLModelPartMapping;
import com.top_logic.tool.boundsec.manager.SecurityStructures;

/**
 * {@link com.top_logic.tool.boundsec.compound.CompoundSecurityLayout.Config Security layout
 * configuration} to extend by templates for "in app" components.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface InAppSecurityLayoutConfig extends ConfigurationItem {

	/** Configuration name for {@link #getHelpID()}. */
	String HELP_ID = "helpID";

	/** Configuration name for {@link #getSecurityDomain()}. */
	String SECURITY_DOMAIN = "securityDomain";

	/**
	 * The name of the security structure for which configuration options are offered.
	 */
	@Nullable
	@StringDefault("SecurityStructure")
	@Name(SECURITY_DOMAIN)
	@Options(fun = SecurityStructures.class, mapping = TLModelPartMapping.class)
	String getSecurityDomain();

	/**
	 * If this property is set, a help page can be generated for the view.
	 */
	@Nullable
	@PropertyEditor(UseHelpEditor.class)
	@ValueInitializer(UUIDInitializer.class)
	@Name(HELP_ID)
	@Label("Enable help")
	String getHelpID();
}


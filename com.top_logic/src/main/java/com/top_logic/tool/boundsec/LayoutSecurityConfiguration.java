/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.NullMapping;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayoutCommandGroupCollector;

/**
 * Definitions of the Compound security layout.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface LayoutSecurityConfiguration extends ConfigurationItem {

	/** Name of property {@link LayoutSecurityConfiguration#getCommandGroupCollector()}. */
	String COMMAND_GROUP_COLLECTOR_NAME = "collector";

	/** Name of property {@link LayoutSecurityConfiguration#getDomainMapper()}. */
	String DOMAIN_MAPPER_NAME = "domain-mapper";
	
	/**
	 * Configuration of the collector to collect {@link BoundCommandGroup} for the layout.
	 */
	@ItemDefault
	@ImplementationClassDefault(CompoundSecurityLayoutCommandGroupCollector.class)
	@Name(COMMAND_GROUP_COLLECTOR_NAME)
	PolymorphicConfiguration<CompoundSecurityLayoutCommandGroupCollector> getCommandGroupCollector();

	@InstanceFormat
	@InstanceDefault(NullMapping.class)
	@Name(DOMAIN_MAPPER_NAME)
	Mapping getDomainMapper();

}


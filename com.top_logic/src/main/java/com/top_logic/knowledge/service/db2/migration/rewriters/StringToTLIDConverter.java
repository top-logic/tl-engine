/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.Map;

import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemUpdate;

/**
 * {@link ConfiguredItemChangeRewriter} that transforms the {@link String} value of the attribute into the
 * {@link TLID} value.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StringToTLIDConverter extends ConfiguredItemChangeRewriter<StringToTLIDConverter.Config> {

	/**
	 * Typed configuration interface definition for {@link StringToTLIDConverter}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<StringToTLIDConverter>, AttributeConfig {
		// sum interface
	}

	/**
	 * Create a {@link StringToTLIDConverter}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public StringToTLIDConverter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void rewriteItemChange(ItemChange event) {
		Map<String, Object> values = event.getValues();
		String attribute = getConfig().getAttribute();
		String id = (String) values.get(attribute);
		if (id != null) {
			ItemUpdate update = new ItemUpdate(event.getRevision(), event.getObjectId(), true);
			update.setValue(attribute, event.getOldValue(attribute), StringID.valueOf(id));
			event.visitItemEvent(ChangeSet.MERGE_UPDATE, update);
		}
	}

}


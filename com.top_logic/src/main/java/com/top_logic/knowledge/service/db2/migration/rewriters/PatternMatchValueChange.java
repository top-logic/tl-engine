/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemUpdate;

/**
 * An {@link ConfiguredItemChangeRewriter} based on a regular expression.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PatternMatchValueChange extends ConfiguredItemChangeRewriter<PatternMatchValueChange.Config> {

	/**
	 * Configuration of a {@link PatternMatchValueChange}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<PatternMatchValueChange>, AttributeConfig {

		/**
		 * Name of configuration {@link #getNewValuePattern()}.
		 */
		String NEW_VALUE_NAME = "new-value";

		/**
		 * Name of configuration {@link #getOldValuePattern()}.
		 */
		String OLD_VALUE_NAME = "old-value";

		/**
		 * Pattern which the value must match to be changed.
		 */
		@Name(OLD_VALUE_NAME)
		String getOldValuePattern();

		/**
		 * Setter for {@link #getOldValuePattern()}.
		 */
		void setOldValuePattern(String value);

		/**
		 * Pattern which the is used to replace the value matched by {@link #getOldValuePattern()}.
		 */
		@Name(NEW_VALUE_NAME)
		@Mandatory
		String getNewValuePattern();

		/**
		 * Setter for {@link #getNewValuePattern()}.
		 */
		void setNewValuePattern(String value);

	}

	private Pattern _oldValuePattern;

	private String _newValuePattern;

	/**
	 * Creates a new {@link PatternMatchValueChange}.
	 */
	public PatternMatchValueChange(InstantiationContext context, Config config) {
		super(context, config);
		_oldValuePattern = Pattern.compile(config.getOldValuePattern());
		_newValuePattern = config.getNewValuePattern();
	}

	@Override
	public void rewriteItemChange(ItemChange event) {
		Map<String, Object> values = event.getValues();
		String attribute = getConfig().getAttribute();
		Object oldAttributeValue = values.get(attribute);
		if (oldAttributeValue != null) {
			Matcher matcher = _oldValuePattern.matcher((CharSequence) oldAttributeValue);
			if (matcher.matches()) {
				String newValue = matcher.replaceAll(_newValuePattern);
				ItemUpdate update = new ItemUpdate(event.getRevision(), event.getObjectId(), true);
				update.setValue(attribute, event.getOldValue(attribute), newValue);
				event.visitItemEvent(ChangeSet.MERGE_UPDATE, update);
			}
		}
	}

}


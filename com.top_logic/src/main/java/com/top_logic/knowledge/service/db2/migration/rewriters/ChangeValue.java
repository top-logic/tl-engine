/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.Map;

import com.google.inject.Inject;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.service.db2.migration.formats.DumpValueSpec;
import com.top_logic.knowledge.service.db2.migration.formats.MigrationValueParser;
import com.top_logic.knowledge.service.db2.migration.formats.ValueParser;

/**
 * {@link ConfiguredItemChangeRewriter} changing values of an attribute
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangeValue extends ConfiguredItemChangeRewriter<ChangeValue.Config> {

	/**
	 * Configuration of an {@link ConfiguredItemChangeRewriter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<ChangeValue>, AttributeConfig {

		/** Name of {@link #getNewValueSpec()}. */
		String NEW_VALUE = "new-value";

		/** Name of {@link #getOldValueSpec()}. */
		String OLD_VALUE = "old-value";

		/**
		 * If not <code>null</code>, the attribute is rewritten, when the value equals this
		 * {@link DumpValueSpec}.
		 */
		@Name(OLD_VALUE)
		DumpValueSpec getOldValueSpec();

		/**
		 * Setter for {@link #getOldValueSpec()}.
		 */
		void setOldValueSpec(DumpValueSpec oldValue);

		/**
		 * New value of the attribute.
		 */
		@Name(NEW_VALUE)
		@Mandatory
		DumpValueSpec getNewValueSpec();

		/**
		 * Setter for {@link #getNewValueSpec()}.
		 */
		void setNewValueSpec(DumpValueSpec newValue);

	}

	private final boolean _checkOldValue;

	private Object _oldValue;

	private Object _newValue;

	/**
	 * Creates a new {@link ChangeValue}.
	 */
	public ChangeValue(InstantiationContext context, Config config) {
		super(context, config);
		_checkOldValue = config.getOldValueSpec() != null;
	}

	/**
	 * Initialises this {@link ChangeValue} with the {@link MORepository} used by the migration.
	 */
	@Inject
	public void initMORepository(MORepository types) {
		ValueParser<ConfigurationError> valueParser = new MigrationValueParser(types);
		if (_checkOldValue) {
			_oldValue = getConfig().getOldValueSpec().resolve(valueParser);
		}
		_newValue = getConfig().getNewValueSpec().resolve(valueParser);
	}

	@Override
	public void rewriteItemChange(ItemChange event) {
		Map<String, Object> values = event.getValues();
		String attribute = getConfig().getAttribute();
		if (!_checkOldValue || Utils.equals(_oldValue, values.get(attribute))) {
			ItemUpdate update = new ItemUpdate(event.getRevision(), event.getObjectId(), true);

			// Note: Even equal values must be stored to allow e.g. resetting an attribute back to
			// null in the following merge operation. The orignal event may set `null` to "foo" but
			// the rewriter wants to drop this set operation (keep the new value `null`). Since the value cannot be set directly in the target event (an event does not accept multiple sets for the same attribute), a new event must be created and merged into the original event. The merge event must explicitly contain a set from `null` to `null` for the attribute in question to allow the following merge drop the "foo" value.
			boolean dropEqualValues = false;

			update.setValue(attribute, event.getOldValue(attribute), _newValue, dropEqualValues);
			event.visitItemEvent(ChangeSet.MERGE_UPDATE, update);
		}
	}

}

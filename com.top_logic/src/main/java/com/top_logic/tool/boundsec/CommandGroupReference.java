/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Objects;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.tool.boundsec.CommandHandler.Config.AllCommandGroups;
import com.top_logic.tool.boundsec.CommandHandler.Config.ToCommandGroupReference;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;

/**
 * Reference to a {@link BoundCommandGroup} to be resolved later on.
 * 
 * @see #resolve()
 * @see BoundCommandGroup
 * @see CommandHandler.Config#getGroup()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Format(CommandGroupReference.ValueFormat.class)
@Options(fun = AllCommandGroups.class, mapping = ToCommandGroupReference.class)
public class CommandGroupReference {

	/**
	 * Format for the {@link CommandGroupReference}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ValueFormat extends AbstractConfigurationValueProvider<CommandGroupReference> {

		/** Singleton {@link CommandGroupReference.ValueFormat} instance. */
		public static final ValueFormat INSTANCE = new ValueFormat(CommandGroupReference.class);

		/**
		 * Creates a new {@link ValueFormat}.
		 * 
		 * @param type
		 *        See {@link #getValueType()}.
		 */
		public ValueFormat(Class<?> type) {
			super(type);
		}

		@Override
		protected CommandGroupReference getValueNonEmpty(String propertyName, CharSequence propertyValue) {
			return new CommandGroupReference(propertyValue.toString());
		}

		@Override
		protected String getSpecificationNonNull(CommandGroupReference configValue) {
			return configValue.id();
		}

	}

	private final String _id;

	/**
	 * Creates a new {@link CommandGroupReference}.
	 * 
	 */
	public CommandGroupReference(String id) {
		_id = Objects.requireNonNull(id);
	}

	/**
	 * Resolves the referenced {@link BoundCommandGroup}.
	 * 
	 * @see #resolve(CommandGroupRegistry)
	 */
	public BoundCommandGroup resolve() {
		return resolve(CommandGroupRegistry.getInstance());
	}

	/**
	 * Resolves the referenced {@link BoundCommandGroup}.
	 * 
	 * @return The resolved {@link BoundCommandGroup}. May be <code>null</code> when the referenced
	 *         group does not exists.
	 */
	public BoundCommandGroup resolve(CommandGroupRegistry registry) {
		return registry.getGroup(id());
	}

	/**
	 * Returns the {@link BoundCommandGroup#getID() id} of the referenced
	 *         {@link BoundCommandGroup}.
	 */
	public String id() {
		return _id;
	}

	@Override
	public String toString() {
		return CommandGroupReference.class.getSimpleName() + "(" + id() + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _id.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommandGroupReference other = (CommandGroupReference) obj;
		if (!_id.equals(other._id))
			return false;
		return true;
	}

}


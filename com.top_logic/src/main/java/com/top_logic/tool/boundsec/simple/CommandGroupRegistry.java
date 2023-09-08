/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.simple;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.tool.boundsec.BoundCommand;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandGroupType;

/**
 * Registry for {@link BoundCommandGroup}'s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommandGroupRegistry extends ConfiguredManagedClass<CommandGroupRegistry.Config> {

	/**
	 * Configuration of the {@link CommandGroupRegistry}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfiguredManagedClass.Config<CommandGroupRegistry> {

		/**
		 * All known {@link GroupDefinition}s, indexed by its {@link GroupDefinition#getName()}.
		 */
		@Key(GroupDefinition.NAME_ATTRIBUTE)
		@DefaultContainer
		Map<String, GroupDefinition> getGroups();

	}

	/**
	 * Definition for a {@link BoundCommandGroup}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface GroupDefinition extends NamedConfigMandatory {

		/**
		 * The ID of the configured {@link BoundCommandGroup}.
		 * 
		 * @see BoundCommandGroup#getID()
		 */
		@Override
		String getName();

		/**
		 * Type of the configured {@link BoundCommandGroup}.
		 * 
		 * @see BoundCommandGroup#getCommandType()
		 */
		CommandGroupType getType();
	}
	
	private final Map<String, BoundCommandGroup> _groups;

	/**
	 * Constructor creates a new {@link CommandGroupRegistry}.
	 */
	public CommandGroupRegistry(InstantiationContext context, Config config) {
		super(context, config);
		Map<String, BoundCommandGroup> groups = config.getGroups().values()
				.stream()
				.map(group -> new SimpleBoundCommandGroup(group.getName(), group.getType()))
				.collect(Collectors.toMap(BoundCommandGroup::getID, Function.identity()));
		addProgrammatic(groups, SimpleBoundCommandGroup.READ);
		addProgrammatic(groups, SimpleBoundCommandGroup.WRITE);
		addProgrammatic(groups, SimpleBoundCommandGroup.CREATE);
		addProgrammatic(groups, SimpleBoundCommandGroup.DELETE);
		addProgrammatic(groups, SimpleBoundCommandGroup.EXPORT);
		addProgrammatic(groups, SimpleBoundCommandGroup.RESTRICTED_WRITE);
		addProgrammatic(groups, SimpleBoundCommandGroup.SYSTEM);

		_groups = Collections.unmodifiableMap(groups);
	}

	private void addProgrammatic(Map<String, BoundCommandGroup> groups, BoundCommandGroup group) {
		groups.put(group.getID(), group);
	}

	/**
	 * The configured {@link BoundCommandGroup} indexed by its {@link BoundCommandGroup#getID() id}.
	 * 
	 * <p>
	 * The value must <b>not</b> be modified.
	 * </p>
	 */
	protected Map<String, BoundCommandGroup> groupsById() {
		return _groups;
	}

	/**
	 * The collection of all registered {@link BoundCommandGroup}s.
	 */
	public Collection<BoundCommandGroup> getAllCommandGroups() {
		return groupsById().values();
	}

	/**
	 * Returns the known {@link BoundCommandGroup} with the given {@link BoundCommand#getID() id}.
	 * 
	 * @param id
	 *        The {@link BoundCommand#getID() id} of the desired {@link BoundCommandGroup}.
	 * @return May be <code>null</code> when there is no such group.
	 */
	public BoundCommandGroup getGroup(String id) {
		return groupsById().get(id);
	}

	/**
	 * Returns the known {@link BoundCommandGroup} with the given {@link BoundCommand#getID() id}.
	 * 
	 * @param id
	 *        The {@link BoundCommand#getID() id} of the desired {@link BoundCommandGroup}.
	 * @return May be <code>null</code> when there is no such group.
	 */
	public static BoundCommandGroup resolve(String id) {
		return Module.INSTANCE.getImplementationInstance().getGroup(id);
	}

	/**
	 * This {@link CommandGroupRegistry} service instance.
	 */
	public static CommandGroupRegistry getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton reference to the {@link CommandGroupRegistry}.
	 */
	public static final class Module extends TypedRuntimeModule<CommandGroupRegistry> {

		/**
		 * Singleton {@link CommandGroupRegistry.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<CommandGroupRegistry> getImplementation() {
			return CommandGroupRegistry.class;
		}

	}

}


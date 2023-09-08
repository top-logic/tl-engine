/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.simple;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.CommaSeparatedSetValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandGroupType;

/**
 * Default implementation of {@link com.top_logic.tool.boundsec.BoundCommandGroup}.
 * 
 * <p>
 * Custom command groups must be defined in the {@link CommandGroupRegistry}.
 * </p>
 * 
 * @see CommandGroupRegistry#getGroup(String)
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public class SimpleBoundCommandGroup implements BoundCommandGroup {

	/** {@link BoundCommandGroup#getID()} of the {@link BoundCommandGroup} {@link #SYSTEM} */
	public static final String SYSTEM_NAME = "System";

	/** {@link BoundCommandGroup#getID()} of the {@link BoundCommandGroup} {@link #READ} */
	public static final String READ_NAME = "Read";

	/** {@link BoundCommandGroup#getID()} of the {@link BoundCommandGroup} {@link #WRITE} */
	public static final String WRITE_NAME = "Write";

	/** {@link BoundCommandGroup#getID()} of the {@link BoundCommandGroup} {@link #RESTRICTED_WRITE} */
	public static final String RESTRICTED_WRITE_NAME = "restrictedWrite";

	/** {@link BoundCommandGroup#getID()} of the {@link BoundCommandGroup} {@link #CREATE} */
	public static final String CREATE_NAME = "Create";

	/** {@link BoundCommandGroup#getID()} of the {@link BoundCommandGroup} {@link #DELETE} */
	public static final String DELETE_NAME = "Delete";

	/** {@link BoundCommandGroup#getID()} of the finish {@link BoundCommandGroup} */
	public static final String FINISH_NAME = "Finish";

	/** {@link BoundCommandGroup#getID()} of the {@link BoundCommandGroup} {@link #EXPORT} */
	public static final String EXPORT_NAME = "Export";

	/**
	 * {@link ConfigurationValueProvider} for {@link BoundCommandGroup}s used in
	 * {@link ConfigurationItem}s.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class ValueProvider extends AbstractConfigurationValueProvider<BoundCommandGroup> {

		/**
		 * Singleton {@link SimpleBoundCommandGroup.ValueProvider} instance.
		 */
		public static final ValueProvider INSTANCE = new SimpleBoundCommandGroup.ValueProvider();

		private ValueProvider() {
			super(BoundCommandGroup.class);
		}

		@Override
		public BoundCommandGroup defaultValue() {
			/* Correct would be: 'return SimpleBoundCommandGroup.READ;' But many CommandHandler have
			 * their own defaults which are not declared in their config interfaces, but set in the
			 * constructor. But to be able to detect in those constructors whether those defaults
			 * have to be applied or an explicit value has been set, the default value has to be
			 * null. Applying the defaults in the constructor and not declaring them is a bug, of
			 * course. But fixing it would require to change more than 200 CommandHandlers, which is
			 * not worth the effort. */
			return null;
		}

		@Override
		public BoundCommandGroup getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			BoundCommandGroup resolvedGroup = CommandGroupRegistry.resolve(propertyValue.toString());
			if (resolvedGroup == null) {
				throw new ConfigurationException(
					I18NConstants.ERROR_COMMAND_GROUP_NOT_REGISTERED__COMMAND_GROUP.fill(propertyValue),
					propertyName, propertyValue);
			}
			return resolvedGroup;
		}

		@Override
		public String getSpecificationNonNull(BoundCommandGroup configValue) {
			return configValue.getID();
		}

	}

	/** Built-in default {@link BoundCommandGroup} for {@link CommandGroupType#READ} commands. */
	public static final BoundCommandGroup READ = new SimpleBoundCommandGroup(READ_NAME, CommandGroupType.READ);

	/** Built-in default {@link BoundCommandGroup} for {@link CommandGroupType#WRITE} commands. */
	public static final BoundCommandGroup WRITE = new SimpleBoundCommandGroup(WRITE_NAME, CommandGroupType.WRITE);
    
	/** Built-in {@link BoundCommandGroup} for commands creating objects. */
	public static final BoundCommandGroup CREATE = new SimpleBoundCommandGroup(CREATE_NAME, CommandGroupType.WRITE);

	/** Built-in default {@link BoundCommandGroup} for {@link CommandGroupType#DELETE} commands. */
	public static final BoundCommandGroup DELETE = new SimpleBoundCommandGroup(DELETE_NAME, CommandGroupType.DELETE);

	/** Built-in {@link BoundCommandGroup} for commands exporting objects. */
	public static final BoundCommandGroup EXPORT = new SimpleBoundCommandGroup(EXPORT_NAME, CommandGroupType.READ);

	/**
	 * Built-in special {@link BoundCommandGroup} for commands that have no security restrictions
	 * (such as closing a dialog).
	 */
	public static final BoundCommandGroup SYSTEM = new SimpleBoundCommandGroup(SYSTEM_NAME, CommandGroupType.READ);
    
	/**
	 * Built-in {@link BoundCommandGroup} for commands that are allowed even for restricted
	 * accounts.
	 * 
	 * @see Person#isRestrictedUser()
	 */
	public static final SimpleBoundCommandGroup RESTRICTED_WRITE =
		new SimpleBoundCommandGroup(RESTRICTED_WRITE_NAME, CommandGroupType.READ);

	/** Singleton {@link #READ} set. */
    public static final Set<BoundCommandGroup> READ_SET = Collections.singleton(READ);

	/** Set of {@link #READ} and {@link #WRITE}. */
    public static final Set<BoundCommandGroup> READWRITE_SET = 
        new HashSet<>(Arrays.asList(new BoundCommandGroup[] {READ, WRITE}));

	/** Set of {@link #READ}, {@link #WRITE} and {@link #DELETE}. */
    public static final Set<BoundCommandGroup> READWRITEDEL_SET = 
        new HashSet<>(Arrays.asList(new BoundCommandGroup[] {READ, WRITE, DELETE}));

	/** Set of {@link #READ} and {@link #CREATE}. */
    public static final Set<BoundCommandGroup> READCREATE_SET = 
        new HashSet<>(Arrays.asList(new BoundCommandGroup[] {READ, CREATE}));
    
	/** Set of {@link #READ}, {@link #WRITE} and {@link #CREATE}. */
    public static final Set<BoundCommandGroup> READWRITECREATE_SET = 
        new HashSet<>(Arrays.asList(new BoundCommandGroup[] {READ, WRITE, CREATE}));
    
	/** Set of {@value #READ}, {@link #WRITE}, {@link #CREATE} and {@link #DELETE}. */
    public static final Set<BoundCommandGroup> READWRITECREATEDEL_SET = 
        new HashSet<>(Arrays.asList(new BoundCommandGroup[] {READ, WRITE, CREATE, DELETE}));

    /** ID of the command group */
	private String id;

    /** Type of the command group */
	private CommandGroupType _type;

	/**
	 * {@link ConfigurationValueProvider} that resolves a comma separated list of command group
	 * names through the {@link CommandGroupRegistry#resolve(String)} method.
	 */
	public static final class SetValueProvider extends CommaSeparatedSetValueProvider<BoundCommandGroup> {

		/** Singleton {@link SimpleBoundCommandGroup.SetValueProvider} instance. */
		public static final SetValueProvider INSTANCE = new SetValueProvider();

		private SetValueProvider() {
			super();
		}

		@Override
		protected BoundCommandGroup parseSingleValue(String propertyName, CharSequence propertyValue, String groupName)
				throws ConfigurationException {
			return ValueProvider.INSTANCE.getValue(propertyName, groupName);
		}

		@Override
		protected String formatSingleValue(BoundCommandGroup singleConfigValue) {
			return ValueProvider.INSTANCE.getSpecification(singleConfigValue);
		}

	}

	/** Construct an new SimpleBoundCommandGroup */
	SimpleBoundCommandGroup(String anID, CommandGroupType type) {
        id = anID;
		_type = type;
    }

    @Override
	public String toString() {
		return id + " (" + _type.getExternalName() + ')';
    }

    @Override
	public int hashCode() {
		return id.hashCode() ^ _type.hashCode();
    }

    @Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
        if (o instanceof BoundCommandGroup) {
            BoundCommandGroup bc = (BoundCommandGroup) o;
            return id.equals(bc.getID())
				&& _type == bc.getCommandType();
        }
        return false;
    }

    @Override
	public String getID() {
        return id;
    }
    
    @Override
	public CommandGroupType getCommandType() {
		return _type;
    }

    /**
	 * This method checks the {@link Person#isRestrictedUser() restricted user state} of the given
	 * person and determines, whether the given person has rights on the given
	 * {@link BoundCommandGroup}, actually it checks whether the given group 'contains' a
	 * {@link CommandGroupType#WRITE write} or a {@link CommandGroupType#DELETE delete} command.
	 * 
	 * @param person
	 *        the {@link Person}whose rights must be checked, must not be <code>null</code>.
	 * @param group
	 *        the group to test, must not be <code>null</code>
	 * @return whether <code>person</code> can execute commands of the {@link BoundCommandGroup
	 *         command group} <code>group</code>
	 */
	public static boolean isAllowedCommandGroup(Person person, BoundCommandGroup group) {
		if (ThreadContext.isSuperUser()) {
			return true;
		}
		if (!person.isRestrictedUser().booleanValue()) {
			return true;
		}
		return !group.isDeleteGroup() && !group.isWriteGroup();
	}

}

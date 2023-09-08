/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import java.util.Collection;
import java.util.Comparator;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContext;

/**
 * {@link TableConfigurationProvider} that enhances the protected columns to ensure that values that
 * must not be seen are not visible for the user.
 * 
 * <p>
 * It adapts all necessary properties of the {@link TableConfiguration} and their
 * {@link ColumnConfiguration}, such that values of protected columns are wrapped. Unprotected
 * columns remain untouched.
 * </p>
 * 
 * <p>
 * A {@link ColumnConfiguration} can be protected by calling
 * {@link SecurityAddingTableConfiguration#protectColumn(ColumnConfiguration)}.
 * </p>
 * 
 * @see GenericTableConfigurationProvider
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SecurityAddingTableConfiguration extends NoDefaultColumnAdaption {

	private static final BoundCommandGroup DEFAULT_RIGHT = SimpleBoundCommandGroup.READ;

	private static final Property<String> NEED_SECURITY = TypedAnnotatable.property(String.class,"needSecurity");

	private static final Property<BoundCommandGroup> NEEDED_RIGHT =
		TypedAnnotatable.property(BoundCommandGroup.class, "neededRight", DEFAULT_RIGHT);

	/**
	 * Ensures that the values in the given column are protected by {@link ProtectedValue} if
	 * necessary.
	 * 
	 * @see SecurityAddingTableConfiguration#protectColumn(ColumnConfiguration, String)
	 */
	public static void protectColumn(ColumnConfiguration col) {
		protectColumn(col, col.getName());
	}

	/**
	 * Ensures that the values in the given column are protected by {@link ProtectedValue} if
	 * necessary.
	 * 
	 * <p>
	 * The values are protected with the security of the attribute with the given name.
	 * </p>
	 * 
	 * @see SecurityAddingTableConfiguration#protectColumn(ColumnConfiguration, String,
	 *      BoundCommandGroup)
	 */
	public static void protectColumn(ColumnConfiguration col, String attributeName) {
		protectColumn(col, attributeName, DEFAULT_RIGHT);
	}

	/**
	 * Ensures that the values in the given column are protected by {@link ProtectedValue} if
	 * necessary.
	 * 
	 * <p>
	 * The values are protected with the security of the attribute with the given name.
	 * </p>
	 * <p>
	 * The {@link BoundCommandGroup} for which a user must have rights to see the protected values
	 * in the column.
	 * </p>
	 */
	public static void protectColumn(ColumnConfiguration col, String attributeName, BoundCommandGroup requiredRight) {
		col.set(NEED_SECURITY, attributeName);
		if (requiredRight != DEFAULT_RIGHT) {
			col.set(NEEDED_RIGHT, requiredRight);
		}
	}

	/**
	 * A {@link SecurityAddingTableConfiguration} which supposes that the row objects of the table
	 * are {@link TLObject}.
	 */
	public static final SecurityAddingTableConfiguration INSTANCE = new SecurityAddingTableConfiguration();

	/**
	 * Creates a new {@link SecurityAddingTableConfiguration}.
	 */
	protected SecurityAddingTableConfiguration() {
		// no additional properties here.
	}

	/**
	 * @param column
	 *        The {@link ColumnConfiguration column} to protect.
	 * @return The name of the attribute which protects the values in the given column, or
	 *         <code>null</code> if no protection is needed.
	 */
	protected final String getProtectionAttribute(ColumnConfiguration column) {
		return column.get(NEED_SECURITY);
	}

	/**
	 * Returns the {@link BoundCommandGroup} for which the user must have rights to see the values
	 * in the column.
	 * 
	 * <p>
	 * Must ony be called when the column is protected.
	 * </p>
	 * 
	 * @see #getProtectionAttribute(ColumnConfiguration)
	 */
	protected final BoundCommandGroup neededRight(ColumnConfiguration col) {
		return col.get(NEEDED_RIGHT);
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		Mapping<Object, ? extends TLObject> modelMapping = null;
		Collection<? extends ColumnConfiguration> declaredColumns = table.getDeclaredColumns();
		for (ColumnConfiguration column : declaredColumns) {
			String protectionAttribute = getProtectionAttribute(column);
			if (protectionAttribute != null) {
				modelMapping = initModelMapping(modelMapping, table);
				addSecurity(column, protectionAttribute, modelMapping);
			}
		}
	}

	private Mapping<Object, ? extends TLObject> initModelMapping(Mapping<Object, ? extends TLObject> currentMapping,
			TableConfiguration table) {
		if (currentMapping != null) {
			return currentMapping;
		}
		Mapping<Object, ? extends TLObject> configuredMapping = table.getModelMapping();
		if (configuredMapping != null) {
			return configuredMapping;
		}
		return DefaultSecurityModelMapping.INSTANCE;
	}

	private void addSecurity(ColumnConfiguration column, String attributeName,
			Mapping<Object, ? extends TLObject> modelMapping) {
		BoundCommandGroup required = neededRight(column);
		adaptAccessor(column, attributeName, required, modelMapping);
		adaptCellRenderer(column, attributeName, required);
		adaptFullTextProvider(column, attributeName, required);
		adaptComparator(column, attributeName, required);
		adaptSortKeyProvider(column, attributeName, required);
		adaptTableFilterProvider(column, attributeName, required);
	}

	/**
	 * Adapts the {@link TableFilterProvider} to be able to all elements returned by the enhanced
	 * {@link ColumnConfiguration#getSortKeyProvider()}.
	 * 
	 * @param column
	 *        The column whose values must be protected.
	 * @param attributeName
	 *        The name of the attribute that protects the values.
	 * @param required
	 *        The {@link BoundCommandGroup} a user must have to see the actual values.
	 * 
	 *        {@link #adaptSortKeyProvider(ColumnConfiguration, String, BoundCommandGroup)}
	 */
	protected void adaptTableFilterProvider(ColumnConfiguration column, String attributeName, BoundCommandGroup required) {
		TableFilterProvider filterProvider = column.getFilterProvider();
		if (filterProvider != null) {
			column.setFilterProvider(new ProtectedValueTableFilterProvider(filterProvider));
		}
	}

	/**
	 * Adapts the {@link ColumnConfiguration#getComparator()} to be able to compare values given by
	 * the enhanced {@link ColumnConfiguration#getSortKeyProvider()} of the column.
	 * 
	 * @param column
	 *        The column whose values must be protected.
	 * @param attributeName
	 *        The name of the attribute that protects the values.
	 * @param required
	 *        The {@link BoundCommandGroup} a user must have to see the actual values.
	 * 
	 * @see #adaptSortKeyProvider(ColumnConfiguration, String, BoundCommandGroup)
	 */
	protected void adaptComparator(ColumnConfiguration column, String attributeName, BoundCommandGroup required) {
		column.setComparator(new ProtectedValueComparator(column.getComparator(), true));
		Comparator descendingComparator = column.getDescendingComparator();
		if (descendingComparator != null) {
			column.setDescendingComparator(new ProtectedValueComparator(column.getComparator(), false));
		}
	}

	/**
	 * Adapts the {@link ColumnConfiguration#getSortKeyProvider()} to be able to handle objects that
	 * are returned by the enhanced {@link ColumnConfiguration#getAccessor()}.
	 * 
	 * @param column
	 *        The column whose values must be protected.
	 * @param attributeName
	 *        The name of the attribute that protects the values.
	 * @param required
	 *        The {@link BoundCommandGroup} a user must have to see the actual values.
	 * 
	 * @see #adaptAccessor(ColumnConfiguration, String, BoundCommandGroup, Mapping)
	 */
	protected void adaptSortKeyProvider(ColumnConfiguration column, String attributeName, BoundCommandGroup required) {
		Mapping origSortKeyProvider = column.getSortKeyProvider();
		Mapping<Object, ?> blockedMapping = ProtectedValueReplacement.getMapping();
		column.setSortKeyProvider(new ProtectedValueMapping(origSortKeyProvider, blockedMapping, required));
	}

	/**
	 * Adapts the {@link ColumnConfiguration#getFullTextProvider()} to handle objects returned by
	 * the enhanced {@link ColumnConfiguration#getSortKeyProvider()}.
	 * 
	 * @param column
	 *        The column whose values must be protected.
	 * @param attributeName
	 *        The name of the attribute that protects the values.
	 * @param required
	 *        The {@link BoundCommandGroup} a user must have to see the actual values.
	 * 
	 * @see #adaptSortKeyProvider(ColumnConfiguration, String, BoundCommandGroup)
	 */
	protected void adaptFullTextProvider(ColumnConfiguration column, String attributeName, BoundCommandGroup required) {
		column.setFullTextProvider(new SecurityFullTextProvider(column.getFullTextProvider()));
	}

	/**
	 * Adapts the {@link ColumnConfiguration#getCellRenderer()} to be able to render all objects
	 * returned by the enhanced {@link ColumnConfiguration#getAccessor()}.
	 * 
	 * @param column
	 *        The column whose values must be protected.
	 * @param attributeName
	 *        The name of the attribute that protects the values.
	 * @param required
	 *        The {@link BoundCommandGroup} a user must have to see the actual values.
	 * 
	 * @see #adaptAccessor(ColumnConfiguration, String, BoundCommandGroup, Mapping)
	 */
	protected void adaptCellRenderer(ColumnConfiguration column, String attributeName, BoundCommandGroup required) {
		column.setCellRenderer(new ProtectedValueCellRenderer(column.finalCellRenderer(), required));
	}

	/**
	 * Adapts the {@link ColumnConfiguration#getAccessor()} of the given column to protect the
	 * values by the attribute with the given name.
	 * 
	 * @param column
	 *        The column whose values must be protected.
	 * @param attributeName
	 *        The name of the attribute that protects the values.
	 * @param required
	 *        The {@link BoundCommandGroup} a user must have to see the actual values.
	 * @param modelMapping
	 *        Mapping from row objects of the table to corresponding business objects for security
	 *        check.
	 */
	protected void adaptAccessor(ColumnConfiguration column, String attributeName, BoundCommandGroup required,
			Mapping<Object, ? extends TLObject> modelMapping) {
		SecurityProvider securityProvider = new SecurityProviderImpl(attributeName, modelMapping);
		Group currentGroup = getCurrentRepresentativeGroup();
		column.setAccessor(new SecurityAccessor(currentGroup, securityProvider, column.getAccessor()));
	}

	private static Group getCurrentRepresentativeGroup() {
		TLContext context = TLContext.getContext();
		if (context == null) {
			return null;
		}
		Person currentPerson = context.getCurrentPersonWrapper();
		if (currentPerson == null) {
			return null;
		}
		return currentPerson.getRepresentativeGroup();
	}

}


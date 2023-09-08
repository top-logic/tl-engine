/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.treetable.component;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.IdentityAccessor;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.form.model.NodeGroupInitializer;
import com.top_logic.layout.table.model.FieldProvider;
import com.top_logic.layout.tree.renderer.DefaultTableDeclaration;

/**
 * {@link NodeGroupInitializer} which initializes the {@link FormGroup} using a configured
 * {@link FieldProvider} and {@link Accessor} for an arbitrary list of column names.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TreeTableNodeGroupInitializer implements NodeGroupInitializer {

	public interface Config extends PolymorphicConfiguration<NodeGroupInitializer> {
		@Name(XML_ATTR_FIELD_PROVIDER)
		@InstanceFormat
		@InstanceDefault(ConstantFieldProvider.class)
		FieldProvider getFieldProvider();

		@Name(XML_ATTRIBUTE_ACCESSOR)
		@InstanceFormat
		@InstanceDefault(IdentityAccessor.class)
		Accessor getFieldAccessor();

		@Name(XML_ATTRIBUTE_COLUMNS)
		@StringDefault(DefaultTableDeclaration.DEFAULT_COLUMN_NAME)
		String getColumns();
	}

	/**
	 * Configuration parameter for the {@link #fieldProvider}
	 */
	private static final String XML_ATTR_FIELD_PROVIDER = "fieldProvider";

	/**
	 * Configuration parameter for {@link #columns}. Value must be a ',' separated string of column
	 * names.
	 */
	private static final String XML_ATTRIBUTE_COLUMNS = "columns";

	/**
	 * Configuration parameter for {@link #accessor}
	 */
	private static final String XML_ATTRIBUTE_ACCESSOR = "fieldAccessor";

	/**
	 * the configured provider to dispatch creation of form fields to.
	 */
	private FieldProvider fieldProvider;

	/**
	 * The columns for which fields must be constructed
	 */
	private String[] columns;

	/**
	 * The {@link Accessor} to determine the user object of the given node to build a field from.
	 */
	private Accessor accessor;

	/**
	 * Is called via reflection from the layout configuration.
	 * 
	 * @param config
	 *        the configuration to determine necessary properties from
	 */
	public TreeTableNodeGroupInitializer(InstantiationContext context, Config config) throws ConfigurationException {
		this.fieldProvider = config.getFieldProvider();
		this.accessor = config.getFieldAccessor();
		final String configuredColumns = config.getColumns();
		this.columns = StringServices.toArray(configuredColumns, ',');
	}

	@Override
	public void createNodeGroup(FormTree formTree, FormGroup nodeGroup, Object node) {
		for (String column : columns) {
			final FormMember createdField = fieldProvider.createField(node, accessor, column);
			if (createdField != null) {
				nodeGroup.addMember(createdField);
			}
		}
	}

}

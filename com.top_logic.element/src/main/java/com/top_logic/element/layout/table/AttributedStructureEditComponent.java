/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.component.DefaultApplyAttributedCommandHandler;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.NodeGroupInitializer;
import com.top_logic.layout.form.treetable.component.ConfigurableStructureEditComponent;
import com.top_logic.layout.tree.model.MutableTLTreeNode;

/**
 * {@link ConfigurableStructureEditComponent} for editing a tree containing {@link Wrapper}.
 * 
 * @since 5.7.6
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AttributedStructureEditComponent<N extends MutableTLTreeNode<N>> extends
		ConfigurableStructureEditComponent<N> {

	/**
	 * Configuration interface of the {@link AttributedStructureEditComponent}.
	 */
	public interface Config extends ConfigurableStructureEditComponent.Config {

		@Override
		@StringDefault(DefaultApplyAttributedCommandHandler.COMMAND_ID)
		String getApplyCommand();

		@Override
		@InstanceDefault(AttributedNodeGroupAccessor.class)
		Accessor getNodeAccessor();

		@Override
		@InstanceDefault(AttributedNodeGroupInitializer.class)
		NodeGroupInitializer getNodeGroupInitializer();

	}

	/**
	 * Creates a new {@link AttributedStructureEditComponent} from the given configuration.
	 */
	public AttributedStructureEditComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public FormContext createFormContext() {
		AttributeFormContext formContext = new AttributeFormContext(this.getResPrefix());
		initFormContext(formContext);
		return formContext;
	}

}


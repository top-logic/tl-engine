/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configEdit;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.DefaultRefVisitor;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.Channel;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.channel.linking.ref.ComponentRef;
import com.top_logic.layout.form.component.AbstractFormCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.values.edit.DefaultValueModel;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} adding a newly created {@link ConfigurationItem} to a configured list
 * property of the {@link ConfigurationItem} model.
 * 
 * @see Config#getProperty()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AddNewConfiguration extends AbstractFormCommandHandler {

	/**
	 * Configuration options for {@link AddNewConfiguration}.
	 */
	public interface Config extends AbstractFormCommandHandler.Config {
		/**
		 * Name of the {@link PropertyDescriptor} to add the newly created configuration to.
		 * 
		 * <p>
		 * The value is allowed to be a dot-separated path of property names. In that case, the path
		 * is evaluated with the command's target model as base object and the new configuration is
		 * stored in the property with the last name in the path.
		 * </p>
		 */
		@Name("property")
		@Mandatory
		String getProperty();

		/**
		 * The channel to post the new object to.
		 */
		@Name("result")
		ModelSpec getResult();
	}


	/**
	 * Creates a {@link AddNewConfiguration} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AddNewConfiguration(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void applyChanges(FormComponent component, Object model, FormContext formContext, HandlerResult result,
			Map<String, Object> arguments) throws Exception {
		ConfigurationItem createdConfig = (ConfigurationItem) EditorFactory.getModel(component);
		ConfigurationItem newConfig = TypedConfiguration.copy(createdConfig);

		Config config = (Config) getConfig();
		DefaultValueModel.resolvePath((ConfigurationItem) model, config.getProperty()).addValue(newConfig);

		result.setCloseDialog(true);
		component.removeFormContext();

		ModelSpec resultDisplay = config.getResult();
		ChannelLinking.updateModel(resultDisplay, component, newConfig);

		invalidateTarget(resultDisplay, component);
	}

	static void invalidateTarget(ModelSpec modelSpec, LayoutComponent component) {
		if (modelSpec instanceof Channel) {
			Channel channel = (Channel) modelSpec;
			ComponentRef ref = channel.getComponentRef();
			LayoutComponent target = DefaultRefVisitor.resolveReference(null, ref, component);
			target.invalidate();
		}
	}

}

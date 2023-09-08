/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.DefaultRefVisitor;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.channel.linking.Channel;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.channel.linking.impl.DirectLinking;
import com.top_logic.layout.channel.linking.ref.ComponentRef;
import com.top_logic.layout.form.component.edit.EditMode;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Plug-in for an {@link AbstractCreateCommandHandler} for specifying common action to be done after
 * successfully creating an object.
 * 
 * @see com.top_logic.layout.form.component.AbstractCreateCommandHandler.Config#getPostCreateActions()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface PostCreateAction {

	/**
	 * The operation to be executed with the newly created model object.
	 * 
	 * @param component
	 *        The component that created the new model.
	 * @param newModel
	 *        The newly created model.
	 */
	void handleNew(LayoutComponent component, Object newModel);

	/**
	 * {@link PostCreateAction} performing no action.
	 */
	class None implements PostCreateAction {

		/**
		 * Configuration options for {@link None}.
		 */
		@TagName(Config.TAG_NAME)
		public interface Config extends PolymorphicConfiguration<None> {
			/**
			 * Short-cut tag name for configuring a {@link None} action.
			 */
			String TAG_NAME = "none";
		}

		@Override
		public void handleNew(LayoutComponent component, Object newModel) {
			// Ignore.
		}

	}

	/**
	 * {@link PostCreateAction} setting the model to some component.
	 * 
	 * @see PostCreateAction.SetModel.Config#getTarget()
	 */
	@InApp
	class SetModel extends AbstractConfiguredInstance<SetModel.Config> implements PostCreateAction {

		/**
		 * Configuration options for {@link PostCreateAction.SetModel}.
		 */
		@TagName(Config.TAG_NAME)
		public interface Config extends PolymorphicConfiguration<SetModel> {

			/**
			 * Short-cut tag name for configuring a {@link SetModel} action.
			 */
			String TAG_NAME = "setModel";

			/**
			 * The target channel to propagate the new model to.
			 */
			@Mandatory
			@Format(ChannelFormat.class)
			PolymorphicConfiguration<? extends DirectLinking> getTarget();

			/**
			 * {@link ConfigurationValueProvider} that parses {@link Channel} definitions.
			 * 
			 * <p>
			 * In contrast to {@link com.top_logic.layout.ModelSpec.Format}, this format guarantees
			 * that only {@link Channel} {@link ModelSpec}s can be configured.
			 * </p>
			 */
			class ChannelFormat extends AbstractConfigurationValueProvider<Channel> {

				/**
				 * Creates a {@link ChannelFormat}.
				 */
				public ChannelFormat() {
					super(Channel.class);
				}

				@Override
				protected Channel getValueNonEmpty(String propertyName, CharSequence propertyValue)
						throws ConfigurationException {
					ModelSpec result = ModelSpec.Format.INSTANCE.getValue(propertyName, propertyValue);
					if (result == null) {
						return null;
					}
					if (result instanceof Channel) {
						return (Channel) result;
					}

					throw new ConfigurationException(I18NConstants.ERROR_COMPONENT_CHANNEL_EXPECTED, propertyName,
						propertyValue);
				}

				@Override
				protected String getSpecificationNonNull(Channel configValue) {
					return ModelSpec.Format.INSTANCE.getSpecification(configValue);
				}

			}
		}

		/**
		 * Creates a {@link PostCreateAction.SetModel} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public SetModel(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public void handleNew(LayoutComponent component, Object newModel) {
			ChannelLinking.updateModel(getConfig().getTarget(), component, newModel);
		}
	}

	/**
	 * {@link PostCreateAction} activating edit mode in an {@link EditMode} component.
	 */
	@InApp
	class SetEditMode extends AbstractConfiguredInstance<SetEditMode.Config> implements PostCreateAction {

		/**
		 * Configuration options for {@link PostCreateAction.SetEditMode}.
		 */
		@TagName("setEditMode")
		public interface Config extends PolymorphicConfiguration<SetEditMode> {
			/**
			 * The component to switch to edit mode.
			 */
			@Name("component-ref")
			@Mandatory
			@DefaultContainer
			ComponentRef getComponentRef();
		}

		/**
		 * Creates a {@link SetEditMode}.
		 */
		public SetEditMode(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public void handleNew(LayoutComponent component, Object newModel) {
			LayoutComponent editComponent = getConfig().getComponentRef().visit(DefaultRefVisitor.INSTANCE, component);
			if (editComponent instanceof EditMode) {
				((EditMode) editComponent).setEditMode();
			}
		}

	}

	/**
	 * Provides the result of a command as download.
	 * 
	 * <p>
	 * The command must create a {@link BinaryDataSource binary value} as result.
	 * </p>
	 */
	@InApp
	class DeliverAsDownload extends AbstractConfiguredInstance<DeliverAsDownload.Config> implements PostCreateAction {

		/**
		 * Configuration options for {@link PostCreateAction.DeliverAsDownload}.
		 */
		@TagName("deliverAsDownload")
		public interface Config extends PolymorphicConfiguration<DeliverAsDownload> {
			// Pure marker interface.
		}

		/**
		 * Creates a {@link DeliverAsDownload}.
		 */
		public DeliverAsDownload(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public void handleNew(LayoutComponent component, Object newModel) {
			BinaryDataSource data = (BinaryDataSource) CollectionUtil.getSingleValueFrom(newModel);
			if (data != null) {
				DefaultDisplayContext.getDisplayContext().getWindowScope().deliverContent(data);
			}
		}

	}

}

/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.component.factory;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.layout.basic.contextmenu.component.factory.ContextMenuUtil.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.component.ContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.config.ConfiguredContextMenuCommandsProvider;
import com.top_logic.layout.basic.contextmenu.config.ContextMenuCommandsProvider;
import com.top_logic.layout.basic.contextmenu.config.MetaContextMenuCommandsProvider;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link ContextMenuProvider} that uses custom {@link CommandHandler}s to build a context menu in
 * the context of a component.
 * 
 * <p>
 * By default, the custom commands are selected depending on the type of the context object using
 * the {@link MetaContextMenuCommandsProvider}.
 * </p>
 * 
 * @see Config#getCustomCommands()
 * @see MetaContextMenuCommandsProvider
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TypeBasedContextMenuFactory<C extends TypeBasedContextMenuFactory.Config<?>>
		extends AbstractConfiguredInstance<C> implements ContextMenuFactory {

	/**
	 * Configuration options for {@link ComponentContextMenuFactory}.
	 */
	public interface Config<I extends ComponentContextMenuFactory<?>> extends PolymorphicConfiguration<I> {

		/**
		 * @see #getCustomCommands()
		 */
		String CUSTOM_COMMANDS = "customCommands";

		/**
		 * @see #getTitleProvider()
		 */
		String TITLE_PROVIDER = "titleProvider";

		/**
		 * {@link LabelProvider} that displays the context object in the menu's title bar.
		 */
		@Name(TITLE_PROVIDER)
		@InstanceFormat
		@InstanceDefault(MetaLabelProvider.class)
		LabelProvider getTitleProvider();

		/**
		 * Commands added to the context menu.
		 */
		@Name(CUSTOM_COMMANDS)
		@DefaultContainer
		@ImplementationClassDefault(ConfiguredContextMenuCommandsProvider.class)
		@ItemDefault(MetaContextMenuCommandsProvider.class)
		PolymorphicConfiguration<? extends ContextMenuCommandsProvider> getCustomCommands();

	}

	final ContextMenuCommandsProvider _provider;

	/**
	 * Creates a {@link TypeBasedContextMenuFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TypeBasedContextMenuFactory(InstantiationContext context, C config) {
		super(context, config);
		_provider = context.getInstance(config.getCustomCommands());
	}

	@Override
	public ContextMenuProvider createContextMenuProvider(LayoutComponent component) {
		return new Provider(component);
	}

	/**
	 * {@link ContextMenuProvider} created by {@link TypeBasedContextMenuFactory}.
	 */
	protected class Provider implements ContextMenuProvider {

		private final LayoutComponent _component;

		/**
		 * Creates a {@link TypeBasedContextMenuFactory.Provider}.
		 * 
		 * @param component
		 *        See {@link #getComponent()}
		 */
		public Provider(LayoutComponent component) {
			_component = component;
		}

		/**
		 * The {@link LayoutComponent} this {@link ContextMenuProvider} operates on.
		 */
		protected LayoutComponent getComponent() {
			return _component;
		}

		@Override
		public boolean hasContextMenu(Object obj) {
			return _provider.hasContextMenuCommands(obj);
		}

		@Override
		public Menu getContextMenu(Object obj) {
			Object model = mapContext(obj);
			List<CommandModel> buttons = createButtons(model, createArguments(model));
			Menu result = ContextMenuUtil.toContextMenu(buttons);
			String title = getConfig().getTitleProvider().getLabel(model);
			if (!StringServices.isEmpty(title)) {
				result.setTitle(Fragments.text(title));
			}
			return result;
		}

		/**
		 * Maps the context objects to a target model of commands to invoke.
		 */
		protected final Object mapContext(Object context) {
			if (context instanceof Collection) {
				return mapSet((Collection<?>) context);
			}
			checkUnsupportedCollections(context);
			return mapContextObject(context);
		}

		private void checkUnsupportedCollections(Object context) {
			if (context instanceof Map || ((context != null) && context.getClass().isArray())) {
				throw new UnsupportedOperationException("Only single objects or collections of them are supported."
					+ " Actual value: " + Utils.debug(context));
			}
		}

		private Object mapSet(Collection<?> objects) {
			Set<Object> mapped = set();
			for (Object original : objects) {
				mapped.add(mapContextObject(original));
			}
			return mapped;
		}

		/**
		 * Hook to map <em>a single object</em> of the context object to a target model of commands
		 * to invoke.
		 * <p>
		 * Subclasses should never call this method but {@link #mapContext(Object)} instead. The
		 * context may be a collection of objects that need to be mapped individually.
		 * {@link #mapContext(Object)} takes care of that by calling this method for each object in
		 * the collection.
		 * </p>
		 */
		protected Object mapContextObject(Object obj) {
			return obj;
		}

		/**
		 * Creates all context menu entries.
		 *
		 * @param model
		 *        The model object for which the context menu is opened.
		 * @param arguments
		 *        The arguments to invoke the commands with.
		 * @return The (unordered) list of context menu entries.
		 */
		protected List<CommandModel> createButtons(Object model, Map<String, Object> arguments) {
			return createProviderButtons(model, arguments);
		}

		/**
		 * Creates context menu entries from {@link Config#getCustomCommands()}.
		 */
		protected final List<CommandModel> createProviderButtons(Object model, Map<String, Object> arguments) {
			return toButtons(getComponent(), arguments, _provider.getContextCommands(model));
		}
	}

}

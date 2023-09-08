/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.Function1;
import com.top_logic.mig.html.layout.ComponentInstantiationContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.ComponentReference;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * {@link CommandHandlerProxy} that delegates to a custom {@link BoundChecker} for access
 * privileges.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class CheckerProxyHandler extends CommandHandlerProxy {

	/**
	 * Configuration options for {@link CheckerProxyHandler}.
	 */
	@TagName(Config.DEFAULT_TAG_NAME)
	public interface Config extends ConfigBase<CheckerProxyHandler>, ComponentReference {

		/**
		 * @see #getCommand()
		 */
		String COMMAND = "command";

		/**
		 * The tag to configure a {@link CheckerProxyHandler} e.g. in
		 * {@link com.top_logic.mig.html.layout.LayoutComponent.Config#getButtons()}.
		 */
		String DEFAULT_TAG_NAME = "with-checker";

		@Override
		@Derived(fun = StringIdentity.class, args = @Ref({ Config.COMMAND, CommandHandler.Config.UI_TITLE }))
		String getCollapsedTitle();

		/**
		 * Algorithm computing the title for a collapsed {@link CheckerProxyHandler} configuration.
		 */
		class StringIdentity extends Function1<String, String> {
			@Override
			public String apply(String arg) {
				return arg;
			}
		}

		/**
		 * Name of the component to delegate access checks to.
		 */
		@Override
		ComponentName getName();

		/**
		 * {@link CommandHandler} to delegate execution to.
		 */
		@Name(COMMAND)
		ConfigBase<? extends CommandHandler> getCommand();

		/**
		 * Setter for {@link #getCommand()}.
		 */
		void setCommand(PolymorphicConfiguration<? extends CommandHandler> command);

	}

	private final BoundCheckerComponent _checker;

	private final CommandHandler _handler;

	/**
	 * Creates a {@link CheckerProxyHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CheckerProxyHandler(InstantiationContext context, Config config) {
		super(context, config);
		_checker = resolveChecker(context, config);
		_handler = createImpl(context, config);
	}

	private static BoundCheckerComponent resolveChecker(InstantiationContext context, Config config) {
		MainLayout mainlayout = ComponentInstantiationContext.getMainLayout(context);
		ComponentName checkerName = config.getName();
		LayoutComponent checkerComponent = mainlayout.getComponentByName(checkerName);
		BoundCheckerComponent checker;
		if (checkerComponent instanceof BoundCheckerComponent) {
			checker = (BoundCheckerComponent) checkerComponent;
		} else {
			context.error("Configured security component '" + checkerName + "' is not a checker (" + checkerComponent
				+ ") at " + config.location());
			checker = null;
		}
		return checker;
	}

	private static CommandHandler createImpl(InstantiationContext context, Config config) {
		return CommandHandlerFactory.getInstance().getCommand(context, config.getCommand());
	}

	@Override
	public boolean checkSecurity(LayoutComponent component, Object model, Map<String, Object> someValues) {
		if (_checker == null) {
			throw new IllegalStateException("Checker for '" + _handler.getID() + "' has not been resolved.");
		}
		// Note: This handler is only inserted, if the legacy option "openButtonSecComp" is given.
		// In these cases, the command's target model is not used and therefore must not be used for
		// computing the base model for security checks.
		BoundObject currentObject = _checker.getCurrentObject(getCommandGroup(), _checker.getModel());
		return _checker.allow(getCommandGroup(), currentObject);
	}

	@Override
	protected CommandHandler impl() {
		return _handler;
	}
}
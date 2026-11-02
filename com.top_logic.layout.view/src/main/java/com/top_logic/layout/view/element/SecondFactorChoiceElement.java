/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.listener.Registration;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackAlign;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.layout.view.login.SecondFactor;
import com.top_logic.layout.view.login.SecondFactorStep;
import com.top_logic.layout.view.login.SecondFactorsConfig;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link UIElement} that offers the {@link SecondFactor}s of the account on its input channel as
 * buttons, one per method.
 *
 * <p>
 * Which methods are offered, and what a button does, is the {@link SecondFactorStep} decided for the
 * account from the {@link SecondFactorsConfig}: the methods the account has set up, each button
 * opening the method's verification, or - for an account required to use a second factor that has
 * none set up - the methods it may set up, each button opening the method's set-up. Renders nothing
 * when the account needs no second factor, or when the channel holds no account.
 * </p>
 */
public class SecondFactorChoiceElement implements UIElement {

	/**
	 * Configuration for {@link SecondFactorChoiceElement}.
	 */
	@TagName("second-factor-choice")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		@Override
		@ClassDefault(SecondFactorChoiceElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel holding the account whose second-factor methods are offered.
		 */
		@Name(INPUT)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();
	}

	private final Config _config;

	/**
	 * Creates a new {@link SecondFactorChoiceElement} from configuration.
	 */
	@CalledByReflection
	public SecondFactorChoiceElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ReactStackControl result =
			new ReactStackControl(context, StackDirection.COLUMN, StackGap.COMPACT, StackAlign.STRETCH, false,
				List.of());
		result.setCssClass(_config.getCssClass());

		ViewChannel channel = context.resolveChannel(_config.getInput());
		List<ReactControl> buttons = new ArrayList<>();
		showMethods(context, result, buttons, channel.get());
		ChannelListener listener =
			(sender, oldValue, newValue) -> showMethods(context, result, buttons, newValue);
		Registration registration = channel.addListener(listener);
		result.addCleanupAction(registration::dispose);
		return result;
	}

	/**
	 * Replaces the buttons of the given stack with those for the given account.
	 *
	 * @param buttons
	 *        The buttons currently shown, updated to the ones shown afterwards.
	 */
	private static void showMethods(ViewContext context, ReactStackControl stack, List<ReactControl> buttons,
			Object value) {
		List<ReactControl> previous = new ArrayList<>(buttons);
		buttons.clear();
		if (value instanceof Person) {
			Person account = (Person) value;
			SecondFactorStep step = SecondFactorStep.decide(account);
			for (SecondFactor method : step.getMethods()) {
				buttons.add(createButton(context, step, account, method));
			}
		}
		stack.setChildren(buttons);
		for (ReactControl button : previous) {
			button.cleanupTree();
		}
	}

	private static ReactButtonControl createButton(ViewContext context, SecondFactorStep step, Person account,
			SecondFactor method) {
		String label = Resources.getInstance().getString(method.getLabel());
		ReactButtonControl button = new ReactButtonControl(context, label, ctx -> {
			step.start(ctx, account, method);
			return HandlerResult.DEFAULT_RESULT;
		});
		ThemeImage icon = method.getIcon();
		if (icon != null) {
			button.setImage(icon);
		}
		return button;
	}

}

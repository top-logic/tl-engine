/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.util.Map;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.ObjectRevealer;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.editor.AllComponentNames;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.provider.ComponentNameLabelProvider;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * Command that makes its target object visible in a component.
 *
 * <p>
 * In a tree, the ancestors of the object are expanded so that it is displayed; in a tree or table,
 * the row of the object is scrolled into the viewport. The object to reveal is the command's target
 * model.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
@Label("Show object in component")
public class RevealObjectCommand extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link RevealObjectCommand}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/** @see #getComponent() */
		String COMPONENT = "component";

		/**
		 * The component in which the target object is revealed.
		 *
		 * <p>
		 * If not set, the object is revealed in the component this command is executed on.
		 * </p>
		 */
		@Name(COMPONENT)
		@Options(fun = AllComponentNames.class)
		@OptionLabels(ComponentNameLabelProvider.class)
		ComponentName getComponent();

	}

	/**
	 * Creates a {@link RevealObjectCommand} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration for this command.
	 */
	public RevealObjectCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {
		LayoutComponent target = resolveTarget(component);
		target.makeVisible();
		if (target instanceof ObjectRevealer) {
			((ObjectRevealer) target).revealObject(model);
		} else if (target instanceof Selectable) {
			((Selectable) target).setSelected(model);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	private LayoutComponent resolveTarget(LayoutComponent component) {
		ComponentName targetName = ((Config) getConfig()).getComponent();
		if (targetName == null) {
			return component;
		}
		LayoutComponent target = component.getComponentByName(targetName);
		if (target == null) {
			throw new TopLogicException(I18NConstants.ERROR_REVEAL_COMPONENT_NOT_FOUND__NAME.fill(targetName));
		}
		return target;
	}

}

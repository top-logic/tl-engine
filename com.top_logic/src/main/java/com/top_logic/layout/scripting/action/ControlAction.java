/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.CommandListener;
import com.top_logic.layout.Control;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.control.AbstractFormFieldControlBase;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ApplicationAction} to directly trigger an internal {@link ControlCommand} of the
 * displaying {@link Control} of a {@link FormMember}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ControlAction extends ApplicationAction {

	@Override
	@ClassDefault(ControlAction.Op.class)
	public Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

	/**
	 * The scope in which to search the control.
	 * 
	 * <p>
	 * If not defined, the top-level scope is used. If the {@link #getModel()} is a
	 * {@link FormMember}, the component is implicitly defined by its {@link FormHandler}.
	 * </p>
	 */
	ModelName getComponent();

	/**
	 * @see #getComponent()
	 */
	void setComponent(ModelName value);

	/**
	 * The displayed {@link FormMember} model.
	 */
	ModelName getModel();

	/**
	 * @see #getModel()
	 */
	void setModel(ModelName value);

	/**
	 * The {@link Control} implementation class to look for.
	 */
	@ClassDefault(AbstractFormFieldControlBase.class)
	Class<? extends Control> getType();

	/**
	 * @see #getType()
	 */
	void setType(Class<? extends Control> value);

	/**
	 * The internal name of the {@link ControlCommand} to trigger.
	 */
	String getCommand();

	/**
	 * @see #getCommand()
	 */
	void setCommand(String value);

	/**
	 * Arguments to pass to the {@link #getCommand()}.
	 */
	List<Argument> getArguments();

	/**
	 * Configuration of a single {@link ControlCommand} argument.
	 */
	interface Argument extends NamedConfigMandatory {

		/**
		 * Description of the value.
		 */
		ModelName getValue();

		/**
		 * @see #getValue()
		 */
		void setValue(ModelName value);
	}

	/**
	 * Default implementation of {@link ControlAction}.
	 */
	class Op<S extends ControlAction> extends AbstractApplicationActionOp<S> {

		/**
		 * Creates a {@link Op} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Op(InstantiationContext context, S config) {
			super(context, config);
		}

		@Override
		protected Object processInternal(ActionContext context, Object argument) throws Throwable {
			S actionConfig = getConfig();
			Object resolvedModel = ModelResolver.locateModel(context, actionConfig.getModel());
			LayoutComponent component =
				(LayoutComponent) ModelResolver.locateModel(context, actionConfig.getComponent());
			if (component == null && resolvedModel instanceof FormMember) {
				FormMember model = (FormMember) resolvedModel;
				component = FormComponent.componentForMember(model);
			}
			if (component == null) {
				component = context.getMainLayout();
			}

			CommandListener control =
				(CommandListener) findControlFor(component.getEnclosingFrameScope(), resolvedModel);

			List<Argument> argumentConfig = actionConfig.getArguments();
			Map<String, Object> arguments;
			if (argumentConfig.isEmpty()) {
				arguments = Collections.emptyMap();
			} else {
				arguments = MapUtil.newMap(argumentConfig.size());
				for (Argument entry : argumentConfig) {
					arguments.put(entry.getName(), ModelResolver.locateModel(context, entry.getValue()));
				}
			}
			HandlerResult result =
				control.executeCommand(context.getDisplayContext(), actionConfig.getCommand(), arguments);

			ApplicationAssertions.assertSuccess(actionConfig, result);
			return argument;
		}

		private Control findControlFor(FrameScope frameScope, Object model) {
			Class<? extends Control> type = getConfig().getType();
			Control uniqueControl = null;
			while (true) {
				for (CommandListener commandListener : frameScope.getCommandListener()) {
					if (commandListener instanceof Control) {
						Control control = (Control) commandListener;

						if (control.getModel() == model) {
							if (control.isViewDisabled()) {
								// In background.
								continue;
							}
							if (!control.isVisible()) {
								// Not displayed.
								continue;
							}
							if (type != null && !type.isInstance(control)) {
								// Not relevant.
								continue;
							}
							if (uniqueControl != null) {
								throw ApplicationAssertions.fail(config.getModel(),
									"Control not unique: " + uniqueControl
										+ ", " + control);
							}

							uniqueControl = control;
						}
					}
				}
				if (uniqueControl == null) {
					FrameScope enclosingScope = frameScope.getEnclosingScope();
					if (enclosingScope == null) {
						throw ApplicationAssertions.fail(config.getModel(), "Control not found.");
					} else {
						frameScope = enclosingScope;
						continue;
					}
				}
				return uniqueControl;
			}
		}

	}

}

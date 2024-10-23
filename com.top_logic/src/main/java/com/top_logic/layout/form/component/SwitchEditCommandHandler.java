/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.form.component.edit.EditMode;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.HistoricDataDisableHistoricRule;
import com.top_logic.tool.execution.InViewModeExecutable;
import com.top_logic.tool.execution.MultiSelectionHidden;
import com.top_logic.tool.execution.NullModelDisabled;

/**
 * Default Command that will switch a Component to {EditModeAware#EDIT_MODE}.
 *
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class SwitchEditCommandHandler extends AJAXCommandHandler {

	public static final ExecutabilityRule EXEC_RULE = CombinedExecutabilityRule.combine(
		InViewModeExecutable.INSTANCE,
		NullModelDisabled.INSTANCE,
		HistoricDataDisableHistoricRule.INSTANCE,
		MultiSelectionHidden.INSTANCE);

    /** The ID of this command. */
    public static final String COMMAND_ID = "switchToAJAXEdit";

	/**
	 * Configuration for {@link SwitchEditCommandHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AJAXCommandHandler.Config {

		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		@Override
		CommandGroupReference getGroup();

	}

    /**
     * Creates a new instance of this class.
     */
    public SwitchEditCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    /**
     * Switch the EditModeAware Component to {EditModeAware#EDIT_MODE}
     */
    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		// Note: The command could have been triggered with a model not already set to the target
		// component, if it was triggered from a context menu entry of a master component.
		ChannelLinking.updateModel(getConfig().getTarget(), aComponent, model);

		if (aComponent instanceof EditMode) {
			EditMode theComp = (EditMode) aComponent;
			theComp.setEditMode();
        }

		return HandlerResult.DEFAULT_RESULT;
    }

    @Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return I18NConstants.SWITCH_TO_EDIT;
    }
    
    @Override
	protected ExecutabilityRule intrinsicExecutability() {
		return super.intrinsicExecutability().combine(ExecutabilityOfComponent.INSTANCE);
	}

	@Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
		// For legacy subclasses that do not use configuration.
		return EXEC_RULE;
    }

	private static class ExecutabilityOfComponent implements ExecutabilityRule {

		/**
		 * Singleton {@link ExecutabilityOfComponent} instance.
		 */
		public static final ExecutabilityOfComponent INSTANCE = new ExecutabilityOfComponent();

		private ExecutabilityOfComponent() {
			// Singleton constructor.
		}

		@Override
		public final ExecutableState isExecutable(LayoutComponent aComponent, Object model,
				Map<String, Object> someValues) {
			if (aComponent instanceof EditComponent editor) {
				return editor.getEditExecutability().isExecutable(aComponent, model, someValues);
			}

			return ExecutableState.EXECUTABLE;
		}

	}
}

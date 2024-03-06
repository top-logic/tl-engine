/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRuleManager;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Uses the {@link EditComponent}'s apply handler to update the model and then
 * (on success) switch back to view mode.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultSaveCommandHandler extends AbstractCommandHandler {

    /** The ID of this command. */
	public static final String COMMAND_ID = "defaultSave";

	/**
	 * Configuration for {@link DefaultSaveCommandHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		@Override
		CommandGroupReference getGroup();

	}

	/**
	 * Creates a new instance of this class.
	 */
	public DefaultSaveCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public ResKey getConfirmKey(LayoutComponent component, Map<String, Object> arguments) {
		ResKey result = super.getConfirmKey(component, arguments);
		if (result != null) {
			return result;
		}

		return getApplyHandler((Editor) component).getConfirmKey(component, arguments);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		Editor editor = (Editor) aComponent;

		HandlerResult theResult = handleApply(aContext, editor, model, someArguments);
		
		if (theResult.isSuccess()) {
			if (editor.openedAsDialog() && editor.saveClosesDialog()) {
                editor.closeDialog();
                return theResult;
			}
			editor.setViewMode();
		}
		return theResult;
	}

	/**
	 * Finds and processes the apply command of the given component.
	 */
	protected HandlerResult handleApply(DisplayContext aContext, Editor editor, Object model,
			Map<String, Object> someArguments) {
		CommandHandler theApplyHandler = getApplyHandler(editor);

		return theApplyHandler.handleCommand(aContext, editor.self(), model, someArguments);
	}

	/**
	 * Finds the apply command of the given component.
	 */
	protected CommandHandler getApplyHandler(Editor editor) {
		CommandHandler theApplyHandler = editor.getApplyCommandHandler();
		assert theApplyHandler != null : "A default save command is only registered, if an apply command exists.";

		return theApplyHandler;
	}

	@Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return I18NConstants.SAVE;
	}
	
	@Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
		return ExecutabilityRuleManager.getRule(ExecutabilityRuleManager.KEY_GENERAL_SAVE);
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), new OnlyWithCanonicalModel(this));
	}

	/** 
     * Delegate {@link #isExecutable(LayoutComponent, Object, Map)} to applyHandler.
     * 
     * Will fall back to InEditModeExecutable in case apllyCommand cannot be resolved.
     * 
     * Trying to cache any of the Objects involved will in make this class
     * or the {@link DefaultSaveCommandHandler} state-full 
     * so better leave this as is :-(  
     */
	public static class DelegateToApplyExecutability implements ExecutabilityRule {
	    
		/**
		 * Singleton {@link DelegateToApplyExecutability} instance.
		 */
	    public static final ExecutabilityRule INSTANCE = new DelegateToApplyExecutability();
	    
	    @Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			CommandHandler applyHandler = ((Editor) aComponent).getApplyCommandHandler();
			if (applyHandler == null) {
				return ExecutableState.NOT_EXEC_HIDDEN;
	        }
			return applyHandler.isExecutable(aComponent, model, someValues);
	    }
	}

}
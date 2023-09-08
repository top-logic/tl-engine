/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link AbstractCommandHandler} that cares about validating a {@link FormContext} of a
 * {@link FormComponent} and handling errors and warnings.
 * 
 * @implNote Actual processing should happen by overriding
 *           {@link #applyChanges(LayoutComponent, FormContext, Object, Map)} (without calling the
 *           super implementation that is for legacy compatibility only).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFormCommandHandler extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link AbstractFormCommandHandler}
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * @see #getIgnoreWarnings()
		 */
		String IGNORE_WARNINGS_PROPERTY = "ignoreWarnings";

		/**
		 * Whether the warning check is disabled for this command.
		 * 
		 * @see AbstractApplyCommandHandler.GlobalConfig#getWarningsDisabled()
		 * @see FormComponent.Config#getIgnoreWarnings()
		 * @see AbstractApplyCommandHandler.Config#getIgnoreWarnings()
		 */
		@Name(IGNORE_WARNINGS_PROPERTY)
		Decision getIgnoreWarnings();

	}

	/**
	 * {@link ThreadLocal} storage to temporarily disable the warning check, when continuing the
	 * request.
	 */
	static final ThreadLocal<Boolean> IGNORE_WARNINGS = new ThreadLocal<>();

	/**
	 * Whether warnings are ignored for this command.
	 */
	private boolean _ignoreCommandWarnings;

	/**
	 * Creates a {@link AbstractFormCommandHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractFormCommandHandler(InstantiationContext context, Config config) {
		super(context, config);

		boolean implClassIgnoresWarning =
			ignoreWarnings().toBoolean(AbstractApplyCommandHandler.warningsDisabledGlobally());
		_ignoreCommandWarnings = config.getIgnoreWarnings().toBoolean(implClassIgnoresWarning);
	}

	/**
	 * Allow sub-classes "configure" the confirm warnings behavior.
	 * 
	 * @deprecated Configure in {@link CommandHandlerFactory}, do not call.
	 */
	@Deprecated
	protected Decision ignoreWarnings() {
		return Decision.DEFAULT;
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent component,
			Object model, Map<String, Object> someArguments) {
		if (!supportsModel(model)) {
			throw new TopLogicException(model == null ? errorKeyNoModel() : errorKeyNotSupported());
		}

		FormContext formContext = ((FormHandler) component).getFormContext();

		if (!ignoreFormContext()) {
			if (formContext == null) {
				throw new TopLogicException(errorKeyNoModel());
			}

			if (!formContext.checkAll()) {
				HandlerResult result = new HandlerResult();
				onInvalidInput(component, formContext, result);
				return result;
			}

			// One must not try do custom check unless the form context
			// is valid. Since custom checks must access the values of
			// fields, it is required for those checks that fields have
			// no (parse) errors. If custom checks are performed with an
			// invalid form context, all those checks would have to
			// inspect their fields, whether the field currently has a
			// value or not. Failing to do so causes a nasty exception
			// that should not be seen by the user.
			validateAdditional(component, formContext, model);

			if (checkWarnings(component) && formContext.hasWarnings()) {
				// Stop processing.
				HandlerResult suspended = HandlerResult.suspended();

				WarningsDialog.openWarningsDialog(aContext.getWindowScope(), getResourceKey(component), formContext,
					resumeContinuation(suspended));

				return suspended;
			}
		}

		return applyChanges(component, formContext, model, someArguments);
	}

	/**
	 * Wraps the given {@link Exception} into a generic error message.
	 * 
	 * @deprecated Use customized error handling with problem reasons.
	 */
	@Deprecated
	protected final TopLogicException reportProblem(Exception ex) {
		throw new TopLogicException(errorKeyApplyFailed(ex), ex);
	}

	/**
	 * Error key for a <code>null</code> model.
	 */
	protected ResKey errorKeyNoModel() {
		return I18NConstants.ERROR_NO_MODEL;
	}

	/**
	 * Error key for an unsupported model.
	 */
	protected ResKey errorKeyNotSupported() {
		return I18NConstants.ERROR_MODEL_NOT_SUPPORTED;
	}

	/**
	 * Creates the error message for a failed commit.
	 * 
	 * @param ex
	 *        The error received.
	 */
	protected ResKey errorKeyApplyFailed(Exception ex) {
		return I18NConstants.ERROR_APPLY_FAILED__MSG.fill(ex.getMessage());
	}

	/**
	 * Callback invoked, if the current {@link FormContext} had errors.
	 * 
	 * @param component
	 *        The context component.
	 * @param formContext
	 *        The current {@link FormContext}.
	 * @param result
	 *        The {@link HandlerResult} to fill with errors.
	 */
	protected void onInvalidInput(LayoutComponent component, FormContext formContext, HandlerResult result) {
		AbstractApplyCommandHandler.fillHandlerResultWithErrors(formContext, result);
	}

	/**
	 * Indicates whether this command handler supports the current model of the component or not.
	 * 
	 * @param model
	 *        the current model of the component
	 * @return <code>true</code>, if this command handler supports the current model of the
	 *         component, <code>false</code> otherwise
	 */
	protected boolean supportsModel(Object model) {
		return true;
	}

	/**
	 * Hook for sub classes to extend the validation handling.
	 * 
	 * @param component
	 *        The context component.
	 * @param formContext
	 *        The context to be validated
	 * @param model
	 *        The object currently used by the component.
	 * 
	 * @throws TopLogicException
	 *         If validation fails.
	 */
	protected void validateAdditional(LayoutComponent component, FormContext formContext, Object model)
			throws TopLogicException {
		// Hook for subclasses.
	}

	/**
	 * Actually performs the change.
	 * 
	 * @param component
	 *        The context component.
	 * @param formContext
	 *        The component's {@link FormComponent}.
	 * @param model
	 *        The component model.
	 * @param arguments
	 *        The current command arguments.
	 * @return The result of the command, see
	 *         {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 */
	protected HandlerResult applyChanges(LayoutComponent component, FormContext formContext, Object model,
			Map<String, Object> arguments) {
		// Compatibility with the legacy API.
		HandlerResult result = new HandlerResult();
		try {
			applyChanges((FormComponent) component, model, formContext, result, arguments);
		} catch (Exception ex) {
			reportProblem(ex);
		}
		return result;
	}

	/**
	 * @deprecated Implement {@link #applyChanges(LayoutComponent, FormContext, Object, Map)}
	 */
	@SuppressWarnings("unused")
	@Deprecated
	protected void applyChanges(FormComponent component, Object model, FormContext formContext,
			HandlerResult result, Map<String, Object> arguments) throws Exception {
		// Legacy implementation hook.
	}

	/**
	 * Hook that decides whether not to validate the {@link FormContext}.
	 */
	protected boolean ignoreFormContext() {
		return false;
	}

	private boolean checkWarnings(LayoutComponent component) {
		boolean componentIgnoresWarning;
		LayoutComponent.Config componentConfig = component.getConfig();
		if (componentConfig instanceof FormComponent.Config) {
			componentIgnoresWarning =
				((FormComponent.Config) componentConfig).getIgnoreWarnings().toBoolean(_ignoreCommandWarnings);
		} else {
			componentIgnoresWarning = _ignoreCommandWarnings;
		}
		boolean ignoreWarnings = ignoreFormContext() || componentIgnoresWarning || warningsDisabledTemporarily();
		return !ignoreWarnings;
	}

	/**
	 * Create a continuation {@link Command} that marks warnings to be ignored.
	 * 
	 * @param suspended
	 *        The {@link HandlerResult} that suspended the execution for confirming warnings.
	 */
	public static Command resumeContinuation(HandlerResult suspended) {
		return confirmContinuation(suspended.resumeContinuation());
	}

	/**
	 * Create a continuation {@link Command} that marks warnings to be ignored.
	 * 
	 * @param customContinuation
	 *        The {@link Command} that should be executed with warnings ignored.
	 */
	public static Command confirmContinuation(final Command customContinuation) {
		return new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				IGNORE_WARNINGS.set(Boolean.TRUE);
				try {
					return customContinuation.executeCommand(context);
				} finally {
					IGNORE_WARNINGS.remove();
				}
			}
		};
	}

	/**
	 * Whether warnings are disabled for the current execution only.
	 */
	public static boolean warningsDisabledTemporarily() {
		return IGNORE_WARNINGS.get() == Boolean.TRUE;
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), new OnlyWithCanonicalModel(this));
	}

}

/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.check.ChangeHandler;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.layoutRenderer.LayoutControlRenderer;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.provider.ButtonComponentButtonsControlProvider;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * The class {@link DirtyHandling} handles changed made in form contexts before
 * some command will be executed.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DirtyHandling {

	private static final TypedAnnotatable.Property<Boolean> SKIP_DIRTY_HANDLING_PROPERTY =
		TypedAnnotatable.property(Boolean.class, "skipDirtyHandling");

	/**
	 * Additional command attribute that marks an execution as being already approved by the user.
	 */
	public static final String SKIP_DIRTY_HANDLING = "skipDirtyHandling";

	private static final DirtyHandling instance = new DirtyHandling();

	private static final ResPrefix RES_PREFIX = I18NConstants.DIRTY_HANDLING;

	private static final DisplayValue DIRTY_HANDLING_DIALOG_TITLE = ConstantDisplayValue.EMPTY_STRING;

	private static final String APPLY_CHANGES = "applyChanges";

	private static final String DISCARD_CHANGES = "discardChanges";

	private static final String CANCEL = "cancel";

	private static final String BUTTONS_GROUP = "buttons";

	private static final String DIRTY_HANDLING_CSS_CLASS = "dirtyHandlingDialog";

	private static final ResKey FORM_ERROR_REASON = I18NConstants.ERROR_INVALID_INPUT;

	private static final ResKey FORM_NO_TAB_APPLY_REASON = I18NConstants.ERROR_APPLY_NOT_POSSIBLE;
	
	/**
	 * Returns the sole instance of {@link DirtyHandling}
	 */
	public static final DirtyHandling getInstance() {
		return instance;
	}

	private final boolean debug = DebuggingConfig.configuredInstance().getDirtyHandlingDebug();

	private DirtyHandling() {
	}

	private Element getGroupTemplate(String messageKey) {
		String templateAsString = ""
			+ "<t:group"
			+ 	" xmlns='" + HTMLConstants.XHTML_NS + "'" 
			+ 	" xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
			+	" xmlns:p='" + FormPatternConstants.PATTERN_NS + "'" + ">"
			+ 		"<div class='layoutControl'"
			+ 			LayoutControlRenderer.getLayoutConstraintInformation(100, DisplayUnit.PERCENT)
			+ 			LayoutControlRenderer.getLayoutInformation(Orientation.VERTICAL, 100)
			+       ">"
			+ 			"<div class='layoutControl'"
			+ 				LayoutControlRenderer.getLayoutConstraintInformation(100, DisplayUnit.PERCENT)
			+ 				LayoutControlRenderer.getLayoutInformation(Orientation.HORIZONTAL, 100)
			+           ">"
			+ 			"<div class='" + DIRTY_HANDLING_CSS_CLASS + "'>"
			+ 				"<div class='message'>"
			+ 					"<h2><t:text key='messageHeader'/></h2>"
			+ 					"<t:text key='" + messageKey + "'/>"
			+				"</div>";
		if (debug) {
			templateAsString +=
							"<div class='changes'>"
			+					"<t:text key='changedMembers'/>"
			+					"<p:field name='changedMember'>"
			+						"<t:list>"
			+ 							"<ul>"
			+								"<t:items>"
			+	 								"<li>"
			+ 										"<p:self />"
			+ 									"</li>"
			+ 								"</t:items>"
			+ 							"</ul>"
			+ 						"</t:list>"
			+ 					"</p:field>"
			+ 				"</div>";
		}

		DisplayDimension themeHeight = ThemeFactory.getTheme().getValue(com.top_logic.layout.Icons.BUTTON_COMP_HEIGHT);
		// See above.
		templateAsString += "</div>"
			+ 			"</div>"
			+			"<div class='layoutControl'"
			+ 				LayoutControlRenderer.getLayoutConstraintInformation(themeHeight.getValue(), themeHeight.getUnit())
			+ 				LayoutControlRenderer.getLayoutInformation(Orientation.HORIZONTAL, 100)
			+           ">"
			+ 				"<p:field name='" + BUTTONS_GROUP + "'>"
			+ 					"<t:list>"
			+ 						"<div class='cmdButtons'>"
			+ 							"<t:items/>"
			+ 						"</div>"
			+ 					"</t:list>"
			+ 				"</p:field>"
			+ 			"</div>"
			+ 		"</div>"
			+ "</t:group>";
		
		return DOMUtil.getFirstElementChild(DOMUtil.parse(templateAsString).getDocumentElement());
	}

	/**
	 * Checks whether some of the affected {@link FormHandler} of the given {@link CheckScope} is
	 * dirty. If <code>true</code> a {@link VetoException} is thrown which opens the "dirty dialog".
	 * 
	 * @param checkScope
	 *        The {@link CheckScope} which returns the {@link FormHandler} to check for
	 *        modifications.
	 * 
	 * @throws VetoException
	 *         iff some of the handler is "dirty".
	 * 
	 * @see DirtyHandling#checkVeto(Collection)
	 * @see DirtyHandling#checkDirty(Collection)
	 */
	public static void checkVeto(CheckScope checkScope) throws VetoException {
		checkVeto(checkScope.getAffectedFormHandlers());
	}

	/**
	 * Checks whether some of the given {@link FormHandler} is dirty. If <code>true</code> a
	 * {@link VetoException} is thrown which opens the "dirty dialog".
	 * 
	 * @param handlers
	 *        The {@link FormHandler}s to check for modifications.
	 * 
	 * @throws VetoException
	 *         iff some of the handler is "dirty".
	 * 
	 * @see DirtyHandling#checkVeto(CheckScope)
	 * @see DirtyHandling#checkDirty(Collection)
	 */
	public static void checkVeto(Collection<? extends ChangeHandler> handlers) throws VetoException {
		if (!DirtyHandling.getInstance().checkDirty(handlers)) {
			return;
		}
		throw new DirtyHandlingVeto(handlers);
	}

	/**
	 * Checks whether dirty handling is necessary and in case it is, the method
	 * opens a dialog to inform the user about it.
	 * 
	 * @param context
	 *        the {@link DisplayContext} which will be used to execute the
	 *        handler. see
	 *        {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)}
	 * @param handler
	 *        the {@link CommandHandler} which will be executed.
	 * @param component
	 *        the {@link LayoutComponent} which will be used to execute the
	 *        handler. see
	 *        {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)}
	 * @param someArguments
	 *        the arguments for the handler. see
	 *        {@link CommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)}
	 * 
	 * @return whether dirty handling has happened.
	 */
	public boolean checkDirty(DisplayContext context, CommandHandler handler, LayoutComponent component,
			Map<String, Object> someArguments) {
		if (skipDirtyHandling(context)) {
			return false;
		}

		Collection<? extends ChangeHandler> affectedFormHandlers =
			handler.checkScopeProvider().getCheckScope(component).getAffectedFormHandlers();
		boolean dirty = checkDirty(affectedFormHandlers);
		if (dirty) {
			Map<String, Object> arguments = MapUtil.newMap(someArguments.size() + 1);
			arguments.putAll(someArguments);
			arguments.put(DirtyHandling.SKIP_DIRTY_HANDLING, Boolean.TRUE);
			openConfirmDialog(new CommandHandlerCommand(handler, component, arguments), affectedFormHandlers,
				context.getWindowScope());
		}
		return dirty;
	}

	private boolean skipDirtyHandling(DisplayContext context) {
		return Utils.isTrue(context.get(DirtyHandling.SKIP_DIRTY_HANDLING_PROPERTY));
	}

	/**
	 * This method opens a dialog to inform the user that some {@link ChangeHandler} have changes
	 * and must be checked. The dialog allows the user to decide whether the changes shall be
	 * stored, discarded or whether the action shall be canceled.
	 * 
	 * @param command
	 *        the command to execute if the user does not cancel the action. action.
	 * @param affectedFormHandlers
	 *        The {@link ChangeHandler}s which may have changes. At least one of them must be
	 *        changed.
	 * @param scope
	 *        the scope used to open the dialog.
	 * @see #checkDirty(Collection)
	 */
	public void openConfirmDialog(Command command, Collection<? extends ChangeHandler> affectedFormHandlers,
			WindowScope scope) {
		DialogWindowControl dialog = createDialogWindowControl(scope, command, affectedFormHandlers);
		scope.openDialog(dialog);
	}

	/**
	 * This method checks whether some {@link FormHandler} in the given
	 * {@link Collection} has a changed {@link FormHandler#getFormContext()
	 * formContext}.
	 * 
	 * @param affectedFormHandlers
	 *        a {@link Collection} of {@link FormHandler}s to check.
	 * @return whether some {@link FormHandler} has a changed
	 *         {@link FormContext}.
	 */
	public boolean checkDirty(Collection<? extends ChangeHandler> affectedFormHandlers) {
		if (affectedFormHandlers.isEmpty()) {
			return false;
		}
		for (Iterator<? extends ChangeHandler> it = affectedFormHandlers.iterator(); it.hasNext();) {
			ChangeHandler currentHandler = it.next();
			if (currentHandler.isChanged()) {
				return true;
			}
		}
		return false;
	}

	private DialogWindowControl createDialogWindowControl(WindowScope scope, Command command,
			Collection<? extends ChangeHandler> affectedFormHandlers) {
		FrameScope ids = scope.getTopLevelFrameScope();
		String fcId = ids.createNewID();
		DefaultDialogModel dialogModel =
			new DefaultDialogModel(layoutData(), DIRTY_HANDLING_DIALOG_TITLE, false, true, null);
		FormContext ctx = new FormContext(fcId, RES_PREFIX);

		FormGroup buttons = new FormGroup(BUTTONS_GROUP, RES_PREFIX);
		Command closeAction = dialogModel.getCloseAction();

		boolean hasError = false;
		// whether all affected FormHandlers have an apply command
		boolean canApply = true;
		for (Iterator<? extends ChangeHandler> theIter = affectedFormHandlers.iterator(); theIter.hasNext();) {
			ChangeHandler theHandler = theIter.next();
			if (theHandler.isChanged()) {
				if (canApply && theHandler.getApplyClosure() == null) {
					canApply = false;
				}
				if (!hasError && theHandler.hasError()) {
					hasError = true;
				}
			} else {
				// Ensure that all remaining handles have changes.
				theIter.remove();
			}
		}
		
		Command applyHander = new ApplyChanges(command, closeAction, affectedFormHandlers);
		CommandField theApply = FormFactory.newCommandField(APPLY_CHANGES, applyHander);
		
		if (!canApply) {
			theApply.setNotExecutable(FORM_NO_TAB_APPLY_REASON);
		} else {
			if (hasError) {
				theApply.setNotExecutable(FORM_ERROR_REASON);
			}
		}
		
		buttons.addMember(theApply);
		dialogModel.setDefaultCommand(theApply);
		Command discardHandler = new DiscardChanges(command, closeAction, affectedFormHandlers);
		CommandField discardChanges = FormFactory.newCommandField(DISCARD_CHANGES, discardHandler);
		buttons.addMember(discardChanges);
		CommandField cancel = FormFactory.newCommandField(CANCEL, closeAction);
		buttons.addMember(cancel);
		ctx.addMember(buttons);

		if (debug) {
			FormGroup changedMembers = new FormGroup("changedMember", RES_PREFIX);
			ctx.addMember(changedMembers);
			Iterator<? extends ChangeHandler> it = affectedFormHandlers.iterator();
			while (it.hasNext()) {
				ChangeHandler current = it.next();
				if (current.isChanged()) {
					String description = current.getChangeDescription();
					if (description != null) {
						changedMembers.addMember(FormFactory.newStringField(ids.createNewID(), description, true));
					}
				}
			}
		}
		final DialogWindowControl dialog = new DialogWindowControl(dialogModel);

		final Element groupTemplate = getGroupTemplate("message");
		FormGroupControl contentControl = new FormGroupControl(ctx, ButtonComponentButtonsControlProvider.INSTANCE, groupTemplate, RES_PREFIX);

		LayoutControlAdapter contentLayout = new LayoutControlAdapter(contentControl);
		dialog.setChildControl(contentLayout);

		return dialog;
	}

	private LayoutData layoutData() {
		return new DefaultLayoutData(Icons.DIRTY_DIALOG_WIDTH.get(), 100, Icons.DIRTY_DIALOG_HEIGHT.get(), 100,
			Scrolling.AUTO);
	}

	/**
	 * {@link DirtyHandlingAction} which discards the changes
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static class DiscardChanges extends DirtyHandlingAction {

		public DiscardChanges(Command stoppedCommand, Command closeCommand,
				Collection<? extends ChangeHandler> dirtyHandlers) {
			super(stoppedCommand, closeCommand, dirtyHandlers);
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			// Don't care about errors. Discard has to work
			for (ChangeHandler currentHandler : _dirtyHandlers) {
				Command discardClosure = currentHandler.getDiscardClosure();
				if (discardClosure == null) {
					Logger.error("Handler '" + currentHandler
						+ "'participates in change handling without discard closure.", DirtyHandlingAction.class);
				} else {
					Boolean oldValue = context.get(SKIP_DIRTY_HANDLING_PROPERTY);
					context.set(SKIP_DIRTY_HANDLING_PROPERTY, true);
					try {
						HandlerResult result = discardClosure.executeCommand(context);
						if (!result.isSuccess()) {
							return result;
						}
					} finally {
						context.set(SKIP_DIRTY_HANDLING_PROPERTY, oldValue);
					}
				}
			}
			return continueExecution(context);
		}
	}

	/**
	 * {@link DirtyHandlingAction} which applies the changes.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static class ApplyChanges extends DirtyHandlingAction {

		private Collection<ChangeHandler> _formerlyFailedHandlers;

		public ApplyChanges(Command stoppedCommand, Command closeCommand,
				Collection<? extends ChangeHandler> dirtyHandlers) {
			super(stoppedCommand, closeCommand, dirtyHandlers);
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			Collection<? extends ChangeHandler> handlersToApply;
			if (_formerlyFailedHandlers == null) {
				handlersToApply = _dirtyHandlers;
			} else {
				handlersToApply = _formerlyFailedHandlers;
			}
			return execute(context, handlersToApply);
		}

		HandlerResult execute(DisplayContext context, Collection<? extends ChangeHandler> handlersToApply) {
			List<ChangeHandler> remainingHandlers = null;
			List<ResKey> encodedErrors = null;
			HandlerResult suspended = null;
			for (ChangeHandler currentHandler : CollectionUtil.nonNull(handlersToApply)) {
				Command applyClosure = currentHandler.getApplyClosure();
				if (applyClosure == null) {
					/* Apply closure changed. That may occur for example when during opening the
					 * dirty dialog the apply command could be executed but when executing applying
					 * the command is not longer possible. */
					CommandModel ok = MessageBox.button(ButtonType.OK);
					Resources resources = context.getResources();
					String warnMessage = resources.getString(I18NConstants.SPONTANEOUS_NO_APPLY_CLOSURE);
					MessageBox.confirm(context, MessageType.WARNING, warnMessage, ok);
				} else {
					if (suspended != null) {
						remainingHandlers = lazyAdd(remainingHandlers, currentHandler);
						continue;
					}

					Boolean oldValue = context.get(SKIP_DIRTY_HANDLING_PROPERTY);
					context.set(SKIP_DIRTY_HANDLING_PROPERTY, true);
					try {
						final HandlerResult result = applyClosure.executeCommand(context);
						if (result.isSuspended()) {
							// Cannot handle more than one confirm upon apply.
							suspended = result;
							continue;
						}
						if (!result.isSuccess()) {
							encodedErrors = lazyAddAll(encodedErrors, result.getEncodedErrors());
							TopLogicException problem = result.getException();
							if (problem != null) {
								encodedErrors = lazyAdd(encodedErrors, problem.getErrorKey());
							}
							remainingHandlers = lazyAdd(remainingHandlers, currentHandler);
						}
					} finally {
						context.set(SKIP_DIRTY_HANDLING_PROPERTY, oldValue);
					}
				}
			}

			if (suspended != null) {
				suspended.appendContinuation(getContinuation(remainingHandlers));
				return suspended;
			}

			if (!CollectionUtil.isEmptyOrNull(encodedErrors)) {
				_formerlyFailedHandlers = remainingHandlers;

				// Show the errors, leave the confirm dialog open, the user can still decide about
				// dropping changes, or stopping the command.
				HandlerResult result = new HandlerResult();
				result.setEncodedErrors(encodedErrors);
				return result;
			}

			return continueExecution(context);

		}

		private static <T> List<T> lazyAdd(List<T> list, T value) {
			if (list == null) {
				list = new ArrayList<>();
			}
			list.add(value);
			return list;
		}

		private static <T> List<T> lazyAddAll(List<T> list, Collection<T> values) {
			if (list == null) {
				list = new ArrayList<>(values);
			} else {
				list.addAll(values);
			}
			return list;
		}

		private final Command getContinuation(final Collection<ChangeHandler> skippedHandlers) {
			return new Command() {
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					return execute(context, skippedHandlers);
				}
			};
		}

	}

	/**
	 * Actions to execute in dialog which informs about dirty handling.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private abstract static class DirtyHandlingAction implements Command {

		/**
		 * The form handlers which were dirty
		 */
		protected final Collection<? extends ChangeHandler> _dirtyHandlers;

		/**
		 * The command which actually closes the dialog
		 */
		private final Command _closeCommand;

		/**
		 * The command which was stopped since something was dirty.
		 */
		private final Command _stoppedCommand;

		public DirtyHandlingAction(Command stoppedCommand, Command closeCommand,
				Collection<? extends ChangeHandler> dirtyHandlers) {
			this._stoppedCommand = stoppedCommand;
			this._dirtyHandlers = dirtyHandlers;
			this._closeCommand = closeCommand;
		}

		/**
		 * Closes the dialog and continues the stopped command.
		 * 
		 * @return the result of the stopped command.
		 */
		protected HandlerResult continueExecution(DisplayContext context) {
			_closeCommand.executeCommand(context);

			return _stoppedCommand.executeCommand(context);
		}

	}
}

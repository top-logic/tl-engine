/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import static com.top_logic.basic.col.filter.FilterFactory.*;
import static java.util.Collections.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandHandlerCommand;
import com.top_logic.layout.basic.DirtyHandling;
import com.top_logic.layout.form.component.AbstractCreateCommandHandler;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.AbstractDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutControlFactory;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.tool.boundsec.CloseModalDialogCommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerUtil;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The class {@link DialogComponent} is a simple container for one
 * {@link LayoutComponent} which is shown in a dialog.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DialogComponent extends AbstractDialogModel {

	private final class CleanupAction implements Command {
		
		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			if (isClosed()) {
				// Break endless recursion that would occur due to
				// the fact that regular close handlers represent
				// both, the cleanup action and the actual dialog
				// close trigger.
				return HandlerResult.DEFAULT_RESULT;
			}
			
			LayoutComponent dialogContents = getContentComponent();
			
			// TODO: Dialog-Close event may be better than cancel button heuristics?
			FindCloseDialogHandler handlerSearch = new FindCloseDialogHandler();
			dialogContents.acceptVisitorRecursively(handlerSearch);
			CloseModalDialogCommandHandler closeHandler = handlerSearch.getCloseHandler();
			final LayoutComponent targetComponent = handlerSearch.getTargetComponent();
			
			if (ScriptingRecorder.isRecordingActive()) {
				recordCloseDialogAction(dialogContents, targetComponent);
			}
			if (closeHandler == null) {
				closeLocally();
				dialogContents.closeDialog();
				return HandlerResult.DEFAULT_RESULT;
			} else {
				Map<String, Object> emptyArgs = Collections.<String,Object>emptyMap();
				boolean dirty = DirtyHandling.getInstance().checkDirty(context, closeHandler, targetComponent, emptyArgs);
				if (dirty) {
					return HandlerResult.DEFAULT_RESULT;
				} else {
					closeLocally();
					return CommandHandlerUtil.handleCommand(closeHandler, context, targetComponent, emptyArgs);
				}
			}
		}

		private void recordCloseDialogAction(LayoutComponent dialogContents, LayoutComponent targetComponent) {
			LayoutComponent target;
			if (targetComponent == null) {
				target = dialogContents;
			} else {
				target = targetComponent;
			}
			if (!ScriptingRecorder.mustNotRecord(target)) {
				ScriptingRecorder.recordAction(ActionFactory.closeDialogAction(target));
			}
		}
		
	}

	/**
	 * Search for the innermost {@link CloseModalDialogCommandHandler}.
	 * 
	 * TODO #2215
	 * 
	 * There will usually be two CloseModalDialogCommandHandlers, one at the outermost
	 * (Bound-)Layout, and the CloseModalDialogCommandHandlers a the the main DialogComopnent
	 * (which may be sub-classed for Cleanup actions ...) 
	 */
	public static class FindCloseDialogHandler extends DefaultDescendingLayoutVisitor {
		private static final Filter<? super Object> IS_CLOSE_DIALOG_HANDLER;
		static {
			Filter<? super Object> isCloseHandler = createClassFilter(CloseModalDialogCommandHandler.class);
			Filter<? super Object> isCreateHandler = createClassFilter(AbstractCreateCommandHandler.class);
			IS_CLOSE_DIALOG_HANDLER = and(isCloseHandler, not(isCreateHandler));
		}
		
		private CloseModalDialogCommandHandler closeHandler;

		private LayoutComponent targetComponent;
		
		@Override
		public boolean visitLayoutComponent(LayoutComponent component) {
			if (! component.isVisible()) {
				return false;
			}
			
			Collection commands = component.getCommands();
			if (commands != null) {
				Object closeHandler = FilterUtil.findFirst(IS_CLOSE_DIALOG_HANDLER, commands);
				if (closeHandler != null) {
					this.closeHandler = (CloseModalDialogCommandHandler) closeHandler;
					this.targetComponent = component;
				}
			}
			return true;
		}
		
		public CloseModalDialogCommandHandler getCloseHandler() {
			return closeHandler;
		}
		
		public LayoutComponent getTargetComponent() {
			return targetComponent;
		}
	}

	private boolean closedLocally;

	private final LayoutComponent _content;

	private final Command _cleanupAction = new CleanupAction();

	private DialogComponent(LayoutComponent dialogContent, LayoutData layoutData, HTMLFragment title,
			boolean resizable, boolean closeButton, String helpID) {
		super(layoutData, title, resizable, closeButton, helpID, ConfigKey.component(dialogContent));
		if (resizable) {
			LayoutControlFactory.checkForComponentNameValidity(dialogContent);
		}
		_content = dialogContent;
	}

	public static DialogComponent newDialog(LayoutComponent dialogContent, DialogInfo aDialogInfo,
			HTMLFragment aDialogTitle) {
		if (dialogContent == null) {
			throw new IllegalArgumentException("'aDialogContent' must not be 'null'.");
		}
		if (aDialogInfo == null) {
			throw new IllegalArgumentException("'aDialogInfo' must not be 'null'.");
		}

		DialogComponent dialogComponent = new DialogComponent(dialogContent,
			new DefaultLayoutData(aDialogInfo),
			aDialogTitle,
			aDialogInfo.isResizable(),
			aDialogInfo.isClosable(),
			aDialogInfo.getHelpId());
		dialogComponent.setMaximized(aDialogInfo.isOpenMaximized());
		return dialogComponent;
	}

	/**
	 * The {@link LayoutComponent} displayed by this {@link DialogComponent}.
	 * 
	 * @return Never <code>null</code>.
	 */
	public final LayoutComponent getContentComponent() {
		return _content;
	}

	@Override
	public Command getDefaultCommand() {
		Command command = super.getDefaultCommand();
		if (command != null) {
			return command;
		}
		/* This has to be resolved dynamically, as for example in dialogs with tabs the default
		 * command might depend on the selected tab. */
		return findUnambiguousDefaultCommand(getContentComponent());
	}

	private static Command findUnambiguousDefaultCommand(LayoutComponent dialogContent) {
		Set<Command> defaultCommands = collectDefaultCommands(dialogContent);
		if (defaultCommands.size() == 1) {
			return CollectionUtil.getFirst(defaultCommands);
		}
		/* Either no or multiple default commands. */
		return null;
	}

	private static Set<Command> collectDefaultCommands(LayoutComponent component) {
		if (component.getDefaultCommand() != null) {
			return Set.of(new CommandHandlerCommand(component.getDefaultCommand(), component));
		}
		if (component instanceof LayoutContainer) {
			return collectDefaultCommands((LayoutContainer) component);
		}
		return emptySet();
	}

	private static Set<Command> collectDefaultCommands(LayoutContainer component) {
		Set<Command> commands = new HashSet<>();
		for (LayoutComponent child : component.getChildList()) {
			if (child.isVisible()) {
				commands.addAll(collectDefaultCommands(child));
			}
		}
		return commands;
	}

	@Override
	public Command getCloseAction() {
		return _cleanupAction;
	}
	
	/*package protected*/ void closeLocally() {
		this.closedLocally = true;
	}
	
	/*package protected*/ void closeDirectly() {
		closeLocally();
		notifyListeners(CLOSED_PROPERTY, this, false, true);
	}
	
	@Override
	public boolean isClosed() {
		return this.closedLocally;
	}
}

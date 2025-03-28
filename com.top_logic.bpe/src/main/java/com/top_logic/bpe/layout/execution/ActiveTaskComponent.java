/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.bpe.bpml.model.Iconified;
import com.top_logic.bpe.bpml.model.Lane;
import com.top_logic.bpe.bpml.model.ManualTask;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Task;
import com.top_logic.bpe.execution.engine.GuiEngine;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.TlBpeExecutionFactory;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.element.layout.formeditor.FormEditorUtil;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.component.DefaultEditAttributedComponent;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.ExecutableState.CommandVisibility;
import com.top_logic.util.TLContext;

/**
 * {@link DefaultEditAttributedComponent} that displays the configured attributes for a given
 * {@link Task}.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class ActiveTaskComponent extends DefaultEditAttributedComponent implements DisplayDescriptionAware {

	/**
	 * Creates an {@link ActiveTaskComponent}.
	 */
	public ActiveTaskComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}
	
	@Override
	public FormDefinition getDisplayDescription() {
		Node node = getToken().getNode();
		return ((ManualTask) node).getDisplayDescription();
	}

	/**
	 * The current process-controlled object.
	 */
	public ProcessExecution getProcessExecution() {
		return getToken().getProcessExecution();
	}

	/**
	 * The displayed workflow state.
	 */
	public Token getToken() {
		Object model = getModel();
		return getToken(model);
	}

	/**
	 * The displayed workflow state calculated from the components model. Either the {@link Token}
	 * itself or a {@link ProcessExecution}. In case of {@link ProcessExecution} get the first
	 * {@link Token} where the current user is an actor.
	 */
	public Token getToken(Object model) {
		if (model instanceof ProcessExecution pe) {
			for (Token token : pe.getActiveTokens()) {
				if (isActor(token)) {
					return token;
				}
			}
		}
		return (Token) model;
	}

	@Override
	public List<String> getExcludeList() {
		List<String> res = super.getExcludeList();

		TLClass tokenType = TlBpeExecutionFactory.getTokenType();
		TLModelUtil.addPartNames(res, tokenType);
		res.remove(Token.PROCESS_EXECUTION_ATTR);
		return res;
	}

	@Override
	protected boolean supportsInternalModelNonNull(Object anObject) {
		if (anObject instanceof ProcessExecution) {
			return true;
		}
		return super.supportsInternalModelNonNull(anObject);
	}

	@Override
	protected boolean observeAllTypes() {
		return true;
	}

	@Override
	protected boolean receiveModelChangedEvent(Object aModel, Object changedBy) {
		WebFolderUtils.updateWebfolder(this, aModel);
		return super.receiveModelChangedEvent(aModel, changedBy);
	}

	@Override
	protected void addMoreAttributes(Object someObject, AttributeFormContext someContext, boolean create) {
		super.addMoreAttributes(someObject, someContext, create);

		if (someObject == null) {
			return;
		}

		Token token = getToken(someObject);

		Node node = token.getNode();
		if (node instanceof ManualTask manualTask) {
			TLObject pe = getContextModel();
			TLClass tType = (TLClass) pe.tType();
			FormDefinition fd = manualTask.getDisplayDescription();
			FormMode formMode = create ? FormMode.CREATE : FormMode.EDIT;
			FormEditorUtil.createEditorGroup(someContext, tType, fd, pe, formMode);
		}
	}

	@Override
	public TLObject getContextModel() {
		return getProcessExecution();
	}

	/**
	 * The icon for the current task.
	 */
	@CalledFromJSP
	public ThemeImage getIcon() {
		Token model = getToken();
		if (model == null) {
			// Can happen after editing this component (when opened in a dialog).
			return null;
		}
		ThemeImage icon = null;
		if (model instanceof ProcessExecution) {
			icon = MetaResourceProvider.INSTANCE.getImage(model, Flavor.ENLARGED);
		} else {
			icon = ((Iconified) model).getIcon();
			if (icon == null) {
				icon = MetaResourceProvider.INSTANCE.getImage(model, Flavor.ENLARGED);
			}
		}
		return icon;
	}
	
	/**
	 * Whether current user is an actor for given {@link Token}.
	 */
	public static boolean isActor(Token token) {
		Node currentNode = token.getNode();
		Lane currentLane = currentNode.getLane();

		// get the processExecution to calculate isActor
		ProcessExecution currentProcessExecution = token.getProcessExecution();

		// Get the current person
		Person currentPerson = TLContext.currentUser();

		// Check if the current person has access rights
		if ((GuiEngine.getInstance().isActor(currentPerson, currentLane, currentProcessExecution)
			|| currentPerson.isAdmin())) {
			return true;
		}
		return false;
	}

	/**
	 * {@link ExecutabilityRule} that decides, whether a currently displayed workflow task can
	 * proceed.
	 */
	public static class FinishTaskRule implements ExecutabilityRule {

		private static final ExecutableState FINISH_TASK_DISABLED_STATE = ExecutableState.createDisabledState(I18NConstants.FINISH_TASK_DISABLED);

		private static final ExecutableState FINISH_TASK_ACCESS_DENIED_STATE =
			ExecutableState.createDisabledState(I18NConstants.FINISH_TASK_ACCESS_DENIED);

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			// Check if the current person has access rights
			if (!isActor(((ActiveTaskComponent) aComponent).getToken(model))) {
				return FINISH_TASK_ACCESS_DENIED_STATE;
			}

			// Only check form validity if user is an actor
			EditComponent editComponent = (EditComponent) aComponent;
			if (editComponent.getFormContext().checkAll()) {
				return ExecutableState.EXECUTABLE;
			}
			return FINISH_TASK_DISABLED_STATE;
		}

	}

	/**
	 * {@link ExecutabilityRule} that controls the edit mode of a task editor.
	 * 
	 * @see ManualTask#getCanEdit()
	 */
	public static class CanEditTask implements ExecutabilityRule {
	
		/**
		 * Singleton {@link CanEditTask} instance.
		 */
		public static final CanEditTask INSTANCE = new CanEditTask();
	
		private CanEditTask() {
			// Singleton constructor.
		}
	
		private static final ExecutableState CANNOT_EDIT =
			new ExecutableState(CommandVisibility.HIDDEN, I18NConstants.CANNOT_EDIT_TASK);
	
		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model,
				Map<String, Object> someValues) {
			if (model instanceof Token token) {
				if (token.getNode() instanceof ManualTask task) {
					if (!task.getCanEdit()) {
						return CANNOT_EDIT;
					}
				}
			}
			return ExecutableState.EXECUTABLE;
		}
	
	}

}

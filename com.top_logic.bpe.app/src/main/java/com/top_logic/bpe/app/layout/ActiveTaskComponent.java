/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Iconified;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Task;
import com.top_logic.bpe.bpml.model.impl.ManualTaskBase;
import com.top_logic.bpe.execution.engine.GuiEngine;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.TlBpeExecutionFactory;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.element.layout.formeditor.FormEditorUtil;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.component.DefaultEditAttributedComponent;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.model.FolderField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.TLContext;

/**
 * {@link DefaultEditAttributedComponent} that displays the configured attributes for a given
 * {@link Task}.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class ActiveTaskComponent extends DefaultEditAttributedComponent implements DisplayDescriptionAware {

	public static final String EXECUTION_ATTRIBUTES = "executionAttributes";

	public ActiveTaskComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}
	
	@Override
	public FormDefinition getDisplayDescription() {
		Node node = getToken().getNode();
		return ((ManualTaskBase) node).getDisplayDescription();
	}

	public ProcessExecution getProcessExecution() {
		return getToken().getProcessExecution();
	}

	public Token getToken() {
		return (Token) getModel();
	}

	public static boolean isTable(FormContext formContext, String attributeID) {
		FormMember member = formContext.getMember(attributeID);
		if (member instanceof SelectField) {
			SelectField sf = (SelectField) member;
			if (sf.isMultiple()) {
				return true;
			} else {
				return false;
			}
		}
		return member instanceof FolderField;
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

		Token token = (Token) someObject;
		Task node = (Task) token.getNode();

		if (node instanceof ManualTaskBase) {
			TLObject pe = getContextModel();
			TLClass tType = (TLClass) pe.tType();
			FormDefinition fd = ((ManualTaskBase) node).getDisplayDescription();
			FormMode formMode = create ? FormMode.CREATE : FormMode.EDIT;
			FormEditorUtil.createEditorGroup(someContext, tType, fd, pe, formMode);
		}
	}

	@Override
	public TLObject getContextModel() {
		return getProcessExecution();
	}

	public ThemeImage getIcon() {
		Object model = getModel();
		ThemeImage res = null;
		if (model instanceof Iconified) {
			res = ((Iconified) model).getIcon();
		}
		if (res == null) {
			res = MetaResourceProvider.INSTANCE.getImage(model, Flavor.ENLARGED);
		}
		return res;
	}

	public static class FinishTaskRule implements ExecutabilityRule {

		private static final ExecutableState FINISH_TASK_DISABLED_STATE = ExecutableState.createDisabledState(I18NConstants.FINISH_TASK_DISABLED);

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			EditComponent editComponent = (EditComponent) aComponent;
			if (editComponent.getFormContext().checkAll()) {
				return ExecutableState.EXECUTABLE;
			} else {
				return FINISH_TASK_DISABLED_STATE;
			}
		}

	}

	public static class ActiveTasksListModelBuilder extends ProcessExecutionListModelBuilder {

		public static class ActorFilter implements Filter<Token> {

			private Person _currentPerson;

			public ActorFilter() {
				PersonManager r = PersonManager.getManager();
				_currentPerson = TLContext.currentUser();
			}

			@Override
			public boolean accept(Token token) {
				return GuiEngine.getInstance().isActor(_currentPerson, token);
			}

		}

		public ActiveTasksListModelBuilder(InstantiationContext context, Config config) throws ConfigurationException {
			super(context, config);
		}

		@Override
		public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
			return listElement instanceof Token && ((Token) listElement).getUserRelevant()
				&& new ActorFilter().accept((Token) listElement);
		}

		@Override
		public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
			return getActiveTokensForCurrentUser((Collaboration) businessModel);
		}

		/**
		 * Tile is shown for collaboration, independant of existing tasks
		 * 
		 * @see com.top_logic.bpe.app.layout.ProcessExecutionListModelBuilder#supportsModel(java.lang.Object,
		 *      com.top_logic.mig.html.layout.LayoutComponent)
		 */
		@Override
		public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
			return aModel instanceof Collaboration;
//			if (aModel instanceof Collaboration) {
//				return getActiveTokensForCurrentUser((Collaboration) aModel).size() > 0;
//			}
//			return false;
		}

		/**
		 * a list with the active tokens which the current user has in the given
		 *         collaboration
		 */
		public static List<Token> getActiveTokensForCurrentUser(Collaboration collaboration) {
			Set<ProcessExecution> allProcessExecutions = getAllProcessExecutions(collaboration);
			ActorFilter filter = new ActorFilter();
			List<Token> result = new ArrayList<>();
			for (ProcessExecution pe : allProcessExecutions) {
				FilterUtil.filterInto(result, filter, pe.getUserRelevantTokens());
			}
			return result;
		}
	}

}

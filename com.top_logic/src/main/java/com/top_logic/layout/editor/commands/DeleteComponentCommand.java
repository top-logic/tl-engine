/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.layout.editor.scripting.CommandActionWithIdentifiers;
import com.top_logic.layout.editor.scripting.CommandActionWithIdentifiers.CommandActionOpWithIdentifiers;
import com.top_logic.layout.editor.scripting.Identifiers;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.mig.html.layout.FindLinkedComponentsVisitor;
import com.top_logic.mig.html.layout.I18NConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutResourceRemover;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.ConfirmCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InDesignModeExecutable;
import com.top_logic.tool.execution.UniqueToolbarCommandRule;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CommandHandler} to delete the layout by the given component.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class DeleteComponentCommand extends ConfirmCommandHandler {

	/**
	 * {@link AbstractCommandHandler#getID() Default ID} under which an instance of this class is
	 * configured in the {@link CommandHandlerFactory}.
	 */
	public static final String DEFAULT_COMMAND_ID = "deleteComponentCommand";

	/**
	 * Creates a {@link DeleteComponentCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DeleteComponentCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HandlerResult internalHandleCommand(DisplayContext context, LayoutComponent component,
			Object model,
			Map<String, Object> arguments) {
		LayoutComponent editedComponent = EditableComponentExecutability.resolveEditedComponent(component, arguments);

		FindLinkedComponentsVisitor visitor = new FindLinkedComponentsVisitor();
		editedComponent.acceptVisitorRecursively(visitor);

		Set<String> linkedLayoutKeys = getLinkedLayoutKeys(visitor);
		Set<String> visitedLayoutKeys = getVisitedLayoutKeys(visitor);

		linkedLayoutKeys.removeAll(visitedLayoutKeys);

		if (linkedLayoutKeys.isEmpty()) {
			Identifiers newIdentifiers;
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

			boolean isRecordingActive = ScriptingRecorder.isRecordingActive();
			if (isRecordingActive) {
				ScriptingRecorder.pause();
			}

			try (Transaction tx = kb.beginTransaction()) {
				newIdentifiers = deleteComponents(editedComponent, visitedLayoutKeys);
				tx.commit();
			}

			if (isRecordingActive) {
				ScriptingRecorder.resume();
			}
			recordDeletion(component, newIdentifiers);

			return HandlerResult.DEFAULT_RESULT;
		} else {
			return HandlerResult.error(I18NConstants.OUTER_REFERENCES_DELETION_ERROR);
		}
	}

	private void recordDeletion(LayoutComponent component, Identifiers newIdentifiers) {
		if (!ScriptingRecorder.isRecordingActive()) {
			return;
		}
		CommandActionWithIdentifiers action = ActionFactory.newApplicationAction(CommandActionWithIdentifiers.class,
			CommandActionOpWithIdentifiers.class);
		Map<String, Boolean> singletonMap = Collections.singletonMap(CONFIRMED_KEY, Boolean.TRUE);
		ActionFactory.setCommandParameters(action, component.getName(), getID(), singletonMap);
		action.setIdentifiers(newIdentifiers);
		ScriptingRecorder.recordAction(action);
	}

	@Override
	protected ResKey getConfirmMessage(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		LayoutComponent editedComponent = EditableComponentExecutability.resolveEditedComponent(component, arguments);

		return I18NConstants.DELETE_COMPONENT_CONFIRMATION__NAME.fill(LayoutUtils.getLabel(editedComponent));
	}

	private Identifiers deleteComponents(LayoutComponent component, Set<String> visitedLayoutKeys)
			throws TopLogicException {
		String rootLayoutKey = LayoutTemplateUtils.getNonNullNameScope(component);
		LayoutComponent parent = component.getParent();
		String parentLayoutKey = LayoutTemplateUtils.getNonNullNameScope(parent);

		Identifiers newIdentifiers;
		try {
			newIdentifiers = updateParent(rootLayoutKey, parentLayoutKey);
		} catch (ConfigurationException ex) {
			throw new TopLogicException(
				com.top_logic.layout.editor.I18NConstants.ERROR_LAYOUT_CONFIGURATION_INVALID__LAYOUT
					.fill(parentLayoutKey),
				ex);
		}

		deleteComponents(visitedLayoutKeys);
		try {
			replaceParent(component, parentLayoutKey);
		} catch (ConfigurationException ex) {
			throw new TopLogicException(I18NConstants.COMPONENT_REPLACING_AFTER_DELETION_ERROR, ex);
		}

		return newIdentifiers;
	}

	private void replaceParent(LayoutComponent component, String parentLayoutKey) throws ConfigurationException {
		LayoutComponent.Config newLayout = createParentConfig(parentLayoutKey);
		component.getMainLayout().replaceComponent(parentLayoutKey, newLayout);
	}

	private LayoutComponent.Config createParentConfig(String layoutKey) {
		try {
			BufferingProtocol protocol = new BufferingProtocol();
			LayoutComponent.Config result = LayoutStorage.getInstance().getOrCreateLayoutConfig(layoutKey);
			if (protocol.hasErrors()) {
				throw new TopLogicException(I18NConstants.COMPONENT_REPLACING_AFTER_DELETION_ERROR)
					.initDetails(ResKey.text(protocol.getError()));
			}
			return result;
		} catch (ConfigurationException exception) {
			throw new TopLogicException(I18NConstants.COMPONENT_REPLACING_AFTER_DELETION_ERROR, exception);
		}
	}

	private void deleteComponents(Set<String> visitedLayoutKeys) {
		visitedLayoutKeys.stream()
			.forEach(visitedLayoutKey -> LayoutTemplateUtils.deletePersistentTemplateLayout(visitedLayoutKey));
	}

	private Identifiers updateParent(String rootLayoutKey, String parentLayoutKey) throws ConfigurationException {
		TLLayout layout = LayoutTemplateUtils.getOrCreateLayout(parentLayoutKey);
		ConfigurationItem templateArgs = layout.getArguments();
		new LayoutResourceRemover().remove(templateArgs, Collections.singleton(rootLayoutKey));
		Identifiers newIdentifiers = LayoutTemplateUtils.replaceInnerTemplates(templateArgs);
		LayoutTemplateUtils.storeLayout(parentLayoutKey, layout.getTemplateName(), templateArgs);
		return newIdentifiers;
	}

	private Set<String> getVisitedLayoutKeys(FindLinkedComponentsVisitor visitor) {
		return visitor
			.getLinkedComponentsByComponent()
			.keySet()
			.stream()
			.map(comp -> LayoutTemplateUtils.getNonNullNameScope(comp))
			.collect(Collectors.toSet());
	}

	private Set<String> getLinkedLayoutKeys(FindLinkedComponentsVisitor visitor) {
		return visitor
			.getLinkedComponentsByComponent()
			.values()
			.stream()
			.flatMap(linkedComponents -> linkedComponents.stream())
			.map(comp -> LayoutTemplateUtils.getNonNullNameScope(comp))
			.collect(Collectors.toSet());
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(InDesignModeExecutable.INSTANCE, new UniqueToolbarCommandRule(this),
			EditableComponentExecutability.INSTANCE);
	}

	@Override
	protected boolean mustNotRecord(DisplayContext context, LayoutComponent component,
			Map<String, Object> someArguments) {
		return true;
	}

	@Override
	protected DialogWindowControl createConfirmDialog(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> someArguments, Command continuation) {
		DialogWindowControl confirmDialog =
			super.createConfirmDialog(context, component, model, someArguments, continuation);
		ScriptingRecorder.annotateAsDontRecord(confirmDialog.getDialogModel());
		return confirmDialog;
	}

}

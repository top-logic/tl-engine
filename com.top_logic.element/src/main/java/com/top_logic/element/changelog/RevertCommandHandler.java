/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog;

import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.changelog.model.ChangeSet;
import com.top_logic.element.changelog.model.trans.TransientChangeSet;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} for reverting a {@link ChangeSet}.
 */
public class RevertCommandHandler extends AbstractCommandHandler {

	/**
	 * Configuration for {@link RevertCommandHandler}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/**
		 * Whether to ask for confirmation in case of conflicts. Then, the user has the option to
		 * cancel the undo operation.
		 */
		@BooleanDefault(true)
		boolean isCheckConflicts();

	}

	/**
	 * Creates a {@link RevertCommandHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RevertCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	private Config config() {
		return (Config) getConfig();
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {

		ChangeSet change = (ChangeSet) model;

		TransientChangeSet undo = change.revert();

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(undo.getMessage())) {
			List<ResKey> problems = undo.apply();

			if (!problems.isEmpty()) {
				if (config().isCheckConflicts()) {
					tx.rollback(I18NConstants.UNDO_CANCELED_BY_USER);
					return openConfirmDialog(context, component, kb, change, undo, problems);
				}
				InfoService.showWarningList(problems);
			}

			tx.commit();
		}

		component.invalidate();

		return HandlerResult.DEFAULT_RESULT;
	}

	private HandlerResult openConfirmDialog(DisplayContext openContext, LayoutComponent component, KnowledgeBase kb,
			ChangeSet change, TransientChangeSet undo, List<ResKey> problems) {
		CommandModel okButton = MessageBox.button(ButtonType.OK, confirmCtx -> {
			try (Transaction tx = kb.beginTransaction(undo.getMessage())) {
				undo.apply();
				tx.commit();
			}
			component.invalidate();
			return HandlerResult.DEFAULT_RESULT;
		});
		HTMLFragment confirmMessage = InfoService.getMessageList(problems,
			I18NConstants.CONFLICTS_WHEN_UNDO_REVISION__REV.fill(change));

		return MessageBox.newBuilder(MessageType.CONFIRM)
			.message(confirmMessage)
			.layout(DefaultLayoutData.newLayoutData(
				DisplayDimension.dim(500, DisplayUnit.PIXEL),
				DisplayDimension.dim(400, DisplayUnit.PIXEL)))
			.buttons(okButton, MessageBox.button(ButtonType.CANCEL))
			.confirm(openContext.getWindowScope());
	}

}

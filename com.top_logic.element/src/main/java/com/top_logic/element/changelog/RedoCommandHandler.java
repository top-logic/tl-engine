/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog;

import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
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
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CommandHandler} that re-applies the most recent undo in the change log window of its
 * target model.
 *
 * <p>
 * The target model is interpreted as a subtree root. A {@code null} model operates on the whole
 * application change log.
 * </p>
 *
 * <p>
 * When the undo stack has been invalidated by a regular forward change committed after the most
 * recent undo, the underlying {@link ChangeSetReverter#findRedoCandidate(TLObject, int, boolean)}
 * raises a {@link TopLogicException}; this handler lets it propagate so the standard error UI
 * informs the user.
 * </p>
 */
@InApp
public class RedoCommandHandler extends AbstractCommandHandler {

	/**
	 * Configuration for {@link RedoCommandHandler}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/** @see #isCheckConflicts() */
		String CHECK_CONFLICTS = "check-conflicts";

		/** @see #getWindowSize() */
		String WINDOW_SIZE = "window-size";

		/** @see #getIncludeSubtree() */
		String INCLUDE_SUBTREE = "include-subtree";

		/**
		 * Whether to open a confirmation dialog when the redo cannot be applied cleanly.
		 */
		@Name(CHECK_CONFLICTS)
		@BooleanDefault(true)
		boolean isCheckConflicts();

		/**
		 * How many of the most recent change log entries are inspected to locate the undo to
		 * redo. {@code 0} means no limit. This bounds the search, not the result. The default of
		 * 50 keeps an interactive Redo button responsive even on a long-lived application; raise
		 * it (or set to {@code 0}) only when older undos must remain reachable.
		 */
		@Name(WINDOW_SIZE)
		@IntDefault(50)
		@Label("Search window size")
		int getWindowSize();

		/**
		 * Whether the entire composition subtree of the target model is considered when
		 * searching for the undo to redo.
		 */
		@Name(INCLUDE_SUBTREE)
		@BooleanDefault(true)
		boolean getIncludeSubtree();

	}

	/**
	 * Creates a {@link RedoCommandHandler} from configuration.
	 */
	@CalledByReflection
	public RedoCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	private Config config() {
		return (Config) getConfig();
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {

		TLObject root = model instanceof TLObject tlModel ? tlModel : null;
		Config cfg = config();

		ChangeSet target = ChangeSetReverter.findRedoCandidate(root, cfg.getWindowSize(), cfg.getIncludeSubtree());
		if (target == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		TransientChangeSet undo = target.revert();
		KnowledgeBase kb = root != null ? root.tKnowledgeBase() : PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(undo.getMessage())) {
			List<ResKey> problems = undo.apply();

			if (!problems.isEmpty()) {
				if (cfg.isCheckConflicts()) {
					tx.rollback(I18NConstants.UNDO_CANCELED_BY_USER);
					return openConfirmDialog(context, component, kb, target, undo, problems);
				}
				InfoService.showWarningList(problems);
			}

			tx.commit();
		}

		component.invalidate();
		return HandlerResult.DEFAULT_RESULT;
	}

	private HandlerResult openConfirmDialog(DisplayContext openContext, LayoutComponent component, KnowledgeBase kb,
			ChangeSet target, TransientChangeSet undo, List<ResKey> problems) {
		CommandModel okButton = MessageBox.button(ButtonType.OK, confirmCtx -> {
			try (Transaction tx = kb.beginTransaction(undo.getMessage())) {
				undo.apply();
				tx.commit();
			}
			component.invalidate();
			return HandlerResult.DEFAULT_RESULT;
		});
		HTMLFragment confirmMessage = InfoService.getMessageList(problems,
			I18NConstants.CONFLICTS_WHEN_UNDO_REVISION__REV.fill(target));

		return MessageBox.newBuilder(MessageType.CONFIRM)
			.message(confirmMessage)
			.layout(DefaultLayoutData.newLayoutData(
				DisplayDimension.dim(500, DisplayUnit.PIXEL),
				DisplayDimension.dim(400, DisplayUnit.PIXEL)))
			.buttons(okButton, MessageBox.button(ButtonType.CANCEL))
			.confirm(openContext.getWindowScope());
	}

}

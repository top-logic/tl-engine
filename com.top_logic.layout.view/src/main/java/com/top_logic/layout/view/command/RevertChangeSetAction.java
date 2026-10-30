/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.changelog.ChangeSetReverter;
import com.top_logic.element.changelog.model.ChangeSet;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.ConfirmDialogControl;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.view.ViewMessages;
import com.top_logic.util.Resources;

/**
 * Interruptible {@link ViewAction} reverting the commit ({@link ChangeSet}) it receives.
 *
 * <p>
 * A commit that has not been reverted is undone; a commit that has already been reverted is
 * re-applied by reverting its revert. The revert is committed in a transaction of its own with
 * the revert message of the reverted commit. An input that is not a {@link ChangeSet} is left
 * alone; the chain continues with {@code null} as result.
 * </p>
 *
 * <p>
 * When later commits have modified the same objects, applying the revert reports conflicts. With
 * {@link Config#isCheckConflicts()} the revert is rolled back and a dialog lists the conflicts: on
 * confirm the revert is applied anyway and the chain resumes, on decline the chain aborts. A chain
 * running without a dialog aborts on conflicts. Without {@link Config#isCheckConflicts()} the revert
 * is committed and the conflicts are reported as a warning.
 * </p>
 *
 * <p>
 * The result of the action is the commit it received.
 * </p>
 *
 * <p>
 * Example: revert the commit shown in the channel {@code changeSet}.
 * </p>
 *
 * <pre>
 * &lt;generic-command inputs="changeSet"&gt;
 *   &lt;executability&gt;
 *     &lt;null-input-disabled/&gt;
 *   &lt;/executability&gt;
 *   &lt;revert-change-set/&gt;
 * &lt;/generic-command&gt;
 * </pre>
 *
 * @see ChangeSetReverter#revertTarget(ChangeSet)
 * @see ChangeSetReverter#revert(KnowledgeBase, ChangeSet, boolean)
 */
@InApp
public class RevertChangeSetAction extends InterruptibleViewAction {

	/**
	 * Configuration for {@link RevertChangeSetAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<RevertChangeSetAction>, WithValueArgument {

		/** Tag name of a {@link RevertChangeSetAction} in an action chain. */
		String TAG_NAME = "revert-change-set";

		/** Configuration name of {@link #isCheckConflicts()}. */
		String CHECK_CONFLICTS = "check-conflicts";

		@Override
		@ClassDefault(RevertChangeSetAction.class)
		Class<? extends RevertChangeSetAction> getImplementationClass();

		/**
		 * Whether the user is asked before a revert conflicting with later commits is applied.
		 *
		 * <p>
		 * If not set, a conflicting revert is applied and the conflicts are reported as a warning.
		 * </p>
		 */
		@Name(CHECK_CONFLICTS)
		@BooleanDefault(true)
		boolean isCheckConflicts();
	}

	private final boolean _checkConflicts;

	/**
	 * Creates a {@link RevertChangeSetAction} from configuration.
	 */
	@CalledByReflection
	public RevertChangeSetAction(InstantiationContext context, Config config) {
		_checkConflicts = config.isCheckConflicts();
	}

	@Override
	public void execute(ReactContext context, Object input, Continuation continuation) {
		if (!(input instanceof ChangeSet change)) {
			continuation.resume(null);
			return;
		}

		ChangeSet target = ChangeSetReverter.revertTarget(change);
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		List<ResKey> problems = ChangeSetReverter.revert(kb, target, !_checkConflicts);
		if (problems.isEmpty()) {
			continuation.resume(change);
			return;
		}

		Resources resources = Resources.getInstance();
		if (!_checkConflicts) {
			ViewMessages.warning(context,
				ResKey.text(message(resources, I18NConstants.REVERTED_DESPITE_CONFLICTS__REV.fill(target),
					problems)));
			continuation.resume(change);
			return;
		}

		DialogManager dialogManager = context.getDialogManager();
		if (dialogManager == null) {
			// Nobody to ask (e.g. headless) - a conflicting revert is not applied unasked.
			continuation.abort();
			return;
		}

		ConfirmDialogControl.openDialog(context, dialogManager,
			resources.getString(I18NConstants.REVERT_CONFLICTS_TITLE),
			message(resources,
				com.top_logic.element.changelog.I18NConstants.CONFLICTS_WHEN_UNDO_REVISION__REV.fill(target),
				problems),
			null,
			null,
			() -> {
				ChangeSetReverter.revert(kb, target, true);
				continuation.resume(change);
			},
			() -> continuation.abort(),
			continuation.tone());
	}

	/**
	 * The given header followed by the given problems, one per line.
	 */
	private static String message(Resources resources, ResKey header, List<ResKey> problems) {
		StringBuilder result = new StringBuilder(resources.getString(header));
		for (ResKey problem : problems) {
			result.append("\n- ").append(resources.getString(problem));
		}
		return result.toString();
	}

	/**
	 * Asks the user in a dialog, or tells them about conflicts.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}

/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.revision;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.ConfirmCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.TLContext;

/**
 * Command to revert a revision represented by a {@link ChangeSet}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RevertSelectedRevisionCommand extends ConfirmCommandHandler {

	private static class MultipleBranchCommit extends Exception {

		long _branch1;

		long _branch2;

		public MultipleBranchCommit(long branch1, long branch2) {
			this._branch1 = branch1;
			this._branch2 = branch2;
		}

	}

	/**
	 * Configuration of the {@link RevertSelectedRevisionCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfirmCommandHandler.Config {

		@Override
		@ListDefault(ChangeSetModelExecutability.class)
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();

		@Override
		@FormattedDefault(TARGET_SELECTION_SELF)
		ModelSpec getTarget();

	}

	private KnowledgeBase _kb;

	/**
	 * Creates a new {@link RevertSelectedRevisionCommand}.
	 */
	public RevertSelectedRevisionCommand(InstantiationContext context, Config config) {
		super(context, config);
		_kb = PersistencyLayer.getKnowledgeBase();
	}

	@Override
	public HandlerResult internalHandleCommand(DisplayContext context, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		ChangeSet cs = (ChangeSet) model;
		long revision = cs.getRevision();
		HistoryManager hm = _kb.getHistoryManager();
		Branch branch;
		try {
			branch = hm.getBranch(getBranchContext(cs));
		} catch (MultipleBranchCommit ex) {
			return HandlerResult.error(I18NConstants.COMMIT_ON_MULTIPLE_BRANCHES.fill(ex._branch1, ex._branch2));
		}
		Revision startRevision = hm.getRevision(revision);
		Revision stopRevision = hm.getRevision(revision - 1);
		try {
			KBUtils.revert(_kb, startRevision, branch, stopRevision, branch);
		} catch (RuntimeException ex) {
			return HandlerResult
				.error(I18NConstants.REVERT_NOT_POSSIBLE__REV_CAUSE.fill(cs.getRevision(), message(context, ex)));
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	private String message(DisplayContext context, Throwable ex) {
		Set<String> messages = new LinkedHashSet<>();

		for (Throwable current = ex; current != null; current = current.getCause()) {
			String message;
			if (current instanceof I18NFailure) {
				ResKey errorKey = ((I18NFailure) current).getErrorKey();
				message = context.getResources().getString(errorKey);
			} else {
				message = current.getMessage();
			}
			if (message != null && !message.isBlank()) {
				messages.add(message);
			}
		}

		return messages.stream().collect(Collectors.joining(" "));
	}

	private long getBranchContext(ChangeSet cs) throws MultipleBranchCommit {
		long branch = -1;
		branch = getBranch(-1, cs.getCreations());
		branch = getBranch(-1, cs.getUpdates());
		branch = getBranch(-1, cs.getDeletions());
		if (branch == -1 ) {
			branch = TLContext.TRUNK_ID;
		}
		return branch;
	}

	private long getBranch(long branch, List<? extends ItemEvent> events) throws MultipleBranchCommit {
		for (ItemEvent evt : events) {
			long ownerBranch = evt.getOwnerBranch();
			if (branch == -1) {
				branch = ownerBranch;
			}
			if (ownerBranch != branch) {
				throw new MultipleBranchCommit(branch, ownerBranch);
			}
		}
		return branch;
	}

	@Override
	protected ResKey getConfirmMessage(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {
		ChangeSet cs = (ChangeSet) model;
		Revision revision = _kb.getHistoryManager().getRevision(cs.getRevision());
		Date date = new Date(revision.getDate());
		return I18NConstants.CONFIRM_REVERT_REVISION__REVISION_DATE.fill(revision, date);
	}

	/**
	 * {@link ExecutabilityRule} allows execution when the model is a {@link ChangeSet}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ChangeSetModelExecutability implements ExecutabilityRule {

		/**
		 * Singleton {@link ChangeSetModelExecutability} instance.
		 */
		public static final ChangeSetModelExecutability INSTANCE = new ChangeSetModelExecutability();

		private ChangeSetModelExecutability() {
			// Singleton constructor.
		}

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			if (!(model instanceof ChangeSet)) {
				return ExecutableState.createDisabledState(I18NConstants.NO_REVISION_SELECTED);
			}
			return ExecutableState.EXECUTABLE;
		}

	}

}


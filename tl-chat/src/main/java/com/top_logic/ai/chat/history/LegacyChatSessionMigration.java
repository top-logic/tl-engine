/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.history;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.util.model.ModelService;

/**
 * One-time eager migration of legacy <code>tl.ai.chat:ChatSession</code> objects to
 * {@link AgentSession}s at application startup.
 *
 * <p>
 * Each legacy session becomes an {@link AgentSession} (kind CHAT) with the same title, owner,
 * timestamps and metadata, plus one synthetic completed {@link AgentExecution} carrying the old
 * V2 message blob verbatim as its transcript. Legacy blobs in the pre-V2 format cannot be
 * replayed; their conversation content is dropped with a warning (title and timestamps are
 * preserved). Each legacy object is deleted after migration, so after one boot of this version
 * the <code>ChatSession</code> table is empty and subsequent startups are a cheap no-op.
 * </p>
 *
 * <p>
 * Migration is best-effort: a failure is logged and never prevents application startup; remaining
 * legacy sessions are retried on the next boot.
 * </p>
 *
 * <p>
 * TODO: Remove the legacy <code>tl.ai.chat</code> model in the release AFTER this eager migration
 * has shipped (every deployment that booted once has an empty <code>ChatSession</code> table
 * then). Checklist:
 * </p>
 * <ol>
 * <li>Register <code>tl-chat</code> as a migration module (MigrationService config in
 * <code>tl-chat.conf.config.xml</code>) and create a migration via the
 * <code>create_migration_script_template</code> Ant task containing:
 *
 * <pre>
 * &lt;pre-processors&gt;
 *     &lt;processor class="com.top_logic.knowledge.service.migration.DeleteTableProcessor"&gt;
 *         &lt;tables&gt;
 *             &lt;table object_name="ChatSession"/&gt;
 *         &lt;/tables&gt;
 *     &lt;/processor&gt;
 *     &lt;delete-class name="tl.ai.chat:ChatSession"/&gt;
 *     &lt;delete-module name="tl.ai.chat"/&gt;
 * &lt;/pre-processors&gt;
 * </pre>
 *
 * </li>
 * <li>Delete <code>WEB-INF/model/tl.ai.chat.model.xml</code>,
 * <code>WEB-INF/kbase/ChatMeta.xml</code> and
 * <code>WEB-INF/autoconf/model.tl.ai.chat.config.xml</code>.</li>
 * <li>Delete this class and its module entry in <code>tl-chat.conf.config.xml</code>.</li>
 * <li>Drop the ChatSession reference from the comment in tl-ai-core
 * <code>WEB-INF/model/tl.ai.execution.model.xml</code>.</li>
 * </ol>
 */
@ServiceDependencies({
	ModelService.Module.class,
	AgentExecutionStore.Module.class,
})
public class LegacyChatSessionMigration
		extends ConfiguredManagedClass<LegacyChatSessionMigration.Config<?>> {

	/**
	 * Configuration interface for {@link LegacyChatSessionMigration}.
	 */
	public interface Config<I extends LegacyChatSessionMigration> extends ConfiguredManagedClass.Config<I> {
		// No configurable properties.
	}

	private static final String LEGACY_MODULE_NAME = "tl.ai.chat";

	private static final String LEGACY_TYPE_NAME = "ChatSession";

	private static final String ATTR_TITLE = "title";

	private static final String ATTR_MESSAGES = "messages";

	private static final String ATTR_CREATED_AT = "createdAt";

	private static final String ATTR_LAST_UPDATED = "lastUpdated";

	private static final String ATTR_METADATA = "metadata";

	private static final String ATTR_OWNER = "owner";

	/** Agent name recorded on the synthetic execution holding a migrated legacy transcript. */
	private static final String LEGACY_IMPORT_AGENT = "legacy-import";

	/**
	 * Creates a {@link LegacyChatSessionMigration}.
	 */
	public LegacyChatSessionMigration(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		try {
			ThreadContextManager.inSystemInteraction(LegacyChatSessionMigration.class,
				this::migrateLegacySessions);
		} catch (RuntimeException ex) {
			Logger.warn("Legacy chat session migration failed, will be retried on next startup: "
				+ ex.getMessage(), ex, LegacyChatSessionMigration.class);
		}
	}

	private void migrateLegacySessions() {
		TLClass legacyType = getLegacySessionType();
		if (legacyType == null) {
			return;
		}
		List<TLObject> legacySessions = MetaElementUtil.getAllInstancesOf(legacyType, TLObject.class);
		if (legacySessions.isEmpty()) {
			return;
		}
		int migrated = 0;
		for (TLObject legacy : legacySessions) {
			try {
				migrateLegacySession(legacy);
				migrated++;
			} catch (RuntimeException ex) {
				Logger.error("Failed to migrate legacy chat session: " + ex.getMessage(), ex,
					LegacyChatSessionMigration.class);
			}
		}
		Logger.info("Migrated " + migrated + " of " + legacySessions.size()
			+ " legacy chat session(s) to AgentSession.", LegacyChatSessionMigration.class);
	}

	private void migrateLegacySession(TLObject legacy) {
		String title = (String) legacy.tValueByName(ATTR_TITLE);
		String messagesJson = (String) legacy.tValueByName(ATTR_MESSAGES);
		String metadataJson = (String) legacy.tValueByName(ATTR_METADATA);
		Date createdAt = (Date) legacy.tValueByName(ATTR_CREATED_AT);
		Date lastUpdated = (Date) legacy.tValueByName(ATTR_LAST_UPDATED);
		Person owner = (Person) legacy.tValueByName(ATTR_OWNER);
		if (owner == null) {
			Logger.warn("Legacy chat session '" + title
				+ "' has no owner; it is migrated but will not appear in any user's session list.",
				LegacyChatSessionMigration.class);
		}

		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession session = store.createSession(SessionKind.CHAT, owner, title);
		if (metadataJson != null) {
			store.saveMetadata(session, metadataJson);
		}

		if (messagesJson != null && !messagesJson.isEmpty()) {
			if (messagesJson.trim().startsWith("{")) {
				AgentExecution execution = store.beginExecution(session,
					"legacy-" + UUID.randomUUID(), LEGACY_IMPORT_AGENT, null, null, null);
				store.finishExecution(execution, ExecutionStatus.COMPLETE, null, null, null,
					messagesJson);
			} else {
				Logger.warn("Legacy chat session '" + title
					+ "' has a pre-V2 message blob; conversation content is not migrated.",
					LegacyChatSessionMigration.class);
			}
		}

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(ResKey.text("Migrate legacy chat session"))) {
			// Preserve the original timestamps for correct history ordering.
			session.tUpdateByName(ATTR_CREATED_AT, createdAt != null ? createdAt : new Date());
			if (lastUpdated != null) {
				session.tUpdateByName(ATTR_LAST_UPDATED, lastUpdated);
			}
			legacy.tDelete();
			tx.commit();
		}
		Logger.info("Migrated legacy chat session '" + title + "' to AgentSession.",
			LegacyChatSessionMigration.class);
	}

	private static TLClass getLegacySessionType() {
		TLModel model = ModelService.getApplicationModel();
		TLModule module = model.getModule(LEGACY_MODULE_NAME);
		if (module == null) {
			return null;
		}
		return (TLClass) module.getType(LEGACY_TYPE_NAME);
	}

	/**
	 * Module for {@link LegacyChatSessionMigration}.
	 */
	public static final class Module extends TypedRuntimeModule<LegacyChatSessionMigration> {

		/** Singleton. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<LegacyChatSessionMigration> getImplementation() {
			return LegacyChatSessionMigration.class;
		}
	}
}

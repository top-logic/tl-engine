/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Map;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.EnabledConfiguration;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.UpdateChain;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.merge.MergeConflictException;
import com.top_logic.model.listen.impl.DefaultModelScope;
import com.top_logic.model.listen.impl.EventBuilder;
import com.top_logic.model.listen.impl.ModelEventSettings;
import com.top_logic.util.TLContextManager;

/**
 * {@link ModelEventForwarder} that consistently delivers model change events
 * from the persistency layer to all active sessions.
 */
public final class GlobalModelEventForwarder extends DefaultModelScope implements ModelEventForwarder {

	/**
	 * Configuration of the {@link GlobalModelEventForwarder}.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends ConfigurationItem {

		/**
		 * Returns configured {@link LinkRelevanceConfig link relevance}.
		 */
		@EntryTag("link")
		@Key(LinkRelevanceConfig.TYPE_NAME)
		Map<String, LinkRelevanceConfig> getLinkRelevance();
	}

	/**
	 * A {@link LinkRelevanceConfig} specifies the attributes of a reference types that are
	 * important for changes of the referencing object, i.e. if the reference object is created,
	 * changed, or deleted, the referenced object is treated as updated.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface LinkRelevanceConfig extends ConfigurationItem {

		/** Name of the type attribute. */
		String TYPE_NAME = "type";

		/** Name of the attributes attribute. */
		String ATTRIBUTES_NAME = "attributes";

		/**
		 * The name of the type to specify relevance for.
		 */
		@Name(TYPE_NAME)
		String getType();

		/**
		 * Definition of the enabled reference attributes of the represented type.
		 */
		@Key(EnabledConfiguration.NAME_ATTRIBUTE)
		@Name(ATTRIBUTES_NAME)
		Map<String, EnabledConfiguration> getAttributes();

	}

	private final UpdateChain _updateChain;

	private boolean _foundFutureEvent = false;

	/**
	 * Creates a {@link GlobalModelEventForwarder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GlobalModelEventForwarder(InstantiationContext context, PolymorphicConfiguration<?> config) {
		this(PersistencyLayer.getKnowledgeBase(), PersistencyLayer.getKnowledgeBase().getSessionUpdateChain(),
			configuredAssociationRelevance());
	}
	
	/**
	 * Creates a {@link GlobalModelEventForwarder}.
	 * 
	 * @param kb
	 *        The {@link KnowledgeBase} the given update chain belongs to.
	 * @param updateChain
	 *        The source of {@link UpdateEvent}s.
	 * @param relevance
	 *        Specification of association end relevance by type and reference attribute. No entry
	 *        means {@link Boolean#TRUE}.
	 */
	public GlobalModelEventForwarder(KnowledgeBase kb, UpdateChain updateChain, AssociationEndRelevance relevance) {
		super(new ModelEventSettings(kb,
			BasicTypes.getObjectType(kb.getMORepository()),
			BasicTypes.getKnowledgeAssociationType(kb.getMORepository()),
			relevance));
		_updateChain = updateChain;
	}

	private static AssociationEndRelevance configuredAssociationRelevance() {
		Config config = ApplicationConfig.getInstance().getConfig(Config.class);
		MORepository typeSystem = PersistencyLayer.getKnowledgeBase().getMORepository();
		LogProtocol log = new LogProtocol(GlobalModelEventForwarder.class);
		AssociationEndRelevance associationEndRelevance =
			MapBasedAssociationEndRelevance.newAssociationEndRelevance(log, config.getLinkRelevance(), typeSystem);
		log.checkErrors();
		return associationEndRelevance;
	}

	@Override
	public boolean synthesizeModelEvents() {
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		if (subSession.isLocked()) {
			/* Sub session is currently locked. No updates must occur. */
			return false;
		}
		KnowledgeBase kb = settings().kb();
		long newSessionRevision = getSessionRevision(kb);
		EventBuilder eventBuilder = createEventBuilder(kb, newSessionRevision);
		if (eventBuilder == null) {
			return false;
		}

		return eventBuilder.notifyListeners();
	}

	private long getSessionRevision(KnowledgeBase knowledgeBase) {
		try {
			return knowledgeBase.getHistoryManager().updateSessionRevision();
		} catch (MergeConflictException ex) {
			/* If there were any local changes, the update must be a noop, because commit updates
			 * the session revision. If no changes are made no conflict can be detected. */
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	private EventBuilder createEventBuilder(KnowledgeBase knowledgeBase, long newSessionRevision) {
		EventBuilder eventBuilder = null;

		// Aggregate potentially multiple updates into a single update.
		while (true) {
			if (!_foundFutureEvent) {
				if (!_updateChain.next()) {
					_foundFutureEvent = false;
					break;
				}
			}
			UpdateEvent event = _updateChain.getUpdateEvent();
			assert knowledgeBase == event.getKnowledgeBase() : "UpdateEvent for foreign knowledge base: expected: '"
				+ knowledgeBase
				+ "' actual: '" + event.getKnowledgeBase() + "'";

			_foundFutureEvent = event.getCommitNumber() > newSessionRevision;
			if (_foundFutureEvent) {
				break;
			}

			if (eventBuilder == null) {
				eventBuilder = eventBuilder();
			}
			eventBuilder.add(event);
		}
		return eventBuilder;
	}

}

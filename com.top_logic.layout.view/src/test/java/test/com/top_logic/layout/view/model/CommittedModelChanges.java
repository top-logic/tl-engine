/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.model;

import java.util.Collections;

import test.com.top_logic.basic.AssertProtocol;

import com.top_logic.basic.Protocol;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.mig.html.layout.AssociationEndRelevance;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.mig.html.layout.MapBasedAssociationEndRelevance;
import com.top_logic.model.TLModule;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Changes a test commits to the knowledge base and delivers to a {@link ModelScope}, as the scope
 * of a browser window delivers the changes committed by anybody.
 *
 * <p>
 * The changed objects are modules of the application model, the instances of
 * {@code tl.model:TLModule}, which every test knowledge base can store.
 * </p>
 */
public class CommittedModelChanges {

	/**
	 * A scope delivering the changes committed to the knowledge base from now on, on each
	 * {@link GlobalModelEventForwarder#synthesizeModelEvents()}.
	 */
	public static GlobalModelEventForwarder modelScope() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Protocol log = new AssertProtocol(CommittedModelChanges.class.getName());
		AssociationEndRelevance relevance = MapBasedAssociationEndRelevance.newAssociationEndRelevance(log,
			Collections.emptyMap(), kb.getMORepository());
		log.checkErrors();
		return new GlobalModelEventForwarder(kb, kb.getUpdateChain(), relevance);
	}

	/**
	 * Creates a module of the given name in the application model in a committed transaction, and
	 * delivers the creation to the given scope.
	 */
	public static TLModule createModule(GlobalModelEventForwarder scope, String name) {
		TLModule result;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			result = TLModelUtil.addModule(ModelService.getApplicationModel(), name);
			tx.commit();
		}
		scope.synthesizeModelEvents();
		return result;
	}

	/**
	 * Deletes the given module in a committed transaction, and delivers the deletion to the given
	 * scope.
	 */
	public static void deleteModule(GlobalModelEventForwarder scope, TLModule module) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			module.tDelete();
			tx.commit();
		}
		scope.synthesizeModelEvents();
	}

	/**
	 * React context of a window observing the model in a given {@link ModelScope}.
	 */
	public static final class ScopedReactContext extends DefaultReactContext {

		private final ModelScope _scope;

		/**
		 * Creates a {@link ScopedReactContext}.
		 *
		 * @param scope
		 *        The scope the controls built in this context observe the model in.
		 */
		public ScopedReactContext(ModelScope scope) {
			super("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
			_scope = scope;
		}

		@Override
		public ModelScope getModelScope() {
			return _scope;
		}
	}

}

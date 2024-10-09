/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.execution.engine.GuiEngine;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.TLContext;

public class ActiveTasksListModelBuilder extends ProcessExecutionListModelBuilder {

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
			ActiveTasksListModelBuilder.ActorFilter filter = new ActorFilter();
			List<Token> result = new ArrayList<>();
			for (ProcessExecution pe : allProcessExecutions) {
				FilterUtil.filterInto(result, filter, pe.getUserRelevantTokens());
			}
			return result;
		}
	}
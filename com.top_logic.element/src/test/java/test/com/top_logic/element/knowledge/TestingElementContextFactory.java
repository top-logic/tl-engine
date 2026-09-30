/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package test.com.top_logic.element.knowledge;

import java.util.function.Consumer;

import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.element.knowledge.ElementDBContext;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.db2.DBContext;
import com.top_logic.knowledge.service.db2.DBContextFactory;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.model.cs.TLObjectChangeSet;

/**
 * {@link DBContextFactory} to test {@link TLObjectChangeSet}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestingElementContextFactory extends DBContextFactory {

	/**
	 * {@link Consumer} to add to the current {@link InteractionContext} to receive the created
	 * {@link TLObjectChangeSet}.
	 */
	@SuppressWarnings("unchecked")
	public static final TypedAnnotatable.Property<Consumer<? super TLObjectChangeSet>> INSTALLER =
		TypedAnnotatable.propertyRaw(Consumer.class, "installer");

	@Override
	public DBContext createContext(DBKnowledgeBase kb, String updater) {
		return new TestingElementDBContext(kb, updater);
	}

	/**
	 * {@link ElementDBContext} calling an attached {@link TLObjectChangeSet} installer.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class TestingElementDBContext extends ElementDBContext {

		/**
		 * Creates a new {@link TestingElementDBContext}.
		 */
		public TestingElementDBContext(DBKnowledgeBase kb, String updater) {
			super(kb, updater);
		}

		@Override
		protected TLObjectChangeSet createModelChangeSet(UpdateEvent event) {
			TLObjectChangeSet changeSet = super.createModelChangeSet(event);
			DisplayContext dc = DefaultDisplayContext.getDisplayContext();
			Consumer<? super TLObjectChangeSet> installer = dc.get(INSTALLER);
			if (installer != null) {
				installer.accept(changeSet);
			}
			return changeSet;
		}

	}
}


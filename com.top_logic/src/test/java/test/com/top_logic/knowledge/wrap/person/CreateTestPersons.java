/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package test.com.top_logic.knowledge.wrap.person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.NamedTestSetup;
import test.com.top_logic.basic.ThreadContextSetup;

import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * {@link NamedTestSetup} to create {@link Person}s for {@link Test}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class CreateTestPersons extends ThreadContextSetup {

	/** {@link Person}s created in {@link #setUp()}. */
	protected List<Person> _createdPersons = new ArrayList<>();

	/**
	 * Creates a new {@link CreateTestPersons}.
	 */
	public CreateTestPersons(Test test) {
		super(test);
	}

	/**
	 * {@link KnowledgeBase} to create {@link Person}.
	 */
	protected KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	@Override
	protected void doSetUp() throws Exception {
		try (Transaction createTX = kb().beginTransaction()) {
			_createdPersons.addAll(createPersons());
			createTX.commit();
		}
	}

	/**
	 * Creates the test persons.
	 */
	protected abstract Collection<? extends Person> createPersons();

	@Override
	protected void doTearDown() throws Exception {
		try (Transaction createTX = kb().beginTransaction()) {
			KBUtils.deleteAll(_createdPersons);
			createTX.commit();
		}
		_createdPersons.clear();

	}

}

/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic;

import junit.extensions.TestSetup;
import junit.framework.Test;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.Computation;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.util.TLContext;

/**
 * Sets up a person in {@link TLContext}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PersonTestSetup extends TestSetup {

	private final String userId;

	private Person personBefore;

	/**
	 * Sets up the <code>root</code> user.
	 */
	public PersonTestSetup(Test testSuite) {
		this("root", testSuite);
	}
	
	public PersonTestSetup(String userId, Test testSuite) {
		super(testSuite);
		this.userId = userId;
	}


	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		Person person =
			ThreadContext.inSystemContext(PersonTestSetup.class, new Computation<Person>() {
				@Override
				public Person run() {
					return PersonManager.getManager().getPersonByName(userId);
				}
			});
		 
		assertNotNull(person);
		
		TLContext context = TLContext.getContext();
		this.personBefore = context.getCurrentPersonWrapper();
		context.setCurrentPerson(person);
	}
	
	@Override
	protected void tearDown() throws Exception {
		TLContext.getContext().setCurrentPerson(this.personBefore);
		
		super.tearDown();
	}
	

}

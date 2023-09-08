/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.MemberChangedListener;
import com.top_logic.layout.form.model.AbstractFormContainer;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.StringField;

/**
 * Tests the class {@link com.top_logic.layout.form.model.FormGroup}.
 * 
 * @author     <a href="mailto:tma@top-logic.com">tma</a>
 */
@SuppressWarnings("javadoc")
public abstract class AbstractFormContainerTest extends TestCase {
	
	/**
	 *  Subclasses should return an instance of FormGroup.
	 * 
	 * @param name the name of the FormGroup
	 * @param i18n the i18n prefix
	 * @return an instance of FormContext
	 */
	protected abstract FormContainer createContainer(String name, ResPrefix i18n);

	public void testParent() {
		FormContainer container = createContainer("foo", ResPrefix.NONE);
		FormField input = FormFactory.newStringField("input");
		container.addMember(input);

		assertEquals(container, input.getParent());
		assertTrue(container.removeMember(input));
		assertEquals(null, input.getParent());
	}

	public void testEvents() {
		FormContainer container = createContainer("foo", ResPrefix.NONE);

		class Listener implements MemberChangedListener {
			int _members = 0;

			@Override
			public Bubble memberRemoved(FormContainer parent, FormMember member) {
				_members--;
				return Bubble.BUBBLE;
			}

			@Override
			public Bubble memberAdded(FormContainer parent, FormMember member) {
				_members++;
				return Bubble.BUBBLE;
			}

			public int getMembers() {
				return _members;
			}
		}

		Listener listener = new Listener();
		container.addListener(FormContainer.MEMBER_ADDED_PROPERTY, listener);
		container.addListener(FormContainer.MEMBER_REMOVED_PROPERTY, listener);

		FormField input = FormFactory.newStringField("input");
		container.addMember(input);

		assertEquals(1, listener.getMembers());
		assertTrue(container.removeMember(input));
		assertEquals(0, listener.getMembers());
		assertFalse(container.removeMember(input));
		assertEquals(0, listener.getMembers());
	}

	/**
	 * Tries to create some illegal FormGroups.
	 */
	public void testConstruction(){
		
		/* an empty i18n is allowed */
		FormContainer formGroup = createContainer("name", ResPrefix.GLOBAL);
		
        assertNotNull(formGroup);

		try{
			formGroup = createContainer(null, ResPrefix.forTest("i18n"));
			fail("Name must not be null");
		}catch (AssertionError e) {
			// expected
		}
		
		
		try {
			formGroup = createContainer("", ResPrefix.forTest("i18n"));
			fail("Name must not be empty");
		} catch (AssertionError e) {
			// expected
		}
		
	}
	
	/**
	 *  Can i be member of myself... I don't think so.
	 */
	public void testMemberOfMyself(){
		FormContainer group = createContainer("homeAdress", ResPrefix.forTest("adress"));
		
		try{
			group.addMember(group);
			fail("A FormGroup may not be member of itself");
		}catch (AssertionError e) {
			// expected
		}
	}
	
	/**
	 * Tests whether all descendants are updated with the new mandatory flag.
	 */
	public void testMandatory(){
		FormContainer formContext1 = createContainer("name", ResPrefix.forTest("i18n"));
		FormField field1 = FormFactory.newStringField("friend");
		field1.setMandatory(false);
		formContext1.addMember(field1);
		
		FormContainer formContext2 = createContainer("name2", ResPrefix.forTest("i18n"));
		FormField field2 = FormFactory.newStringField("friend2");
		field2.setMandatory(false);
		formContext2.addMember(field2);
		
		formContext1.addMember(formContext2);
		
		formContext2.setMandatory(true);
		assertTrue(field2.isMandatory());
		assertFalse(field1.isMandatory());
		
		formContext1.setMandatory(true);
		assertTrue(field2.isMandatory());
		assertTrue(field1.isMandatory());
	}
	
	/**
	 * Two members of one {@link FormGroup} may not have the same name. Adding a
	 * field with an already existing name fails.
	 */
	public void testForNameUniqueness(){
		FormContainer adress1 = createContainer("adress1", ResPrefix.forTest("adress"));
		
		FormField street1 = FormFactory.newStringField("street");
		adress1.addMember(street1);
		
		try {
			FormField street2 = FormFactory.newStringField("street");
			adress1.addMember(street2);
			fail();
		} catch (AssertionError ex) {
			// expected.
		}
		
		Iterator<? extends FormMember> members = adress1.getMembers();
		assertEquals(street1, members.next());
		
		assertFalse(members.hasNext());
	}
	
	public void testHasChanged() {
		FormContainer container = createContainer("container", ResPrefix.forTest("container"));

		assertFalse("Newly constructed container should not be changed!", container.isChanged());

		StringField field = FormFactory.newStringField("name", "field", false);
		
		assertFalse("Newly constructed field should not be changed!", field.isChanged());

		container.addMember(field);
		assertFalse("Container with not changed field should not be changed!", container.isChanged());

		field.setValue("field2");
		assertTrue("Field got new value '" + field.getValue() + "' but has no changed flag.", field.isChanged());
		assertTrue("Container was not informed about value changing.", container.isChanged());

		container.removeMember(field);
		assertFalse("changed flag should react on removing changed member.", container.isChanged());

		container.addMember(field);
		assertTrue("changed flag should react on adding changed member.", container.isChanged());

		field.setValue("field");
		assertFalse("Set value to default value should reset changed flag.", field.isChanged());
		assertFalse("Set value on child to default value should reset changed flag.", container.isChanged());

		field.setValue("field2");
		field.reset();
		assertFalse("After setting new value and reset, the changed flag should be reset", field.isChanged());
		assertFalse("After setting new value and reset, the changed flag should be reset", container.isChanged());
		
		field.initializeField("field2");
		assertFalse("After setting to new default, the changed flag should be reset", field.isChanged());
		assertFalse("After setting to new default, the changed flag should be reset", container.isChanged());
	}
	
	public void testAddMember() {
		FormContainer container = createContainer("container", ResPrefix.forTest("container"));

		List<FormMember> members = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			members.add(FormFactory.newStringField("field" + i, "field" + i, false));
		}
		if (container instanceof AbstractFormContainer) {
			((AbstractFormContainer) container).addMembers(members);
		} else {
			for (Iterator<FormMember> it = members.iterator(); it.hasNext();) {
				container.addMember(it.next());
			}
		}

		for (int i = 0; i < 20; i++) {
			assertTrue(container.hasMember("field" + i));
			assertEquals(members.get(i), container.getMember("field" + i));
		}
		for (int i = 0; i < 20; i = i + 2) {
			container.removeMember(members.get(i));
		}
		for (int i = 0; i < 20; i = i + 2) {
			assertFalse(container.hasMember("field" + i));
			try {
				container.getMember("field" + i);
				fail();
			} catch (NoSuchElementException ex) {
				// expected
			}
		}
	}
}

/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ImmutablePropertyListener;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.IntField;
import com.top_logic.layout.form.model.StringField;

/**
 * Tests the {@link com.top_logic.layout.form.FormMember} class.
 * 
 * @author <a href="mailto:tma@top-logic.com">tma</a>
 */
@SuppressWarnings("javadoc")
public class TestFormMember extends BasicTestCase {

	private static final ResPrefix I18N_PREFIX = ResPrefix.legacyClass(TestFormMember.class).append(".i18n");

	private FormContext customerContext;
	private BooleanField formMemberMarried;
	private IntField formMemberAge;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		customerContext = new FormContext("customer", I18N_PREFIX);

		formMemberMarried = FormFactory.newBooleanField("married");
		formMemberAge = FormFactory.newIntField("age");

		customerContext.addMembers(new FormMember[] { formMemberMarried, formMemberAge });
	}

	/**
	 * Tests the {@link com.top_logic.layout.form.FormMember#getQualifiedName()}
	 * method.
	 */
	public void testGetQualifiedName() {

		assertEquals("customer.age", formMemberAge.getQualifiedName());
		assertEquals("customer.married", formMemberMarried.getQualifiedName());
		
		customerContext.removeMember(formMemberAge);
		assertEquals("age", formMemberAge.getQualifiedName());
		
		customerContext.addMember(formMemberAge);
		assertEquals("customer.age", formMemberAge.getQualifiedName());
	}
	
	/**
	 * Tests the {@link com.top_logic.layout.form.FormMember#getParent()}
	 * method.
	 */
	public void testGetParent(){
		assertEquals(customerContext,formMemberAge.getParent());
		
		customerContext.removeMember(formMemberAge);
		assertEquals(null,formMemberAge.getParent());
	
	}
	
	/**
	 * Tests bubbling of events.
	 */
	public void testBubble() {
		class CheckBubbleListener implements ImmutablePropertyListener {

			boolean _called = false;
			boolean _eventBubbled = false;

			private final FormMember _owner;

			public CheckBubbleListener(FormMember owner) {
				_owner = owner;
			}

			@Override
			public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
				_called = true;
				_eventBubbled = sender != _owner;
				return Bubble.BUBBLE;
			}

		}
		CheckBubbleListener contextListener = new CheckBubbleListener(customerContext);
		customerContext.addListener(FormMember.IMMUTABLE_PROPERTY, contextListener);
		CheckBubbleListener memberListener = new CheckBubbleListener(formMemberAge);
		formMemberAge.addListener(FormMember.IMMUTABLE_PROPERTY, memberListener);

		formMemberAge.setImmutable(true);
		assertTrue("Member was not immutable before.", memberListener._called);
		assertFalse("Member is creator of event.", memberListener._eventBubbled);
		assertTrue("Event bubbles to context.", contextListener._called);
		assertTrue("Event bubbles because event is produced by child.", contextListener._eventBubbled);

	}

	public void testLabel() {
		assertTrue("Name of age member is internationalised in TestMessages.", formMemberAge.hasLabel());
		formMemberAge.setLabel("Age");
		assertTrue("Label is set direct.", formMemberAge.hasLabel());
		formMemberAge.setLabel(null);
		assertTrue("deleting direct set label re-installs default which is internationalisation.",
			formMemberAge.hasLabel());

		assertFalse("Name of married member is not internationalised in TestMessages.", formMemberMarried.hasLabel());
		formMemberMarried.setLabel("Married");
		assertTrue("Label is set direct.", formMemberMarried.hasLabel());
		formMemberMarried.setLabel(null);
		assertFalse("Label is deleted.", formMemberMarried.hasLabel());
	}

	public void testCssClasses() {
		StringField field = FormFactory.newStringField("foobar");
		assertNull(field.getCssClasses());

		assertFalse(field.addCssClass(""));
		assertFalse(field.addCssClass(null));
		assertNull(field.getCssClasses());

		assertTrue(field.addCssClass("foo"));
		assertEquals("foo", field.getCssClasses());

		assertFalse(field.addCssClass("foo"));
		assertEquals("foo", field.getCssClasses());

		assertFalse(field.removeCssClass(""));
		assertFalse(field.removeCssClass(null));
		assertEquals("foo", field.getCssClasses());

		assertTrue(field.addCssClass("bar"));
		assertEquals("foo bar", field.getCssClasses());

		assertFalse(field.addCssClass("bar"));
		assertEquals("foo bar", field.getCssClasses());

		assertTrue(field.addCssClass("baz"));
		assertEquals("foo bar baz", field.getCssClasses());

		// Remove from within.
		assertTrue(field.removeCssClass("bar"));
		assertEquals("foo baz", field.getCssClasses());

		assertFalse(field.removeCssClass("bar"));
		assertEquals("foo baz", field.getCssClasses());

		// Remove from beginning.
		assertTrue(field.removeCssClass("foo"));
		assertEquals("baz", field.getCssClasses());

		assertTrue(field.addCssClass("foo"));
		assertEquals("baz foo", field.getCssClasses());

		// Remove from end.
		assertTrue(field.removeCssClass("foo"));
		assertEquals("baz", field.getCssClasses());

		// Remove last.
		assertTrue(field.removeCssClass("baz"));
		assertNull(field.getCssClasses());

		assertFalse(field.removeCssClass("baz"));
		assertNull(field.getCssClasses());
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestFormMember.class);
	}

}

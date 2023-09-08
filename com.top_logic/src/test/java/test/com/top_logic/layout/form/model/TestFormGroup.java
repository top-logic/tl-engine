/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ComputationEx;
import com.top_logic.knowledge.wrap.person.TransientPersonalConfiguration;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.component.SimpleFormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.FormState;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.tool.boundsec.SecurityObjectProviderManager;
import com.top_logic.util.TLContext;

/**
 * Test case for {@link FormGroup}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestFormGroup extends AbstractFormContainerTest {

	static final ResPrefix I18N_PREFIX = ResPrefix.forTest("i18n");

	@Override
	protected FormContainer createContainer(String name, ResPrefix i18n) {
		return new FormGroup(name, i18n);
	}
	
	/**
	 *  Tests the iterators of FormGroup.
	 */
	public void testIterators(){
		FormGroup formGroup1 = (FormGroup) createContainer("formGroup1", I18N_PREFIX);
		assertFalse(formGroup1.getDescendantFields().hasNext());
		assertFalse(formGroup1.getFieldNames().hasNext());
		assertFalse(formGroup1.getGroupNames().hasNext());
		assertFalse(formGroup1.getMemberNames().hasNext());
		assertNull(formGroup1.getParent());
		
		FormContainer formGroup2 = createContainer("formGroup2", I18N_PREFIX);
		formGroup1.addMember(formGroup2);
		
		assertFalse(formGroup1.getDescendantFields().hasNext()); // does only deliver the form fields (not the groups)
		assertFalse("formGroup2", formGroup1.getFieldNames().hasNext());
		assertEquals("formGroup2", formGroup1.getGroupNames().next());
		assertEquals("formGroup2", formGroup1.getMemberNames().next());
	}
	
    /**
     *  Tests {@link FormGroup#replaceMember(com.top_logic.layout.form.FormMember)}.
     */
    public void testReplaceMember(){
        FormGroup formGroup1 = (FormGroup) createContainer("formGroup1", I18N_PREFIX);
        
        FormContainer formGroup2A = createContainer("formGroup2", I18N_PREFIX);
        FormContainer formGroup2B = createContainer("formGroup2", I18N_PREFIX);
        FormContainer formGroupX  = createContainer("formGroupX", I18N_PREFIX);
        FormContainer formGroupY  = createContainer("formGroupY", I18N_PREFIX);
        
        assertNull(formGroup1.replaceMember(formGroup2A));
        
        formGroup1.addMember(formGroup2A);
        formGroup1.addMember(formGroupX);
        Iterator desc = formGroup1.getDescendants();
        assertSame(formGroup2A, desc.next());
        assertSame(formGroupX , desc.next());
        assertFalse(desc.hasNext());

        assertSame(formGroup2A, formGroup1.replaceMember(formGroup2B));

        // Check that order is the same.
        desc = formGroup1.getDescendants();
        assertSame(formGroup2B, desc.next());
        assertSame(formGroupX , desc.next());
        assertFalse(desc.hasNext());
        
        assertNull(formGroup1.replaceMember(formGroupY));
    }

    /**
	 * Tests the descendant-iterator. 
	 * 
	 * At First a tree of form-fields is created. The test assumes
	 * that the iterator returns all of the form-fields of the tree.
	 */
	public void testDescendants() {
		MemberFactory fac = new MemberFactory();

		FormGroup g1 = (FormGroup) createContainer("root", I18N_PREFIX);
		{
			fac.newField(g1); // member0
			fac.newField(g1); // member1

			FormContainer c1 = fac.newContainer(g1); // member2
			{
				fac.newField(c1); // member3
				fac.newField(c1); // member4
				fac.newField(c1); // member5
			}

			fac.newField(g1); // member6

			FormContainer c2 = fac.newContainer(g1); // member7
			{
				fac.newField(c2); // member8
				fac.newField(c2); // member9
				fac.newField(c2); // member10

				FormContainer c3 = fac.newContainer(c2); // member11
				{
					fac.newField(c3); // member12
					fac.newField(c3); // member13
				}
				fac.newField(c2); // member14
				fac.newField(c2); // member15

				FormContainer c4 = fac.newContainer(c2); // member16
				{
					fac.newField(c4); // member17
					fac.newField(c4); // member18
				}
				fac.newField(c2); // member19
			}

			fac.newField(g1); // member20
			fac.newField(g1); // member21
		}

		// Check the descendant iterator.
		{
			HashSet allDescendants = new HashSet();
			HashSet allMembers = new HashSet(fac.membersInDFSOrder);
			for (Iterator it1 = g1.getDescendants(); it1.hasNext();) {
				Object member = it1.next();
				
				assertFalse(member + "returned only once", allDescendants.contains(member));
				assertTrue(member + " found", allMembers.remove(member));
				
				allDescendants.add(member);
			}

			assertEquals(allMembers.size(), 0);
		}

		// Check the descendant field iterator.
		{
			HashSet allDescendants = new HashSet();
			HashSet allMembers = new HashSet(fac.membersInDFSOrder);
			for (Iterator it1 = g1.getDescendantFields(); it1.hasNext();) {
				Object member = it1.next();

				assertTrue(member instanceof FormField);
				assertFalse(member + "returned only once", allDescendants.contains(member));
				assertTrue(member + " found", allMembers.remove(member));

				allDescendants.add(member);
			}
			
			// Only form containers left.
			for (Iterator it2 = allMembers.iterator(); it2.hasNext();) {
				Object container = it2.next();
				assertTrue(container + "is container", container instanceof FormContainer);
			}
		}
	}

	class MemberFactory {
		ArrayList membersInDFSOrder = new ArrayList();
		int nr = 0;

		FormContainer newContainer(FormContainer parent) {
			FormContainer newContainer = createContainer(newName(), I18N_PREFIX);
			membersInDFSOrder.add(newContainer);
			parent.addMember(newContainer);
			return newContainer;
		}

		StringField newField(FormContainer parent) {
			StringField field = FormFactory.newStringField(newName());
			membersInDFSOrder.add(field);
			parent.addMember(field);
			return field;
		}

		private String newName() {
			return "member" + (nr++);
		}
	}
	
	public void testGetMembersByRelativeName() {
		FormContext form = new FormContext("form", I18N_PREFIX);
		FormGroup group1 = new FormGroup("group1", I18N_PREFIX);
		StringField field0 = FormFactory.newStringField("field0");
		form.addMember(field0);
		form.addMember(group1);
		FormGroup group2 = new FormGroup("group2", I18N_PREFIX);
		group1.addMember(group2);
		StringField field1 = FormFactory.newStringField("field1");
		group1.addMember(field1);
		StringField field2 = FormFactory.newStringField("field2");
		group2.addMember(field2);

		assertEquals(group2, FormGroup.getMemberByRelativeName(group1, "group2"));
		assertEquals(field1, FormGroup.getMemberByRelativeName(group1, "field1"));
		assertEquals(field2, FormGroup.getMemberByRelativeName(group1, "group2.field2"));
		assertEquals(field2, FormGroup.getMemberByRelativeName(group1, "group2/field2"));
		assertEquals(field2, FormGroup.getMemberByRelativeName(group1, "group2//field2"));
		assertEquals(field2, FormGroup.getMemberByRelativeName(group1, "group2/./field2"));
		assertEquals(field2, FormGroup.getMemberByRelativeName(group1, "./group2.field2"));
		assertEquals(field2, FormGroup.getMemberByRelativeName(group1, "/group1.group2.field2"));
		assertEquals(group1, FormGroup.getMemberByRelativeName(group1, "."));
		assertEquals(field0, FormGroup.getMemberByRelativeName(group1, "/field0"));
		assertEquals(field0, FormGroup.getMemberByRelativeName(group1, "../field0"));

		assertEquals(group2, FormGroup.getMemberByRelativeName(group2, "."));
		assertEquals(group2, FormGroup.getMemberByRelativeName(group2, "../group2"));
		assertEquals(field1, FormGroup.getMemberByRelativeName(group2, "../field1"));
		assertEquals(field2, FormGroup.getMemberByRelativeName(group2, "field2"));
		assertEquals(group1, FormGroup.getMemberByRelativeName(group2, ".."));

		assertEquals(group2, FormGroup.getMemberByRelativeName(field2, ".."));
		assertEquals(field1, FormGroup.getMemberByRelativeName(field2, "../../field1"));
		assertEquals(field2, FormGroup.getMemberByRelativeName(field2, "../field2"));
		assertEquals(field2, FormGroup.getMemberByRelativeName(field2, "."));
		assertEquals(group1, FormGroup.getMemberByRelativeName(field2, "../.."));
		
		assertEquals(field1, FormGroup.getMemberByQualifiedName(form, "form.group1.field1"));
		assertEquals(group1, FormGroup.getMemberByQualifiedName(form, "form.group1"));
		assertEquals(form, FormGroup.getMemberByQualifiedName(form, "form"));

		try {
			FormGroup.getMemberByRelativeName(group2, "../field1.somename");
			fail("Expected error.");
		} catch (IllegalArgumentException ex) {
			BasicTestCase.assertContains("somename", ex.getMessage());
			BasicTestCase.assertContains(field1.getQualifiedName(), ex.getMessage());
		}
	}

	public void testSaveState() throws ConfigurationException {
		TLContext.inSystemContext(TestFormGroup.class, new ComputationEx<Void, ConfigurationException>() {

			@Override
			public Void run() throws ConfigurationException {
				FormContext form = new FormContext("form", I18N_PREFIX);
				FormGroup group1 = new FormGroup("group1", I18N_PREFIX);
				FormGroup group2 = new FormGroup("group2", I18N_PREFIX);
				form.addMember(group1);
				form.addMember(group2);
				FormGroup group21 = new FormGroup("group21", I18N_PREFIX);
				FormGroup group22 = new FormGroup("group22", I18N_PREFIX);
				group2.addMember(group21);
				group2.addMember(group22);

				setInitialCollapseState(group1, group2, group22);

				FormComponent nameProvider =
					new SimpleFormComponent(new DefaultInstantiationContext(TestFormGroup.class),
						TypedConfiguration.newConfigItem(SimpleFormComponent.Config.class));

				TransientPersonalConfiguration config = new TransientPersonalConfiguration();

				FormState.loadFormState(config, nameProvider, form);
				assertInitialCollapseStateForEmptySavedState(group1, group2, group22);

				assertSwitchCollapseStates(group2, group22);

				FormState.saveFormState(config, nameProvider, form);
				setInitialCollapseState(group1, group2, group22);
				FormState.loadFormState(config, nameProvider, form);

				assertRestoringSavedCollapsedState(group1, group2, group22);
				return null;
			}

			private void setInitialCollapseState(FormGroup group1, FormGroup group2, FormGroup group22) {
				group1.setCollapsed(true);
				group2.setCollapsed(false);
				group22.setCollapsed(true);
			}

			private void assertInitialCollapseStateForEmptySavedState(FormGroup group1, FormGroup group2,
					FormGroup group22) {
				assertTrue(group1.isCollapsed());
				assertFalse(group2.isCollapsed());
				assertTrue(group22.isCollapsed());
			}

			private void assertSwitchCollapseStates(FormGroup group2, FormGroup group22) {
				group2.setCollapsed(true);
				assertTrue("Collapsing of FormGroup failed", group2.isCollapsed());
				group22.setCollapsed(false);
				assertFalse("Collapsing of FormGroup failed", group22.isCollapsed());
			}

			private void assertRestoringSavedCollapsedState(FormGroup group1, FormGroup group2, FormGroup group22) {
				assertTrue(group1.isCollapsed());
				assertTrue(group2.isCollapsed());
				assertFalse(group22.isCollapsed());
			}
		});

	}

	public static Test suite () {
		Test test = new TestSuite(TestFormGroup.class);
		test = ServiceTestSetup.createSetup(test, SecurityObjectProviderManager.Module.INSTANCE);
		return TLTestSetup.createTLTestSetup(test);
    }

	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestFormGroup.class);
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta;

import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.ModelFactory;
import com.top_logic.element.model.cache.ElementModelCacheService;
import com.top_logic.element.model.cache.ModelTables;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.knowledge.wrap.person.PersonalConfigurationWrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.I18NConstants;
import com.top_logic.util.model.ModelService;

/**
 * Test that a commit checks the constraints of an unchanged object only if one of its attribute
 * values is changed in a row other than the object's own row.
 *
 * <p>
 * Each test makes an existing object invalid by adding a mandatory attribute to its type after the
 * object has been created.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestCheckValueOwners extends TestWithModelExtension {

	private static final String MODULE = TestCheckValueOwners.class.getName();

	private static final String REQUIRED = "requiredTestCheckValueOwners";

	private ModelFactory _factory;

	private TLModule _module;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_module = TLModelUtil.findModule(MODULE);
		_factory = DynamicModelService.getFactoryFor(MODULE);
	}

	/**
	 * Storing the personal configuration of an account must not fail because of an unrelated
	 * constraint violated by the account: the link from the account to its configuration does not
	 * store a model attribute value.
	 */
	public void testStorePersonalConfigurationOfInvalidAccount() {
		ThreadContext.withoutSecurityChecks(() -> {
			Person person;
			try (Transaction tx = beginTX()) {
				person = Person.create(_kb, "testCheckValueOwners",
					TLSecurityDeviceManager.getInstance().getAuthenticationDevice("dbSecurity"));
				tx.commit();
			}
			try {
				withMandatoryAttribute(person.tType(), () -> {
					// Creates the configuration.
					storePersonalConfiguration(person, "first");
					// Replaces the configuration, deletes the link to the old one.
					storePersonalConfiguration(person, "second");
				});
				assertEquals("second", PersonalConfigurationWrapper.getPersonalConfiguration(person).getValue("key"));
			} finally {
				try (Transaction tx = beginTX()) {
					person.tDelete();
					tx.commit();
				}
			}
			return null;
		});
	}

	private void storePersonalConfiguration(Person person, String value) {
		try (Transaction tx = beginTX()) {
			// Same operations as TransientPersonalConfiguration#storeConfiguration(..).
			PersonalConfigurationWrapper old = PersonalConfigurationWrapper.getPersonalConfiguration(person);
			if (old != null) {
				old.tDelete();
			}
			PersonalConfigurationWrapper config = PersonalConfigurationWrapper.createPersonalConfiguration(person);
			config.setValue("key", value);
			tx.commit();
		}
	}

	/**
	 * Changing a reference stored in a link table checks the owner of the reference (the link
	 * source).
	 */
	public void testLinkStoredReferenceChecksOwner() {
		TLObject holder;
		TLObject item;
		try (Transaction tx = beginTX()) {
			holder = newObject("Holder");
			item = newObject("Item");
			tx.commit();
		}
		try {
			withMandatoryAttribute(holder.tType(), () -> {
				assertConstraintViolation(() -> {
					try (Transaction tx = beginTX()) {
						holder.tUpdateByName("others", Set.of(item));
						tx.commit();
					}
				});
				assertEquals(Set.of(), holder.tValueByName("others"));
			});
		} finally {
			delete(holder, item);
		}
	}

	/**
	 * Removing a value of a reference stored in a link table checks the owner of the reference.
	 */
	public void testLinkStoredReferenceRemovalChecksOwner() {
		TLObject holder;
		TLObject item;
		try (Transaction tx = beginTX()) {
			holder = newObject("Holder");
			item = newObject("Item");
			holder.tUpdateByName("others", Set.of(item));
			tx.commit();
		}
		try {
			withMandatoryAttribute(holder.tType(), () -> {
				assertConstraintViolation(() -> {
					try (Transaction tx = beginTX()) {
						holder.tUpdateByName("others", Set.of());
						tx.commit();
					}
				});
				assertEquals(Set.of(item), holder.tValueByName("others"));
			});
		} finally {
			delete(holder, item);
		}
	}

	/**
	 * Changing a reference stored in the rows of the referenced objects checks the owner of the
	 * reference, which is not the object whose row is changed.
	 */
	public void testInlineStoredReferenceChecksOwner() {
		TLObject holder;
		try (Transaction tx = beginTX()) {
			holder = newObject("Holder");
			tx.commit();
		}
		try {
			withMandatoryAttribute(holder.tType(), () -> {
				assertConstraintViolation(() -> {
					try (Transaction tx = beginTX()) {
						TLObject item = newObject("Item");
						holder.tUpdateByName("items", Set.of(item));
						tx.commit();
					}
				});
				assertEquals(Set.of(), holder.tValueByName("items"));
			});
		} finally {
			delete(holder);
		}
	}

	/**
	 * Setting a value of a reference created in the same transaction checks the owner of the
	 * reference: the commit check uses model tables reflecting the uncommitted model.
	 */
	public void testReferenceCreatedInSameTransactionChecksOwner() {
		TLObject holder;
		TLObject item;
		try (Transaction tx = beginTX()) {
			holder = newObject("Holder");
			item = newObject("Item");
			tx.commit();
		}
		try {
			withMandatoryAttribute(holder.tType(), () -> {
				ModelTables committedTables = ElementModelCacheService.getApplicationModelTables();
				assertConstraintViolation(() -> {
					try (Transaction tx = beginTX()) {
						TLModelUtil.addReference(holder.tType(), "extra", item.tType(), null);
						assertNotSame("Model tables must reflect the model change in the transaction.",
							committedTables, ElementModelCacheService.getApplicationModelTables());
						holder.tUpdateByName("extra", item);
						tx.commit();
					}
				});
				assertNull(holder.tType().getPart("extra"));
			});
		} finally {
			delete(holder, item);
		}
	}

	/**
	 * Changing a referenced object does not check the objects referencing it.
	 */
	public void testChangingReferencedObjectDoesNotCheckReferrer() {
		TLObject holder;
		TLObject other;
		TLObject part;
		try (Transaction tx = beginTX()) {
			holder = newObject("Holder");
			other = newObject("Item");
			part = newObject("Item");
			holder.tUpdateByName("others", Set.of(other));
			holder.tUpdateByName("items", Set.of(part));
			tx.commit();
		}
		try {
			withMandatoryAttribute(holder.tType(), () -> {
				try (Transaction tx = beginTX()) {
					other.tUpdateByName("name", "other");
					part.tUpdateByName("name", "part");
					tx.commit();
				}
			});
			assertEquals("part", part.tValueByName("name"));
		} finally {
			delete(part, other, holder);
		}
	}

	private TLObject newObject(String typeName) {
		return _factory.createObject((TLClass) TLModelUtil.findType(_module, typeName));
	}

	/**
	 * Runs the given test while the given type has an additional mandatory attribute without
	 * value in all existing instances.
	 */
	private void withMandatoryAttribute(TLType type, Runnable test) {
		TLStructuredTypePart required;
		try (Transaction tx = beginTX()) {
			TLType stringType = ModelService.getApplicationModel().getModule("tl.core").getType("String");
			required = TLModelUtil.addProperty((TLClass) type, REQUIRED, stringType);
			required.setMandatory(true);
			tx.commit();
		}
		try {
			test.run();
		} finally {
			delete(required);
		}
	}

	private static void assertConstraintViolation(Runnable commit) {
		try {
			commit.run();
		} catch (RuntimeException ex) {
			for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
				if (cause instanceof TopLogicException tlEx
					&& I18NConstants.CONSTRAINTS_VIOLATED.equals(tlEx.getErrorKey())) {
					return;
				}
			}
			throw new AssertionError("Commit failed without a constraint violation.", ex);
		}
		fail("Commit of a change to an invalid object did not fail.");
	}

	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(new TestSuite(TestCheckValueOwners.class),
			TLSecurityDeviceManager.Module.INSTANCE, PersonManager.Module.INSTANCE);
		return suite(new ModelExtensionTestSetup(test, TestCheckValueOwners.class));
	}
}

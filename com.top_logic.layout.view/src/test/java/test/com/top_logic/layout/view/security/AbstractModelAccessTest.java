/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.security;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.basic.util.ResKeyTemplate;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.execution.service.CommandApprovalService;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Base class for tests of the view layer that depend on the model access rights.
 *
 * <p>
 * The tests run against the test model {@value #MODULE} with the access rights configured in
 * {@value #CONFIG_FILE} (and its typed counterpart), next to this class. That configuration is
 * installed for the suites built with {@link #suite(Class)} only, the default test application of
 * the module stays unchanged.
 * </p>
 *
 * <p>
 * The model and its rights:
 * </p>
 * <ul>
 * <li>{@value #PROJECT}: a top-level type. The account in {@value #RESPONSIBLE} holds the role
 * {@value #ROLE_RESPONSIBLE} on the project, which grants read, write, create, delete and the custom
 * command group {@value #FINISH}. Writing
 * {@value #SECRET} is denied for every role. The {@link CommandApprovalService} refuses writing a
 * project named {@value #FROZEN} (disabled, with a reason) and deleting it (hidden).</li>
 * <li>{@value #TASK}: the type of the composition {@value #TASKS} of a project. A task inherits the
 * role {@value #ROLE_RESPONSIBLE} from its project, with the same grants. Its reference
 * {@value #CREATED_IN} defaults to the context the task is created in. Writing its attribute
 * <code>note</code> is granted to {@value #ROLE_RESPONSIBLE} only, writing its {@value #SECRET}
 * (with a constant default) to no role.</li>
 * <li>{@value #CATEGORY}: the type of the reference {@value #CATEGORY_REF} of a project. The account
 * in {@value #READER} holds the role {@value #ROLE_READER}, which grants read only.</li>
 * </ul>
 *
 * <p>
 * The fixture created for each test: the {@link #_responsible} user is responsible for
 * {@link #_project} (holding {@link #_task}) and reader of {@link #_category}, but not of
 * {@link #_hiddenCategory}. The {@link #_roleless} user holds no role at all, and {@link #_root}
 * bypasses the model access rights.
 * </p>
 */
public abstract class AbstractModelAccessTest extends AbstractSearchExpressionTest {

	/** Name of the test model module. */
	protected static final String MODULE = "TestViewModelAccess";

	/** Name of the top-level test type. */
	protected static final String PROJECT = "Project";

	/** Name of the type of the objects in the composition {@link #TASKS}. */
	protected static final String TASK = "Task";

	/**
	 * Name of the {@link #TASK} reference defaulting to the context the task is created in.
	 */
	protected static final String CREATED_IN = "createdIn";

	/** Name of the type of the objects referenced by {@link #CATEGORY_REF}. */
	protected static final String CATEGORY = "Category";

	/** Name of the <code>name</code> attribute every test type has. */
	protected static final String NAME = "name";

	/** Name of the {@link #PROJECT} attribute nobody but a super-user may write. */
	protected static final String SECRET = "secret";

	/** Name of the {@link #PROJECT} reference to the account holding {@link #ROLE_RESPONSIBLE}. */
	protected static final String RESPONSIBLE = "responsible";

	/** Name of the {@link #PROJECT} reference to a {@link #CATEGORY}. */
	protected static final String CATEGORY_REF = "category";

	/** Name of the {@link #PROJECT} composition of {@link #TASK}s. */
	protected static final String TASKS = "tasks";

	/** Name of the {@link #CATEGORY} reference to the account holding {@link #ROLE_READER}. */
	protected static final String READER = "reader";

	/** Name of a {@link #PROJECT} the {@link CommandApprovalService} refuses to write. */
	protected static final String FROZEN = "frozen";

	/** Name of the custom command group granted on a {@link #PROJECT}. */
	protected static final String FINISH = "Finish";

	/** Role granting everything on a project and its tasks. */
	protected static final String ROLE_RESPONSIBLE = MODULE + ".Responsible";

	/** Role granting read access to a category. */
	protected static final String ROLE_READER = MODULE + ".Reader";

	/**
	 * Name of the untyped configuration file next to this class; the typed configuration file
	 * installing the model and its access rights is derived from it.
	 */
	protected static final String CONFIG_FILE = "ModelAccessTest.xml";

	private final List<Person> _persons = new ArrayList<>();

	private Person _formerUser;

	private String _formerContextId;

	/** The super-user, bypassing the model access rights. */
	protected Person _root;

	/** Holds {@link #ROLE_RESPONSIBLE} on {@link #_project} and {@link #ROLE_READER} on {@link #_category}. */
	protected Person _responsible;

	/** Holds no role on any object. */
	protected Person _roleless;

	/** A category {@link #_responsible} may read. */
	protected TLObject _category;

	/** A category no user but {@link #_root} may read. */
	protected TLObject _hiddenCategory;

	/** The project {@link #_responsible} is responsible for. */
	protected TLObject _project;

	/** The only task of {@link #_project}. */
	protected TLObject _task;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TLContext context = TLContext.getContext();
		_formerUser = context.getCurrentPersonWrapper();
		_formerContextId = context.getContextId();
		_root = PersonManager.getManager().getRoot();
		_responsible = newPerson("viewModelAccessResponsible");
		_roleless = newPerson("viewModelAccessRoleless");

		_category = newObject(qualified(CATEGORY), "category");
		_hiddenCategory = newObject(qualified(CATEGORY), "hiddenCategory");
		_project = newObject(qualified(PROJECT), "project");
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_category.tUpdateByName(READER, _responsible);
			_project.tUpdateByName(RESPONSIBLE, _responsible);
			_project.tUpdateByName(CATEGORY_REF, _category);

			_task = DynamicModelService.getFactoryFor(MODULE).createObject(type(TASK));
			_task.tUpdateByName(NAME, "task");
			_project.tUpdateByName(TASKS, List.of(_task));
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		// Deletes the objects, and with the project its task.
		super.tearDown();
		for (Person person : _persons) {
			TestPerson.deletePersonAndUser(person);
		}
		_persons.clear();

		TLContext context = TLContext.getContext();
		context.setCurrentPerson(_formerUser);
		if (_formerUser == null) {
			// Without a person, the context is identified by its explicit context id.
			context.setContextId(_formerContextId);
		}
	}

	private Person newPerson(String name) {
		Person result = TestPerson.createPerson(name);
		_persons.add(result);
		return result;
	}

	/**
	 * The fully qualified name of the test type with the given local name.
	 */
	protected static String qualified(String typeName) {
		return MODULE + TLModelUtil.QUALIFIED_NAME_SEPARATOR + typeName;
	}

	/**
	 * The test type with the given local name.
	 */
	protected static TLClass type(String typeName) {
		return (TLClass) TLModelUtil.findType(model(), qualified(typeName));
	}

	/**
	 * The attribute with the given name of the test type with the given local name.
	 */
	protected static TLStructuredTypePart part(String typeName, String partName) {
		return type(typeName).getPartOrFail(partName);
	}

	/**
	 * Asserts that the given action fails with a {@link TopLogicException} reporting the given
	 * message.
	 */
	protected static void assertRefused(ResKeyTemplate expectedMessage, Runnable action) {
		try {
			action.run();
		} catch (TopLogicException ex) {
			assertEquals(expectedMessage.getKey(), ex.getErrorKey().getKey());
			return;
		}
		fail("Expected the operation to be refused with '" + expectedMessage.getKey() + "'.");
	}

	/**
	 * Creates the suite for the given test class, installing the test model and its access rights
	 * and starting the services deciding about the model access rights.
	 */
	protected static Test suite(Class<? extends AbstractModelAccessTest> testClass) {
		Test kbTest = KBSetup.getSingleKBTest(testClass,
			ServiceTestSetup.createStarterFactoryForModules(getModules(
				AccessManager.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				InitialRolesManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				CommandApprovalService.Module.INSTANCE)));
		String configFile = CustomPropertiesDecorator.createFileName(AbstractModelAccessTest.class, CONFIG_FILE);
		return TLTestSetup.createTLTestSetup(TestUtils.doNotMerge(new CustomPropertiesSetup(kbTest, configFile, true)));
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.ObservableRule;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.command.ViewExecutabilityRules;
import com.top_logic.layout.view.security.AccessChecks;
import com.top_logic.layout.view.security.AccessControl;
import com.top_logic.layout.view.security.CommandAccess;
import com.top_logic.layout.view.security.CreateAccess;
import com.top_logic.layout.view.security.I18NConstants;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.layout.view.security.OperationAccess;
import com.top_logic.layout.view.security.UnitAccess;
import com.top_logic.layout.view.security.WithAccessControl;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * Tests the {@link ViewCommand.Config#getAccess() access} of a command, {@link CommandAccess}: the
 * operations it performs ({@link OperationAccess}), checked on its object, on the object of the
 * enclosing unit, or on the security root, and the objects it creates ({@link CreateAccess}).
 */
public class TestCommandAccess extends AbstractModelAccessTest {

	/** The channel the command and the unit read their object from. */
	private static final String OBJECT_CHANNEL = "object";

	/** The tag of a generic command. */
	private static final String GENERIC_COMMAND = "generic-command";

	/** The abstract type of the composition {@link #DELIVERABLES} of a project. */
	private static final String DELIVERABLE = "Deliverable";

	/** The concrete {@link #DELIVERABLE} the responsible may create. */
	private static final String SPEC = "Spec";

	/** The concrete {@link #DELIVERABLE} no role may create. */
	private static final String PLAN = "Plan";

	/** The project composition of {@link #DELIVERABLE}s. */
	private static final String DELIVERABLES = "deliverables";

	/** The project composition of an abstract type whose only concrete specialization no role may create. */
	private static final String ARCHIVE = "archive";

	/** An abstract type without concrete specialization. */
	private static final String EMPTY = "Empty";

	/** The operation declared for the security root by the engine. */
	private static final String ADMINISTRATION = "Administration";

	private ViewChannel _object;

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_object = new DefaultViewChannel(OBJECT_CHANNEL);
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_context.registerChannel(OBJECT_CHANNEL, _object);
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_object = null;
		super.tearDown();
	}

	/**
	 * A command without object checks its operation on the object of the enclosing unit.
	 */
	public void testUnitObject() throws ConfigurationException {
		ViewContext unit = unitContext();
		ViewExecutabilityRule rule = rule(command(null, operation(FINISH, null)), unit);
		_object.set(_project);

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(null));

		becomeUser(_roleless);
		ExecutableState denied = rule.isExecutable(null);
		assertDisabled(I18NConstants.ERROR_OPERATION_DENIED__OPERATION, denied);
		assertEquals("You are not allowed to perform the operation \"Finish\" on this object.",
			Resources.getInstance(Locale.ENGLISH).getString(denied.getI18NReasonKey()));
	}

	/**
	 * Without an object of the unit, the command is disabled.
	 */
	public void testUnitWithoutObject() throws ConfigurationException {
		ViewExecutabilityRule rule = rule(command(null, operation("Delete", null)), unitContext());

		becomeUser(_responsible);
		assertSame(ExecutableState.NO_EXEC_NO_MODEL, rule.isExecutable(null));

		_object.set(Collections.emptyList());
		assertSame("An empty collection is no object.", ExecutableState.NO_EXEC_NO_MODEL, rule.isExecutable(null));
	}

	/**
	 * A command checked on the object of the unit is re-evaluated when the input of the unit
	 * changes.
	 */
	public void testUnitObjectObserved() throws ConfigurationException {
		ViewExecutabilityRule rule = rule(command(null, operation("Delete", null)), unitContext());
		int[] revalidations = { 0 };
		Runnable stop = ((ObservableRule) rule).observe(() -> revalidations[0]++);

		becomeUser(_responsible);
		_object.set(_project);
		assertEquals(1, revalidations[0]);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(null));

		stop.run();
		_object.set(null);
		assertEquals(1, revalidations[0]);
		assertSame(ExecutableState.NO_EXEC_NO_MODEL, rule.isExecutable(null));
	}

	/**
	 * A command whose operation no role can hold on the checked type is hidden.
	 */
	public void testNotGrantableHidden() throws ConfigurationException {
		ViewExecutabilityRule rule = rule(command(OBJECT_CHANNEL, operation("Delete", CATEGORY)), _context);
		_object.set(_category);

		becomeUser(_responsible);
		assertSame("No role may delete a category.", ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(_category));

		becomeUser(_root);
		assertSame("A user bypassing the model security holds every operation.", ExecutableState.EXECUTABLE,
			rule.isExecutable(_category));
	}

	/**
	 * A command on a composition part decided by its container, the access parent it gets by
	 * default, is offered when its operation is grantable on a type of the container, and hidden
	 * when it is grantable on none.
	 */
	public void testDefaultContainerAccessParent() throws ConfigurationException {
		ViewExecutabilityRule deleteMilestone =
			rule(command(OBJECT_CHANNEL, operation("Delete", MILESTONE)), _context);
		ViewExecutabilityRule deleteNote = rule(command(OBJECT_CHANNEL, operation("Delete", NOTE)), _context);
		TLObject milestone = newPart(_project, MILESTONES, MILESTONE);
		TLObject note = newPart(_category, NOTES, NOTE);

		becomeUser(_responsible);
		assertSame("The responsible may write the project holding the milestone.", ExecutableState.EXECUTABLE,
			deleteMilestone.isExecutable(milestone));
		assertSame("No role may write a category, the container of a note.", ExecutableState.NOT_EXEC_HIDDEN,
			deleteNote.isExecutable(note));

		becomeUser(_roleless);
		assertDisabled("Some role may write a project, but not this user.", I18NConstants.ERROR_DELETE_DENIED,
			deleteMilestone.isExecutable(milestone));

		becomeUser(_root);
		assertSame(ExecutableState.EXECUTABLE, deleteNote.isExecutable(note));
	}

	/**
	 * Creating a composition part decided by its container, the access parent it gets by default,
	 * is offered when writing a type of the container is grantable, and hidden when it is not.
	 */
	public void testCreateDefaultContainerAccessParent() throws ConfigurationException {
		ViewExecutabilityRule createMilestone =
			rule(command(OBJECT_CHANNEL, createByReference("$object", MILESTONES)), _context);
		ViewExecutabilityRule createNote = rule(command(OBJECT_CHANNEL,
			"<create in='$object' reference='" + qualified(CATEGORY) + "#" + NOTES + "'/>"), _context);

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, createMilestone.isExecutable(_project));
		assertSame("No role may write a category.", ExecutableState.NOT_EXEC_HIDDEN,
			createNote.isExecutable(_category));

		becomeUser(_roleless);
		// Some role may write a project, but not this user.
		assertDisabled(I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE, createMilestone.isExecutable(_project));
		assertSame("No role may write a category.", ExecutableState.NOT_EXEC_HIDDEN,
			createNote.isExecutable(_category));

		becomeUser(_root);
		assertSame(ExecutableState.EXECUTABLE, createNote.isExecutable(_category));
	}

	/**
	 * An operation on an object, or on an attribute of objects of a type, decided by an access
	 * parent is refused for a user without the right on the parent, but not hidden as long as some
	 * role can hold the operation on a type of the parent.
	 */
	public void testDelegatingTypeRefusedNotHidden() {
		TLObject milestone = newPart(_project, MILESTONES, MILESTONE);
		TLObject note = newPart(_category, NOTES, NOTE);
		TLStructuredTypePart milestoneName = part(MILESTONE, NAME);
		TLStructuredTypePart noteName = part(NOTE, NAME);

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE,
			ModelAccessPolicy.onObject(SimpleBoundCommandGroup.WRITE, milestone, milestoneName));
		assertSame(ExecutableState.EXECUTABLE,
			ModelAccessPolicy.onAttributeOfType(SimpleBoundCommandGroup.WRITE, type(MILESTONE), milestoneName));
		assertSame("No role may write a category, the container of a note.", ExecutableState.NOT_EXEC_HIDDEN,
			ModelAccessPolicy.onAttributeOfType(SimpleBoundCommandGroup.WRITE, type(NOTE), noteName));
		assertSame(ExecutableState.NOT_EXEC_HIDDEN,
			ModelAccessPolicy.onObject(SimpleBoundCommandGroup.WRITE, note, noteName));

		becomeUser(_roleless);
		assertTrue("Some role may write a project, but not this user.",
			ModelAccessPolicy.onObject(SimpleBoundCommandGroup.WRITE, milestone, null).isDisabled());
		assertTrue(ModelAccessPolicy.onObject(SimpleBoundCommandGroup.WRITE, milestone, milestoneName).isDisabled());
		assertSame("Whether the user may write depends on the object.", ExecutableState.EXECUTABLE,
			ModelAccessPolicy.onAttributeOfType(SimpleBoundCommandGroup.WRITE, type(MILESTONE), milestoneName));
	}

	/**
	 * A command on a type whose access parents have types not known statically is offered: only
	 * the check on the concrete object decides.
	 */
	public void testUnknownAccessParentTypes() throws ConfigurationException {
		ViewExecutabilityRule rule = rule(command(OBJECT_CHANNEL, operation("Delete", REMARK)), _context);
		TLObject remark = newObject(qualified(REMARK), "remark");

		becomeUser(_responsible);
		assertSame(ExecutableState.NO_EXEC_NO_MODEL, rule.isExecutable(null));
		assertDisabled("A remark in no container has no access parent.", I18NConstants.ERROR_DELETE_DENIED,
			rule.isExecutable(remark));

		becomeUser(_root);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(remark));
	}

	/**
	 * A restricted user may never delete, so the command is hidden.
	 */
	public void testRestrictedUserHidden() throws ConfigurationException {
		ViewExecutabilityRule rule = onObject("Delete");
		try (com.top_logic.knowledge.service.Transaction tx =
			kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_responsible.setRestrictedUser(Boolean.TRUE);
			tx.commit();
		}

		becomeUser(_responsible);
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(_project));
	}

	/**
	 * The object expression is evaluated over the arguments of the command: a single object.
	 */
	public void testObjectSingle() throws ConfigurationException {
		ViewExecutabilityRule rule = onObject("Delete");

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(_project));
		assertSame(ExecutableState.NO_EXEC_NO_MODEL, rule.isExecutable(null));

		becomeUser(_roleless);
		assertDisabled(I18NConstants.ERROR_DELETE_DENIED, rule.isExecutable(_project));
	}

	/**
	 * A collection is allowed only when every element is; an empty collection is no object.
	 */
	public void testObjectCollection() throws ConfigurationException {
		TLObject other = newObject(qualified(PROJECT), "other");
		ViewExecutabilityRule rule = onObject("Write");

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(List.of(_project)));
		assertDisabled("The responsible may not write the other project.", I18NConstants.ERROR_WRITE_DENIED,
			rule.isExecutable(List.of(_project, other)));
		assertSame(ExecutableState.NO_EXEC_NO_MODEL, rule.isExecutable(List.of()));
	}

	/**
	 * An object that is no instance of the declared type is refused.
	 */
	public void testObjectTypeMismatch() throws ConfigurationException {
		ViewExecutabilityRule rule = onObject("Read");

		becomeUser(_responsible);
		assertDisabled("The responsible may read the category, but it is no project.", I18NConstants.ERROR_READ_DENIED,
			rule.isExecutable(_category));
	}

	/**
	 * The object expression sees every argument of the command, not only its main argument.
	 */
	public void testObjectOverNamedArgument() throws ConfigurationException {
		ViewChannel other = new DefaultViewChannel("other");
		_context.registerChannel("other", other);
		ViewCommandModel model = model(command("object, other as project",
			"<operation name='Write' object='$project' type='" + qualified(PROJECT) + "'/>"), _context);

		becomeUser(_responsible);
		_object.set(_category);
		other.set(_project);
		assertSame(ExecutableState.EXECUTABLE, model.executability(_category));

		other.set(null);
		assertSame(ExecutableState.NO_EXEC_NO_MODEL, model.executability(_category));
	}

	/**
	 * Without an enclosing unit, a command without object is checked on the security root.
	 */
	public void testSecurityRoot() throws ConfigurationException {
		ViewExecutabilityRule rule = rule(command(null, operation(ADMINISTRATION, null)), _context);

		becomeUser(_roleless);
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(null));

		becomeUser(_root);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(null));
	}

	/**
	 * A refused command is rejected when it is dispatched to the server, also when the state of its
	 * button did not keep up.
	 */
	public void testRefusedOnDispatch() throws ConfigurationException {
		ViewCommandModel model = model(command(OBJECT_CHANNEL, operation("Delete", PROJECT)), _context);
		_object.set(_project);

		becomeUser(_responsible);
		model.attach(null);
		assertTrue(model.isExecutable());

		becomeUser(_roleless);
		HandlerResult result = model.perform(_context);
		assertFalse("The refused command is not executed.", result.isSuccess());
		model.detach();
	}

	/**
	 * An operation the access rights allow is refused when the approval service refuses it on the
	 * object: disabled with the reason of the approval, or hidden as the approval decides.
	 */
	public void testApproval() throws ConfigurationException {
		ViewExecutabilityRule write = onObject("Write");
		ViewExecutabilityRule read = onObject("Read");
		ViewExecutabilityRule delete = onObject("Delete");
		TLObject frozen = newObject(qualified(PROJECT), FROZEN);

		becomeUser(_root);
		assertSame(ExecutableState.EXECUTABLE, write.isExecutable(_project));
		ExecutableState state = write.isExecutable(frozen);
		assertTrue("Expected a disabled command, got: " + state, state.isDisabled());
		assertEquals("The project is frozen.", Resources.getInstance(Locale.ENGLISH).getString(state.getI18NReasonKey()));
		assertSame("The approval checks writing only.", ExecutableState.EXECUTABLE, read.isExecutable(frozen));
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, delete.isExecutable(frozen));
		assertSame("Each element of a collection is approved.", ExecutableState.NOT_EXEC_HIDDEN,
			delete.isExecutable(List.of(_project, frozen)));
	}

	/**
	 * The approval configured by the framework disables modifying the anonymous account, even for
	 * a user the access rights allow everything.
	 */
	public void testApprovalAnonymousAccount() throws ConfigurationException {
		ViewExecutabilityRule write = rule(command(OBJECT_CHANNEL,
			"<operation name='Write' object='$object' type='tl.accounts:Person'/>"), _context);
		Person anonymous = PersonManager.getManager().getAnonymous();

		becomeUser(_root);
		assertDisabled(
			com.top_logic.knowledge.gui.layout.person.I18NConstants.ERROR_NOT_EXECUTABLE_FOR_ANONYMOUS_ACCOUNT,
			write.isExecutable(anonymous));
		assertSame(ExecutableState.EXECUTABLE, write.isExecutable(_roleless));
	}

	/**
	 * The configured rules decide in addition to the access check, which comes first.
	 */
	public void testCombinedWithConfiguredRules() throws ConfigurationException {
		ViewExecutabilityRule rule = rule("<generic-command inputs='object'><access>" + operation("Delete", PROJECT)
			+ "</access><executability><disabled-if expr='true'/></executability></generic-command>", _context);

		becomeUser(_roleless);
		assertDisabled("The access check comes first.", I18NConstants.ERROR_DELETE_DENIED, rule.isExecutable(_project));

		becomeUser(_responsible);
		assertSame(ExecutableState.NOT_EXEC_DISABLED_REASON, rule.isExecutable(_project).getI18NReasonKey());
	}

	/**
	 * A command without an operation is not checked.
	 */
	public void testNoOperation() throws ConfigurationException {
		assertSame(ViewExecutabilityRule.ALWAYS_EXECUTABLE, rule("<generic-command/>", _context));
		assertSame(ViewExecutabilityRule.ALWAYS_EXECUTABLE, rule("<generic-command><access/></generic-command>", _context));
	}

	/**
	 * The configuration errors reported when the command is built.
	 */
	public void testConfigurationErrors() throws ConfigurationException {
		assertInvalid("An operation must apply to the type.",
			command(OBJECT_CHANNEL, "<operation name='" + FINISH + "' object='$object' type='" + qualified(CATEGORY) + "'/>"),
			_context);
		assertInvalid("An operation must apply to the security root.", command(null, operation(FINISH, null)), _context);
		assertInvalid("An object needs a type.",
			command(OBJECT_CHANNEL, "<operation name='Read' object='$object'/>"), _context);
		assertInvalid("A type needs an object.",
			command(null, "<operation name='Read' type='" + qualified(PROJECT) + "'/>"), _context);
		assertInvalid("The operation must exist.", command(null, operation("NoSuchOperation", null)), _context);
		assertInvalid("An operation must apply to the type of the enclosing unit.",
			command(null, operation(ADMINISTRATION, null)), unitContext());
		assertInvalid("The created type must exist.",
			command(null, "<create type='" + qualified("NoSuchType") + "'/>"), _context);
		assertInvalid("A reference must be a composition.",
			command(OBJECT_CHANNEL, create(CATEGORY, "$object", CATEGORY_REF)), _context);
		assertInvalid("The created type must be compatible with the reference.",
			command(OBJECT_CHANNEL, create(CATEGORY, "$object", TASKS)), _context);
		assertInvalid("A creation without reference needs a type.",
			command(OBJECT_CHANNEL, "<create in='$object'/>"), _context);
		assertInvalid("An abstract type needs a concrete specialization.",
			command(OBJECT_CHANNEL, create(EMPTY, "$object", null)), _context);
		assertInvalid("A reference needs a context.",
			command(null, "<create type='" + qualified(TASK) + "' reference='" + qualified(PROJECT) + "#" + TASKS
				+ "'/>"),
			_context);
	}

	/**
	 * A creation into a reference of a context needs the creation right in the context and the
	 * right to write the reference.
	 */
	public void testCreateWithReference() throws ConfigurationException {
		ViewExecutabilityRule rule = rule(command(OBJECT_CHANNEL, create(TASK, "$object", TASKS)), _context);

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(_project));

		becomeUser(_roleless);
		assertDisabled(I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE, rule.isExecutable(_project));
	}

	/**
	 * A creation in a context without a reference needs the creation right in the context.
	 */
	public void testCreateInContext() throws ConfigurationException {
		ViewExecutabilityRule rule = rule(command(OBJECT_CHANNEL, create(TASK, "$object", null)), _context);

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(_project));

		becomeUser(_roleless);
		assertDisabled(I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE, rule.isExecutable(_project));
	}

	/**
	 * Without a context there is nothing to create in; in a collection of contexts, the creation
	 * must be allowed in every one.
	 */
	public void testCreateContexts() throws ConfigurationException {
		TLObject other = newObject(qualified(PROJECT), "other");
		ViewExecutabilityRule rule = rule(command(OBJECT_CHANNEL, create(TASK, "$object", TASKS)), _context);

		becomeUser(_responsible);
		assertSame(ExecutableState.NO_EXEC_NO_MODEL, rule.isExecutable(null));
		assertSame(ExecutableState.NO_EXEC_NO_MODEL, rule.isExecutable(List.of()));
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(List.of(_project)));
		assertDisabled(I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE, rule.isExecutable(List.of(_project, other)));
	}

	/**
	 * Without an expression, the object is created in the object of the enclosing unit.
	 */
	public void testCreateInUnitObject() throws ConfigurationException {
		ViewExecutabilityRule rule = rule(command(null,
			"<create type='" + qualified(TASK) + "' reference='" + qualified(PROJECT) + "#" + TASKS + "'/>"),
			unitContext());

		becomeUser(_responsible);
		assertSame(ExecutableState.NO_EXEC_NO_MODEL, rule.isExecutable(null));
		_object.set(_project);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(null));

		becomeUser(_roleless);
		assertDisabled(I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE, rule.isExecutable(null));
	}

	/**
	 * Without a context, the creation is decided against the security root, and a refusal hides
	 * the command.
	 */
	public void testCreateAtRoot() throws ConfigurationException {
		ViewExecutabilityRule rule = rule(command(null, "<create type='" + qualified(PROJECT) + "'/>"), _context);

		becomeUser(_responsible);
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(null));

		becomeUser(_root);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(null));
	}

	/**
	 * A type no role may create hides the command.
	 */
	public void testNotCreatableHidden() throws ConfigurationException {
		ViewExecutabilityRule rule = rule(command(OBJECT_CHANNEL, create(CATEGORY, "$object", null)), _context);

		becomeUser(_responsible);
		assertSame("No role may create a category.", ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(_project));

		becomeUser(_root);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(_project));
	}

	/**
	 * Without a type, the created type is the type of the reference.
	 */
	public void testCreateTypeOfReference() throws ConfigurationException {
		ViewExecutabilityRule rule = rule(command(OBJECT_CHANNEL, createByReference("$object", TASKS)), _context);

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(_project));

		becomeUser(_roleless);
		assertDisabled(I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE, rule.isExecutable(_project));
	}

	/**
	 * An abstract type is created where at least one of its concrete specializations may be
	 * created; in a collection of contexts, in every one.
	 */
	public void testCreateAbstractType() throws ConfigurationException {
		TLObject other = newObject(qualified(PROJECT), "other");
		ViewExecutabilityRule byReference =
			rule(command(OBJECT_CHANNEL, createByReference("$object", DELIVERABLES)), _context);
		ViewExecutabilityRule byType = rule(command(OBJECT_CHANNEL, create(DELIVERABLE, "$object", null)), _context);

		becomeUser(_responsible);
		assertSame("The responsible may create a spec.", ExecutableState.EXECUTABLE, byReference.isExecutable(_project));
		assertSame(ExecutableState.EXECUTABLE, byType.isExecutable(_project));
		assertDisabled(I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE,
			byReference.isExecutable(List.of(_project, other)));

		becomeUser(_roleless);
		// Some role may create a spec, but not this user.
		assertDisabled(I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE, byReference.isExecutable(_project));
	}

	/**
	 * An abstract type no concrete specialization of which any role may create hides the command.
	 */
	public void testCreateAbstractTypeNotCreatable() throws ConfigurationException {
		ViewExecutabilityRule rule = rule(command(OBJECT_CHANNEL, createByReference("$object", ARCHIVE)), _context);

		becomeUser(_responsible);
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(_project));

		becomeUser(_root);
		assertSame(ExecutableState.EXECUTABLE, rule.isExecutable(_project));
	}

	/**
	 * An explicit type narrows the type of the reference to the concrete type created.
	 */
	public void testCreateNarrowerType() throws ConfigurationException {
		ViewExecutabilityRule spec = rule(command(OBJECT_CHANNEL, create(SPEC, "$object", DELIVERABLES)), _context);
		ViewExecutabilityRule plan = rule(command(OBJECT_CHANNEL, create(PLAN, "$object", DELIVERABLES)), _context);

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, spec.isExecutable(_project));
		assertSame("No role may create a plan.", ExecutableState.NOT_EXEC_HIDDEN, plan.isExecutable(_project));
	}

	/**
	 * All checks must pass: the first failing one gives the reason, and a check that can never
	 * pass hides the command.
	 */
	public void testCombinedChecks() throws ConfigurationException {
		ViewExecutabilityRule both = rule(command(OBJECT_CHANNEL,
			operation("Delete", PROJECT) + create(TASK, "$object", TASKS)), _context);
		ViewExecutabilityRule createFirst = rule(command(OBJECT_CHANNEL,
			create(TASK, "$object", TASKS) + operation("Delete", PROJECT)), _context);
		ViewExecutabilityRule withHidden = rule(command(OBJECT_CHANNEL,
			operation("Delete", PROJECT) + create(CATEGORY, "$object", null)), _context);

		becomeUser(_responsible);
		assertSame(ExecutableState.EXECUTABLE, both.isExecutable(_project));

		becomeUser(_roleless);
		assertDisabled(I18NConstants.ERROR_DELETE_DENIED, both.isExecutable(_project));
		assertDisabled(I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE, createFirst.isExecutable(_project));
		assertSame("A check that can never pass hides the command.", ExecutableState.NOT_EXEC_HIDDEN,
			withHidden.isExecutable(_project));
	}


	private ViewContext unitContext() throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestCommandAccess.class);
		UnitAccess access = UnitAccess.create(context, accessControl(context,
			"<access-control operation='Read' inputs='object' object='$object' type='" + qualified(PROJECT) + "'/>"));
		context.checkErrors();
		return AccessChecks.contentContext(access, _context);
	}

	private ViewExecutabilityRule onObject(String operation) throws ConfigurationException {
		return rule(command(OBJECT_CHANNEL, operation(operation, PROJECT)), _context);
	}

	/**
	 * A command with the given inputs and access checks.
	 */
	private static String command(String inputs, String access) {
		return "<generic-command" + (inputs == null ? "" : " inputs='" + inputs + "'") + "><access>" + access
			+ "</access></generic-command>";
	}

	/**
	 * An operation on the object in {@link #OBJECT_CHANNEL} of the given type, or, without a type,
	 * on the object of the enclosing unit or the security root.
	 */
	private static String operation(String operation, String typeName) {
		if (typeName == null) {
			return "<operation name='" + operation + "'/>";
		}
		return "<operation name='" + operation + "' object='$object' type='" + qualified(typeName) + "'/>";
	}

	/**
	 * A creation in the given context, added to the given reference of a project, of the type of the
	 * reference.
	 */
	private static String createByReference(String in, String projectReference) {
		return "<create in='" + in + "' reference='" + qualified(PROJECT) + "#" + projectReference + "'/>";
	}

	/**
	 * A creation of the given type in the given context, added to the given reference of a
	 * project.
	 */
	private static String create(String typeName, String in, String projectReference) {
		return "<create type='" + qualified(typeName) + "' in='" + in + "'"
			+ (projectReference == null ? "" : " reference='" + qualified(PROJECT) + "#" + projectReference + "'")
			+ "/>";
	}

	private static ViewExecutabilityRule rule(String xml, ViewContext context) throws ConfigurationException {
		return ViewExecutabilityRules.build(command(xml), null, context);
	}

	private static ViewCommandModel model(String xml, ViewContext context) throws ConfigurationException {
		GenericViewCommand.Config config = command(xml);
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestCommandAccess.class);
		ViewCommand command = instantiation.getInstance(config);
		instantiation.checkErrors();
		return ViewCommandModel.forCommand(context, command, config);
	}

	private static void assertInvalid(String message, String xml, ViewContext context) {
		try {
			rule(xml, context);
		} catch (ConfigurationError | ConfigurationException ex) {
			return;
		}
		fail(message + ": " + xml);
	}

	private static GenericViewCommand.Config command(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestCommandAccess.class);
		Map<String, ConfigurationDescriptor> descriptors = new HashMap<>();
		descriptors.put(GENERIC_COMMAND, TypedConfiguration.getConfigurationDescriptor(GenericViewCommand.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		GenericViewCommand.Config result = (GenericViewCommand.Config) reader.read();
		context.checkErrors();
		return result;
	}

	private static AccessControl accessControl(DefaultInstantiationContext context, String xml)
			throws ConfigurationException {
		Map<String, ConfigurationDescriptor> descriptors = new HashMap<>();
		descriptors.put(WithAccessControl.ACCESS_CONTROL,
			TypedConfiguration.getConfigurationDescriptor(AccessControl.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		return (AccessControl) reader.read();
	}

	private static void assertDisabled(ResKey expectedReason, ExecutableState state) {
		assertDisabled("", expectedReason, state);
	}

	private static void assertDisabled(com.top_logic.basic.util.ResKeyTemplate expectedReason,
			ExecutableState state) {
		assertTrue("Expected a disabled command, got: " + state, state.isDisabled());
		assertEquals(expectedReason.getKey(), state.getI18NReasonKey().getKey());
	}

	private static void assertDisabled(String message, ResKey expectedReason, ExecutableState state) {
		assertTrue(message + " Expected a disabled command, got: " + state, state.isDisabled());
		assertEquals(message, expectedReason.getKey(), state.getI18NReasonKey().getKey());
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suiteWith(TestCommandAccess.class, TypeIndex.Module.INSTANCE, CommandCliqueService.Module.INSTANCE);
	}

}

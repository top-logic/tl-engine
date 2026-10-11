/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.provider.DefaultProvider;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.security.ToolChangeSet.Creation;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * The {@link PreCommitGuard} that asks the run's {@link ModelSecurity} about every accumulated change.
 *
 * <p>
 * Adds <b>no policy of its own</b>, exactly like {@link ModelAccessRightsSecurity}: the questions it
 * asks are the ones the engine answers for the interactive UI, so a tool that bypassed its own check
 * ends up bounded by the same rights the acting user would have had in the UI.
 * </p>
 *
 * <p>
 * <b>Every check runs in the committed view</b> ({@link KnowledgeBase#withoutModifications}). Two
 * reasons, both forced by the engine:
 * </p>
 * <ul>
 * <li>An object deleted in the current transaction refuses attribute access, and
 * {@code ModelAccessRights} has to read its type and its security parent to decide. In the committed
 * view the object is visible again, which is where its DELETE right is meaningful anyway.</li>
 * <li>An object created in the current transaction is allowed everything by construction (see the
 * {@link PreCommitGuard known limit}), so asking about it would answer nothing. Its creation is
 * checked through operands that <em>are</em> committed - the type and the container - which is
 * precisely the check the engine itself performs.</li>
 * </ul>
 *
 * <p>
 * <b>The initial values of a new object are the exception</b> (#683). They are asked with
 * {@link ModelSecurity#canSetInitial}, the question the application's create dialogs ask, and that
 * question is about the object being created - so it is asked in the transaction's own view, where
 * that object and its container exist, after the committed-view checks passed for its creation. An
 * attribute counts as given an initial value unless its value is the one the engine creates it with
 * ({@link #givenValues}).
 * </p>
 *
 * @see PreCommitGuard#forRun()
 */
public class ModelSecurityPreCommitGuard implements PreCommitGuard {

	@Override
	public List<Violation> check(SecurityContext security, ToolChangeSet changes) {
		if (changes == null || changes.isEmpty()) {
			return List.of();
		}
		Check check = new Check(security.modelSecurity(), changes);
		if (!PersistencyLayer.Module.INSTANCE.isActive()) {
			// No knowledge base whose modifications could be hidden - a unit test against a stubbed
			// ModelSecurity. The checks are the same; only the view they run in is not.
			check.run();
		} else {
			ComputationEx2<List<Violation>, RuntimeException, RuntimeException> job = check::run;
			PersistencyLayer.getKnowledgeBase().withoutModifications(job);
		}
		// In the transaction's own view: a new object exists only there.
		return check.runInitialValues();
	}

	/**
	 * The attributes of the given new object that the turn gave a value.
	 *
	 * <p>
	 * An attribute is left out only when its value is the one the engine creates the object with: the
	 * default the model gives the attribute, or an empty value where the model gives none. The engine
	 * sets that default when it creates the object, and a create dialog creates it as well for a user
	 * who may not edit the attribute. The value is compared with the default before anything else, so
	 * {@code false} or an empty value given to an attribute whose default is {@code true} or not empty
	 * counts as given.
	 * </p>
	 *
	 * <p>
	 * A default that is different every time it is computed (a UUID, the current time) is computed
	 * once more and the attribute is left out only when the value equals it, which in practice means
	 * it is asked: the value cannot be told from one a tool wrote, and a refusal is safer than a
	 * skip. A default the engine computes in the transaction (a sequence number) cannot be computed
	 * again without drawing the next one, so such an attribute is always asked when it has a value.
	 * </p>
	 *
	 * <p>
	 * Derived and backwards references are left out: a derived attribute stores nothing, and a
	 * backwards reference is stored on the object it points to, where the guard checks it as a write
	 * of that object.
	 * </p>
	 */
	static List<TLStructuredTypePart> givenValues(TLObject object, TLStructuredType type) {
		List<TLStructuredTypePart> result = new ArrayList<>();
		for (TLStructuredTypePart part : type.getAllParts()) {
			if (part.isDerived() || (part instanceof TLReference reference && reference.isBackwards())) {
				continue;
			}
			if (isAsCreated(object, part, object.tValue(part))) {
				continue;
			}
			result.add(part);
		}
		return result;
	}

	/** Whether the given value is the one the engine creates the object with. */
	private static boolean isAsCreated(TLObject object, TLStructuredTypePart part, Object value) {
		DefaultProvider provider = DisplayAnnotations.getDefaultProvider(part);
		if (provider == null) {
			return isEmpty(value);
		}
		if (provider.isComputedInTransaction()) {
			// Computing it again draws the next number, so there is nothing to compare with.
			return isEmpty(value);
		}
		Object initial = provider.createDefault(object.tContainer(), part);
		return sameValue(initial, value) || (isEmpty(initial) && isEmpty(value));
	}

	private static boolean isEmpty(Object value) {
		return value == null
			|| Boolean.FALSE.equals(value)
			|| (value instanceof CharSequence text && text.length() == 0)
			|| (value instanceof Collection<?> collection && collection.isEmpty());
	}

	private static boolean sameValue(Object a, Object b) {
		if (a instanceof Collection<?> left && b instanceof Collection<?> right) {
			return new HashSet<>(left).equals(new HashSet<>(right));
		}
		return Objects.equals(a, b);
	}

	/**
	 * One evaluation of one change set, against one principal.
	 *
	 * <p>
	 * The principal is taken from the run's {@link ModelSecurity}, which bound it once when the run's
	 * context was built, rather than read from the thread: a concurrent tool batch runs on worker
	 * threads that re-establish the sub-session, and the account named in an audit record must be the
	 * one the checks were actually made for.
	 * </p>
	 */
	private static final class Check {

		private final ModelSecurity _model;

		private final ToolChangeSet _changes;

		private final String _accountName;

		private final List<Violation> _violations = new ArrayList<>();

		/** The creations whose type and container passed, whose initial values are asked next. */
		private final List<Creation> _permittedCreations = new ArrayList<>();

		Check(ModelSecurity model, ToolChangeSet changes) {
			_model = model;
			_changes = changes;
			_accountName = accountName(model);
		}

		private static String accountName(ModelSecurity model) {
			if (model instanceof ModelAccessRightsSecurity rights) {
				Person account = rights.person();
				return account == null ? "(none)" : account.getName();
			}
			return "(unknown)";
		}

		List<Violation> run() {
			for (Creation creation : _changes.created()) {
				checkCreation(creation);
			}
			for (Map.Entry<TLObject, Set<TLStructuredTypePart>> update : _changes.updated().entrySet()) {
				checkUpdate(update.getKey(), update.getValue());
			}
			for (TLObject deleted : _changes.deleted()) {
				checkDeletion(deleted);
			}
			return _violations;
		}

		/**
		 * Condition 1 and condition 2 of a creation: CREATE on the type in the committed container,
		 * and WRITE on the container's composition attribute.
		 *
		 * <p>
		 * The created object itself is never asked about - see the {@link PreCommitGuard known limit}.
		 * </p>
		 */
		private void checkCreation(Creation creation) {
			TLClass type = creation.type();
			if (type == null) {
				// Not an application type. ToolChangeSet.pending() never reports such an item, so
				// this is only reachable from a hand-built change set.
				return;
			}
			String typeName = String.valueOf(type);
			if (!_model.canCreate(type, creation.container())) {
				record(creation.object(), typeName, SimpleBoundCommandGroup.CREATE, null);
				return;
			}
			TLStructuredTypePart composition = creation.containerReference();
			if (composition != null && !_model.canCreate(creation.container(), composition, type)) {
				record(creation.object(), typeName, SimpleBoundCommandGroup.CREATE, null);
				return;
			}
			_permittedCreations.add(creation);
		}

		/**
		 * The initial values of every new object whose creation passed: the right to set each
		 * attribute the turn gave a value, as the create dialogs ask it.
		 *
		 * <p>
		 * Only the first refused attribute per object is recorded, as for an update.
		 * </p>
		 *
		 * @return All violations found, those of {@link #run()} included.
		 */
		List<Violation> runInitialValues() {
			for (Creation creation : _permittedCreations) {
				TLObject object = creation.object();
				for (TLStructuredTypePart attribute : givenValues(object, creation.type())) {
					if (!_model.canSetInitial(object, attribute)) {
						record(object, String.valueOf(creation.type()), SimpleBoundCommandGroup.WRITE, attribute,
							true);
						break;
					}
				}
			}
			return _violations;
		}

		/**
		 * WRITE on the object, plus WRITE on every attribute the turn touched.
		 *
		 * <p>
		 * Both, not either: model security is attribute-granular, so a principal may hold the object
		 * and lack one of its attributes. Only the first failure per object is recorded - the model
		 * needs to know that this object was refused and why, not the full list of attributes it may
		 * not touch, which would itself disclose the shape of what it cannot see.
		 * </p>
		 */
		private void checkUpdate(TLObject object, Set<TLStructuredTypePart> attributes) {
			String typeName = typeName(object);
			if (!_model.canWrite(object)) {
				record(object, typeName, SimpleBoundCommandGroup.WRITE, null);
				return;
			}
			for (TLStructuredTypePart attribute : attributes) {
				if (!_model.canWrite(object, attribute)) {
					record(object, typeName, SimpleBoundCommandGroup.WRITE, attribute);
					return;
				}
			}
		}

		private void checkDeletion(TLObject object) {
			if (!_model.canDelete(object)) {
				record(object, typeName(object), SimpleBoundCommandGroup.DELETE, null);
			}
		}

		/**
		 * Records the violation in the audit log and collects it.
		 *
		 * <p>
		 * The log line names the operation, the type and the account, and never the object's values -
		 * an audit record that quotes the withheld data defeats its own purpose, exactly as for a
		 * {@link ToolAuthorization} denial. Its fields follow that denial's shape, so one log format
		 * answers "why was this refused?" for both layers.
		 * </p>
		 *
		 * <p>
		 * At {@code ERROR}, where a tool denial is a {@code WARN}: a denied invocation means a user
		 * asked for something they may not have, a violation here means a tool <em>wrote</em> something
		 * its caller may not write. That is the highest-signal event this design produces, and section
		 * 3.8 of the architecture document asks for it at this level.
		 * </p>
		 */
		private void record(TLObject object, String typeName, BoundCommandGroup operation,
				TLStructuredTypePart attribute) {
			record(object, typeName, operation, attribute, false);
		}

		private void record(TLObject object, String typeName, BoundCommandGroup operation,
				TLStructuredTypePart attribute, boolean initialValue) {
			String reason = initialValue
				? "the account may not set '" + attribute.getName() + "' of a new object of this type"
				: "the account holds no '" + operation.getID() + "' right on this type";
			Logger.error("Pre-commit check failed: operation='" + operation.getID()
				+ "' type='" + (typeName == null ? "(none)" : typeName) + "'"
				+ " user=" + _accountName
				+ " reason=" + reason, ModelSecurityPreCommitGuard.class);
			_violations.add(new Violation(object, typeName, operation, attribute, reason, initialValue));
		}

		private static String typeName(TLObject object) {
			if (object == null) {
				return null;
			}
			TLStructuredType type;
			try {
				type = object.tType();
			} catch (RuntimeException ex) {
				// A deleted object seen outside the committed view. Not worth failing over: the
				// violation is still recorded, only without its type name.
				return null;
			}
			// String.valueOf, not TLModelUtil.qualifiedName: a model part's own toString() is its
			// qualified name, and this is the shape ModelSecurity.requireRead already reports.
			return type == null ? null : String.valueOf(type);
		}
	}
}

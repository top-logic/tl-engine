/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
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
 * @see PreCommitGuard#forRun(SecurityContext)
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
			return check.run();
		}
		ComputationEx2<List<Violation>, RuntimeException, RuntimeException> job = check::run;
		return PersistencyLayer.getKnowledgeBase().withoutModifications(job);
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
				record(creation.object(), typeName, SimpleBoundCommandGroup.CREATE);
				return;
			}
			TLStructuredTypePart composition = creation.containerReference();
			if (composition != null && !_model.canCreate(creation.container(), composition)) {
				record(creation.object(), typeName, SimpleBoundCommandGroup.CREATE);
			}
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
				record(object, typeName, SimpleBoundCommandGroup.WRITE);
				return;
			}
			for (TLStructuredTypePart attribute : attributes) {
				if (!_model.canWrite(object, attribute)) {
					record(object, typeName, SimpleBoundCommandGroup.WRITE);
					return;
				}
			}
		}

		private void checkDeletion(TLObject object) {
			if (!_model.canDelete(object)) {
				record(object, typeName(object), SimpleBoundCommandGroup.DELETE);
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
		private void record(TLObject object, String typeName, BoundCommandGroup operation) {
			String reason = "the account holds no '" + operation.getID() + "' right on this type";
			Logger.error("Pre-commit check failed: operation='" + operation.getID()
				+ "' type='" + (typeName == null ? "(none)" : typeName) + "'"
				+ " user=" + _accountName
				+ " reason=" + reason, ModelSecurityPreCommitGuard.class);
			_violations.add(new Violation(object, typeName, operation, reason));
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

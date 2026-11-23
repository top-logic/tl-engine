/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.agent;

import java.util.Set;

import com.top_logic.ai.core.model.Skill;
import com.top_logic.basic.col.Sink;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.form.OverlayLookup;
import com.top_logic.model.util.Pointer;
import com.top_logic.util.TLContext;

/**
 * {@link ConstraintCheck} that ensures that the {@code name} of a stored agent or skill is free in
 * the whole application.
 *
 * <p>
 * A name is taken when another stored record of the type that declares the attribute has it, also
 * a record of another specialization and a record that the editing person may not read: names are
 * global, and the lookup by name does not depend on the person ({@link AgentDefinitionQuery}). The
 * commit runs this check with the security checks switched off, so it sees every record.
 * </p>
 *
 * <p>
 * The name of a configured agent is taken for a stored agent, and the name of a configured skill for
 * a stored skill. A stored record wins over the configured definition of the same name, so a person
 * who saved such a name would put an own text in place of the configured agent for every account.
 * Only an administrator and code in a system context, for example the import of the initial data,
 * may save such a name.
 * </p>
 *
 * <p>
 * The comparison is case-insensitive to match the persistence-level backstop: the {@code name}
 * columns of {@code AIAgent} / {@code AISkill} are {@code String} columns, which the database
 * renders case-insensitively (e.g. {@code VARCHAR_IGNORECASE} on H2), and the lookup by name finds a
 * record in any case. Aligning both layers ensures the form gives fast feedback for exactly those
 * duplicates the unique DB index would reject.
 * </p>
 */
public class UniqueNameConstraint implements ConstraintCheck {

	@Override
	public ResKey check(TLObject object, TLStructuredTypePart attribute) {
		Object value = object.tValue(attribute);
		if (!(value instanceof String name) || name.isEmpty()) {
			return null;
		}
		if (takenByOtherRecord(object, attribute, name) || takesConfiguredName(object, name)) {
			return I18NConstants.DUPLICATE_NAME_ERROR__NAME.fill(name);
		}
		return null;
	}

	/**
	 * Whether another stored record of the type that declares the attribute has the given name.
	 *
	 * <p>
	 * The declaring type and not the type of the object: all specializations share one name space,
	 * because the lookup by name searches all of them.
	 * </p>
	 */
	private static boolean takenByOtherRecord(TLObject object, TLStructuredTypePart attribute, String name) {
		TLClass declaringType = (TLClass) attribute.getDefinition().getOwner();
		for (TLObject other : MetaElementUtil.getAllInstancesOf(declaringType, TLObject.class)) {
			if (other.equals(object) || !other.tValid()) {
				continue;
			}
			if (other.tValue(attribute) instanceof String otherName && name.equalsIgnoreCase(otherName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether the given name is the name of a configured definition of the same kind, which the
	 * current context may not take.
	 */
	private static boolean takesConfiguredName(TLObject object, String name) {
		if (mayTakeConfiguredName() || !TopLogicAgentDefinitions.Module.INSTANCE.isActive()) {
			return false;
		}
		TopLogicAgentDefinitions configured = TopLogicAgentDefinitions.getInstance();
		Set<String> names = object instanceof Skill
			? configured.getAllConfigSkills().keySet()
			: configured.getAllConfigAgents().keySet();
		for (String configuredName : names) {
			if (configuredName.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether the current context may save a stored record with the name of a configured one.
	 *
	 * <p>
	 * Not {@link ThreadContext#isAdmin()}: the commit runs the constraints with the security checks
	 * switched off, and that method counts such a context as an administrator.
	 * </p>
	 */
	private static boolean mayTakeConfiguredName() {
		return ThreadContext.isSystemContext() || Person.isAdmin(TLContext.currentUser());
	}

	@Override
	public void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace,
			OverlayLookup overlays) {
		// Only depends on the annotated attribute's own value — no additional dependencies.
	}
}

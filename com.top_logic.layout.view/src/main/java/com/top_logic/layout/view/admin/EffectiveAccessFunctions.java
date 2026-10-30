/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.element.boundsec.manager.explain.AccessExplanation;
import com.top_logic.element.boundsec.manager.explain.EffectiveAccess;
import com.top_logic.element.boundsec.manager.explain.HeldRole;
import com.top_logic.element.boundsec.manager.explain.OperationDecision;
import com.top_logic.element.boundsec.manager.explain.RoleAssignment;
import com.top_logic.element.boundsec.manager.explain.RoleSource;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.operations.AdminOnly;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.Resources;

/**
 * TL-Script functions presenting the {@link EffectiveAccess effective access} of an account: why it
 * may or may not perform an operation on an object, and which roles it holds where.
 *
 * <p>
 * The explanation of an account's access to an object is computed once by
 * {@link #explain(Person, TLObject)} and handed to the other functions, which read the parts a view
 * shows from it: the {@link #operations(AccessExplanation) decisions} with their granting and
 * admitting roles, and the {@link #roleRows(AccessExplanation) held roles} with their sources.
 * Without an object, {@link #assignments(Person, Collection)} lists all roles the account holds.
 * </p>
 *
 * @implNote Every public static method here is a TL-Script function named by {@link #PREFIX}
 *           followed by the capitalized method name, so {@link #explain(Person, TLObject)} is
 *           called <code>effectiveAccessExplain</code>. The functions reveal the access rights of
 *           every account, which only an administrator may read from a script entered
 *           interactively.
 */
@ScriptPrefix(EffectiveAccessFunctions.PREFIX)
@AdminOnly
public class EffectiveAccessFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "effectiveAccess";

	/** Separator between the roles listed in a text. */
	private static final String SEPARATOR = ", ";

	/** Starts a paragraph of {@link #info(Person, AccessExplanation)}. */
	private static final String HTML_PARAGRAPH_START = "<p>";

	/** Ends a paragraph of {@link #info(Person, AccessExplanation)}. */
	private static final String HTML_PARAGRAPH_END = "</p>";

	/**
	 * Explains the access the given account has to the given object.
	 *
	 * @param account
	 *        The account to explain the access of.
	 * @param object
	 *        The object to explain the access to.
	 * @return The explanation, <code>null</code> when the account or the object is missing.
	 */
	@Label("Effective access of an account on an object")
	@SideEffectFree
	public static AccessExplanation explain(Person account, TLObject object) {
		if (account == null || object == null) {
			return null;
		}
		return EffectiveAccess.getInstance().explain(account, object);
	}

	/**
	 * All roles the given account holds on any object, with their sources.
	 *
	 * <p>
	 * A role inherited from a role parent is listed once, on the role parent.
	 * </p>
	 *
	 * @param account
	 *        The account to list the roles of.
	 * @param roles
	 *        The roles to list, empty for all roles.
	 * @return The role assignments, empty for no account or when the
	 *         {@link #sourcesAvailable() sources are not available}.
	 */
	@Label("Role assignments of an account")
	@SideEffectFree
	public static List<RoleAssignment> assignments(Person account, Collection<BoundedRole> roles) {
		if (account == null) {
			return List.of();
		}
		return EffectiveAccess.getInstance().assignments(account, roles);
	}

	/**
	 * Whether the sources of the roles an account holds can be determined.
	 *
	 * @return <code>false</code> when the access manager keeps no security storage or the storage
	 *         is being rebuilt.
	 */
	@Label("Whether the sources of roles are available")
	@SideEffectFree
	public static boolean sourcesAvailable() {
		return EffectiveAccess.getInstance().isSourcesAvailable();
	}

	/**
	 * What a view tells above the access of the given account, as paragraphs of HTML: the bypasses
	 * applying to the account and to the type of the object, the object deciding for an object
	 * delegating to an access parent, and whether the sources of the roles are available.
	 *
	 * @param account
	 *        The account whose access is shown.
	 * @param explanation
	 *        The explanation of the account's access to an object, see
	 *        {@link #explain(Person, TLObject)}, <code>null</code> when the view lists the
	 *        {@link #assignments(Person, Collection) role assignments} of the account.
	 * @return The paragraphs in the language of the user, <code>null</code> for nothing to tell.
	 */
	@Label("Explanation of the effective access")
	@SideEffectFree
	public static String info(Person account, AccessExplanation explanation) {
		List<ResKey> texts = new ArrayList<>();
		if (account == null) {
			texts.add(I18NConstants.EFFECTIVE_ACCESS_NO_ACCOUNT);
		} else if (explanation == null) {
			addAccountInfo(texts, account);
			if (!sourcesAvailable()) {
				texts.add(I18NConstants.EFFECTIVE_ACCESS_SOURCES_UNAVAILABLE);
			}
			texts.add(I18NConstants.EFFECTIVE_ACCESS_NO_OBJECT);
		} else {
			addAccountInfo(texts, explanation.person());
			TLObject object = explanation.object();
			String type = MetaLabelProvider.INSTANCE.getLabel(object.tType());
			if (explanation.withoutSecurity()) {
				texts.add(I18NConstants.EFFECTIVE_ACCESS_WITHOUT_SECURITY__TYPE.fill(type));
			}
			if (explanation.internal()) {
				texts.add(I18NConstants.EFFECTIVE_ACCESS_INTERNAL__TYPE.fill(type));
			}
			if (explanation.decidingObject() == null) {
				texts.add(I18NConstants.EFFECTIVE_ACCESS_NO_DECIDING_OBJECT);
			} else if (explanation.isDelegating()) {
				texts.add(I18NConstants.EFFECTIVE_ACCESS_DELEGATED__TYPE_OBJECT.fill(type,
					MetaLabelProvider.INSTANCE.getLabel(explanation.decidingObject())));
			}
			if (!explanation.sourcesAvailable()) {
				texts.add(I18NConstants.EFFECTIVE_ACCESS_SOURCES_UNAVAILABLE);
			}
		}
		if (texts.isEmpty()) {
			return null;
		}
		Resources resources = Resources.getInstance();
		StringBuilder result = new StringBuilder();
		for (ResKey text : texts) {
			result.append(HTML_PARAGRAPH_START);
			result.append(TagUtil.encodeXML(resources.getString(text)));
			result.append(HTML_PARAGRAPH_END);
		}
		return result.toString();
	}

	private static void addAccountInfo(List<ResKey> texts, Person account) {
		String name = MetaLabelProvider.INSTANCE.getLabel(account);
		if (Person.isAdmin(account)) {
			texts.add(I18NConstants.EFFECTIVE_ACCESS_ADMINISTRATOR__ACCOUNT.fill(name));
		}
		if (account.isRestrictedUser().booleanValue()) {
			texts.add(I18NConstants.EFFECTIVE_ACCESS_RESTRICTED_USER__ACCOUNT.fill(name));
		}
	}

	/**
	 * The decisions for the operations applying to the object of the given explanation.
	 *
	 * @param explanation
	 *        The explanation, see {@link #explain(Person, TLObject)}.
	 * @return The decisions, empty for no explanation.
	 */
	@Label("Operation decisions of the effective access")
	@SideEffectFree
	public static List<OperationDecision> operations(AccessExplanation explanation) {
		return explanation == null ? List.of() : explanation.operations();
	}

	/**
	 * The label of the operation of the given decision.
	 *
	 * @param decision
	 *        A decision, see {@link #operations(AccessExplanation)}.
	 * @return The label of the command group of the operation.
	 */
	@Label("Label of the operation of a decision")
	@SideEffectFree
	public static ResKey operationLabel(@Mandatory OperationDecision decision) {
		return SecurityGrantFunctions.labelKey(decision.operation());
	}

	/**
	 * Whether the account may perform the operation of the given decision.
	 *
	 * @param decision
	 *        A decision, see {@link #operations(AccessExplanation)}.
	 * @return The decision of the access check, made for the account.
	 */
	@Label("Whether an operation is allowed")
	@SideEffectFree
	public static boolean allowed(@Mandatory OperationDecision decision) {
		return decision.allowed();
	}

	/**
	 * The roles granting the operation of the given decision: those of the admitting roles the
	 * account holds on the deciding object.
	 *
	 * @param decision
	 *        A decision, see {@link #operations(AccessExplanation)}.
	 * @return The granting roles.
	 */
	@Label("Granting roles of a decision")
	@SideEffectFree
	public static Set<BoundedRole> grantingRoles(@Mandatory OperationDecision decision) {
		return decision.grantingRoles();
	}

	/**
	 * The roles admitting the operation of the given decision when held on the deciding object.
	 *
	 * @param decision
	 *        A decision, see {@link #operations(AccessExplanation)}.
	 * @return The admitting roles.
	 */
	@Label("Admitting roles of a decision")
	@SideEffectFree
	public static Set<BoundedRole> admittingRoles(@Mandatory OperationDecision decision) {
		return decision.admittingRoles();
	}

	/**
	 * Whether the account must not perform the operation of the given decision at all, because it
	 * is a restricted user and the operation modifies or deletes.
	 *
	 * @param decision
	 *        A decision, see {@link #operations(AccessExplanation)}.
	 * @return Whether the operation is denied whatever roles the account holds.
	 */
	@Label("Whether an operation is restricted")
	@SideEffectFree
	public static boolean restricted(@Mandatory OperationDecision decision) {
		return decision.restricted();
	}

	/**
	 * Why the given decision is made.
	 *
	 * <p>
	 * For an object delegating to an access parent, the reason names the operation checked on the
	 * deciding object in place of the operation of the decision, see
	 * {@link OperationDecision#checkedOperation()}.
	 * </p>
	 *
	 * @param explanation
	 *        The explanation, see {@link #explain(Person, TLObject)}.
	 * @param decision
	 *        A decision of it, see {@link #operations(AccessExplanation)}.
	 * @return The reason in the language of the user.
	 */
	@Label("Reason of a decision")
	@SideEffectFree
	public static String reason(@Mandatory AccessExplanation explanation, @Mandatory OperationDecision decision) {
		ResKey reason;
		if (decision.allowed()) {
			if (!decision.grantingRoles().isEmpty()) {
				reason = I18NConstants.EFFECTIVE_ACCESS_ALLOWED_BY_ROLES__ROLES.fill(roles(decision.grantingRoles()));
			} else if (explanation.administrator()) {
				reason = I18NConstants.EFFECTIVE_ACCESS_ALLOWED_AS_ADMINISTRATOR;
			} else if (explanation.withoutSecurity()) {
				reason = I18NConstants.EFFECTIVE_ACCESS_ALLOWED_WITHOUT_SECURITY;
			} else {
				reason = I18NConstants.EFFECTIVE_ACCESS_ALLOWED_OTHERWISE;
			}
		} else if (decision.restricted()) {
			reason = I18NConstants.EFFECTIVE_ACCESS_DENIED_RESTRICTED;
		} else if (decision.admittingRoles().isEmpty()) {
			reason = I18NConstants.EFFECTIVE_ACCESS_DENIED_NOT_GRANTED;
		} else if (decision.grantingRoles().isEmpty()) {
			reason = I18NConstants.EFFECTIVE_ACCESS_DENIED_NO_ROLE__ROLES.fill(roles(decision.admittingRoles()));
		} else {
			reason = I18NConstants.EFFECTIVE_ACCESS_DENIED_OTHERWISE__ROLES.fill(roles(decision.grantingRoles()));
		}
		Resources resources = Resources.getInstance();
		String text = resources.getString(reason);
		TLObject decidingObject = explanation.decidingObject();
		if (!explanation.isDelegating() || decidingObject == null) {
			return text;
		}
		return resources.getString(I18NConstants.EFFECTIVE_ACCESS_CHECKED_ON__OPERATION_OBJECT_REASON.fill(
			resources.getString(SecurityGrantFunctions.labelKey(decision.checkedOperation())),
			MetaLabelProvider.INSTANCE.getLabel(decidingObject), text));
	}

	private static String roles(Set<BoundedRole> roles) {
		return roles.stream()
			.map(MetaLabelProvider.INSTANCE::getLabel)
			.sorted()
			.collect(Collectors.joining(SEPARATOR));
	}

	/**
	 * The rows of the roles the account of the given explanation holds on the deciding object: one
	 * per {@link RoleAssignment source} of a role, and one {@link HeldRole} for a role whose
	 * sources are not known.
	 *
	 * @param explanation
	 *        The explanation, see {@link #explain(Person, TLObject)}.
	 * @return The rows, empty for no explanation.
	 */
	@Label("Held roles of the effective access")
	@SideEffectFree
	public static List<Object> roleRows(AccessExplanation explanation) {
		if (explanation == null) {
			return List.of();
		}
		List<Object> result = new ArrayList<>();
		for (HeldRole held : explanation.heldRoles()) {
			if (held.sources().isEmpty()) {
				result.add(held);
			} else {
				result.addAll(held.sources());
			}
		}
		return result;
	}

	/**
	 * The role of the given row.
	 *
	 * @param row
	 *        A {@link RoleAssignment}, see {@link #assignments(Person, Collection)}, or a row of
	 *        {@link #roleRows(AccessExplanation)}.
	 * @return The role held.
	 */
	@Label("Role of a role assignment")
	@SideEffectFree
	public static BoundedRole role(@Mandatory Object row) {
		if (row instanceof HeldRole held) {
			return held.role();
		}
		return ((RoleAssignment) row).role();
	}

	/**
	 * Why the role of the given row is held.
	 *
	 * @param row
	 *        A {@link RoleAssignment}, see {@link #assignments(Person, Collection)}, or a row of
	 *        {@link #roleRows(AccessExplanation)}.
	 * @return The source of the role, <code>null</code> when it is not known.
	 */
	@Label("Source of a role assignment")
	@SideEffectFree
	public static RoleSource source(@Mandatory Object row) {
		return row instanceof RoleAssignment assignment ? assignment.source() : null;
	}

	/**
	 * The identifier of the role rule delivering the role of the given row.
	 *
	 * @param row
	 *        A {@link RoleAssignment}, see {@link #assignments(Person, Collection)}, or a row of
	 *        {@link #roleRows(AccessExplanation)}.
	 * @return The identifier the rule is configured with, <code>null</code> when no rule delivers
	 *         the role or it is not known.
	 */
	@Label("Rule of a role assignment")
	@SideEffectFree
	public static String ruleId(@Mandatory Object row) {
		return row instanceof RoleAssignment assignment ? assignment.ruleId() : null;
	}

	/**
	 * The object the role of the given row is held on: the object deciding, or a role parent of
	 * it, from which the role is inherited.
	 *
	 * @param row
	 *        A {@link RoleAssignment}, see {@link #assignments(Person, Collection)}, or a row of
	 *        {@link #roleRows(AccessExplanation)}.
	 * @return The object, <code>null</code> when it is not known.
	 */
	@Label("Object of a role assignment")
	@SideEffectFree
	public static TLObject object(@Mandatory Object row) {
		return row instanceof RoleAssignment assignment ? assignment.object() : null;
	}

	/**
	 * The group holding the role of the given row: the representative group of the account, or a
	 * group the account is a member of.
	 *
	 * @param row
	 *        A {@link RoleAssignment}, see {@link #assignments(Person, Collection)}, or a row of
	 *        {@link #roleRows(AccessExplanation)}.
	 * @return The group, <code>null</code> when it is not known.
	 */
	@Label("Group of a role assignment")
	@SideEffectFree
	public static Group group(@Mandatory Object row) {
		return row instanceof RoleAssignment assignment ? assignment.group() : null;
	}

	/**
	 * Whether the role of the given row is inherited from a role parent of the deciding object.
	 *
	 * @param explanation
	 *        The explanation, see {@link #explain(Person, TLObject)}.
	 * @param row
	 *        A row of {@link #roleRows(AccessExplanation)}.
	 * @return <code>null</code> when the object the role is held on is not known.
	 */
	@Label("Whether a held role is inherited")
	@SideEffectFree
	public static Boolean inherited(@Mandatory AccessExplanation explanation, @Mandatory Object row) {
		if (!(row instanceof RoleAssignment assignment) || assignment.object() == null) {
			return null;
		}
		return Boolean.valueOf(explanation.isInherited(assignment));
	}

	/**
	 * What the cell of the object a role of the given row is held on tells: the role parent it is
	 * inherited from.
	 *
	 * @param explanation
	 *        The explanation, see {@link #explain(Person, TLObject)}.
	 * @param row
	 *        A row of {@link #roleRows(AccessExplanation)}.
	 * @return The tooltip in the language of the user, <code>null</code> for a role held on the
	 *         deciding object itself.
	 */
	@Label("Tooltip of the object of a held role")
	@SideEffectFree
	public static String objectTooltip(@Mandatory AccessExplanation explanation, @Mandatory Object row) {
		if (!Boolean.TRUE.equals(inherited(explanation, row))) {
			return null;
		}
		return Resources.getInstance().getString(I18NConstants.EFFECTIVE_ACCESS_INHERITED__PARENT
			.fill(MetaLabelProvider.INSTANCE.getLabel(((RoleAssignment) row).object())));
	}

}

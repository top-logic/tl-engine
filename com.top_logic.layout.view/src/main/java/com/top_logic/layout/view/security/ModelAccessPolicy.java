/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.security.CheckedObjects;
import com.top_logic.model.security.CheckedObjects.Decision;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.service.CommandApprovalService;
import com.top_logic.util.TLContext;

/**
 * Maps a decision of the {@link ModelAccessRights} for the current user to the
 * {@link ExecutableState} of the command offering the operation.
 *
 * <p>
 * An operation on a concrete object additionally needs the approval of the
 * {@link CommandApprovalService}: the global checks configured for the type of the object (e.g. that
 * the anonymous account is neither edited nor deleted) apply to every view-layer command checking
 * the operation on the object, in the same way they apply to the commands of the classic UI. The
 * view layer knows no component and no command ID, so only checks that do not depend on these (e.g.
 * checks selecting the command group) take effect.
 * </p>
 *
 * <p>
 * An allowed operation is {@link ExecutableState#EXECUTABLE executable}. A refused operation is
 * displayed as derived from what the refusal depends on:
 * </p>
 * <ul>
 * <li>The command is <em>hidden</em> when the refusal does not depend on a concrete object: the
 * check runs against the security root (a creation without a container, a check on a type), the
 * user holds none of the roles granted the operation on any object, or the user is restricted (see
 * {@link BoundChecker#isAllowedBypass(Person, BoundCommandGroup)}). The operation is then not
 * possible for this user on any object. Whether the user holds a granted role on some object is
 * answered by {@link AccessManager#hasRoleOnAnyObject(Person, Collection)}, which may overestimate
 * the roles of the user but never hides an operation the user may perform on some object. An
 * operation on an attribute with a grant of its own for the operation (see
 * {@link ModelAccessRights#hasGrant(TLStructuredTypePart, BoundCommandGroup)}) is decided by the
 * roles that grant lists: it replaces the grant of the type for this attribute. Otherwise, the user
 * holds no granted role when they hold none of the roles the type grants the operation to and the
 * operation is not {@link #isOffered(BoundCommandGroup, TLClass) offered} to them on the type either
 * - not on one of its specializations, and, for a type with an access parent, not on the types the
 * access parent may lead to.</li>
 * <li>The command is <em>disabled</em> when the check on a concrete object (the object operated on,
 * or the container to create in) fails. It gives the refusal as its reason, naming the
 * operation.</li>
 * <li>When the {@link CommandApprovalService} refuses an operation the access rights allow on an
 * object, the command is disabled or hidden as the approval check decides. A disabled command gives
 * the reason of the approval check.</li>
 * </ul>
 *
 * @see CommandAccess
 * @see IntrinsicAccessRules
 */
public final class ModelAccessPolicy {

	private ModelAccessPolicy() {
		// Static utility.
	}

	/**
	 * The state of a command performing the given operation on the given object, or on an attribute
	 * of it.
	 *
	 * <p>
	 * When the access rights allow the operation, the {@link CommandApprovalService} decides about
	 * it on the object, see {@link #approval(BoundCommandGroup, TLObject)}. A refusal is hidden when
	 * the user is restricted or holds none of the roles granted the operation on the type of the
	 * object on any object (see {@link ModelAccessPolicy}), and disabled otherwise.
	 * </p>
	 *
	 * @param operation
	 *        The operation performed.
	 * @param object
	 *        The object operated on.
	 * @param attribute
	 *        The attribute of the object operated on, {@code null} for an operation on the object
	 *        itself.
	 */
	public static ExecutableState onObject(BoundCommandGroup operation, TLObject object,
			TLStructuredTypePart attribute) {
		ModelAccessRights rights = ModelAccessRights.getInstance();
		Person user = TLContext.currentUser();
		boolean allowed = attribute == null
			? rights.isAllowed(user, object, operation)
			: rights.isAllowed(user, object, attribute, operation);
		if (!allowed) {
			boolean hide = restricted(user, operation)
				|| heldByNoGrantedRole(rights, user, classOf(object), attribute, operation);
			return refused(hide, () -> objectReason(operation, attribute));
		}
		return approval(operation, object, attribute);
	}

	/**
	 * The decision of the {@link CommandApprovalService} about the given operation on the given
	 * object, asked without a component and command ID.
	 *
	 * <p>
	 * A refusal is hidden or disabled as the approval check decides. A disabled state gives the
	 * reason of the approval check, or the generic refusal of the operation when it gives none.
	 * </p>
	 *
	 * @param operation
	 *        The operation the access rights allow on the object.
	 * @param object
	 *        The object operated on.
	 */
	public static ExecutableState approval(BoundCommandGroup operation, TLObject object) {
		return approval(operation, object, null);
	}

	private static ExecutableState approval(BoundCommandGroup operation, TLObject object,
			TLStructuredTypePart attribute) {
		ExecutableState approval =
			CommandApprovalService.getInstance().isExecutable(null, operation, null, object, Collections.emptyMap());
		if (approval.isExecutable()) {
			return ExecutableState.EXECUTABLE;
		}
		return refused(approval.isHidden(), () -> approvalReason(approval, operation, attribute));
	}

	/**
	 * The state of a command performing the given operation on a checked value: a single object,
	 * every element of a collection, or nothing for {@code null} or an empty collection (see
	 * {@link CheckedObjects}).
	 *
	 * <ul>
	 * <li>Hidden, when the user can never hold the operation on objects of the given type, see
	 * {@link #isOffered(BoundCommandGroup, TLClass)}.</li>
	 * <li>{@link ExecutableState#NO_EXEC_NO_MODEL Disabled}, when there is no object.</li>
	 * <li>Disabled with the {@link #deniedReason(BoundCommandGroup) refusal of the operation} as
	 * reason, when the operation is refused on an element.</li>
	 * <li>Otherwise, as the {@link #approval(BoundCommandGroup, TLObject) approval} of every element
	 * decides.</li>
	 * </ul>
	 *
	 * @param operation
	 *        The operation performed.
	 * @param type
	 *        The static type of the checked object(s), {@code null} when unknown: then the type is
	 *        not checked for reach.
	 * @param value
	 *        The checked value.
	 * @param elementCheck
	 *        Decides the operation on a single element, see
	 *        {@link CheckedObjects#check(Object, Function)}.
	 */
	public static ExecutableState onChecked(BoundCommandGroup operation, TLClass type, Object value,
			Function<Object, Decision> elementCheck) {
		if (type != null && !isOffered(operation, type)) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		Decision decision = CheckedObjects.check(value, elementCheck);
		switch (decision.getOutcome()) {
			case NO_OBJECT:
				return ExecutableState.NO_EXEC_NO_MODEL;
			case DENIED:
			case INVALID:
				return ExecutableState.createDisabledState(deniedReason(operation));
			case ALLOWED:
				return approvalOfAll(operation, value);
		}
		throw new IllegalArgumentException("No such outcome: " + decision.getOutcome());
	}

	private static ExecutableState approvalOfAll(BoundCommandGroup operation, Object value) {
		Collection<?> elements = value instanceof Collection<?> collection ? collection : List.of(value);
		for (Object element : elements) {
			if (element instanceof TLObject object) {
				ExecutableState state = approval(operation, TLFormObjectBase.editedObject(object));
				if (!state.isExecutable()) {
					return state;
				}
			}
		}
		return ExecutableState.EXECUTABLE;
	}

	/**
	 * The state of the given operation on an attribute of an object displayed in a form or table.
	 *
	 * <p>
	 * The object may be the persistent object itself, a {@link TLFormObjectBase form object}
	 * buffering the edit of an object, or a transient draft of an object to be created:
	 * </p>
	 * <ul>
	 * <li>A form object editing an object is decided by the {@link TLFormObjectBase#getEditedObject()
	 * edited object}: the buffer itself is transient and holds no roles.</li>
	 * <li>A persistent object is decided like {@link #onObject(BoundCommandGroup, TLObject,
	 * TLStructuredTypePart)} does, with the display derived from the check. Reading
	 * is decided by the access rights alone, the {@link CommandApprovalService} is not asked.</li>
	 * <li>A transient object (a draft created for a create dialog, or a form object creating an
	 * object) holds no roles yet. Its attribute rights are decided through its access parent, see
	 * {@link ModelAccessRights#isAllowedInitial(Person, TLObject, TLStructuredTypePart, BoundCommandGroup)}.
	 * A refusal is hidden when the attribute has a grant of its own for the operation listing no
	 * role the user holds on any object, or the user is restricted, and disabled otherwise: it
	 * depends on the access parent.</li>
	 * </ul>
	 *
	 * <p>
	 * Reading an attribute is checked on the object as well, also for an attribute without a
	 * {@link ModelAccessRights#hasGrant(TLStructuredTypePart, BoundCommandGroup) read grant of its
	 * own}: an object reached in the UI (a table row, a referenced object) is only known to be
	 * discoverable, which releases none of its attributes. Such an attribute is decided by the read
	 * grant of the object's type.
	 * </p>
	 *
	 * <p>
	 * A disabled state gives the refusal naming the operation on the attribute as its reason.
	 * </p>
	 *
	 * @param operation
	 *        The operation on the attribute, e.g. {@link SimpleBoundCommandGroup#READ} for displaying
	 *        its value or {@link SimpleBoundCommandGroup#WRITE} for editing it.
	 * @param object
	 *        The object displayed.
	 * @param attribute
	 *        The attribute of the object accessed.
	 */
	public static ExecutableState onAttribute(BoundCommandGroup operation, TLObject object,
			TLStructuredTypePart attribute) {
		TLObject target = editedObject(object);
		if (target.tTransient()) {
			return onDraftAttribute(operation, target, attribute);
		}
		if (isRead(operation)) {
			return onReadAttribute(operation, target, attribute);
		}
		return onObject(operation, target, attribute);
	}

	/**
	 * The state of displaying the given attribute of the given persistent object: decided by the
	 * access rights alone, with the display derived from the check.
	 */
	private static ExecutableState onReadAttribute(BoundCommandGroup operation, TLObject object,
			TLStructuredTypePart attribute) {
		ModelAccessRights rights = ModelAccessRights.getInstance();
		Person user = TLContext.currentUser();
		if (rights.isAllowed(user, object, attribute, operation)) {
			return ExecutableState.EXECUTABLE;
		}
		boolean hide = restricted(user, operation)
			|| heldByNoGrantedRole(rights, user, classOf(object), attribute, operation);
		return refused(hide, () -> objectReason(operation, attribute));
	}

	private static boolean isRead(BoundCommandGroup operation) {
		return SimpleBoundCommandGroup.READ.getID().equals(operation.getID());
	}

	/**
	 * Whether the given operation on the given attribute is refused for the current user
	 * independent of any concrete object of the given type, e.g. to decide whether a table offers a
	 * column for the attribute at all.
	 *
	 * <p>
	 * The operation is refused independent of an object when the user is restricted (see
	 * {@link BoundChecker#isAllowedBypass(Person, BoundCommandGroup)}), or the user holds no role
	 * granted the operation on any object: the attribute's own grant for the operation, if it has one
	 * (see {@link ModelAccessRights#hasGrant(TLStructuredTypePart, BoundCommandGroup)}), lists no
	 * role the user holds, or, without one, the user holds none of the roles the type grants the
	 * operation to and it is not {@link #isOffered(BoundCommandGroup, TLClass) offered} to the user on the
	 * type either, also through an access parent of the type. A refusal is
	 * {@link ExecutableState#NOT_EXEC_HIDDEN hidden}, like
	 * {@link #onObject(BoundCommandGroup, TLObject, TLStructuredTypePart)} displays it
	 * on a concrete object. Everything else depends on the object and is
	 * {@link ExecutableState#EXECUTABLE executable} here; it is decided per object by
	 * {@link #onAttribute(BoundCommandGroup, TLObject, TLStructuredTypePart)}.
	 * </p>
	 *
	 * @param operation
	 *        The operation on the attribute.
	 * @param type
	 *        The type of the objects whose attribute is accessed, {@code null} when unknown: then
	 *        the type defining the attribute stands for it.
	 * @param attribute
	 *        The attribute accessed.
	 */
	public static ExecutableState onAttributeOfType(BoundCommandGroup operation, TLStructuredType type,
			TLStructuredTypePart attribute) {
		ModelAccessRights rights = ModelAccessRights.getInstance();
		TLClass clazz = type instanceof TLClass typeClass ? typeClass
			: attribute.getOwner() instanceof TLClass owner ? owner : null;
		if (clazz != null && rights.isWithoutSecurity(clazz)) {
			return ExecutableState.EXECUTABLE;
		}
		Person user = TLContext.currentUser();
		Boolean bypass = BoundChecker.isAllowedBypass(user, operation);
		if (bypass != null) {
			return bypass.booleanValue() ? ExecutableState.EXECUTABLE : ExecutableState.NOT_EXEC_HIDDEN;
		}
		if (heldByNoGrantedRole(rights, user, clazz, attribute, operation)) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		return ExecutableState.EXECUTABLE;
	}

	/**
	 * The state of editing the given object in a form: the given operation on the object, decided
	 * like a command checking the operation on it (see
	 * {@link #onChecked(BoundCommandGroup, TLClass, Object, Function)}), the type being the type of
	 * the object.
	 *
	 * <p>
	 * A {@link TLFormObjectBase form object} editing an object is decided by the
	 * {@link TLFormObjectBase#getEditedObject() edited object}. A transient object (e.g. the draft
	 * of a create dialog) is always editable: creating it was checked where it was created (see
	 * {@link #createIn(TLObject, TLStructuredTypePart, TLClass)}), and the right to set each of its
	 * values is decided per attribute, see
	 * {@link #onAttribute(BoundCommandGroup, TLObject, TLStructuredTypePart)}.
	 * </p>
	 *
	 * <p>
	 * An operation not applying to the type of the object (see
	 * {@link ModelAccessRights#getObjectOperations(TLObject)}) is a configuration problem; it is
	 * reported, and editing is hidden.
	 * </p>
	 *
	 * @param operation
	 *        The operation required for editing, {@link SimpleBoundCommandGroup#WRITE} by default.
	 * @param object
	 *        The object displayed in the form.
	 */
	public static ExecutableState onEdit(BoundCommandGroup operation, TLObject object) {
		TLObject target = editedObject(object);
		if (target.tTransient()) {
			return ExecutableState.EXECUTABLE;
		}
		ModelAccessRights rights = ModelAccessRights.getInstance();
		if (!rights.getObjectOperations(target).appliesTo(operation)) {
			Logger.error("The operation '" + operation.getID() + "' required for editing '" + target
				+ "' does not apply to its type; declare it in the <security-config>.", ModelAccessPolicy.class);
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		Person user = TLContext.currentUser();
		return onChecked(operation, classOf(target), target,
			element -> CheckedObjects.checkElement(user, element, operation));
	}

	/**
	 * The object whose rights decide about the given object: the edited object of a form object
	 * editing an object, the given object otherwise.
	 */
	private static TLObject editedObject(TLObject object) {
		TLObject result = object;
		while (result instanceof TLFormObjectBase formObject && !formObject.isCreate()) {
			TLObject edited = formObject.getEditedObject();
			if (edited == null) {
				break;
			}
			result = edited;
		}
		return result;
	}

	/**
	 * The state of the given operation on an attribute of the given object to be created.
	 *
	 * @see #onAttribute(BoundCommandGroup, TLObject, TLStructuredTypePart)
	 */
	private static ExecutableState onDraftAttribute(BoundCommandGroup operation, TLObject draft,
			TLStructuredTypePart attribute) {
		ModelAccessRights rights = ModelAccessRights.getInstance();
		Person user = TLContext.currentUser();
		if (rights.isAllowedInitial(user, draft, attribute, operation)) {
			return ExecutableState.EXECUTABLE;
		}
		boolean hide = restricted(user, operation) || attributeHeldByNoGrantedRole(rights, user, attribute, operation);
		return refused(hide, () -> objectReason(operation, attribute));
	}

	/**
	 * The reason the {@link CommandApprovalService} gives for refusing an operation, the generic
	 * refusal of the operation when it gives none (a hidden command has no reason to show).
	 */
	private static ResKey approvalReason(ExecutableState approval, BoundCommandGroup operation,
			TLStructuredTypePart attribute) {
		ResKey reason = approval.getI18NReasonKey();
		return approval.isDisabled() && reason != null ? reason : objectReason(operation, attribute);
	}

	/**
	 * The state of a command performing the given operation on objects of the given type, checked
	 * against the security root.
	 *
	 * <p>
	 * For the {@link SimpleBoundCommandGroup#CREATE creation}, this is the check whether the user may
	 * create objects of the type at all ({@link ModelAccessRights#isAllowedCreate(Person, TLClass,
	 * TLObject)} without context).
	 * </p>
	 *
	 * @param operation
	 *        The operation performed.
	 * <p>
	 * A refused command is hidden: the check depends on no concrete object.
	 * </p>
	 *
	 * @param type
	 *        The type of the objects operated on.
	 */
	public static ExecutableState onType(BoundCommandGroup operation, TLClass type) {
		ModelAccessRights rights = ModelAccessRights.getInstance();
		Person user = TLContext.currentUser();
		boolean allowed;
		if (isCreate(operation)) {
			allowed = rights.isAllowedCreate(user, type, (TLObject) null);
		} else {
			allowed = rights.isWithoutSecurity(type) || rights.getAccessibleTypes(user, operation).contains(type);
		}
		if (allowed) {
			return ExecutableState.EXECUTABLE;
		}
		return refused(true, () -> typeReason(operation, type));
	}

	/**
	 * Whether the current user may hold the given operation on objects of the given type at all.
	 *
	 * <p>
	 * A user bypassing the model security (see
	 * {@link BoundChecker#isAllowedBypass(Person, BoundCommandGroup)}) holds every operation. The
	 * operation is out of reach, independent of any concrete object, when the user is restricted,
	 * or when the user holds, on any object, none of the roles granted the operation (or an
	 * operation implying it) on the type or on one of its specializations, as
	 * {@link AccessManager#hasRoleOnAnyObject(Person, Collection)} answers it. A type with an access
	 * parent is decided by the types its access parent may
	 * lead to, with {@link SimpleBoundCommandGroup#WRITE} in place of
	 * {@link SimpleBoundCommandGroup#CREATE} and {@link SimpleBoundCommandGroup#DELETE} (see
	 * {@link ModelAccessRights#getAccessParentTypes(TLClass)}). A type without security, one whose
	 * singletons have rights of their own, or one whose access parent types are not known statically
	 * is always in reach.
	 * </p>
	 *
	 * <p>
	 * A UI unit or command offering the operation is hidden when it is out of reach: no object
	 * changes that.
	 * </p>
	 *
	 * @param operation
	 *        The operation.
	 * @param type
	 *        The (static) type of the objects the operation is performed on.
	 */
	public static boolean isOffered(BoundCommandGroup operation, TLClass type) {
		Person user = TLContext.currentUser();
		Boolean bypass = BoundChecker.isAllowedBypass(user, operation);
		if (bypass != null) {
			return bypass.booleanValue();
		}
		return isGrantable(ModelAccessRights.getInstance(), user, operation, type, new HashSet<>());
	}

	/**
	 * Whether the given user may hold the given operation on some object of the given type or of
	 * one of its specializations, see {@link #isOffered(BoundCommandGroup, TLClass)}.
	 *
	 * @param seen
	 *        The types already visited, to stop at cyclic access parent relations.
	 */
	private static boolean isGrantable(ModelAccessRights rights, Person user, BoundCommandGroup operation,
			TLClass type, Set<TLClass> seen) {
		for (TLClass candidate : TLModelUtil.getReflexiveTransitiveSpecializations(type)) {
			if (!seen.add(candidate)) {
				continue;
			}
			if (rights.isWithoutSecurity(candidate) || rights.hasSingletonRights(candidate)) {
				return true;
			}
			if (rights.getAccessParent(candidate) != null) {
				Set<TLClass> parentTypes = rights.getAccessParentTypes(candidate);
				if (parentTypes == null) {
					// Not known statically: only the check of a concrete object decides.
					return true;
				}
				BoundCommandGroup parentOperation = isCreate(operation) || isDelete(operation)
					? SimpleBoundCommandGroup.WRITE
					: operation;
				for (TLClass parentType : parentTypes) {
					if (isGrantable(rights, user, parentOperation, parentType, seen)) {
						return true;
					}
				}
				continue;
			}
			if (holdsAnyOf(user, rights.getAllowedRoles(candidate, operation))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The reason given for refusing the given operation on a concrete object.
	 *
	 * @param operation
	 *        The refused operation.
	 */
	public static ResKey deniedReason(BoundCommandGroup operation) {
		return objectReason(operation, null);
	}

	/**
	 * The state of a command creating an object in the given container.
	 *
	 * <p>
	 * The user needs the {@link SimpleBoundCommandGroup#CREATE creation} right on the created type in
	 * the context of the container and, when the object is added to a reference of the container,
	 * the {@link SimpleBoundCommandGroup#WRITE write} right on that reference.
	 * </p>
	 *
	 * <p>
	 * A refusal is hidden when the user is restricted, or when the user holds, on any object, none of
	 * the roles granted the creation on the created type or, for a creation into a reference, writing
	 * on the type of the container (see {@link ModelAccessPolicy}): a type with an access parent is
	 * decided by the types its access parent may lead to. Otherwise, the refusal depends on the container
	 * and is disabled.
	 * </p>
	 *
	 * @param container
	 *        The object the created object belongs to.
	 * @param reference
	 *        The (composition) reference of the container the created object is added to,
	 *        {@code null} when the object is created in the context of the container only. Then, the
	 *        type must be given.
	 * @param type
	 *        The type of the created object, {@code null} for the type of the given reference.
	 */
	public static ExecutableState createIn(TLObject container, TLStructuredTypePart reference, TLClass type) {
		ModelAccessRights rights = ModelAccessRights.getInstance();
		Person user = TLContext.currentUser();
		TLClass createdType = type != null ? type : targetClass(reference);

		boolean allowed;
		if (reference == null) {
			allowed = rights.isAllowedCreate(user, createdType, container);
		} else {
			allowed = rights.isAllowedCreate(user, container, reference, type);
		}
		if (allowed) {
			return ExecutableState.EXECUTABLE;
		}
		boolean hide = restricted(user, SimpleBoundCommandGroup.CREATE)
			|| heldByNoGrantedRole(rights, user, createdType, SimpleBoundCommandGroup.CREATE)
			|| (reference != null && (restricted(user, SimpleBoundCommandGroup.WRITE)
				|| heldByNoGrantedRole(rights, user, classOf(container), SimpleBoundCommandGroup.WRITE)));
		return refused(hide, () -> createReason(createdType));
	}

	/**
	 * The concrete types an object of the given type is created as: the type itself when it is
	 * concrete, its concrete specializations when it is abstract.
	 *
	 * <p>
	 * A creation of an abstract type stands for the choice among its concrete specializations: it
	 * is allowed where at least one of them may be created. The result is meant to be computed
	 * once, when the check is set up.
	 * </p>
	 *
	 * @return The concrete types, empty for an abstract type without concrete specialization.
	 */
	public static Set<TLClass> concreteTypes(TLClass type) {
		if (!type.isAbstract()) {
			return Collections.singleton(type);
		}
		return Collections.unmodifiableSet(TLModelUtil.getConcreteSpecializations(type));
	}

	/**
	 * Whether the current user can create objects of the given type at all, independent of the
	 * context they are created in.
	 *
	 * <p>
	 * The creation is out of reach when the {@link SimpleBoundCommandGroup#CREATE creation} is not
	 * {@link #isOffered(BoundCommandGroup, TLClass) offered} on any of the concrete types, or, for a
	 * creation into a reference, when writing the reference is out of reach: its own write grant
	 * lists no role the user holds on any object, or, without one, writing is not offered on the
	 * type defining the reference.
	 * </p>
	 *
	 * @param concreteTypes
	 *        The concrete types the object may be created as, see {@link #concreteTypes(TLClass)}.
	 * @param reference
	 *        The composition reference the object is added to, {@code null} for none.
	 */
	public static boolean isCreateOffered(Set<TLClass> concreteTypes, TLStructuredTypePart reference) {
		boolean offered = false;
		for (TLClass type : concreteTypes) {
			if (isOffered(SimpleBoundCommandGroup.CREATE, type)) {
				offered = true;
				break;
			}
		}
		if (!offered) {
			return false;
		}
		if (reference == null) {
			return true;
		}
		Person user = TLContext.currentUser();
		Boolean bypass = BoundChecker.isAllowedBypass(user, SimpleBoundCommandGroup.WRITE);
		if (bypass != null) {
			return bypass.booleanValue();
		}
		ModelAccessRights rights = ModelAccessRights.getInstance();
		if (rights.hasGrant(reference, SimpleBoundCommandGroup.WRITE)) {
			return holdsAnyOf(user, rights.getAllowedRoles(reference, SimpleBoundCommandGroup.WRITE));
		}
		return !(reference.getOwner() instanceof TLClass owner) || isOffered(SimpleBoundCommandGroup.WRITE, owner);
	}

	/**
	 * The state of a command creating an object of the given type without a context, checked
	 * against the security root: executable when at least one of the concrete types may be
	 * created there, hidden otherwise.
	 *
	 * @param type
	 *        The declared type of the created object, naming it in the reason.
	 * @param concreteTypes
	 *        The concrete types the object may be created as, see {@link #concreteTypes(TLClass)}.
	 */
	public static ExecutableState onCreateWithoutContext(TLClass type, Set<TLClass> concreteTypes) {
		ExecutableState result = ExecutableState.NOT_EXEC_HIDDEN;
		for (TLClass concreteType : concreteTypes) {
			result = onType(SimpleBoundCommandGroup.CREATE, concreteType);
			if (result.isExecutable()) {
				return result;
			}
		}
		return result;
	}

	/**
	 * The state of a command creating an object of the given type in the given context(s).
	 *
	 * <ul>
	 * <li>Hidden, when the user can never create any of the concrete types, see
	 * {@link #isCreateOffered(Set, TLStructuredTypePart)}.</li>
	 * <li>{@link ExecutableState#NO_EXEC_NO_MODEL Disabled}, when there is no context
	 * ({@code null} or an empty collection).</li>
	 * <li>Otherwise, in every context at least one of the concrete types must be allowed, see
	 * {@link #createIn(TLObject, TLStructuredTypePart, TLClass)}. A context where all of them are
	 * refused hides the command when every refusal hides it, else disables it with the refusal of
	 * the declared type as reason. A context that is no object, or no instance of the type defining
	 * the reference, is refused.</li>
	 * </ul>
	 *
	 * @param type
	 *        The declared type of the created object, naming it in the reason.
	 * @param concreteTypes
	 *        The concrete types the object may be created as, see {@link #concreteTypes(TLClass)}.
	 * @param reference
	 *        The composition reference of the context the object is added to, {@code null} for a
	 *        creation in the context only.
	 * @param contexts
	 *        A single context object, a collection of context objects (the creation must be allowed
	 *        in every one), or {@code null}.
	 */
	public static ExecutableState onCreate(TLClass type, Set<TLClass> concreteTypes, TLStructuredTypePart reference,
			Object contexts) {
		if (!isCreateOffered(concreteTypes, reference)) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		Collection<?> elements;
		if (contexts == null) {
			elements = List.of();
		} else if (contexts instanceof Collection<?> collection) {
			elements = collection;
		} else {
			elements = List.of(contexts);
		}
		if (elements.isEmpty()) {
			return ExecutableState.NO_EXEC_NO_MODEL;
		}
		ExecutableState firstRefusal = null;
		for (Object element : elements) {
			ExecutableState state = createInElement(type, concreteTypes, reference, element);
			if (state.isHidden()) {
				return state;
			}
			if (!state.isExecutable() && firstRefusal == null) {
				firstRefusal = state;
			}
		}
		return firstRefusal != null ? firstRefusal : ExecutableState.EXECUTABLE;
	}

	private static ExecutableState createInElement(TLClass type, Set<TLClass> concreteTypes,
			TLStructuredTypePart reference, Object element) {
		if (!(element instanceof TLObject candidate)) {
			return ExecutableState.createDisabledState(createReason(type));
		}
		TLObject context = TLFormObjectBase.editedObject(candidate);
		if (reference != null && !TLModelUtil.isCompatibleInstance(reference.getOwner(), context)) {
			Logger.warn("Creation in '" + context + "' refused: it has no reference '"
				+ TLModelUtil.qualifiedName(reference) + "'.", ModelAccessPolicy.class);
			return ExecutableState.createDisabledState(createReason(type));
		}
		boolean allHidden = true;
		for (TLClass concreteType : concreteTypes) {
			ExecutableState state = createIn(context, reference, concreteType);
			if (state.isExecutable()) {
				return state;
			}
			allHidden &= state.isHidden();
		}
		return allHidden ? ExecutableState.NOT_EXEC_HIDDEN : ExecutableState.createDisabledState(createReason(type));
	}


	/**
	 * Whether the given operation is the creation of objects.
	 */
	public static boolean isCreate(BoundCommandGroup operation) {
		return SimpleBoundCommandGroup.CREATE.getID().equals(operation.getID());
	}

	private static boolean isDelete(BoundCommandGroup operation) {
		return SimpleBoundCommandGroup.DELETE.getID().equals(operation.getID());
	}

	private static ExecutableState refused(boolean hide, Supplier<ResKey> reason) {
		if (hide) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		return ExecutableState.createDisabledState(reason.get());
	}

	/**
	 * Whether the user may never perform the operation, independent of any role: a restricted user
	 * asking for a modification, or no user at all.
	 */
	private static boolean restricted(Person user, BoundCommandGroup operation) {
		return Boolean.FALSE.equals(BoundChecker.isAllowedBypass(user, operation));
	}

	/**
	 * Whether the given user holds one of the given granted roles on some object.
	 *
	 * <p>
	 * The answer of {@link AccessManager#hasRoleOnAnyObject(Person, Collection)}: it may
	 * overestimate the roles of the user, but is never <code>false</code> when the user holds one
	 * of the roles on an object. Without roles, it is <code>false</code>.
	 * </p>
	 */
	private static boolean holdsAnyOf(Person user, Set<BoundedRole> grantedRoles) {
		return AccessManager.getInstance().hasRoleOnAnyObject(user, grantedRoles);
	}

	/**
	 * Whether the given user holds no role granted the operation on objects of the given type, on
	 * any object.
	 *
	 * <p>
	 * The user holds none of the roles the type grants the operation to, and the operation is not
	 * {@link #isGrantable(ModelAccessRights, Person, BoundCommandGroup, TLClass, Set) grantable} to
	 * the user on it either: not on one of its specializations, and, for a type with an access
	 * parent, not on the types its access parent may lead to.
	 * </p>
	 */
	private static boolean heldByNoGrantedRole(ModelAccessRights rights, Person user, TLClass type,
			BoundCommandGroup operation) {
		return type != null && !holdsAnyOf(user, rights.getAllowedRoles(type, operation))
			&& !isGrantable(rights, user, operation, type, new HashSet<>());
	}

	/**
	 * Whether the given user holds no role, on any object, that the grant deciding the operation on
	 * the given attribute of objects of the given type lists.
	 *
	 * <p>
	 * An attribute with a grant of its own for the operation is decided by that grant, which
	 * replaces the grant of the type for the attribute. Otherwise the type decides.
	 * </p>
	 *
	 * @param attribute
	 *        The attribute operated on, {@code null} for an operation on the object itself.
	 */
	private static boolean heldByNoGrantedRole(ModelAccessRights rights, Person user, TLClass type,
			TLStructuredTypePart attribute, BoundCommandGroup operation) {
		if (attribute != null && rights.hasGrant(attribute, operation)) {
			return !holdsAnyOf(user, rights.getAllowedRoles(attribute, operation));
		}
		return heldByNoGrantedRole(rights, user, type, operation);
	}

	/**
	 * Whether the given attribute has a grant of its own for the operation listing no role the
	 * given user holds on any object.
	 */
	private static boolean attributeHeldByNoGrantedRole(ModelAccessRights rights, Person user,
			TLStructuredTypePart attribute, BoundCommandGroup operation) {
		return rights.hasGrant(attribute, operation)
			&& !holdsAnyOf(user, rights.getAllowedRoles(attribute, operation));
	}

	private static TLClass classOf(TLObject object) {
		TLStructuredType type = object.tType();
		return type instanceof TLClass clazz ? clazz : null;
	}

	private static TLClass targetClass(TLStructuredTypePart reference) {
		TLType target = reference.getType();
		return target instanceof TLClass clazz ? clazz : null;
	}

	private static ResKey objectReason(BoundCommandGroup operation, TLStructuredTypePart attribute) {
		String id = operation.getID();
		if (attribute == null) {
			if (SimpleBoundCommandGroup.READ.getID().equals(id)) {
				return I18NConstants.ERROR_READ_DENIED;
			}
			if (SimpleBoundCommandGroup.WRITE.getID().equals(id)) {
				return I18NConstants.ERROR_WRITE_DENIED;
			}
			if (SimpleBoundCommandGroup.DELETE.getID().equals(id)) {
				return I18NConstants.ERROR_DELETE_DENIED;
			}
			return I18NConstants.ERROR_OPERATION_DENIED__OPERATION.fill(operationLabel(operation));
		}
		ResKey attributeLabel = TLModelI18N.getI18NKey(attribute);
		if (SimpleBoundCommandGroup.READ.getID().equals(id)) {
			return I18NConstants.ERROR_ATTRIBUTE_READ_DENIED__ATTRIBUTE.fill(attributeLabel);
		}
		if (SimpleBoundCommandGroup.WRITE.getID().equals(id)) {
			return I18NConstants.ERROR_ATTRIBUTE_WRITE_DENIED__ATTRIBUTE.fill(attributeLabel);
		}
		return I18NConstants.ERROR_ATTRIBUTE_OPERATION_DENIED__OPERATION_ATTRIBUTE.fill(operationLabel(operation),
			attributeLabel);
	}

	private static ResKey typeReason(BoundCommandGroup operation, TLClass type) {
		if (isCreate(operation)) {
			return createReason(type);
		}
		return I18NConstants.ERROR_TYPE_OPERATION_DENIED__OPERATION_TYPE.fill(operationLabel(operation),
			TLModelNamingConvention.getTypeLabelKey(type));
	}

	private static ResKey createReason(TLClass type) {
		if (type == null) {
			return I18NConstants.ERROR_CREATE_DENIED;
		}
		return I18NConstants.ERROR_CREATE_TYPE_DENIED__TYPE.fill(TLModelNamingConvention.getTypeLabelKey(type));
	}

	private static ResKey operationLabel(BoundCommandGroup operation) {
		return com.top_logic.tool.boundsec.simple.I18NConstants.COMMAND_GROUP_NAMES.key(operation.getID());
	}
}

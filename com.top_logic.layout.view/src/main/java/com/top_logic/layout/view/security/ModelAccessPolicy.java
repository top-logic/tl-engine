/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import java.util.Collections;
import java.util.function.Supplier;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
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
 * displayed as the caller demands with a {@link DeniedDisplay}. Without such a demand, the display
 * is derived from what the refusal depends on:
 * </p>
 * <ul>
 * <li>The command is <em>hidden</em> when the refusal does not depend on a concrete object: the
 * check runs against the security root (a creation without a container, a check on a type), the type
 * grants the operation to no role at all, or the user is restricted (see
 * {@link BoundChecker#isAllowedBypass(Person, BoundCommandGroup)}). The operation is then never
 * possible for this user.</li>
 * <li>The command is <em>disabled</em> when the check on a concrete object (the object operated on,
 * or the container to create in) fails. It gives the refusal as its reason, naming the
 * operation.</li>
 * <li>When the {@link CommandApprovalService} refuses an operation the access rights allow on an
 * object, the command is disabled or hidden as the approval check decides. A disabled command gives
 * the reason of the approval check.</li>
 * </ul>
 *
 * @see ModelAccessRule
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
	 * it on the object, without a component and command ID. A refusal is displayed as the approval
	 * check decides, unless the caller demands a display.
	 * </p>
	 *
	 * @param operation
	 *        The operation performed.
	 * @param object
	 *        The object operated on.
	 * @param attribute
	 *        The attribute of the object operated on, {@code null} for an operation on the object
	 *        itself.
	 * @param denied
	 *        How a refused command is displayed, {@code null} for the display derived from the
	 *        check.
	 */
	public static ExecutableState onObject(BoundCommandGroup operation, TLObject object,
			TLStructuredTypePart attribute, DeniedDisplay denied) {
		ModelAccessRights rights = ModelAccessRights.getInstance();
		Person user = TLContext.currentUser();
		boolean allowed = attribute == null
			? rights.isAllowed(user, object, operation)
			: rights.isAllowed(user, object, attribute, operation);
		if (!allowed) {
			boolean hide = restricted(user, operation) || grantedToNoRole(rights, classOf(object), operation);
			return refused(denied, hide, () -> objectReason(operation, attribute));
		}
		ExecutableState approval =
			CommandApprovalService.getInstance().isExecutable(null, operation, null, object, Collections.emptyMap());
		if (approval.isExecutable()) {
			return ExecutableState.EXECUTABLE;
		}
		return refused(denied, approval.isHidden(), () -> approvalReason(approval, operation, attribute));
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
	 * @param type
	 *        The type of the objects operated on.
	 * @param denied
	 *        How a refused command is displayed, {@code null} for hiding it: the check depends on no
	 *        concrete object.
	 */
	public static ExecutableState onType(BoundCommandGroup operation, TLClass type, DeniedDisplay denied) {
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
		return refused(denied, true, () -> typeReason(operation, type));
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
	 * @param container
	 *        The object the created object belongs to.
	 * @param reference
	 *        The (composition) reference of the container the created object is added to,
	 *        {@code null} when the object is created in the context of the container only. Then, the
	 *        type must be given.
	 * @param type
	 *        The type of the created object, {@code null} for the type of the given reference.
	 * @param denied
	 *        How a refused command is displayed, {@code null} for the display derived from the
	 *        check.
	 */
	public static ExecutableState createIn(TLObject container, TLStructuredTypePart reference, TLClass type,
			DeniedDisplay denied) {
		ModelAccessRights rights = ModelAccessRights.getInstance();
		Person user = TLContext.currentUser();
		TLClass createdType = type != null ? type : targetClass(reference);

		boolean allowed;
		if (reference == null) {
			allowed = rights.isAllowedCreate(user, createdType, container);
		} else if (type == null) {
			allowed = rights.isAllowedCreate(user, container, reference);
		} else {
			allowed = rights.isAllowedCreate(user, type, container)
				&& rights.isAllowed(user, container, reference, SimpleBoundCommandGroup.WRITE);
		}
		if (allowed) {
			return ExecutableState.EXECUTABLE;
		}
		boolean hide = restricted(user, SimpleBoundCommandGroup.CREATE)
			|| grantedToNoRole(rights, createdType, SimpleBoundCommandGroup.CREATE)
			|| (reference != null && (restricted(user, SimpleBoundCommandGroup.WRITE)
				|| grantedToNoRole(rights, classOf(container), SimpleBoundCommandGroup.WRITE)));
		return refused(denied, hide, () -> createReason(createdType));
	}

	/**
	 * Whether the given operation is the creation of objects.
	 */
	public static boolean isCreate(BoundCommandGroup operation) {
		return SimpleBoundCommandGroup.CREATE.getID().equals(operation.getID());
	}

	private static ExecutableState refused(DeniedDisplay denied, boolean hideByDefault,
			Supplier<ResKey> reason) {
		DeniedDisplay display = denied != null ? denied : (hideByDefault ? DeniedDisplay.HIDE : DeniedDisplay.DISABLE);
		switch (display) {
			case HIDE:
				return ExecutableState.NOT_EXEC_HIDDEN;
			case DISABLE:
				return ExecutableState.createDisabledState(reason.get());
		}
		throw new IllegalArgumentException("No such display: " + display);
	}

	/**
	 * Whether the user may never perform the operation, independent of any role: a restricted user
	 * asking for a modification, or no user at all.
	 */
	private static boolean restricted(Person user, BoundCommandGroup operation) {
		return Boolean.FALSE.equals(BoundChecker.isAllowedBypass(user, operation));
	}

	/**
	 * Whether the access controlled type grants the operation to no role at all.
	 */
	private static boolean grantedToNoRole(ModelAccessRights rights, TLClass type, BoundCommandGroup operation) {
		return type != null && !rights.isWithoutSecurity(type) && rights.getAllowedRoles(type, operation).isEmpty();
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

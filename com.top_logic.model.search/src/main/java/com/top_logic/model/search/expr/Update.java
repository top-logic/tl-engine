/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Collection;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Updating the value of a model property as side-effect.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Update extends SearchExpressionWithSecurity {

	private SearchExpression _self;

	private TLStructuredTypePart _part;

	private SearchExpression _value;

	Update(SearchExpression self, TLStructuredTypePart part, SearchExpression value, boolean usesSecurity) {
		super(usesSecurity);
		_self = self;
		_part = part;
		_value = value;
	}

	/**
	 * The object on which the {@link #getPart()} is updated.
	 */
	public SearchExpression getSelf() {
		return _self;
	}

	/**
	 * @see #getSelf()
	 */
	public void setSelf(SearchExpression self) {
		_self = self;
	}

	/**
	 * The updated attribute.
	 */
	public TLStructuredTypePart getPart() {
		return _part;
	}

	/**
	 * @see #getPart()
	 */
	public void setPart(TLStructuredTypePart part) {
		_part = part;
	}

	/**
	 * The expression delivering the value to which the {@link #getPart()} of {@link #getSelf()} is
	 * updated.
	 */
	public SearchExpression getValue() {
		return _value;
	}

	/**
	 * @see #getValue()
	 */
	public void setValue(SearchExpression value) {
		_value = value;
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		TLObject self = asTLObjectNonNull(getSelf().evalWith(definitions, args));
		Object value = getValue().evalWith(definitions, args);
		TLStructuredTypePart part = getPart();

		if (usesSecurity()) {
			checkWritePermission(self, part);
			checkNewTargets(self, part, value);
		}

		self.tUpdate(part, value);
		return null;
	}

	/**
	 * Ensures that the current user may modify the given attribute of the given object.
	 *
	 * <p>
	 * This is the write check of every model operation that modifies an attribute value, in
	 * <i>TL-Script</i> as well as in an application writing edited values back to an object. The
	 * decision is made by the {@link ModelAccessRights}.
	 * </p>
	 *
	 * @param self
	 *        The object to modify.
	 * @param part
	 *        The attribute of the given object to modify.
	 * @throws TopLogicException
	 *         If the current user is not allowed to modify the attribute.
	 */
	public static void checkWritePermission(TLObject self, TLStructuredTypePart part) {
		ModelAccessRights accessRights = ModelAccessRights.getInstance();
		if (!accessRights.isAllowed(TLContext.currentUser(), self, part, SimpleBoundCommandGroup.WRITE)) {
			throw new TopLogicException(I18NConstants.WRITE_PERMISSION_DENIED__OBJECT_ATTRIBUTE.fill(self, part));
		}
	}

	/**
	 * Checks that the current user may discover every object the given new value adds to the
	 * reference.
	 *
	 * <p>
	 * Objects already contained in the stored value are not checked: they may stay even if the user
	 * cannot discover them.
	 * </p>
	 *
	 * @param self
	 *        The object to modify.
	 * @param part
	 *        The attribute of the given object to modify. Only references are checked.
	 * @param newValue
	 *        The value that is going to be stored.
	 * @throws TopLogicException
	 *         If the new value refers to an object that is not already referenced and that the
	 *         current user cannot discover.
	 */
	public static void checkNewTargets(TLObject self, TLStructuredTypePart part, Object newValue) {
		if (!(part instanceof TLReference)) {
			return;
		}
		Collection<?> stored = SearchExpression.asSet(self.tValue(part));
		ModelAccessRights accessRights = ModelAccessRights.getInstance();
		for (Object target : SearchExpression.asList(newValue)) {
			if (target instanceof TLObject object && !stored.contains(object)
				&& !accessRights.isDiscoverAllowed(object)) {
				throw new TopLogicException(
					I18NConstants.ERROR_TARGET_NOT_DISCOVERABLE__OBJECT_ATTRIBUTE.fill(self, part));
			}
		}
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitUpdate(this, arg);
	}

}

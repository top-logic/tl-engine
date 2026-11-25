/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * {@link TLObject} without persistency.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TransientObject extends AbstractTLObject {

	@Override
	public KnowledgeItem tHandle() {
		throw new UnsupportedOperationException();
	}

	/**
	 * A {@link TransientObject} exists on its own and is therefore always valid; a subclass that is
	 * created in a {@link #tContainer() container} is valid while that container is.
	 * 
	 * @see TLObject#tValid()
	 */
	@Override
	public boolean tValid() {
		return true;
	}

	/**
	 * The persistent object this object stands for, e.g. the edited object of a form buffer.
	 * 
	 * <p>
	 * The life-cycle information of this object ({@link #tCreator()}, {@link #tCreationDate()},
	 * {@link #tLastModifier()}, {@link #tLastModificationDate()}, ...) is the one of the persistent
	 * counterpart. An object without persistent counterpart has never been created nor modified in
	 * the persistency layer: it has neither author nor date.
	 * </p>
	 * 
	 * @return The persistent counterpart, or <code>null</code> if this object has none (yet).
	 */
	protected TLObject persistentCounterpart() {
		return null;
	}

	@Override
	public Person tCreator() {
		TLObject counterpart = persistentCounterpart();
		return counterpart == null ? null : counterpart.tCreator();
	}

	@Override
	public Date tCreationDate() {
		TLObject counterpart = persistentCounterpart();
		return counterpart == null ? null : counterpart.tCreationDate();
	}

	@Override
	public long tCreationTime() {
		TLObject counterpart = persistentCounterpart();
		return counterpart == null ? 0L : counterpart.tCreationTime();
	}

	@Override
	public Person tLastModifier() {
		TLObject counterpart = persistentCounterpart();
		return counterpart == null ? null : counterpart.tLastModifier();
	}

	@Override
	public Date tLastModificationDate() {
		TLObject counterpart = persistentCounterpart();
		return counterpart == null ? null : counterpart.tLastModificationDate();
	}

	@Override
	public long tLastModificationTime() {
		TLObject counterpart = persistentCounterpart();
		return counterpart == null ? 0L : counterpart.tLastModificationTime();
	}

	@Override
	public Revision tRevision() {
		return Revision.CURRENT;
	}

	@Override
	public TLStructuredType tType() {
		return null;
	}

	@Override
	public Object tValue(TLStructuredTypePart part) {
		return null;
	}

	@Override
	public Set<? extends TLObject> tReferers(TLReference ref) {
		return Collections.emptySet();
	}

	@Override
	public void tUpdate(TLStructuredTypePart part, Object value) {
		throw new UnsupportedOperationException("Transient object can not be updated.");
	}

	@Override
	public Object tGetData(String property) {
		return null;
	}

	@Override
	public Object tSetData(String property, Object value) {
		throw new UnsupportedOperationException("Transient object can not be updated.");
	}

	/**
	 * @deprecated Use {@link #tId()} instead
	 */
	@Override
	@Deprecated
	public ObjectKey getObjectKey() {
		return tId();
	}

	@Override
	public ObjectKey tId() {
		return null;
	}

	@Override
	public long tHistoryContext() {
		return Revision.CURRENT_REV;
	}

	@Override
	public KnowledgeBase tKnowledgeBase() {
		// For generic code that e.g. resolves the revision of this object, it is required to get
		// some value.
		return PersistencyLayer.getKnowledgeBase();
	}

	@Override
	public String toString() {
		String values;
		try {
			values = this.toStringValues();
		} catch (Throwable ex) {
			values = ", ERROR: '" + PersistentObject.toString(ex) + "'";
		}

		String className = this.getClass().getSimpleName();
		return className + "(" + "type:" + tType() + values + ")";
	}

	/**
	 * Hook for {@link #toString()} to retrieve relevant values in a format " (
	 * <code>, property: value</code>)*".
	 */
	protected String toStringValues() {
		return "";
	}

}

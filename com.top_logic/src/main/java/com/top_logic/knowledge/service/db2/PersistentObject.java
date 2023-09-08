/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.KnowledgeObjectRef;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.knowledge.service.event.ModificationListener;
import com.top_logic.model.AbstractTLObject;
import com.top_logic.model.TLObject;

/**
 * Base class of all persistent objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class PersistentObject extends AbstractTLObject implements Serializable {

	/**
	 * The abstract table type all objects copy from.
	 */
	public static final String OBJECT_TYPE = "TLObject";

	/** Reference attribute pointing to the object type. */
	public static final String TYPE_REF = "tType";

	private KnowledgeObject _tHandle;

	/**
	 * Creates a {@link PersistentObject}.
	 */
	public PersistentObject(KnowledgeObject ko) {
		_tHandle = ko;
	}

	@Override
	public KnowledgeObject tHandle() {
		return _tHandle;
	}

	/**
	 * Callback invoked before an object is deleted.
	 * 
	 * <p>
	 * Note: Implementations must not directly perform any actions, but only collect required
	 * information from this object being deleted and return a {@link Modification} callback that is
	 * called after the initial deletion has completed.
	 * </p>
	 * 
	 * @see ModificationListener#notifyUpcomingDeletion(com.top_logic.knowledge.service.KnowledgeBase,
	 *      KnowledgeItem)
	 * @see #handleDelete()
	 * @see Modification#andThen(Modification) Combining modifications from overridden methods with
	 *      own modifications.
	 */
	protected Modification notifyUpcomingDeletion() {
		return Modification.NONE;
	}

	/**
	 * Hook for subclasses to clean up external state after the deletion of this object.
	 * 
	 * <p>
	 * Note: At call time this object has already been deleted and the deletion has been committed.
	 * </p>
	 * 
	 * @see #notifyUpcomingDeletion()
	 * @see #handleLoad()
	 */
	protected void handleDelete() {
		// nothing special to do here
	}

	/**
	 * Hook for subclasses to initialize due to loading of the corresponding {@link KnowledgeItem}.
	 * 
	 * <p>
	 * Note: This method is called for both, current and historic objects.
	 * </p>
	 * 
	 * @see #handleDelete()
	 */
	protected void handleLoad() {
		// nothing special to do here
	}

	/**
	 * Some object description useful for debugging.
	 * 
	 * <p>
	 * Override {@link #toStringValues()} (or, {@link #toStringSafe()})
	 * </p>
	 */
	@Override
	public final String toString() {
		try {
			if (tValid()) {
				return toStringSafe();
			} else {
				StringBuilder out = new StringBuilder();
				out.append("DeletedObject(");
				KnowledgeItemImpl.toStringInvalid(out, tHandle());
				out.append(")");
				return out.toString();
			}
		} catch (Throwable ex) {
			return "Error in toString() of '" + tId() + "': " + toString(ex);
		}
	}

	private static String toString(Throwable ex) {
		final String className = ex.getClass().getName();
		if (ex instanceof I18NFailure) {
			// Note: If the error is an internationalized failure, arguments of that failure must
			// not be accessed, because this might trigger the same failure causing a stack
			// overflow in the end. The message produced here must be absolutely safe.
			ResKey errorKey = ((I18NFailure) ex).getErrorKey();
			return className + ": " + (errorKey.hasKey() ? errorKey.getKey() : errorKey.toString());
		} else {
			final String message = ex.getMessage();
			if (message == null) {
				return className;
			} else {
				return className + ": " + message;
			}
		}
	}

	/**
	 * Implementation of {@link #toString()} that is safe to throw exceptions.
	 * 
	 * @throws Throwable
	 *         if any problem occurs. This does not break {@link #toString()}.
	 */
	protected String toStringSafe() throws Throwable {
		String values;
		try {
			values = this.toStringValues();
		} catch (Throwable ex) {
			values = ", ERROR: '" + toString(ex) + "'";
		}

		String className = this.getClass().getSimpleName();
		return className + "(" + "type:" + tType() + ", id: " + tHandle().tId() + values + ")";
	}

	/**
	 * Hook for {@link #toString()} to retrieve relevant values in a format " (
	 * <code>, property: value</code>)*".
	 */
	protected String toStringValues() {
		return "";
	}

	/**
	 * For Java-internal implementation of {@link Serializable}.
	 */
	@CalledByReflection
	protected Object writeReplace() throws ObjectStreamException {
		if (!tValid()) {
			throw new InvalidObjectException("Trying to serialize an invalid object.");
		}

		return new Ref(tHandle());
	}

	/**
	 * Reference to a {@link TLObject} in the Java-internal serialization format.
	 */
	private static class Ref extends KnowledgeObjectRef {

		/**
		 * Creates a {@link Ref}.
		 *
		 * @param handle
		 *        The {@link KnowledgeItem} that should be referenced from the serialization.
		 */
		public Ref(KnowledgeItem handle) {
			super(handle);
		}

		/** When reading the reference, resolve the object referenced by ID. */
		protected Object readResolve() throws ObjectStreamException {
			try {
				return getKnowledgeObject().getWrapper();
			} catch (InvalidLinkException ilx) {
				throw new InvalidObjectException("Failed to resolve '" + this + "'.");
			}
		}

	}

}

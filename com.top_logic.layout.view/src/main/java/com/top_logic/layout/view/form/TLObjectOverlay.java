/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.layout.form.FormMember;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TransientObject;
import com.top_logic.model.annotate.util.ConstraintCheck;
import com.top_logic.model.fallback.StorageWithFallback;
import com.top_logic.model.form.TracingScope;
import com.top_logic.model.search.expr.Update;
import com.top_logic.util.error.TopLogicException;

/**
 * Lean overlay over a persistent {@link TLObject} that intercepts writes.
 *
 * <p>
 * Unchanged attributes delegate to the base object. Changed attributes are stored in a local map.
 * This serves as the transient editing buffer for the form system.
 * </p>
 *
 * <p>
 * The overlay stands for the object it wraps: it reports that object as its
 * {@link #getEditedObject() edited object} and shares its {@link #tId() identity}. An algorithm
 * receiving the editing buffer instead of the object - most notably a {@link ConstraintCheck}
 * comparing the checked object with objects found in the database - therefore recognizes the edited
 * object instead of treating it as a stranger.
 * </p>
 *
 * <p>
 * A change of an attribute value made through {@link #tUpdate(TLStructuredTypePart, Object)} or
 * {@link #revert(TLStructuredTypePart)} is announced to the registered {@link ValueChangeListener}s,
 * whoever made it: a field the user edits, or code writing the overlay directly, such as an
 * {@link com.top_logic.model.annotate.AttributeValueListener}. Ending the edit - transferring the
 * changes to the base object ({@link #apply()}, {@link #applyToBuffer()}) or discarding all of them
 * ({@link #reset()}) - changes no value seen through the overlay and announces nothing.
 * </p>
 */
public class TLObjectOverlay extends TransientObject implements TLFormObjectBase {

	private final TLObject _base;

	private final Map<TLStructuredTypePart, Object> _changes = new LinkedHashMap<>();

	private final List<ValueChangeListener> _valueChangeListeners = new ArrayList<>();

	/**
	 * @see #isReceivingTransfer()
	 */
	private boolean _receivingTransfer;

	/**
	 * Listener informed about a change of an attribute value of a {@link TLObjectOverlay}.
	 *
	 * @see TLObjectOverlay#addValueChangeListener(ValueChangeListener)
	 */
	public interface ValueChangeListener {

		/**
		 * Called after the value of the given attribute seen through the overlay has changed.
		 *
		 * @param overlay
		 *        The overlay whose value changed.
		 * @param part
		 *        The changed attribute.
		 * @param oldValue
		 *        The value before the change.
		 * @param newValue
		 *        The value after the change.
		 */
		void onValueChanged(TLObjectOverlay overlay, TLStructuredTypePart part, Object oldValue, Object newValue);
	}

	/**
	 * Creates a new overlay over the given base object.
	 *
	 * @param base
	 *        The persistent object to overlay. Must not be {@code null}.
	 */
	public TLObjectOverlay(TLObject base) {
		_base = base;
	}

	@Override
	public TLStructuredType tType() {
		return _base.tType();
	}

	@Override
	public boolean isCreate() {
		return false;
	}

	@Override
	public TLObject getEditedObject() {
		return getBase();
	}

	@Override
	public String getDomain() {
		return null;
	}

	/**
	 * The identity of the {@link #getEditedObject() edited object}.
	 *
	 * @implNote Sharing the identity makes the overlay {@link #equals(Object) equal} to the object
	 *           it stands for, so an algorithm comparing the edited object with objects read from
	 *           the database (e.g. a uniqueness check) does not report a conflict of the object
	 *           with itself.
	 */
	@Override
	public ObjectKey tId() {
		return _base.tId();
	}

	/**
	 * Whether the {@link #getEditedObject() edited object} is valid.
	 *
	 * <p>
	 * An overlay is valid exactly as long as the object it stands for: an overlay of a deleted
	 * object is not valid.
	 * </p>
	 */
	@Override
	public boolean tValid() {
		return _base.tValid();
	}

	@Override
	public KnowledgeItem tHandle() {
		return _base.tHandle();
	}

	@Override
	public TLObject tContainer() {
		return _base.tContainer();
	}

	@Override
	public TLReference tContainerReference() {
		return _base.tContainerReference();
	}

	/**
	 * The objects referring to the {@link #getEditedObject() edited object} through the given
	 * reference.
	 *
	 * <p>
	 * The overlay stands for the object it wraps, and who refers to that object is answered by the
	 * database: an edit pending in the form changes what the object holds, not what holds it. An
	 * options or constraint expression navigating backwards from the edited object therefore sees
	 * the same referrers here as it would see on the object itself.
	 * </p>
	 */
	@Override
	public Set<? extends TLObject> tReferers(TLReference ref) {
		return _base.tReferers(ref);
	}

	/**
	 * The value of the given attribute as edited in this overlay.
	 *
	 * <p>
	 * A changed attribute delivers the buffered value, an unchanged one the value of the base
	 * object. A {@link TLStructuredTypePart#isDerived() derived} attribute is computed by its
	 * storage on the base object in a {@link TracingScope} that resolves the base object to this
	 * overlay (see {@link TracingScope#computeOnOverlay(TLObject, TLObject, TLStructuredTypePart)}),
	 * so that the storage receives the base object and still reflects the edits buffered here.
	 * </p>
	 *
	 * <p>
	 * In a {@link TracingScope}, the read is reported to the scope's trace.
	 * </p>
	 */
	@Override
	public Object tValue(TLStructuredTypePart part) {
		TracingScope scope = TracingScope.current();
		if (scope != null) {
			scope.record(this, part);
		}
		return getFieldValue(part);
	}

	/**
	 * Records the given value as the change of the given attribute.
	 *
	 * <p>
	 * The value of an attribute with a {@link StorageWithFallback} is its explicit value, which is
	 * recorded where the storage keeps it (e.g. a storage attribute of its own), so that it is
	 * found again by {@link StorageWithFallback#getExplicitValue(TLObject, TLStructuredTypePart)}.
	 * </p>
	 *
	 * <p>
	 * When the value seen through the overlay changes, the {@link ValueChangeListener}s are
	 * informed after the value is recorded.
	 * </p>
	 */
	@Override
	public void tUpdate(TLStructuredTypePart part, Object value) {
		Object oldValue = valueBeforeChange(part);
		if (part.getStorageImplementation() instanceof StorageWithFallback fallback) {
			fallback.setExplicitValue(this, part, value);
			fireValueChanged(part, oldValue);
			return;
		}
		_changes.put(part, value);
		fireValueChanged(part, oldValue);
	}

	/**
	 * Registers a listener for the value changes of this overlay.
	 *
	 * @see #removeValueChangeListener(ValueChangeListener)
	 */
	public void addValueChangeListener(ValueChangeListener listener) {
		_valueChangeListeners.add(listener);
	}

	/**
	 * Deregisters a listener added with {@link #addValueChangeListener(ValueChangeListener)}.
	 */
	public void removeValueChangeListener(ValueChangeListener listener) {
		_valueChangeListeners.remove(listener);
	}

	/**
	 * The value of the given attribute before a change, read only if a listener is to be informed
	 * about the change.
	 */
	private Object valueBeforeChange(TLStructuredTypePart part) {
		return _valueChangeListeners.isEmpty() ? null : getFieldValue(part);
	}

	/**
	 * Informs the {@link ValueChangeListener}s about a change of the given attribute, if its value
	 * seen through this overlay differs from the given value it had before.
	 *
	 * <p>
	 * Values are compared like for {@link #isModified(TLStructuredTypePart)}: replacing referenced
	 * objects by their overlays is no change.
	 * </p>
	 */
	private void fireValueChanged(TLStructuredTypePart part, Object oldValue) {
		if (_valueChangeListeners.isEmpty()) {
			return;
		}
		Object newValue = getFieldValue(part);
		if (!isChange(newValue, oldValue)) {
			return;
		}
		for (ValueChangeListener listener : new ArrayList<>(_valueChangeListeners)) {
			listener.onValueChanged(this, part, oldValue, newValue);
		}
	}

	/**
	 * The value this overlay holds for the given attribute: the buffered value of a changed
	 * attribute, the value of the base object otherwise.
	 *
	 * <p>
	 * A {@link TLStructuredTypePart#isDerived() derived} attribute that is not changed is computed
	 * over the edits of this overlay, see {@link #tValue(TLStructuredTypePart)}. In contrast to
	 * {@link #tValue(TLStructuredTypePart)}, the read of this overlay itself is not reported to a
	 * {@link TracingScope}.
	 * </p>
	 *
	 * <p>
	 * An attribute with a {@link StorageWithFallback} is read by its storage from this overlay: its
	 * explicit value as buffered here, or its fallback value computed over the edits of this overlay
	 * if no explicit value is set. The attributes the storage reads are reported to a
	 * {@link TracingScope}.
	 * </p>
	 */
	@Override
	public Object getFieldValue(TLStructuredTypePart attribute) {
		if (attribute.getStorageImplementation() instanceof StorageWithFallback fallback) {
			// The explicit value is buffered where the storage keeps it, see tUpdate(), and the
			// fallback follows the edits of this overlay.
			return fallback.getAttributeValue(this, attribute);
		}
		if (_changes.containsKey(attribute)) {
			return _changes.get(attribute);
		}
		if (attribute.isDerived()) {
			return TracingScope.computeOnOverlay(_base, this, attribute);
		}
		return baseValue(attribute);
	}

	/**
	 * Always <code>null</code>, since a React form is not built from {@link FormMember}s but from
	 * {@link AttributeFieldModel}s.
	 */
	@Override
	public FormMember getField(TLStructuredTypePart attribute) {
		return null;
	}

	@Override
	public Object getBaseValue(TLStructuredTypePart attribute) {
		return baseValue(attribute);
	}

	@Override
	public Object defaultValue(TLStructuredTypePart part) {
		return baseValue(part);
	}

	/**
	 * The value of the given attribute of the base object.
	 *
	 * <p>
	 * Read through {@link TracingScope#readBase(TLObject, TLStructuredTypePart)}, so that the read
	 * is neither reported to a {@link TracingScope} nor redirected back to this overlay.
	 * </p>
	 */
	private Object baseValue(TLStructuredTypePart part) {
		return TracingScope.readBase(_base, part);
	}

	/**
	 * Whether any attribute holds a value that differs from the base object's value.
	 *
	 * <p>
	 * A stored value equal to the base value does not count as a change: writing back an unchanged
	 * value (e.g. a composition table registering its unmodified row list on edit-mode entry, or an
	 * edit that is typed and reverted) leaves the overlay clean.
	 * </p>
	 *
	 * <p>
	 * An overlay stands for the object it wraps, so a reference value that only replaces objects by
	 * their overlays is no change either. Edits <em>within</em> a referenced object are tracked by
	 * that object's own overlay, not by the reference holding it.
	 * </p>
	 */
	public boolean isDirty() {
		for (Map.Entry<TLStructuredTypePart, Object> entry : _changes.entrySet()) {
			if (isChange(entry.getValue(), baseValue(entry.getKey()))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether the stored value differs from the base value, comparing overlays by the objects they
	 * stand for.
	 */
	private static boolean isChange(Object stored, Object baseValue) {
		if (stored instanceof Collection<?> storedValues && baseValue instanceof Collection<?> baseValues) {
			return !resolveAll(storedValues).equals(resolveAll(baseValues));
		}
		return !Objects.equals(resolve(stored), resolve(baseValue));
	}

	private static List<Object> resolveAll(Collection<?> values) {
		List<Object> result = new ArrayList<>(values.size());
		for (Object value : values) {
			result.add(resolve(value));
		}
		return result;
	}

	/**
	 * The object an overlay stands for; any other value unchanged.
	 */
	private static Object resolve(Object value) {
		return value instanceof TLObjectOverlay overlay ? overlay.getBase() : value;
	}

	/**
	 * Whether the given attribute has been changed in this overlay.
	 */
	public boolean isChanged(TLStructuredTypePart part) {
		return _changes.containsKey(part);
	}

	/**
	 * The attributes this overlay holds a value for, in the order they were first written.
	 *
	 * @see #isChanged(TLStructuredTypePart)
	 */
	public List<TLStructuredTypePart> getChangedParts() {
		return new ArrayList<>(_changes.keySet());
	}

	/**
	 * Whether the value this overlay holds for the given attribute differs from the value of the
	 * base object, comparing overlays by the objects they stand for.
	 *
	 * <p>
	 * A reference value that only replaces objects by their overlays is no modification: what
	 * changed is within the referenced objects, which their own overlays hold.
	 * </p>
	 *
	 * @see #isDirty()
	 */
	public boolean isModified(TLStructuredTypePart part) {
		return _changes.containsKey(part) && isChange(_changes.get(part), baseValue(part));
	}

	/**
	 * Transfers all accumulated changes to a base object that is itself an editing buffer - another
	 * overlay or a transient object - and discards them here.
	 *
	 * <p>
	 * No rights are checked: nothing is stored, the base object only buffers the changes as well.
	 * The rights are checked when the changes are stored.
	 * </p>
	 *
	 * @throws IllegalStateException
	 *         If the base object is a persistent object, see {@link #apply()}.
	 */
	public void applyToBuffer() {
		if (!(_base instanceof TLObjectOverlay) && !_base.tTransient()) {
			throw new IllegalStateException("The base of an overlay is no editing buffer: " + _base);
		}
		transferChanges();
		_changes.clear();
	}

	/**
	 * Writes all accumulated changes to the base object.
	 *
	 * <p>
	 * A base that is an overlay itself {@link #isReceivingTransfer() receives a transfer} while
	 * the values are written.
	 * </p>
	 */
	private void transferChanges() {
		TLObjectOverlay receiver = _base instanceof TLObjectOverlay overlay ? overlay : null;
		boolean before = receiver != null && receiver._receivingTransfer;
		if (receiver != null) {
			receiver._receivingTransfer = true;
		}
		try {
			for (Map.Entry<TLStructuredTypePart, Object> entry : _changes.entrySet()) {
				_base.tUpdate(entry.getKey(), entry.getValue());
			}
		} finally {
			if (receiver != null) {
				receiver._receivingTransfer = before;
			}
		}
	}

	/**
	 * Whether the values currently written to this overlay are transferred from an overlay over it
	 * that ends its edit ({@link #apply()}, {@link #applyToBuffer()}), e.g. a dialog storing its
	 * changes into the buffer of a row.
	 *
	 * <p>
	 * The values were entered and reacted to in the overlay they come from: a
	 * {@link ValueChangeListener} learns about the transferred values to show them, but takes them
	 * as no new edit.
	 * </p>
	 */
	public boolean isReceivingTransfer() {
		return _receivingTransfer;
	}

	/**
	 * Ensures that the current user may write all changes {@link #apply()} would transfer.
	 *
	 * <p>
	 * Every attribute whose value {@link #isDirty() differs} from the base value must be writable
	 * for the current user, see {@link Update#checkWritePermission(TLObject, TLStructuredTypePart)}.
	 * A buffered value equal to the base value is no change and needs no right.
	 * </p>
	 *
	 * <p>
	 * A changed reference must not add an object the current user may not discover, see
	 * {@link Update#checkNewTargets(TLObject, TLStructuredTypePart, Object)}. Objects the reference
	 * already holds may stay, also when the user cannot discover them (e.g. the masked elements of
	 * a reference field).
	 * </p>
	 *
	 * <p>
	 * A save writing several overlays checks all of them before the first one is applied, so a
	 * refused save leaves every overlay with its changes.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         If the current user is not allowed to modify one of the changed attributes, or a changed
	 *         reference adds an object the user may not discover.
	 */
	public void checkApply() {
		for (Map.Entry<TLStructuredTypePart, Object> entry : _changes.entrySet()) {
			TLStructuredTypePart part = entry.getKey();
			Object value = entry.getValue();
			if (isChange(value, baseValue(part))) {
				Update.checkWritePermission(_base, part);
				// Rows of a composition are buffered in overlays, which stand for their objects.
				Update.checkNewTargets(_base, part,
					value instanceof Collection<?> values ? resolveAll(values) : resolve(value));
			}
		}
	}

	/**
	 * Transfers all accumulated changes to the base object.
	 *
	 * <p>
	 * The changes are {@link #checkApply() checked} before the first value is written, so a refused
	 * write leaves the base object and this overlay untouched.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         If the current user is not allowed to modify one of the changed attributes.
	 */
	public void apply() {
		checkApply();
		transferChanges();
		// The base now holds all values, so the overlay has no unsaved changes anymore. Reads
		// delegate to the base, and dirty tracking reports a clean state.
		_changes.clear();
	}

	/**
	 * Discards the change of the given attribute. Afterwards, reads of the attribute delegate to the
	 * base object again.
	 *
	 * <p>
	 * When the value seen through the overlay changes thereby, the {@link ValueChangeListener}s are
	 * informed.
	 * </p>
	 *
	 * @param part
	 *        The attribute whose change is discarded.
	 */
	public void revert(TLStructuredTypePart part) {
		if (!_changes.containsKey(part)) {
			return;
		}
		Object oldValue = valueBeforeChange(part);
		_changes.remove(part);
		fireValueChanged(part, oldValue);
	}

	/**
	 * Discards all accumulated changes. After reset, all reads delegate to the base object again.
	 */
	public void reset() {
		_changes.clear();
	}

	/**
	 * The base object this overlay wraps.
	 *
	 * @see #getEditedObject()
	 */
	public TLObject getBase() {
		return _base;
	}
}

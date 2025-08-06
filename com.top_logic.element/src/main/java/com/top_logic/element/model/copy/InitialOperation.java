/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.copy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.export.PreloadContext;

class InitialOperation extends CopyOperationImpl {

	private final Map<TLObject, TLObject> _copies = new HashMap<>();

	/**
	 * Mapping from either {@link TLStructuredType} or Pair<TLStructuredType, TLStructuredType>
	 * to attributes to copy from source to target.
	 */
	private final Map<Object, CopyAttributes> _copyAttributes = new HashMap<>();

	private Map<TLReference, DescendBatch> _unresolvedCopies = new HashMap<>();

	private Map<TLStructuredType, ValueBatch> _valueBatches = new HashMap<>();

	private class CopyAttributes {

		private List<TLReference> _compositions = new ArrayList<>();

		private List<TLStructuredTypePart> _properties = new ArrayList<>();

		public List<TLReference> compositions() {
			return _compositions;
		}

		public List<TLStructuredTypePart> getProperties() {
			return _properties;
		}

		public void add(TLStructuredTypePart part) {
			if (part.getModelKind() == ModelKind.REFERENCE) {
				TLReference reference = (TLReference) part;
				if (reference.getEnd().isComposite()) {
					_compositions.add((TLReference) part);
					return;
				}
			}

			_properties.add(part);
		}
	}

	private class Batch {
		protected final List<TLObject> _origBatch = new ArrayList<>();

		protected final List<TLObject> _copyBatch = new ArrayList<>();

		public void add(TLObject orig, TLObject copy) {
			_origBatch.add(orig);
			_copyBatch.add(copy);
		}
	}

	private class DescendBatch extends Batch {
		private final TLReference _reference;

		/**
		 * Creates a {@link DescendBatch}.
		 */
		public DescendBatch(TLReference reference) {
			_reference = reference;
		}

		public void excuteBatch() {
			try (PreloadContext context = new PreloadContext()) {
				TLReference reference = _reference;
				MetaElementUtil.preloadAttribute(context, _origBatch, reference);

				for (int n = 0, cnt = _origBatch.size(); n < cnt; n++) {
					TLObject orig = _origBatch.get(n);
					TLObject copy = _copyBatch.get(n);

					copyComposite(orig, reference, copy);
				}
			}
		}
	}

	private class ValueBatch extends Batch {
		private CopyAttributes _attributes;

		/**
		 * Creates a {@link DescendBatch}.
		 */
		public ValueBatch(CopyAttributes attributes) {
			_attributes = attributes;
		}

		public void excuteBatch() {
			List<TLStructuredTypePart> properties = _attributes.getProperties();
			if (properties.isEmpty()) {
				return;
			}

			try (PreloadContext context = new PreloadContext()) {
				MetaElementUtil.preloadAttributes(context, _origBatch,
					properties.toArray(new TLStructuredTypePart[0]));

				for (int n = 0, cnt = _origBatch.size(); n < cnt; n++) {
					TLObject orig = _origBatch.get(n);
					TLObject copy = _copyBatch.get(n);

					copyValues(properties, orig, copy);
				}
			}
		}
	}

	@Override
	public TLObject resolveCopy(TLObject orig) {
		return _copies.get(orig);
	}

	@Override
	public void enterCopy(TLObject orig, TLObject copy) {
		_copies.put(orig, copy);

		TLStructuredType type = orig.tType();
		TLStructuredType copyType = copy.tType();

		CopyAttributes attributes = copyAttributes(type, copyType);
		for (TLReference reference : attributes.compositions()) {
			_unresolvedCopies.computeIfAbsent(reference, DescendBatch::new).add(orig, copy);
		}

		_valueBatches.computeIfAbsent(type, x -> new ValueBatch(attributes)).add(orig, copy);
	}

	private CopyAttributes copyAttributes(TLStructuredType srcType, TLStructuredType targetType) {
		Object key;
		boolean trivial = srcType == targetType;
		if (trivial) {
			// Prevent creating lots of pairs for just simple lookups.
			key = srcType;
		} else {
			key = Pair.create(srcType, targetType);
		}

		CopyAttributes cache = _copyAttributes.get(key);
		if (cache != null) {
			return cache;
		}

		CopyAttributes result = new CopyAttributes();
		for (TLStructuredTypePart srcPart : srcType.getAllParts()) {
			TLStructuredTypePart targetPart;
			if (trivial) {
				targetPart = srcPart;
			} else {
				targetPart = targetType.getPart(srcPart.getName());
				if (targetPart == null) {
					// Not defined in target.
					continue;
				}
			}

			if (targetPart.isDerived()) {
				continue;
			}

			result.add(srcPart);
		}
		_copyAttributes.put(key, result);
		return result;
	}

	@Override
	protected final Set<Entry<TLObject, TLObject>> localCopies() {
		return _copies.entrySet();
	}

	@Override
	public void finish() {
		// Copy along composition references in batch mode.
		while (true) {
			Map<TLReference, DescendBatch> batch = _unresolvedCopies;
			if (batch.isEmpty()) {
				break;
			}

			_unresolvedCopies = new HashMap<>();

			for (Entry<TLReference, DescendBatch> entry : batch.entrySet()) {
				entry.getValue().excuteBatch();
			}
		}

		for (ValueBatch valueBatch : _valueBatches.values()) {
			valueBatch.excuteBatch();
		}
	}

	private void copyValues(List<TLStructuredTypePart> parts, TLObject orig, TLObject copy) {
		for (TLStructuredTypePart part : parts) {
			// Note: The target object may be of another type than the source object and not
			// define all properties of the source.
			if (!defines(copy, part)) {
				continue;
			}

			Object value = orig.tValue(part);
			if (!_filter.accept(part, value, orig)) {
				continue;
			}

			Object copyValue;
			if (part.getModelKind() == ModelKind.REFERENCE) {
				TLReference reference = (TLReference) part;
				copyValue = rewriteReferences(reference.getHistoryType(), value);
			} else {
				copyValue = value;
			}
			copy.tUpdate(part, copyValue);
		}
	}

	private Object rewriteReferences(HistoryType historyType, Object value) {
		if (value instanceof Collection<?>) {
			return rewriteCollection(historyType, (Collection<?>) value);
		} else {
			return rewriteReference(historyType, (TLObject) value);
		}
	}

	private Object rewriteCollection(HistoryType historyType, Collection<?> value) {
		Collection<Object> result = allocateCopy(value);
		for (Object orig : value) {
			result.add(rewriteReference(historyType, (TLObject) orig));
		}
		return result;
	}

	private Object rewriteReference(HistoryType historyType, TLObject orig) {
		if (orig == null) {
			return null;
		}

		TLObject copy = resolveCopy(orig);
		if (copy == null) {
			switch (historyType) {
				case HISTORIC:
				case MIXED:
					return orig;

				case CURRENT:
					// When copying a stable version, current references must only contain
					// current values. If the target object is not copied (because it is not
					// part of the copy operation, it's current version must be used).
					return WrapperHistoryUtils.getCurrent(orig);
			}
			throw new UnreachableAssertion("No such history type: " + historyType);
		} else {
			return copy;
		}
	}
}
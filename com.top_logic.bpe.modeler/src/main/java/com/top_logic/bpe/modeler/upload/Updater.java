/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.upload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.bpe.bpml.model.Externalized;
import com.top_logic.bpe.bpml.model.annotation.BPMLExtension;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * Algorithm for structurally updating a diagram model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Updater {
	private final TLObject _orig;

	private final TLObject _update;

	private BidiMap<String, TLObject> _origById;

	/**
	 * Mapping of new diagram element to corresponding existing diagram element.
	 */
	private BidiMap<TLObject, TLObject> _replacements;

	private List<TLObject> _outdated;

	private boolean _updateExtensions;

	/**
	 * Creates a {@link com.top_logic.bpe.modeler.upload.Updater}.
	 *
	 * @param orig
	 *        The original object being updated (containing BPML extensions to be preserved, if
	 *        desired).
	 * @param update
	 *        The new object containing updated values (from the BPML core model, and potential BPML
	 *        extensions if desired).
	 * @param updateExtensions
	 *        Whether the BPML extensions in the original collaboration must be updated with the
	 *        extensions from the new one.
	 */
	public Updater(TLObject orig, TLObject update, boolean updateExtensions) {
		_orig = orig;
		_update = update;
		_updateExtensions = updateExtensions;
	}

	/**
	 * Entry point for starting the update.
	 *
	 * @return The updated object.
	 */
	public TLObject update() {
		_origById = new BidiHashMap<>();
		index(_orig);

		_replacements = new BidiHashMap<>();
		_outdated = new ArrayList<>();
		findCandidates(_update, _orig);

		TLObject result = doUpdate(_update);

		for (Entry<TLObject, TLObject> entry : _replacements.entrySet()) {
			TLObject updateObj = entry.getKey();
			TLObject origObj = entry.getValue();
			if (updateObj != origObj) {
				// The updated object was replaced by an original one, delete the update.
				if (updateObj.tValid()) {
					destroy(updateObj);
				}
			}
		}

		for (TLObject origObj : _origById.values()) {
			if (!_replacements.containsValue(origObj)) {
				// The original was not reused (to replace an updated object), delete the original.
				if (origObj.tValid()) {
					// May already be implicitly deleted, due to deletion policies of references.
					// Unfortunately, double delete causes an error.
					destroy(origObj);
				}
			}
		}
		return result;
	}

	private void destroy(TLObject composite) {
		for (TLStructuredTypePart attribute : composite.tType().getAllParts()) {
			if (!isComposite(attribute)) {
				continue;
			}

			// Clear composite references to prevent parts from being destroyed when destroying the
			// composite. Parts of a new composite may be inserted into the diagram, even if the
			// composite from the new diagram is destroyed because there is already a correspondence
			// in the existing diagram.
			if (isBackRef(attribute)) {
				// A rare case - an inverse back reference: It cannot be cleared directly, but the
				// other end (the "owner" attribute") must be cleared.
				TLReference owner = TLModelUtil.getOtherEnd(((TLReference) attribute).getEnd()).getReference();

				Object v = composite.tValue(attribute);
				Collection<?> others = v == null ? Collections.emptyList()
					: v instanceof Collection ? (Collection<?>) v : Collections.singleton(v);
				for (Object other : others) {
					// Note: An owner attribute cannot be multiple.
					((TLObject) other).tUpdate(owner, null);
				}
			} else {
				composite.tUpdate(attribute, null);
			}
		}
		composite.tDelete();
	}

	/**
	 * Fills the {@link #_replacements} map by traversing the updated graph and taking corresponding
	 * objects from the original graph, if its type-compatible with the updated one.
	 */
	private void findCandidates(TLObject update, TLObject contextCandidate) {
		TLObject candidate;
		if (update instanceof Externalized) {
			String extId = ((Externalized) update).getExtId();
			TLObject orig = _origById.get(extId);
			if (orig == null) {
				// New object.
				candidate = update;
			} else {
				candidate = orig;
			}
		} else {
			candidate = contextCandidate;
		}

		TLObject replacement;
		if (candidate != null && candidate.tType() == update.tType()) {
			replacement = candidate;
		} else {
			if (candidate != null) {
				if (!_updateExtensions) {
					copyExtensions(update, candidate);
				}
				_outdated.add(candidate);
			}
			replacement = update;
		}
		_replacements.put(update, replacement);

		TLStructuredType updateType = update.tType();
		for (TLStructuredTypePart attribute : updateType.getAllParts()) {
			if (!isComposite(attribute)) {
				continue;
			}

			Object updateValue = update.tValue(attribute);
			if (updateValue == null) {
				continue;
			}

			Object candidateValue;
			if (candidate != null && hasAttribute(candidate, attribute)) {
				candidateValue = candidate.tValue(attribute);
			} else {
				candidateValue = updateValue;
			}

			if (updateValue instanceof Collection<?>) {
				if (updateValue instanceof List<?> && candidateValue instanceof List<?>) {
					List<?> candidateList = (List<?>) candidateValue;
					int index = 0;
					for (Object entry : (List<?>) updateValue) {
						TLObject entryObject = (TLObject) entry;

						TLObject candidateEntry =
							index < candidateList.size() ? (TLObject) candidateList.get(index++) : entryObject;
						findCandidates(entryObject, candidateEntry);
					}
				} else {
					for (Object entry : (Collection<?>) updateValue) {
						TLObject entryObject = (TLObject) entry;
						findCandidates(entryObject, entryObject);
					}
				}
			} else {
				findCandidates((TLObject) updateValue, (TLObject) candidateValue);
			}
		}
	}

	private boolean hasAttribute(TLObject candidate, TLStructuredTypePart attribute) {
		return candidate.tType().getPart(attribute.getName()) == attribute;
	}

	private TLObject doUpdate(TLObject update) {
		TLObject result = _replacements.get(update);
		copyValues(result, update);
		return result;
	}

	private void copyValues(TLObject result, TLObject update) {
		// Note: Even if the resulting element and the update element are the same (the element is
		// new in the updated diagram) it is nevertheless necessary to copy (and rewrite) the
		// contents because references in the new element must potentially be changed to their
		// existing counterparts.
		for (TLStructuredTypePart attribute : result.tType().getAllParts()) {
			if (!_updateExtensions && isExtension(attribute)) {
				continue;
			}

			Object updateValue = update.tValue(attribute);

			if (isComposite(attribute)) {
				descend(updateValue);
			}

			if (!isDerived(attribute) && !isBackRef(attribute)) {
				result.tUpdate(attribute, rewrite(updateValue));
			}
		}
	}

	private void descend(Object value) {
		if (value instanceof TLObject) {
			TLObject update = (TLObject) value;
			TLObject result = _replacements.get(update);
			assert result != null : "No replacement for '" + update + "' found.";

			copyValues(result, update);
		} else if (value instanceof Collection<?>) {
			Collection<?> valueCollection = (Collection<?>) value;
			for (Object entry : valueCollection) {
				descend(entry);
			}
		}
	}

	private Object rewrite(Object value) {
		if (value instanceof TLObject) {
			TLObject update = (TLObject) value;

			TLObject result = _replacements.get(update);
			if (result == null) {
				// "No replacement for found.";
				return value;
			}
			return result;
		} else if (value instanceof Collection<?>) {
			Collection<?> valueCollection = (Collection<?>) value;
			ArrayList<Object> result = new ArrayList<>(valueCollection.size());
			for (Object entry : valueCollection) {
				result.add(rewrite(entry));
			}
			return result;
		} else {
			return value;
		}
	}

	private void copyExtensions(TLObject result, TLObject orig) {
		TLStructuredType origType = orig.tType();
		for (TLStructuredTypePart attribute : result.tType().getAllParts()) {
			TLStructuredTypePart origPart = origType.getPart(attribute.getName());
			if (origPart != attribute) {
				continue;
			}
			if (isDerived(attribute)) {
				continue;
			}
			if (isBackRef(attribute)) {
				continue;
			}
			if (!isExtension(attribute)) {
				continue;
			}

			Object updateValue = orig.tValue(attribute);
			result.tUpdate(attribute, updateValue);
		}
	}

	private boolean isDerived(TLStructuredTypePart attribute) {
		return attribute.isDerived();
	}

	private boolean isBackRef(TLStructuredTypePart attribute) {
		if (attribute.getModelKind() == ModelKind.REFERENCE) {
			TLReference reference = (TLReference) attribute;
			if (TLModelUtil.getEndIndex(reference.getEnd()) != 1) {
				return true;
			}
		}
		return false;
	}

	private boolean isComposite(TLStructuredTypePart attribute) {
		if (attribute.getModelKind() == ModelKind.REFERENCE) {
			TLReference reference = (TLReference) attribute;
			if (reference.getEnd().isComposite()) {
				return true;
			}
		}
		return false;
	}

	private boolean isExtension(TLStructuredTypePart attribute) {
		return attribute.getAnnotation(BPMLExtension.class) != null;
	}

	private void index(TLObject obj) {
		if (obj == null) {
			return;
		}
		if (obj instanceof Externalized) {
			_origById.put(((Externalized) obj).getExtId(), obj);
		}

		for (TLStructuredTypePart attr : obj.tType().getAllParts()) {
			if (attr.getModelKind() != ModelKind.REFERENCE) {
				continue;
			}

			TLReference ref = (TLReference) attr;
			if (!ref.getEnd().isComposite()) {
				continue;
			}

			Object value = obj.tValue(ref);
			if (value instanceof Collection<?>) {
				for (Object entry : (Collection<?>) value) {
					index((TLObject) entry);
				}
			} else {
				index((TLObject) value);
			}
		}
	}
}
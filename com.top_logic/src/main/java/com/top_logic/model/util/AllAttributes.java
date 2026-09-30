/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.cache.TLModelCacheService;

/**
 * Option provider delivering the attributes of all {@link TLClass}es.
 *
 * <p>
 * The options are presented as tree of {@link TLClass}es with their attributes as leafs. Only the
 * attributes are selectable, the types are pure structure.
 * </p>
 *
 * @implNote Inherited attributes are offered below every {@link TLClass} that has them, but such an
 *           option is the attribute of its declaring type: Selecting an inherited attribute results
 *           in the attribute of the type it is declared in, not in an attribute of the type it was
 *           selected below.
 *
 * @see Options#fun()
 * @see AllTypeAttributes Selecting an attribute of an already selected type.
 * @see AllClasses Selecting the type itself.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AllAttributes extends AllClasses {

	@Override
	protected TypesTree tree() {
		return new AttributesTree();
	}

	@Override
	protected Filter<? super TLModelPart> modelFilter() {
		return new IsAcceptedAttribute();
	}

	/**
	 * Whether the given attribute is offered as option.
	 *
	 * <p>
	 * The result is relevant twice: An attribute that is not accepted is neither displayed in the
	 * option tree, nor selectable in it. Therefore, specializing this method is sufficient to
	 * restrict the offered attributes.
	 * </p>
	 *
	 * @param part
	 *        An attribute of some {@link TLClass}, potentially inherited from one of its
	 *        generalizations.
	 */
	protected boolean acceptPart(TLStructuredTypePart part) {
		return true;
	}

	/**
	 * Tree model of {@link TLClass} options with their attributes as leafs.
	 *
	 * @see com.top_logic.model.util.AllClasses.ClassesTree
	 */
	protected class AttributesTree extends ClassesTree {

		@Override
		public List<TLModelPart> visitClass(TLClass model, Void arg) {
			List<TLModelPart> result = new ArrayList<>();
			// Note: The inherited attributes are taken from the cache, since the children of a node
			// are computed over and over again while the option tree is displayed.
			for (TLStructuredTypePart part : TLModelCacheService.getOperations().getAllAttributes(model)) {
				if (acceptPart(part)) {
					result.add(part);
				}
			}
			Collections.sort(result, LabelComparator.newCachingInstance());

			// Note: The specializations are appended after the attributes, since the attributes of
			// the type itself are the primary options.
			result.addAll(super.visitClass(model, arg));
			return result;
		}

	}

	/**
	 * {@link Filter} accepting the {@link TLStructuredTypePart}s offered as options.
	 *
	 * @see AllAttributes#acceptPart(TLStructuredTypePart)
	 */
	protected class IsAcceptedAttribute implements Filter<Object> {

		@Override
		public boolean accept(Object anObject) {
			return anObject instanceof TLStructuredTypePart part && acceptPart(part);
		}

	}

}

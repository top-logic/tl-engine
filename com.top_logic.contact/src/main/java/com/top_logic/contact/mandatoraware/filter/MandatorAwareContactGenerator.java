/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.element.core.TLElementVisitor;
import com.top_logic.element.core.TraversalFactory;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.ListGeneratorAdaptor;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.element.structured.wrap.MandatorAware;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;

/**
 * The MandatorAwareContactGenerator caches {@link MandatorAware}s indexed by their {@link Mandator}
 *
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public abstract class MandatorAwareContactGenerator extends ListGeneratorAdaptor {

	/**
	 * {@link TLElementVisitor} collection all {@link Mandator}s in the visited tree.
	 */
	public static class MandatorCollector implements TLElementVisitor {

		private List<Mandator> collect;

		/**
		 * Creates a new {@link MandatorCollector}.
		 */
		public MandatorCollector() {
			this.collect = new ArrayList<>();
		}

		@Override
		public boolean onVisit(StructuredElement anElement, int aDepth) {
			if (anElement instanceof Mandator) {
				this.collect.add((Mandator) anElement);
				return true;
			}
			return false;
		}

		public List<Mandator> getMandators() {
			return this.collect;
		}

		/**
		 * Return all parent {@link Mandator}s, including the given {@link Mandator}.
		 */
		public static List<Mandator> collectUpwards(Mandator aMandator) {
			MandatorCollector theVisitor = new MandatorCollector();
			TraversalFactory.traverse(aMandator, theVisitor, TraversalFactory.ANCESTORS, false);
			return theVisitor.getMandators();
		}

		/**
		 * Return all child {@link Mandator}s, but not the given {@link Mandator}.
		 */
		public static List<Mandator> collectDownwards(Mandator aMandator) {
			MandatorCollector theVisitor = new MandatorCollector();
			TraversalFactory.traverse(aMandator, theVisitor, TraversalFactory.DEPTH_FIRST_POSTORDER, true);
			return theVisitor.getMandators();
		}

	}

	@Override
	public List<?> generateList(EditContext editContext) {
		TLObject object = editContext.getObject();
		Mandator theMandator =
			MandatorAwareContactFilter.getMandator(object, editContext);

		return CollectionUtil.toList(this.getCachedContacts(
		        theMandator, MandatorAwareContactFilter.isSearch(object, editContext)));
	}

	/**
	 * Return a {@link Collection} of {@link MandatorAware} of a {@link Mandator} and all of
	 * its parent {@link Mandator}s.
	 * If isSearch, the {@link MandatorAware}s of all child {@link Mandator}s will be included in
	 * the result, too.
	 */
	private Collection<MandatorAware> getCachedContacts(Mandator aMandator, boolean isSearch) {

//		long start = System.currentTimeMillis();

		List<MandatorAware> theContacts = new ArrayList<>();
		for (Mandator theMandator : MandatorCollector.collectUpwards(aMandator)) {
			theContacts.addAll(this._getCachedContacts(theMandator));
		}

		if (isSearch) {
			for (Mandator theMandator : MandatorCollector.collectDownwards(aMandator)) {
				theContacts.addAll(this._getCachedContacts(theMandator));
			}
		}

//		Logger.info("Caching took " + DebugHelper.getTime(System.currentTimeMillis() - start), this);
		return theContacts;
	}

	/**
	 * Return the {@link TLStructuredTypePart} of a {@link Mandator} from which referring links are
	 * generated.
	 */
	protected abstract TLReference getReference();

	/**
	 * Filter wrappers that are referring to {@link #getReference()}.
	 */
	protected abstract Filter<? super TLObject> getFilter();

	/**
	 * Return an {@link Set} of {@link MandatorAware} of a {@link Mandator}, never <code>null</code>
	 * .
	 */
	protected final Set<MandatorAware> _getCachedContacts(Mandator aMandator) {
		TLReference reference = getReference();
		Set<? extends TLObject> referers = aMandator.tReferers(reference);
		Filter<? super TLObject> filter = getFilter();
		if (filter != FilterFactory.trueFilter()) {
			FilterUtil.filterInline(filter, referers);
		}
		return (Set) referers;
	}
	
}

/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.util;

import static com.top_logic.model.util.TLModelUtil.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.demo.model.plain.A;
import com.top_logic.demo.model.plain.DemoPlainA;
import com.top_logic.demo.model.plain.DemoPlainFactory;
import com.top_logic.demo.model.types.DemoTypesFactory;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link DemoTreeBuilder} that treats objects of type {@link A#A_TYPE} in the attribute
 * {@link DemoPlainA#LIST_ATTR} as children.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CyclicDemoTreeBuilder<C extends CyclicDemoTreeBuilder.Config<?>> extends DemoTreeBuilder<C> {

	/**
	 * Creates a {@link CyclicDemoTreeBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CyclicDemoTreeBuilder(InstantiationContext context, C config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public Collection<? extends Wrapper> getParents(LayoutComponent contextComponent, TLObject node) {
		Set<Wrapper> referrers = getPlainAReferrers(node);
		if (referrers.isEmpty()) {
			return super.getParents(contextComponent, node);
		} else {
			Collection<? extends Wrapper> realParents = super.getParents(contextComponent, node);
			if (realParents.isEmpty()) {
				return referrers;
			} else {
				LinkedHashSet<Wrapper> allParents = new LinkedHashSet<>();
				allParents.addAll(realParents);
				allParents.addAll(referrers);
				return allParents;
			}
		}
	}

	private Set<Wrapper> getPlainAReferrers(TLObject node) {
		TLClass concreteType = (TLClass) node.tType();
		TLTypePart listAttr = DemoPlainFactory.getListDemoPlainAAttr();
		TLClass expectedType = (TLClass) listAttr.getType();
		boolean maybeReferenced = TLModelUtil.isGeneralization(expectedType, concreteType);
		Set<Wrapper> referrers;
		if (maybeReferenced) {
			Set<? extends TLObject> refs = AttributeOperations.getReferers((TLObject) node, (TLStructuredTypePart) listAttr);
			referrers = CollectionUtil.dynamicCastView(Wrapper.class, refs);
		} else {
			referrers = Collections.emptySet();
		}
		return referrers;
	}

	@Override
	public Iterator<? extends TLObject> getChildIterator(TLObject node) {
		String meType = qualifiedName(node.tType());
		if (meType.equals(qualifiedName(DemoPlainFactory.DEMO_PLAIN_STRUCTURE, A.A_TYPE))) {
			return ((DemoPlainA) node).getList().iterator();
		} else {
			return super.getChildIterator(node);
		}
	}

	@Override
	public boolean canExpandAll() {
		return false;
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		return DemoTypesFactory.getInstance().getRoot();
	}

	@Override
	public Object retrieveModelFromNode(LayoutComponent contextComponent, TLObject node) {
		return null;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null;
	}

}


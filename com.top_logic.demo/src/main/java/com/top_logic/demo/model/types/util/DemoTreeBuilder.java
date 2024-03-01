/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.util;

import static com.top_logic.model.util.TLModelUtil.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.ArrayQueue;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.demo.model.plain.A;
import com.top_logic.demo.model.plain.DemoPlainFactory;
import com.top_logic.demo.model.types.DemoTypesAll;
import com.top_logic.demo.model.types.DemoTypesFactory;
import com.top_logic.demo.model.types.DemoTypesL;
import com.top_logic.demo.model.types.DemoTypesRoot;
import com.top_logic.demo.model.types.L;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.tree.component.AbstractTreeModelBuilder;
import com.top_logic.layout.tree.component.TreeModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link TreeModelBuilder} that creates a joined tree from the {@link DemoTypesFactory} structure
 * with {@link DemoPlainFactory} objects linked from {@link DemoTypesL} nodes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoTreeBuilder<C extends DemoTreeBuilder.Config<?>> extends AbstractTreeModelBuilder<TLObject>
		implements ConfiguredInstance<C> {

	/**
	 * Configuration options for {@link DemoTreeBuilder}.
	 */
	public interface Config<I extends DemoTreeBuilder<?>> extends PolymorphicConfiguration<I> {

		@Format(CommaSeparatedStrings.class)
		@FormattedDefault(DemoTypesFactory.DEMO_TYPES_STRUCTURE + ":" + DemoTypesAll.DEMO_TYPES_ALL_TYPE + "," +
			DemoTypesFactory.DEMO_TYPES_STRUCTURE + ":" + DemoTypesRoot.DEMO_TYPES_ROOT_TYPE + "," +
			DemoPlainFactory.DEMO_PLAIN_STRUCTURE + ":" + A.A_TYPE
		)
		List<String> getAllTypes();
	}

	private final List<TLType> _allTypes = new ArrayList<>();

	private C _config;

	/**
	 * Creates a {@link DemoTreeBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DemoTreeBuilder(InstantiationContext context, C config) throws ConfigurationException {
		_config = config;

		for (String name : config.getAllTypes()) {
			TLType type = TLModelUtil.findType(name);
			_allTypes.add(type);
		}
	}

	@Override
	public C getConfig() {
		return _config;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return supportsNode(aComponent, aModel);
	}

	@Override
	public Object retrieveModelFromNode(LayoutComponent contextComponent, TLObject node) {
		Object currentModel = contextComponent.getModel();
		HashSet<TLObject> processed = new HashSet<>();
		Queue<TLObject> remaining = new ArrayQueue<>();
		remaining.offer(node);
		do {
			node = remaining.poll();
			if (processed.contains(node)) {
				continue;
			}
			for (TLObject parent : getParents(contextComponent, node)) {
				if (parent == currentModel) {
					return currentModel;
				}
				remaining.offer(parent);
			}
		} while (!remaining.isEmpty());
		return node;
	}

	@Override
	public Collection<? extends Wrapper> getParents(LayoutComponent contextComponent, TLObject node) {
		String meType = qualifiedName(node.tType());
		if (meType.equals(qualifiedName(DemoPlainFactory.DEMO_PLAIN_STRUCTURE, A.A_TYPE))) {
			TLStructuredTypePart attribute = DemoTypesFactory.getPlainChildrenDemoTypesLAttr();
			return CollectionUtil.dynamicCastView(Wrapper.class,
				AttributeOperations.getReferers(node, attribute));
		} else {
			Wrapper parent = ((StructuredElement) node).getParent();
			if (parent == null) {
				// node is root
				return Collections.emptyList();
			} else {
				return Collections.singleton(parent);
			}
		}
	}

	@Override
	public boolean supportsNode(LayoutComponent contextComponent, Object node) {
		if (!(node instanceof Wrapper)) {
			return false;
		}
		return typeSupported(((Wrapper) node).tType());
	}

	private boolean typeSupported(TLStructuredType actualType) {
		if (actualType == null) {
			return false;
		}
		for (TLType type : _allTypes) {
			if (TLModelUtil.isCompatibleType(type, actualType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<? extends TLObject> getChildIterator(TLObject node) {
		String meType = qualifiedName(node.tType());
		if (meType.equals(qualifiedName(DemoTypesFactory.DEMO_TYPES_STRUCTURE, L.L_TYPE))) {
			return ((DemoTypesL) node).getPlainChildren().iterator();
		} else if (node instanceof StructuredElement) {
			return CollectionUtil.dynamicCastView(Wrapper.class, ((StructuredElement) node).getChildren()).iterator();
		} else {
			return Collections.<Wrapper>emptyList().iterator();
		}
	}


}

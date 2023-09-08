/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.scripting;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ui.BreadcrumbStrings;
import com.top_logic.layout.scripting.recorder.ref.value.object.VersionedObjectNaming;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.TLModuleSingletons;
import com.top_logic.model.TLObject;
import com.top_logic.util.model.ModelService;

/**
 * {@link ModelNamingScheme} for {@link StructuredElement} using a path of names.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StructuredElementNaming extends VersionedObjectNaming<StructuredElement, StructuredElementNaming.Name> {

	/**
	 * {@link ModelName} for {@link StructuredElementNaming}.
	 */
	public interface Name extends VersionedObjectNaming.Name {

		/**
		 * @see #getSingletonLabel()
		 */
		String SINGLETON_LABEL = "singleton-label";

		/**
		 * @see #getStructure()
		 */
		String STRUCTURE = "structure";

		/**
		 * @see #getPath()
		 */
		String PATH = "path";

		/**
		 * @see #getSingleton()
		 */
		String SINGLETON = "singleton";

		/**
		 * The name of the structure the referenced object is part of.
		 */
		@com.top_logic.basic.config.annotation.Name(STRUCTURE)
		String getStructure();

		/**
		 * @see #getStructure()
		 */
		void setStructure(String value);

		/**
		 * List of node names that describe a path from the structure root (exclusive) to the
		 * referenced node (inclusive).
		 */
		@com.top_logic.basic.config.annotation.Name(PATH)
		@Format(BreadcrumbStrings.class)
		List<String> getPath();

		/**
		 * @see #getPath()
		 */
		void setPath(List<String> value);

		/**
		 * The name of the singleton.
		 */
		@com.top_logic.basic.config.annotation.Name(SINGLETON)
		@StringDefault(TLModule.DEFAULT_SINGLETON_NAME)
		String getSingleton();

		/**
		 * @see #getSingleton()
		 */
		void setSingleton(String value);

		/**
		 * The label of the singleton.
		 * 
		 * @implNote The label is not needed but only be set to be able to have better readability
		 *           of this {@link Name}.
		 */
		@com.top_logic.basic.config.annotation.Name(SINGLETON_LABEL)
		String getSingletonLabel();

		/**
		 * @see #getSingletonLabel()
		 */
		void setSingletonLabel(String value);
	}

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Class<StructuredElement> getModelClass() {
		return StructuredElement.class;
	}

	private static boolean isFakeWrapper(Wrapper wrapper) {
		return wrapper.tHandle() == null;
	}

	@Override
	public Maybe<Name> buildName(StructuredElement model) {
		if ((model == null) || isFakeWrapper(model)) {
			return Maybe.none();
		}
		ArrayList<String> path = new ArrayList<>();

		StructuredElement currentNode = model;
		StructuredElement currentParent = model.getParent();
		while (currentParent != null) {
			String nodeName = currentNode.getName();

			checkNodeResolvability(currentNode, nodeName);

			path.add(0, nodeName);
			currentNode = currentParent;
			currentParent = currentParent.getParent();
		}

		if (isSingleton(currentNode)) {
			Name name = createName();

			setVersionContext(name, model);

			name.setStructure(model.getStructureName());
			name.setPath(path);
			name.setSingleton(model.tType().getModule().getSingletonName(currentNode));
			String singletonLabel = MetaLabelProvider.INSTANCE.getLabel(currentNode);
			if (!StringServices.isEmpty(singletonLabel)) {
				name.setSingletonLabel(singletonLabel);
			}

			return Maybe.some(name);
		} else {
			return Maybe.none();
		}
	}

	/**
	 * The root node is not added to the path, since the root node is identified by the empty path.
	 * 
	 * @return Whether the path ended in a singleton object.
	 */
	private static boolean buildStructurePath(List<String> path, StructuredElement node) {
		StructuredElement currentNode = node;
		StructuredElement currentParent = node.getParent();
		while (currentParent != null) {
			String nodeName = currentNode.getName();

			checkNodeResolvability(currentNode, nodeName);

			path.add(0, nodeName);
			currentNode = currentParent;
			currentParent = currentParent.getParent();
		}
		return isSingleton(currentNode);
	}

	/**
	 * Whether the given object is a model singleton.
	 */
	public static boolean isSingleton(StructuredElement currentNode) {
		for (TLModuleSingleton link : currentNode.tType().getModel().getQuery(TLModuleSingletons.class).getAllSingletons()) {
			if (link.getSingleton() == currentNode) {
				return true;
			}
		}
		return false;
	}

	private static void checkNodeResolvability(StructuredElement node, String nodeName) {
		StructuredElement parent = node.getParent();
		StructuredElement resolvedNode = getChild(parent, nodeName);
		if (!node.equals(resolvedNode)) {
			throw new IllegalStateException(
				"Failed to build a resolvable structure path. Resolving the path resulted in a different object.");
		}
	}

	private static StructuredElement getChild(StructuredElement node, String name) {
		StructuredElement match = null;
		for (StructuredElement child : node.getChildren()) {
			if (name.equals(child.getName())) {
				if (match != null) {
					throw new IllegalStateException(
						"Failed to build a resolvable structure path. Name is not unique: " + name);
				}
				match = child;
			}
		}
		return match;
	}

	@Override
	public StructuredElement locateModel(ActionContext context, Name name) {
		Revision revision = getRevision(context, name);
		Branch branch = getBranch(context, name);
		TLObject singleton = ModelService.getInstance().getModel()
			.getModule(name.getStructure()).getSingleton(name.getSingleton());
		StructuredElement node = (StructuredElement) WrapperHistoryUtils.getWrapper(branch, revision, singleton);
		List<String> nodeNames = name.getPath();
		for (String nodeName : nodeNames) {
			StructuredElement child = getChild(node, nodeName);
			if (child == null) {
				throw ApplicationAssertions.fail(name,
					"Cannot resolve structure node. Searched for a child named '" + nodeName
						+ "' in the node: " + StringServices.getObjectDescription(node));
			}
			node = child;
		}
		return node;
	}
}

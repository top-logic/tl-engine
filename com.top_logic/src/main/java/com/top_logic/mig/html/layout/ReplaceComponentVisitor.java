/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.top_logic.layout.xml.LayoutControlComponent;

/**
 * Visitor replacing one component by another in the visited component tree. Descending stops when a
 * component was found that stored under a different layout key.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class ReplaceComponentVisitor extends DefaultDescendingLayoutVisitor {

	private final Map<LayoutComponent, LayoutComponent> _componentMapping;

	private String _rootLayoutKey;

	private Function<LayoutComponent, String> _keyMapping;

	/**
	 * Creates a new {@link ReplaceComponentVisitor}.
	 * 
	 * @param componentMapping
	 *        Definition of the replacement operation. The {@link Map} must not contain
	 *        <code>null</code> as value.
	 * @param keyMapping
	 *        Function that delivers layout keys for component. If a component has a different key,
	 *        descending stops.
	 */
	public ReplaceComponentVisitor(Map<LayoutComponent, LayoutComponent> componentMapping,
			Function<LayoutComponent, String> keyMapping) {
		_componentMapping = componentMapping;
		_keyMapping = keyMapping;
	}

	void replaceComponents(LayoutComponent rootComponent) {
		_rootLayoutKey = layoutKey(rootComponent);
		rootComponent.acceptVisitorRecursively(this);
	}

	private String layoutKey(LayoutComponent component) {
		return _keyMapping.apply(component);
	}

	@Override
	public boolean visitLayoutContainer(LayoutContainer aComponent) {
		List<LayoutComponent> currentChildren = aComponent.getChildList();
		List<LayoutComponent> newChildren = replace(currentChildren);
		if (newChildren != null) {
			aComponent.setChildren(newChildren);
		}
		return super.visitLayoutContainer(aComponent);
	}

	private List<LayoutComponent> replace(List<? extends LayoutComponent> components) {
		int childCount = components.size();
		int index = 0;
		while (index < childCount) {
			if (_componentMapping.containsKey(components.get(index))) {
				break;
			}
			index++;
		}
		if (index < childCount) {
			List<LayoutComponent> newChildren = new ArrayList<>(childCount);
			newChildren.addAll(components.subList(0, index));
			for (; index < childCount; index++) {
				LayoutComponent child = components.get(index);
				LayoutComponent replacement = _componentMapping.get(child);
				if (replacement != null) {
					newChildren.add(replacement);
				} else {
					newChildren.add(child);
				}
			}
			return newChildren;
		}

		return null;
	}

	@Override
	public boolean visitLayoutComponent(LayoutComponent aComponent) {
		if (aComponent instanceof LayoutControlComponent) {
			LayoutControlComponent cmp = (LayoutControlComponent) aComponent;
			LayoutComponent mappedReference = _componentMapping.get(cmp.getReferencedComponent());
			if (mappedReference != null) {
				cmp.replaceReferencedComponent(mappedReference);
			}

		}
		List<? extends LayoutComponent> oldDialogs = aComponent.getDialogs();
		if (oldDialogs.size() > 0) {
			List<LayoutComponent> newDialogs = replace(oldDialogs);
			if (newDialogs != null) {
				aComponent.setDialogs(newDialogs);

				LayoutComponent currentDialog = aComponent.getDialog();
				if (currentDialog != null) {
					LayoutComponent mappedDialog = _componentMapping.get(currentDialog);
					if (mappedDialog != null) {
						aComponent.setDialog(mappedDialog);
					}
				}
			}
		}

		String layoutKey = layoutKey(aComponent);
		if (layoutKey == null) {
			// Component has no own key. Descend.
			return true;
		}
		if (_rootLayoutKey == null) {
			// root component has no own layout key.
			return false;
		} else {
			// Component and root have the same key descend until key mismatch.
			return _rootLayoutKey.equals(layoutKey);
		}
	}

}


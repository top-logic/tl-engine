/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUIBuilder;
import com.top_logic.layout.tree.model.DefaultTreeUINodeModel.DefaultTreeUINode;
import com.top_logic.layout.view.model.TreeSelectionDisplay;

/**
 * Tests {@link TreeSelectionDisplay#intercepting(TreeSelectionDisplay, java.util.function.Predicate)}.
 */
public class TestInterceptingTreeSelectionDisplay extends TestCase {

	/**
	 * A display holding its selection in a set and reporting every change of it.
	 */
	private static final class SetDisplay implements TreeSelectionDisplay {

		private final Set<DefaultTreeUINode> _selection = new LinkedHashSet<>();

		private final List<Listener> _listeners = new ArrayList<>();

		/** Selects the given node as a user click does. */
		void click(DefaultTreeUINode node) {
			showSelection(Set.of(node), false);
		}

		Set<DefaultTreeUINode> selection() {
			return _selection;
		}

		@Override
		public Set<Object> getSelectedObjects() {
			return TreeSelectionDisplay.businessObjects(_selection);
		}

		@Override
		public boolean canDisplaySeveral() {
			return false;
		}

		@Override
		public void showSelection(Set<DefaultTreeUINode> nodes, boolean reveal) {
			if (_selection.equals(nodes)) {
				return;
			}
			_selection.clear();
			_selection.addAll(nodes);
			for (Listener listener : new ArrayList<>(_listeners)) {
				listener.selectionChanged(new ArrayList<>(nodes));
			}
		}

		@Override
		public Registration addSelectionListener(Listener listener) {
			_listeners.add(listener);
			return Registration.onDispose(() -> _listeners.remove(listener));
		}
	}

	/**
	 * Selecting a node the handler takes over reaches the handler, not the listeners; what the
	 * handler selects in answer reaches the listeners.
	 */
	public void testInterceptedSelection() {
		DefaultTreeUINodeModel tree = new DefaultTreeUINodeModel(new DefaultTreeUIBuilder(), "root");
		DefaultTreeUINode target = tree.getRoot().createChild("target");
		DefaultTreeUINode link = tree.getRoot().createChild("link");
		DefaultTreeUINode plain = tree.getRoot().createChild("plain");

		SetDisplay inner = new SetDisplay();
		List<DefaultTreeUINode> handled = new ArrayList<>();
		TreeSelectionDisplay display = TreeSelectionDisplay.intercepting(inner, node -> {
			if (node != link) {
				return false;
			}
			handled.add(node);
			inner.showSelection(Set.of(target), true);
			return true;
		});
		List<Collection<DefaultTreeUINode>> reported = new ArrayList<>();
		List<Collection<DefaultTreeUINode>> reportedToSecond = new ArrayList<>();
		display.addSelectionListener(reported::add);
		Registration second = display.addSelectionListener(reportedToSecond::add);

		inner.click(link);

		assertEquals("The handler sees the selection once, whatever the number of listeners.", List.of(link),
			handled);
		assertEquals(Set.of(target), inner.selection());
		assertEquals(List.of(List.of(target)), reported);
		assertEquals(List.of(List.of(target)), reportedToSecond);

		reported.clear();
		second.dispose();
		inner.click(plain);

		assertEquals(Set.of(plain), inner.selection());
		assertEquals(List.of(List.of(plain)), reported);
		assertEquals("A disposed listener hears nothing.", 1, reportedToSecond.size());
	}
}

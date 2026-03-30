/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.layout;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.react.flow.data.Border;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.GraphEdge;
import com.top_logic.react.flow.data.GraphLayout;
import com.top_logic.react.flow.data.Padding;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.server.ui.AWTContext;
import com.top_logic.react.flow.svg.RenderContext;

/**
 * Test case for {@link GraphLayout} with Sugiyama algorithm.
 */
@SuppressWarnings("javadoc")
public class TestGraphLayout extends TestCase {

	public void testSimpleGraph() {
		// Same graph as graph-layout-demo.view.xml
		Box person = node("Person");
		Box company = node("Company");
		Box address = node("Address");
		Box order = node("Order");
		Box product = node("Product");
		Box category = node("Category");

		GraphEdge e1 = GraphEdge.create().setSource(person).setTarget(company);
		GraphEdge e2 = GraphEdge.create().setSource(person).setTarget(address);
		GraphEdge e3 = GraphEdge.create().setSource(person).setTarget(order);
		GraphEdge e4 = GraphEdge.create().setSource(order).setTarget(product);
		GraphEdge e5 = GraphEdge.create().setSource(product).setTarget(category);

		GraphLayout layout = GraphLayout.create();
		layout.getNodes().add(person);
		layout.getNodes().add(company);
		layout.getNodes().add(address);
		layout.getNodes().add(order);
		layout.getNodes().add(product);
		layout.getNodes().add(category);
		layout.getEdges().add(e1);
		layout.getEdges().add(e2);
		layout.getEdges().add(e3);
		layout.getEdges().add(e4);
		layout.getEdges().add(e5);

		Diagram diagram = Diagram.create().setRoot(layout);

		RenderContext context = new AWTContext(12f);

		// Phase 1: computeIntrinsicSize triggers Sugiyama
		diagram.layout(context);

		// Nodes must have non-zero positions
		assertTrue("Person.x", person.getX() >= 0);
		assertTrue("Person.y", person.getY() >= 0);
		assertTrue("Company.x", company.getX() >= 0);
		assertTrue("Company.y", company.getY() > 0);
		assertTrue("Order.x", order.getX() >= 0);
		assertTrue("Order.y", order.getY() > 0);
		assertTrue("Product.y", product.getY() > order.getY());

		// Person should be in the top layer
		assertTrue("Person above Company", person.getY() < company.getY());
		assertTrue("Person above Order", person.getY() < order.getY());

		// Edges should have waypoints
		for (GraphEdge edge : layout.getEdges()) {
			assertTrue("Edge should have waypoints: " + edge,
				edge.getWaypoints().size() >= 2);
		}

		// Layout dimensions must be positive
		assertTrue("Layout width > 0", layout.getWidth() > 0);
		assertTrue("Layout height > 0", layout.getHeight() > 0);

		System.out.println("Layout: " + layout.getWidth() + " x " + layout.getHeight());
		System.out.println("Person: " + person.getX() + ", " + person.getY() + " (" + person.getWidth() + "x" + person.getHeight() + ")");
		System.out.println("Company: " + company.getX() + ", " + company.getY());
		System.out.println("Order: " + order.getX() + ", " + order.getY());
		System.out.println("Product: " + product.getX() + ", " + product.getY());

		for (GraphEdge edge : layout.getEdges()) {
			System.out.println("Edge waypoints: " + edge.getWaypoints().size());
			edge.getWaypoints().forEach(wp -> System.out.println("  " + wp.getX() + ", " + wp.getY()));
		}
	}

	private static Box node(String label) {
		return Border.create().setContent(
			Padding.create().setAll(5).setContent(
				Text.create().setValue(label)));
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.script;

import java.io.IOException;

import junit.framework.TestCase;

import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.server.script.FlowFactory;

import de.haumacher.msgbuf.graph.DefaultScope;
import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Test case for {@link FlowFactory#chart(com.top_logic.react.flow.data.Box, String, Object, boolean)}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestFlowFactoryChart extends TestCase {

	/**
	 * A diagram is panned and zoomed as a whole unless the script fixes its view.
	 */
	public void testFixedViewDefault() {
		Diagram diagram = FlowFactory.chart(Text.create().setValue("A"), null, null, false);
		assertFalse(diagram.isFixedView());
	}

	/**
	 * A fixed view reaches the client with the diagram.
	 */
	public void testFixedViewSentToClient() throws IOException {
		Diagram diagram = FlowFactory.chart(Text.create().setValue("A"), null, null, true);
		assertTrue(diagram.isFixedView());

		StringW out = new StringW();
		diagram.writeTo(new DefaultScope(2, 0), new JsonWriter(out));

		Diagram received = Diagram.readDiagram(new DefaultScope(2, 1), new JsonReader(new StringR(out.toString())));
		assertTrue(received.isFixedView());
	}

}

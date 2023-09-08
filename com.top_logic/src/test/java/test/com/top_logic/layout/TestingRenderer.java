/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;

/**
 * Pseudo {@link Renderer} that collects rendered objects for test case
 * assertions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestingRenderer implements Renderer<Object> {

	private List<Object> renderedObjects = new ArrayList<>();
	
	@Override
	public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
		renderedObjects.add(value);
		if (value != null) {
			out.writeText(value.toString());
		}
	}
	
	/**
	 * Clears the list of {@link #getRenderedObjects()}.
	 */
	public void clear() {
		this.renderedObjects.clear();
	}
	
	/**
	 * The list of rendered objects since the last call to {@link #clear()}.
	 */
	public List getRenderedObjects() {
		return renderedObjects;
	}

}

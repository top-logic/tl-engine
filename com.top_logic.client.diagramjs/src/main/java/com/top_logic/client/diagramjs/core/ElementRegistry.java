package com.top_logic.client.diagramjs.core;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Registry to keep track of {@link Diagram} elements.
 * 
 * @see Diagram#getElementRegistry()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ElementRegistry extends JavaScriptObject {

	/**
	 * Creates a {@link ElementRegistry}.
	 */
	protected ElementRegistry() {
		super();
	}

	/**
	 * True if the diagram contains an element with the given business object.
	 */
	public final native boolean hasElementWithObject(Object businessObject) /*-{
		var filteredElements = this.filter(function(element) {
			return element.sharedGraphPart == businessObject;
		});

		console.log(filteredElements);

		return filteredElements.length != 0;
	}-*/;

}

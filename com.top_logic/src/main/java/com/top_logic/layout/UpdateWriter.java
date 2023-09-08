/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Stack;

import com.top_logic.base.services.simpleajax.AJAXConstants;
import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.basic.CombinedWriter;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.mig.html.layout.LayoutUtils;

/**
 * The class {@link UpdateWriter} is an implementation of {@link UpdateQueue}
 * which directly writes the added {@link ClientAction} to some
 * {@link TagWriter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UpdateWriter implements UpdateQueue {

	private final TagWriter out;
	private final DisplayContext context;
	private final Integer sequence;
	private final String requestSourceContext;	

	private boolean dropped;
	private boolean headerWritten;

	private LayoutComponent targetComponent;

	/*
	 * Debug variables
	 */
	private StringBuffer debugBuffer;
	private OutputStreamWriter fileWriter;

	private final String _encoding;

	/**
	 * Creates a {@link UpdateWriter}. It is not indicated to use an UpdateWriter more
	 * than once.
	 * 
	 * @param context
	 *        the {@link DisplayContext} which is used to evaluate the added {@link ClientAction}s.
	 *        must not be <code>null</code>.
	 * @param out
	 *        the {@link TagWriter} which is used to write the added {@link ClientAction} to. must
	 *        not be <code>null</code>.
	 * @param encoding
	 *        Character encoding of the underlying writer.
	 */
	public UpdateWriter(DisplayContext context, TagWriter out, String encoding, Integer sequence) {
		this(context, out, encoding, sequence, "");
	}
	
	/**
	 * Creates a {@link UpdateWriter}. It is not indicated to use an UpdateWriter more
	 * than once.
	 * 
	 * @param context
	 *        the {@link DisplayContext} which is used to evaluate the added {@link ClientAction}s.
	 *        must not be <code>null</code>.
	 * @param out
	 *        the {@link TagWriter} which is used to write the added {@link ClientAction} to. must
	 *        not be <code>null</code>.
	 * @param encoding
	 *        Encoding of the underlying writer.
	 */
	public UpdateWriter(DisplayContext context, TagWriter out, String encoding, Integer sequence,
			String requestSourceContext) {
		_encoding = encoding;
		if (out == null) {
			throw new IllegalArgumentException("'out' must not be 'null'.");
		}
		this.out = out;
		if (isDebugMode()) {
			debugTagWriter();
		}
		if (context == null) {
			throw new IllegalArgumentException("'context' must not be 'null'.");
		}
		this.context = context;
		this.sequence = sequence;
		this.requestSourceContext = requestSourceContext;
	}

	/**
	 * Installs a writer to the {@link #out} which writes to the origin
	 * {@link Writer} of out, fills {@link #debugBuffer}, and writes content to
	 * the file "temp/updateWriter.xml".
	 */
	private void debugTagWriter() {
		final Writer originalWriter = this.out.internalTargetWriter();

		StringWriter stringWriter = new StringWriter();
		debugBuffer = stringWriter.getBuffer();

		CombinedWriter writer;
		try {
			File folder = new File("temp");
			if (!folder.exists()) {
				folder = new File("tmp");
				if (!folder.exists()) {
					folder.mkdir();
				}
			}
			fileWriter =
				new OutputStreamWriter(new FileOutputStream(new File(folder, "updateWriter.xml")),
					LayoutConstants.UTF_8);
			writer = new CombinedWriter(new Writer[] { originalWriter, stringWriter, fileWriter });
		} catch (IOException ex) {
			writer = new CombinedWriter(new Writer[] { originalWriter, stringWriter });
		}

		out.setOut(writer);
	}

	/**
	 * Whether debug mode is enabled. In debug mode the actions added to this
	 * {@link UpdateWriter} will also be written to a file.
	 */
	private boolean isDebugMode() {
		return false;
	}

	/**
	 * This method returns the {@link DisplayContext} this {@link UpdateWriter}
	 * uses to evaluate the added actions.
	 */
	public DisplayContext getDisplayContext() {
		return context;
	}

	public final void setTargetComponent(LayoutComponent targetComponent) {
		this.targetComponent = targetComponent;
	}

	public final LayoutComponent getTargetComponent() {
		return targetComponent;
	}

	/**
	 * The added {@link ClientAction} are directly evaluated and written to the
	 * {@link TagWriter} of this {@link UpdateWriter}. If the first action is
	 * added the UpdateWriter writes the beginning of the response to its
	 * {@link TagWriter}.
	 * 
	 * @see com.top_logic.layout.UpdateQueue#add(com.top_logic.base.services.simpleajax.ClientAction)
	 */
	@Override
	public void add(ClientAction action) {
		if (action == null) {
			return;
		}
		String targetContext = null;
		if (getTargetComponent() != null) {
			targetContext = LayoutUtils.createActionContext(getTargetComponent());
		}

		if (!headerWritten) {
			try {
				beginResponse();
			} catch (IOException ex) {
				throw new FatalXMLError(ex);
			}
			headerWritten = true;
		}

		try {
			Stack<String> openedTags = out.getStack();
			action.writeAsXML(context, out, targetContext);
		} catch (IOException ex) {
			throw new FatalXMLError(ex);
		}
	}

	/**
	 * Service method to add all actions in the given array.
	 * 
	 * @see #add(ClientAction)
	 */
	public final void add(ClientAction... actions) {
		if (actions != null) {
			for (int i = 0, length = actions.length; i < length; i++) {
				add(actions[i]);
			}
		}
	}

	/**
	 * This method indicates that the current request is dropped.
	 * 
	 * @return whether a corresponding property was written to the response.
	 */
	public boolean setRequestDropped() {
		if (headerWritten) {
			return false;
		}
		this.dropped = true;
		return true;
	}

	/**
	 * This method closes the {@link UpdateWriter}. This method also writes the
	 * end of the response. It also closes its internal {@link TagWriter}.
	 * <p>
	 * This method must be called to write the end of the response.
	 * </p>
	 */
	public void endResponse() {
		try {
			if (!headerWritten) {
				beginResponse();
			}
			out.endTag(AJAXConstants.AJAX_ACTIONS_ELEMENT);

			out.endTag("env:Body");
			out.endTag("env:Envelope");
			
			if (isDebugMode() && fileWriter != null) {
				// flush buffer of TagWriter to ensure content is flushed to fileWriter
				out.flushBuffer();
				try {
					fileWriter.close();
				} catch (IOException ex) {
					// nothing to do: debug code
				}
			}
		} catch (IOException ex) {
			throw new FatalXMLError(ex);
		}
	}

	private void beginResponse() throws IOException {
		out.writeXMLHeader(_encoding);

		out.beginBeginTag("env:Envelope");
		out.writeAttribute("xmlns", HTMLConstants.XHTML_NS);
		out.writeAttribute("xmlns:env", "http://www.w3.org/2001/12/soap-envelope");
		out.writeAttribute(AJAXConstants.AJAX_XMLNS_ATTRIBUTE, AJAXConstants.AJAX_NAMESPACE);
		out.endBeginTag();
		out.beginTag("env:Body");

		out.beginBeginTag(AJAXConstants.AJAX_ACTIONS_ELEMENT);
		if (sequence != null) {
			out.writeAttribute(AJAXConstants.RX_ATTRIBUTE, sequence.intValue());
			out.writeAttribute(AJAXConstants.REQUEST_SOURCE_REFERENCE_ATTRIBUTE, requestSourceContext);
			if (dropped) {
				out.writeAttribute(AJAXConstants.DROPPED_ATTRIBUTE, AJAXConstants.DROPPED_ATTRIBUTE_VALUE);
			}
		}
		out.endBeginTag();
	}

	/**
	 * The class {@link UpdateWriter.FatalXMLError} is a
	 * {@link RuntimeException} which indicates that the response stream is
	 * totally destroyed and not reparable.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static class FatalXMLError extends RuntimeException {

		public FatalXMLError(Throwable cause) {
			super(cause);
		}
	}

}

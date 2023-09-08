/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import test.com.top_logic.basic.AssertNoErrorLogListener;
import test.com.top_logic.basic.StringWriterNonNull;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateListener;
import com.top_logic.layout.UpdateWriter;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.component.ControlSupport;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The class {@link TestControl} is a superclass which provides methods for handling {@link Control}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TestControl extends AbstractLayoutTest {

	protected boolean hasUpdates(Control control) {
		// TODO: Should be routed through ControlSupport. 
		return ((UpdateListener) control).isInvalid();
	}

	/**
	 * This method executes the given command on the given {@link Control}. It is necessary to write
	 * the control before using {@link #tryWritingControl(Control)} or
	 * {@link #writeControl(Control)}.
	 * 
	 * @param ctrl
	 *        the {@link Control} whose {@link ControlCommand} shall be executed.
	 * @param command
	 *        the name of the command to execute.
	 * @param arguments
	 *        the arguments which are necessary to execute the given command.
	 * @return the {@link HandlerResult} returned by the control command.
	 * @throws IllegalStateException
	 *         if the control was not rendered before.
	 */
	protected HandlerResult executeControlCommand(Control ctrl, String command, Map<String, Object> arguments) {
		if (displayContext() == null) {
			throw new IllegalStateException("Control '" + ctrl + "' must be written before executing command");
		}
		ControlScope scope = displayContext().getExecutionScope();
		assertInstanceof(scope, ControlSupport.class);

		return ((ControlSupport) scope).executeCommand(displayContext(), ctrl.getID(), command, arguments);
	}

	/**
	 * This method writes the given {@link Control}
	 * 
	 * @param ctrl
	 *            the Control to write.
	 * @return the resulting output of the rendering process.
	 * @see Control#write(DisplayContext, TagWriter)
	 */
	protected String tryWritingControl(Control ctrl) throws IOException, AssertionFailedError {
		StringWriter stringWriter = new StringWriterNonNull();
		TagWriter out = new TagWriter(stringWriter);
		AssertNoErrorLogListener logListener = new AssertNoErrorLogListener(true);
		logListener.activate();
		try {
			ctrl.write(displayContext(), out);
			logListener.assertNoErrorLogged("Error when writing control");
		} finally {
			logListener.deactivate();
		}
		assertTrue(ctrl.isAttached());
		if (ctrl instanceof AbstractControlBase) {
			assertFalse("Ctrl " + ctrl + " is invalid directly after rendering", hasUpdates(ctrl));
		}
		return stringWriter.getBuffer().toString();
	}

	/**
	 * This method is a service method to write the given control, but catches all exceptions which
	 * potentially fall.
	 * 
	 * @see #tryWritingControl(Control)
	 */
	protected final String writeControl(Control ctrl) {
		try {
			return tryWritingControl(ctrl);
		} catch (IOException ex) {
			throw (AssertionFailedError) new AssertionFailedError("'" + IOException.class + "' during writing '" + ctrl + "': " + ex.getMessage()).initCause(ex);
		}
	}

	protected Document getControlDocument(AbstractControlBase control) {
		if (control.isAttached() && hasUpdates(control)) {
			StringWriter buffer = new StringWriterNonNull();
			revalidate(control, buffer);
				
			// TODO: Evaluate result.
			parse(buffer.toString());
		}
		Document controlDisplay = createControlDocument(control);
		return controlDisplay;
	}

	protected void revalidate(AbstractControlBase control, StringWriter out) throws AssertionError {
		TagWriter responseWriter = new TagWriter(out);
		
		{
			UpdateWriter updates =
				new UpdateWriter(displayContext(), responseWriter, LayoutConstants.UTF_8, Integer.valueOf(42));
			control.revalidate(displayContext(), updates);
			updates.endResponse();
		}
		
		try {
			responseWriter.flush();
			responseWriter.close();
		} catch (IOException ex) {
			throw (AssertionError) new AssertionError("Flush failed.").initCause(ex);
		}
	}

	private Document createControlDocument(Control control) {
		String controlOutput = writeControl(control);

		/* must surround by an element which declares the namespaces eventually written within the
		 * control */
		StringBuilder enhancedXML = new StringBuilder("<?xml version=\"1.0\" encoding=\"");
		enhancedXML = appendEncoding(enhancedXML);
		enhancedXML.append("\" ?>");
		enhancedXML.append("<test:root xmlns:test=\"http://fake.test.com\" ");
		appendNameSpaceDeclarations(enhancedXML);
		enhancedXML.append('>');
		enhancedXML.append(controlOutput);
		enhancedXML.append("</test:root>");
		return parse(enhancedXML.toString());
	}

	/**
	 * Appends declaration of namespaces eventually used by the control append must occur in form
	 * 'xmlns:xxx="yyy" '.
	 * 
	 * @param builder
	 *        the builder to append declaration
	 */
	protected void appendNameSpaceDeclarations(StringBuilder builder) {
		builder.append("xmlns:bar=\"http://fake.test.com\" ");
	}

	/**
	 * The encoding used when writing the control.
	 */
	protected StringBuilder appendEncoding(StringBuilder builder) {
		return builder.append("utf-8");
	}

	private Document parse(String controlOutput) {
		try {
			DocumentBuilder builder = DOMUtil.newDocumentBuilderNamespaceAware();
			Document controlDocument = builder.parse(new InputSource(new StringReader(controlOutput)));
			return controlDocument;
		} catch (FactoryConfigurationError e) {
			throw new AssertionError(e);
		} catch (ParserConfigurationException e) {
			throw new AssertionError(e);
		} catch (SAXException e) {
			throw new AssertionError(e);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
	
	/**
	 * Factory method to create test for the given class
	 * 
	 * @param testClass
	 *        the class under test. Must not be <code>null</code>.
	 */
	public static Test suite(Class<? extends Test> testClass) {
		return suite(new TestSuite(testClass));
	}

	public static Test suite(Test test) {
		return AbstractLayoutTest.suite(ServiceTestSetup.createSetup(test, LabelProviderService.Module.INSTANCE,
			CommandHandlerFactory.Module.INSTANCE, SafeHTML.Module.INSTANCE));
	}

}

/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.mig.html.layout.demo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.base.services.simpleajax.AJAXCommandScriptWriter;
import com.top_logic.base.services.simpleajax.AJAXConstants;
import com.top_logic.base.services.simpleajax.AJAXServlet;
import com.top_logic.base.services.simpleajax.ContentReplacement;
import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.FragmentInsertion;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.base.services.simpleajax.RangeReplacement;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.demo.layout.form.demo.TracLinks;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.component.AJAXSupport;
import com.top_logic.layout.basic.component.BasicAJAXSupport;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandScriptWriter;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link LayoutComponent} demonstrating basic AJAX interactions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AJAXDemo extends BoundComponent implements HTMLConstants {

	static final String ORIG_CSS_CLASS = "orig";

	static final String UPDATED_CSS_CLASS = "updated";

	static final String FRAGMENT_INSERTION_TARGET = "targetID";

	static final String FRAGMENT_INSERTION_SCRIPT_TARGET = "FragmentInsertionScriptTarget";

	static final String ELEMENT_REPLACEMENT_TARGET = "targetIDElementReplacement";

	static final String ELEMENT_REPLACEMENT_SCRIPT_TARGET = "ElementReplacementScriptTarget";

	static final String ELEMENT_REPLACEMENT_AFTER = "targetIDElementReplacementAfterReplacement";

	static final String CONTENT_REPLACEMENT_TARGET = "targetIDContentReplacement";

	static final String CONTENT_REPLACEMENT_SCRIPT_TARGET = "ContentReplacementScriptTarget";

	static final String CONTENT_REPLACEMENT_CONTENT = "contentReplacementContent";

	static final String FRAGMENT_INSERTION_BEFORE = "targetIDbefore";

	static final String FRAGMENT_INSERTION_AFTER = "targetIDafter";

	static final String RANGE_REPLACEMENT_START = "startIDRangeReplacement";

	static final String RANGE_REPLACEMENT_CONTENT = "rangeReplacementContent";

	static final String RANGE_REPLACEMENT_STOP = "stopIDRangeReplacement";

	static final String RANGE_REPLACEMENT_SCRIPT_TARGET1 = "RangeReplacementScriptTarget1";
	
	static final String RANGE_REPLACEMENT_SCRIPT_TARGET2 = "RangeReplacementScriptTarget2";

	static final String FRAGMENT_INSERTION_TD = "fragmentInsertionTd";

	static final String FRAGMENT_INSERTION_TR = "fragmentInsertionTr";

	static final String FRAGMENT_INSERTION_TBODY = "fragmentInsertionTbody";

	static final String RANGE_REPLACEMENT_TD1 = "rangeReplacementTd1";

	static final String RANGE_REPLACEMENT_TD2 = "rangeReplacementTd2";

	static final String RANGE_REPLACEMENT_TD3 = "rangeReplacementTd3";

	static final String RANGE_REPLACEMENT_TR1 = "rangeReplacementTr1";

	static final String RANGE_REPLACEMENT_TR2 = "rangeReplacementTr2";

	static final String RANGE_REPLACEMENT_TR3 = "rangeReplacementTr3";

	static final String RANGE_REPLACEMENT_TBODY1 = "rangeReplacementTbody1";

	static final String RANGE_REPLACEMENT_TBODY2 = "rangeReplacementTbody2";

	static final String RANGE_REPLACEMENT_TBODY3 = "rangeReplacementTbody3";

	static final String ON_LOAD_UPDATE_TARGET1 = "onLoadUpdate1";

	static final String ON_LOAD_UPDATE_TARGET2 = "onLoadUpdate2";

	static final String MESSAGE_AREA = "msg";

	public static final class IllegalAjaxRequest extends AJAXCommandHandler {

		public static final String COMMAND_ID = "illegalAjaxRequest";

		public IllegalAjaxRequest(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			// Unreachable.
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public CommandScriptWriter getCommandScriptWriter(LayoutComponent component) {
			return new AJAXCommandScriptWriter() {
				@Override
				protected void appendCallStatement(
						LayoutComponent component, PrintWriter anOut,
						CommandHandler command, String argumentObject) {

					anOut.write("services.ajax.execute(");
					TagUtil.writeJsString(anOut,
						// Commands may not contain XML specials, to trigger an error during command
						// transmission, use something other than command.getID().
						"<wrong>");
					anOut.write(',');
					anOut.write(argumentObject);
					anOut.write("); ");

					anOut.write("return false;");
				}
			};
		}
	}

	public static final class IllegalAjaxResponse extends AJAXCommandHandler {
		public static final String COMMAND_ID = "illegalAjaxResponse";

		public IllegalAjaxResponse(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
				Object model, Map<String, Object> someArguments) {
			AJAXDemo demo = (AJAXDemo) aComponent;
			demo.ajaxSupport.add(new ElementReplacement(ELEMENT_REPLACEMENT_TARGET, new HTMLFragment() {
				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
					out.beginBeginTag(DIV);
					out.writeAttribute(ID_ATTR, ELEMENT_REPLACEMENT_TARGET);
					out.endBeginTag();
					// Note: "Undefined" are no longer a problem, since HTML content is no longer
					// transmitted as XML.
					out.writeContent("Some contents containing an illegal entity (&nbsp;).");
					out.endTag(DIV);
				}
			}) {
				@Override
				protected void writeChildrenAsXML(DisplayContext context, TagWriter out) throws IOException {
					super.writeChildrenAsXML(context, out);
					out.writeContent("<this is invaid xml>");
				}
			});

			demo.markAsValid();

			return HandlerResult.DEFAULT_RESULT;
		}
	}

	public static final class NoAjaxResponse extends AJAXCommandHandler {
		public static final String COMMAND_ID = "noAjaxResponse";

		public NoAjaxResponse(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
				Object model, Map<String, Object> someArguments) {
			AJAXDemo demo = (AJAXDemo) aComponent;
			demo.ajaxSupport.add(new JSSnipplet("alert('Should never be displayed.')") {

				@Override
				protected void writeChildrenAsXML(DisplayContext context, TagWriter writer) throws IOException {
					super.writeChildrenAsXML(context, writer);

					context.asResponse().setHeader(AJAXServlet.HEADER_X_RESPONSE_SERVER, "NoAJAXResponse");
				}

			});
			demo.markAsValid();

			return HandlerResult.DEFAULT_RESULT;
		}
	}

	public static final class TestCommand extends AJAXCommandHandler {

		public static final String COMMAND_ID = "testCommand";
		
		public TestCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext context, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			Logger.info("Received AJAX command: command=" + this.getID(), this);
			AJAXDemo demo = (AJAXDemo) aComponent;
			demo.ajaxSupport.add(new FragmentInsertion(FRAGMENT_INSERTION_TARGET, AJAXConstants.AJAX_POSITION_BEFORE_VALUE,
				new HTMLFragment() {
				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
					out.beginBeginTag(DIV);
					out.writeAttribute(ID_ATTR, FRAGMENT_INSERTION_BEFORE);
						out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
					out.endBeginTag();
					out.writeText("FragmentInsertion Before: ");
					out.beginBeginTag(ANCHOR);
					out.writeAttribute(HREF_ATTR, "http://www.top-logic.com/");
					out.endBeginTag();
					out.writeText("<i>TopLogic</i>!");
						out.endTag(ANCHOR);
					out.endTag(DIV);

					out.beginScript();
						out.write("document.getElementById('" + FRAGMENT_INSERTION_SCRIPT_TARGET + "')."
							+ CLASS_PROPERTY + "='" + UPDATED_CSS_CLASS + "';");
					out.endScript();
				}
			}));
			demo.ajaxSupport.add(new FragmentInsertion(FRAGMENT_INSERTION_TARGET, AJAXConstants.AJAX_POSITION_AFTER_VALUE,
				new HTMLFragment() {
				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
					out.beginBeginTag(DIV);
					out.writeAttribute(ID_ATTR, FRAGMENT_INSERTION_AFTER);
						out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
					out.endBeginTag();
					out.writeText("FragmentInsertion After: ");
					out.beginBeginTag(ANCHOR);
					out.writeAttribute(HREF_ATTR, "http://www.top-logic.com/");
					out.endBeginTag();
					out.writeText("<i>TopLogic</i>!");
						out.endTag(ANCHOR);
					out.endTag(DIV);
				}
			}));
			demo.ajaxSupport.add(new FragmentInsertion(FRAGMENT_INSERTION_TD, AJAXConstants.AJAX_POSITION_BEFORE_VALUE,
				new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						out.beginBeginTag(TD);
						out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
						out.endBeginTag();
						out.writeText("Before td");
						out.endTag(TD);
					}
				}));
			demo.ajaxSupport.add(new FragmentInsertion(FRAGMENT_INSERTION_TD, AJAXConstants.AJAX_POSITION_AFTER_VALUE,
				new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						out.beginBeginTag(TD);
						out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
						out.endBeginTag();
						out.writeText("After td");
						out.endTag(TD);
					}
				}));
			demo.ajaxSupport.add(new FragmentInsertion(FRAGMENT_INSERTION_TR, AJAXConstants.AJAX_POSITION_START_VALUE,
				new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						out.beginBeginTag(TD);
						out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
						out.endBeginTag();
						out.writeText("Start tr");
						out.endTag(TD);
					}
				}));
			demo.ajaxSupport.add(new FragmentInsertion(FRAGMENT_INSERTION_TR, AJAXConstants.AJAX_POSITION_END_VALUE,
				new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						out.beginBeginTag(TD);
						out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
						out.endBeginTag();
						out.writeText("End tr");
						out.endTag(TD);
					}
				}));

			demo.ajaxSupport.add(new FragmentInsertion(FRAGMENT_INSERTION_TR, AJAXConstants.AJAX_POSITION_BEFORE_VALUE,
				new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						out.beginTag(TR);
						out.beginBeginTag(TD);
						out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
						out.writeAttribute(COLSPAN_ATTR, 5);
						out.endBeginTag();
						out.writeText("Before tr");
						out.endTag(TD);
						out.endTag(TR);
					}
				}));
			demo.ajaxSupport.add(new FragmentInsertion(FRAGMENT_INSERTION_TR, AJAXConstants.AJAX_POSITION_AFTER_VALUE,
				new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						out.beginTag(TR);
						out.beginBeginTag(TD);
						out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
						out.writeAttribute(COLSPAN_ATTR, 5);
						out.endBeginTag();
						out.writeText("After tr");
						out.endTag(TD);
						out.endTag(TR);
					}
				}));
			demo.ajaxSupport.add(new FragmentInsertion(FRAGMENT_INSERTION_TBODY, AJAXConstants.AJAX_POSITION_START_VALUE,
				new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						out.beginTag(TR);
						out.beginBeginTag(TD);
						out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
						out.writeAttribute(COLSPAN_ATTR, 5);
						out.endBeginTag();
						out.writeText("Start tbody");
						out.endTag(TD);
						out.endTag(TR);
					}
				}));
			demo.ajaxSupport.add(new FragmentInsertion(FRAGMENT_INSERTION_TBODY, AJAXConstants.AJAX_POSITION_END_VALUE,
				new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						out.beginTag(TR);
						out.beginBeginTag(TD);
						out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
						out.writeAttribute(COLSPAN_ATTR, 5);
						out.endBeginTag();
						out.writeText("End tbody");
						out.endTag(TD);
						out.endTag(TR);
					}
				}));

			demo.ajaxSupport.add(new ElementReplacement(FRAGMENT_INSERTION_TD,
				new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						out.beginBeginTag(TD);
						out.writeAttribute(ID_ATTR, FRAGMENT_INSERTION_TD);
						out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
						out.endBeginTag();
						out.writeText("New cell");
						out.endTag(TD);
					}
				}));

			demo.ajaxSupport.add(new FragmentInsertion(FRAGMENT_INSERTION_TD, AJAXConstants.AJAX_POSITION_START_VALUE,
				new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						out.beginTag(SPAN);
						out.writeText("(before) ");
						out.endTag(SPAN);
					}
				}));
			demo.ajaxSupport.add(new FragmentInsertion(FRAGMENT_INSERTION_TD, AJAXConstants.AJAX_POSITION_END_VALUE,
				new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						out.beginTag(SPAN);
						out.writeText(" (after)");
						out.endTag(SPAN);
					}
				}));

			demo.ajaxSupport.add(new RangeReplacement(RANGE_REPLACEMENT_TD1, RANGE_REPLACEMENT_TD2,
				new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						out.beginBeginTag(TD);
						out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
						out.endBeginTag();
						out.writeText("Replaced TD range.");
						out.endTag(TD);
					}
				}));
			
			demo.ajaxSupport.add(new RangeReplacement(RANGE_REPLACEMENT_TR1, RANGE_REPLACEMENT_TR2,
				new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						out.beginTag(TR);
						out.beginBeginTag(TD);
						out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
						out.writeAttribute(COLSPAN_ATTR, 2);
						out.endBeginTag();
						out.writeText("Replaced TR range.");
						out.endTag(TD);
						out.endTag(TR);
					}
				}));

			demo.ajaxSupport.add(new RangeReplacement(RANGE_REPLACEMENT_TBODY1, RANGE_REPLACEMENT_TBODY2,
				new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						out.beginTag(TBODY);
						out.beginTag(TR);
						out.beginBeginTag(TD);
						out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
						out.writeAttribute(COLSPAN_ATTR, 2);
						out.endBeginTag();
						out.writeText("Replaced TBODY range.");
						out.endTag(TD);
						out.endTag(TR);
						out.endTag(TBODY);
					}
				}));

			demo.ajaxSupport.add(new ElementReplacement(RANGE_REPLACEMENT_TD3,
				new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						out.beginBeginTag(TD);
						out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
						out.endBeginTag();
						out.writeText("Replaced single TD.");
						out.endTag(TD);
					}
				}));
			
			demo.ajaxSupport.add(new ElementReplacement(RANGE_REPLACEMENT_TR3,
				new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						out.beginTag(TR);
						out.beginBeginTag(TD);
						out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
						out.writeAttribute(COLSPAN_ATTR, 2);
						out.endBeginTag();
						out.writeText("Replaced single TR.");
						out.endTag(TD);
						out.endTag(TR);
					}
				}));
			
			demo.ajaxSupport.add(new ElementReplacement(RANGE_REPLACEMENT_TBODY3,
				new HTMLFragment() {
					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						out.beginTag(TBODY);
						out.beginTag(TR);
						out.beginBeginTag(TD);
						out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
						out.writeAttribute(COLSPAN_ATTR, 2);
						out.endBeginTag();
						out.writeText("Replaced single TBODY.");
						out.endTag(TD);
						out.endTag(TR);
						out.endTag(TBODY);
					}
				}));

			demo.ajaxSupport.add(new PropertyUpdate(FRAGMENT_INSERTION_TARGET, HTMLConstants.CLASS_PROPERTY,
				new ConstantDisplayValue(UPDATED_CSS_CLASS)));

			demo.ajaxSupport.add(new ElementReplacement(ELEMENT_REPLACEMENT_TARGET, new HTMLFragment() {
				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
					out.beginBeginTag(DIV);
					out.writeAttribute(ID_ATTR, ELEMENT_REPLACEMENT_AFTER);
					out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
					out.endBeginTag();
					out.writeText("ElementReplacement:");
					out.endTag(DIV);
			
					out.beginBeginTag(DIV);
					out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
					out.endBeginTag();
					out.writeText("Content being replaced in the following action.");
					out.endTag(DIV);
			
					out.beginBeginTag(DIV);
					out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
					out.endBeginTag();
					out.writeText("Replaced contents.");
					out.endTag(DIV);
			
					out.beginScript();
					out.write("document.getElementById('" + ELEMENT_REPLACEMENT_SCRIPT_TARGET + "')."
						+ CLASS_PROPERTY + "='" + UPDATED_CSS_CLASS + "';");
					out.endScript();
				}
			}));
			demo.ajaxSupport.add(new ContentReplacement(CONTENT_REPLACEMENT_TARGET, new HTMLFragment() {
				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
					out.beginBeginTag(DIV);
					out.writeAttribute(ID_ATTR, CONTENT_REPLACEMENT_CONTENT);
					out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
					out.endBeginTag();
					out.writeText("ContentReplacement:");
					out.endTag(DIV);
			
					out.beginBeginTag(ANCHOR);
					out.writeAttribute(HREF_ATTR, "http://www.top-logic.com/");
					out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
					out.endBeginTag();
					out.writeText("<i>TopLogic</i>!");
					out.endTag(ANCHOR);
			
					out.beginScript();
					out.write("document.getElementById('" + CONTENT_REPLACEMENT_SCRIPT_TARGET + "')."
						+ CLASS_PROPERTY + "='" + UPDATED_CSS_CLASS + "';");
					out.endScript();
				}
			}));
			demo.ajaxSupport.add(new PropertyUpdate(CONTENT_REPLACEMENT_TARGET, HTMLConstants.CLASS_PROPERTY,
				new ConstantDisplayValue("")));
			demo.ajaxSupport.add(new RangeReplacement(RANGE_REPLACEMENT_START, RANGE_REPLACEMENT_STOP,
				new HTMLFragment() {
				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
					out.beginBeginTag(DIV);
					out.writeAttribute(ID_ATTR, RANGE_REPLACEMENT_CONTENT);
						out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
					out.endBeginTag();
					out.writeText("RangeReplacement:");
					out.endTag(DIV);
			
					out.beginBeginTag(ANCHOR);
					out.writeAttribute(HREF_ATTR, "http://www.top-logic.com/");
					out.writeAttribute(CLASS_ATTR, UPDATED_CSS_CLASS);
					out.endBeginTag();
					out.writeText("<i>TopLogic</i>!");
						out.endTag(ANCHOR);
			
					out.beginScript();
					out.write("document.getElementById('" + RANGE_REPLACEMENT_SCRIPT_TARGET1 + "')."
							+ CLASS_PROPERTY + "='" + UPDATED_CSS_CLASS + "';");
					out.endScript();
					
					// Additional script in the same action.
					out.beginScript();
					out.write("document.getElementById('" + RANGE_REPLACEMENT_SCRIPT_TARGET2 + "')."
							+ CLASS_PROPERTY + "='" + UPDATED_CSS_CLASS + "';");
					out.endScript();
				}
			}));

			demo.ajaxSupport.add(new JSSnipplet("var ok = "
				+ "document.getElementById('" + FRAGMENT_INSERTION_SCRIPT_TARGET + "')." + CLASS_PROPERTY + "=='" + UPDATED_CSS_CLASS + "'"
				+ " && document.getElementById('" + ELEMENT_REPLACEMENT_SCRIPT_TARGET + "')." + CLASS_PROPERTY + "=='" + UPDATED_CSS_CLASS + "'"
				+ " && document.getElementById('" + CONTENT_REPLACEMENT_SCRIPT_TARGET + "')." + CLASS_PROPERTY + "=='" + UPDATED_CSS_CLASS + "'"
				+ " && document.getElementById('" + RANGE_REPLACEMENT_SCRIPT_TARGET1 + "')." + CLASS_PROPERTY + "=='" + UPDATED_CSS_CLASS + "'"
				+ " && document.getElementById('" + RANGE_REPLACEMENT_SCRIPT_TARGET2 + "')." + CLASS_PROPERTY + "=='" + UPDATED_CSS_CLASS + "'"
				+ " && document.getElementById('" + FRAGMENT_INSERTION_BEFORE + "') != null"
				+ " && document.getElementById('" + FRAGMENT_INSERTION_AFTER + "') != null"
				+ " && document.getElementById('" + ELEMENT_REPLACEMENT_TARGET + "') == null"
				+ " && document.getElementById('" + ELEMENT_REPLACEMENT_AFTER + "') != null"
				+ " && document.getElementById('" + CONTENT_REPLACEMENT_CONTENT + "') != null"
				+ " && document.getElementById('" + CONTENT_REPLACEMENT_TARGET + "') != null"
				+ " && document.getElementById('" + RANGE_REPLACEMENT_START + "') == null"
				+ " && document.getElementById('" + RANGE_REPLACEMENT_STOP + "') == null"
				+ " && document.getElementById('" + RANGE_REPLACEMENT_CONTENT + "') != null;"
				+ "alert(ok ? 'AJAX command successfully executed!' : 'AJAX command execution failed!');"));

			demo.markAsValid();

			return HandlerResult.DEFAULT_RESULT;
		}
	}

	protected BasicAJAXSupport ajaxSupport = new BasicAJAXSupport();

	public AJAXDemo(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	protected AJAXSupport ajaxSupport() {
		return ajaxSupport;
	}

	@Override
	public void writeHeader(String contextPath, TagWriter out, HttpServletRequest req) throws IOException {
		super.writeHeader(contextPath, out, req);

		out.beginBeginTag(STYLE_ELEMENT);
		out.writeAttribute(TYPE_ATTR, CSS_TYPE_VALUE);
		out.endBeginTag();
		out.writeText("table {cell-spacing: 3px; }");
		out.writeText("." + ORIG_CSS_CLASS + " {background-color: red; color: white; }");
		out.writeText("." + UPDATED_CSS_CLASS + " {background-color: #0F0; }");
		out.endTag(STYLE_ELEMENT);
	}

	@Override
	public void writeBody(ServletContext context, HttpServletRequest req, HttpServletResponse resp, TagWriter out) throws IOException, ServletException {
		HTMLUtil.beginBeginForm(out, "ajax-command-source", null);
		out.writeAttribute("onsubmit", 
			"services.ajax.execute(\'" + TestCommand.COMMAND_ID
				+ "\', {param0: \'value0\', param1: \'value1\'}); return false;");
		HTMLUtil.endBeginForm(out);

		out.beginTag(H2);
		out.writeText("Testfälle");
		out.endTag(H2);

		TracLinks.writeWikiLink(out, "Testfallkatalog/AJAXKommunikation");

		out.beginTag(H2);
		out.writeText("FragmentInsertion Test");
		out.endTag(H2);

		out.beginBeginTag(HTMLConstants.DIV);
		out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
		out.writeAttribute(HTMLConstants.ID_ATTR, FRAGMENT_INSERTION_TARGET);
		out.endBeginTag();
		out.writeText("Test for FragmentInsertion: This contents is dynamically replaced. Please press the button.");
		out.endTag(HTMLConstants.DIV);

		out.beginBeginTag(PARAGRAPH);
		out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
		out.writeAttribute(HTMLConstants.ID_ATTR, FRAGMENT_INSERTION_SCRIPT_TARGET);
		out.endBeginTag();
		out.writeText("Script execution.");
		out.endTag(PARAGRAPH);

		out.beginTag(H2);
		out.writeText("ElementReplacement Test");
		out.endTag(H2);

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
		out.writeAttribute(ID_ATTR, ELEMENT_REPLACEMENT_TARGET);
		out.endBeginTag();
		out.writeText("Test for ElementReplacement: This contents is dynamically replaced. Please press the button.");
		out.endTag(DIV);

		out.beginBeginTag(PARAGRAPH);
		out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
		out.writeAttribute(HTMLConstants.ID_ATTR, ELEMENT_REPLACEMENT_SCRIPT_TARGET);
		out.endBeginTag();
		out.writeText("Script execution.");
		out.endTag(PARAGRAPH);

		out.beginTag(H2);
		out.writeText("ContentReplacement Test");
		out.endTag(H2);

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
		out.writeAttribute(ID_ATTR, CONTENT_REPLACEMENT_TARGET);
		out.endBeginTag();
		out.writeText("Test for ContentReplacement: This contents is dynamically replaced. Please press the button.");
		out.endTag(DIV);

		out.beginBeginTag(PARAGRAPH);
		out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
		out.writeAttribute(HTMLConstants.ID_ATTR, CONTENT_REPLACEMENT_SCRIPT_TARGET);
		out.endBeginTag();
		out.writeText("Script execution.");
		out.endTag(PARAGRAPH);

		out.beginTag(H2);
		out.writeText("RangeReplacement Test");
		out.endTag(H2);

		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, RANGE_REPLACEMENT_START);
		out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
		out.endBeginTag();
		out.writeText("rangeReplacement start");
		out.endTag(DIV);

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
		out.endBeginTag();
		out.writeText("rangeReplacement middle");
		out.endTag(DIV);

		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, RANGE_REPLACEMENT_STOP);
		out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
		out.endBeginTag();
		out.writeText("rangeReplacement stop");
		out.endTag(DIV);

		out.beginBeginTag(PARAGRAPH);
		out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
		out.writeAttribute(HTMLConstants.ID_ATTR, RANGE_REPLACEMENT_SCRIPT_TARGET1);
		out.endBeginTag();
		out.writeText("Script execution.");
		out.endTag(PARAGRAPH);

		out.beginBeginTag(PARAGRAPH);
		out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
		out.writeAttribute(HTMLConstants.ID_ATTR, RANGE_REPLACEMENT_SCRIPT_TARGET2);
		out.endBeginTag();
		out.writeText("Additional script execution.");
		out.endTag(PARAGRAPH);
		
		out.beginTag(H2);
		out.writeText("Table Test");
		out.endTag(H2);

		out.beginTag(TABLE);
		{
			out.beginBeginTag(TBODY);
			out.writeAttribute(ID_ATTR, FRAGMENT_INSERTION_TBODY);
			out.endBeginTag();
			{
				out.beginBeginTag(TR);
				out.writeAttribute(ID_ATTR, FRAGMENT_INSERTION_TR);
				out.endBeginTag();
				{
					out.beginBeginTag(TD);
					out.writeAttribute(ID_ATTR, FRAGMENT_INSERTION_TD);
					out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
					out.endBeginTag();
					{
						out.writeText("Orig cell");
					}
					out.endTag(TD);
				}
				out.endTag(TR);
			}
			out.endTag(TBODY);
		}
		out.endTag(TABLE);

		out.beginTag(TABLE);
		{
			out.beginBeginTag(TBODY);
			out.endBeginTag();
			{
				out.beginBeginTag(TR);
				out.endBeginTag();
				{
					out.beginBeginTag(TD);
					out.writeAttribute(ID_ATTR, RANGE_REPLACEMENT_TD1);
					out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
					out.endBeginTag();
					{
						out.writeText(RANGE_REPLACEMENT_TD1);
					}
					out.endTag(TD);

					out.beginBeginTag(TD);
					out.writeAttribute(ID_ATTR, RANGE_REPLACEMENT_TD2);
					out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
					out.endBeginTag();
					{
						out.writeText(RANGE_REPLACEMENT_TD2);
					}
					out.endTag(TD);

					out.beginBeginTag(TD);
					out.writeAttribute(ID_ATTR, RANGE_REPLACEMENT_TD3);
					out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
					out.endBeginTag();
					{
						out.writeText(RANGE_REPLACEMENT_TD3);
					}
					out.endTag(TD);
				}
				out.endTag(TR);

				out.beginBeginTag(TR);
				out.writeAttribute(ID_ATTR, RANGE_REPLACEMENT_TR1);
				out.endBeginTag();
				{
					out.beginBeginTag(TD);
					out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
					out.writeAttribute(COLSPAN_ATTR, 3);
					out.endBeginTag();
					{
						out.writeText(RANGE_REPLACEMENT_TR1);
					}
					out.endTag(TD);
				}
				out.endTag(TR);
				
				out.beginBeginTag(TR);
				out.writeAttribute(ID_ATTR, RANGE_REPLACEMENT_TR2);
				out.endBeginTag();
				{
					out.beginBeginTag(TD);
					out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
					out.writeAttribute(COLSPAN_ATTR, 3);
					out.endBeginTag();
					{
						out.writeText(RANGE_REPLACEMENT_TR2);
					}
					out.endTag(TD);
				}
				out.endTag(TR);

				out.beginBeginTag(TR);
				out.writeAttribute(ID_ATTR, RANGE_REPLACEMENT_TR3);
				out.endBeginTag();
				{
					out.beginBeginTag(TD);
					out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
					out.writeAttribute(COLSPAN_ATTR, 3);
					out.endBeginTag();
					{
						out.writeText(RANGE_REPLACEMENT_TR3);
					}
					out.endTag(TD);
				}
				out.endTag(TR);
			}
			out.endTag(TBODY);

			out.beginBeginTag(TBODY);
			out.writeAttribute(ID_ATTR, RANGE_REPLACEMENT_TBODY1);
			out.endBeginTag();
			{
				out.beginBeginTag(TR);
				out.endBeginTag();
				{
					out.beginBeginTag(TD);
					out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
					out.writeAttribute(COLSPAN_ATTR, 3);
					out.endBeginTag();
					{
						out.writeText(RANGE_REPLACEMENT_TBODY1);
					}
					out.endTag(TD);
				}
				out.endTag(TR);
			}
			out.endTag(TBODY);

			out.beginBeginTag(TBODY);
			out.writeAttribute(ID_ATTR, RANGE_REPLACEMENT_TBODY2);
			out.endBeginTag();
			{
				out.beginBeginTag(TR);
				out.endBeginTag();
				{
					out.beginBeginTag(TD);
					out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
					out.writeAttribute(COLSPAN_ATTR, 3);
					out.endBeginTag();
					{
						out.writeText(RANGE_REPLACEMENT_TBODY2);
					}
					out.endTag(TD);
				}
				out.endTag(TR);
			}
			out.endTag(TBODY);

			out.beginBeginTag(TBODY);
			out.writeAttribute(ID_ATTR, RANGE_REPLACEMENT_TBODY3);
			out.endBeginTag();
			{
				out.beginBeginTag(TR);
				out.endBeginTag();
				{
					out.beginBeginTag(TD);
					out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
					out.writeAttribute(COLSPAN_ATTR, 3);
					out.endBeginTag();
					{
						out.writeText(RANGE_REPLACEMENT_TBODY3);
					}
					out.endTag(TD);
				}
				out.endTag(TR);
			}
			out.endTag(TBODY);

		}
		out.endTag(TABLE);

		out.beginTag(H2);
		out.writeText("Load time");
		out.endTag(H2);

		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, ON_LOAD_UPDATE_TARGET1);
		out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
		out.endBeginTag();
		out.writeText("Updated during page load.");
		out.endTag(DIV);

		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, ON_LOAD_UPDATE_TARGET2);
		out.writeAttribute(CLASS_ATTR, ORIG_CSS_CLASS);
		out.endBeginTag();
		out.writeText("Also updated during page load.");
		out.endTag(DIV);

		out.beginTag(H2);
		out.writeText("Test Execution");
		out.endTag(H2);

		out.beginBeginTag(INPUT);
		out.writeAttribute("type", "submit");
		out.writeAttribute("value", "Test Ajax");
		out.endEmptyTag();

		out.beginTag(PARAGRAPH);

		out.beginBeginTag(HTMLConstants.BUTTON);
		out.writeAttribute(HTMLConstants.ONCLICK_ATTR, IllegalAjaxRequest.COMMAND_ID + "(); return false;");
		out.endBeginTag();
		out.writeText("Test illegal request");
		out.endTag(HTMLConstants.BUTTON);

		out.writeText(HTMLConstants.NBSP);

		out.beginBeginTag(HTMLConstants.BUTTON);
		out.writeAttribute(HTMLConstants.ONCLICK_ATTR, IllegalAjaxResponse.COMMAND_ID + "(); return false;");
		out.endBeginTag();
		out.writeText("Test illegal response");
		out.endTag(HTMLConstants.BUTTON);

		out.writeText(HTMLConstants.NBSP);

		out.beginBeginTag(HTMLConstants.BUTTON);
		out.writeAttribute(HTMLConstants.ONCLICK_ATTR, NoAjaxResponse.COMMAND_ID + "(); return false;");
		out.endBeginTag();
		out.writeText("Test no AJAX response");
		out.endTag(HTMLConstants.BUTTON);

		out.endTag(PARAGRAPH);

		HTMLUtil.endForm(out);

		HTMLUtil.beginBeginForm(out, "ajax-error-source", null);
		HTMLUtil.endBeginForm(out);
		{
			out.beginBeginTag(INPUT);
			out.writeAttribute("type", "submit");
			out.writeAttribute("value", "Trigger JavaScript error");
			out.writeAttribute("onclick", "setTimeout('var bar = this.foo.bar;', 200); return false;");
			out.endEmptyTag();
		}
		HTMLUtil.endForm(out);

		HTMLUtil.beginBeginForm(out, "ajax-info-source", null);
		HTMLUtil.endBeginForm(out);
		{
			out.beginBeginTag(INPUT);
			out.writeAttribute("id", MESSAGE_AREA);
			out.writeAttribute("type", "text");
			out.writeAttribute("value", "Log message text...");
			out.endEmptyTag();

			out.beginBeginTag(INPUT);
			out.writeAttribute("type", "submit");
			out.writeAttribute("value", "Log info to server");
			out.writeAttribute("onclick",
				"services.log.info(document.getElementById('" + MESSAGE_AREA + "').value); return false;");
			out.endEmptyTag();
		}
		HTMLUtil.endForm(out);

		out.beginScript();
		// Test whether the '&' special char is correctly rendered in a script that is executed at
		// load time.
		out.writeScript("if (true && true) {document.getElementById('" + ON_LOAD_UPDATE_TARGET1 + "').className='"
			+ UPDATED_CSS_CLASS + "';}");
		out.writeScript("document.getElementById('" + ON_LOAD_UPDATE_TARGET2 + "').className='" + UPDATED_CSS_CLASS
			+ "';");
		out.endScript();
	}

}

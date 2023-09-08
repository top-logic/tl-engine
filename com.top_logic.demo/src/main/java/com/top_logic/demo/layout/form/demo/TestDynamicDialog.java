/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DecoratedControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.DefaultButtonRenderer;
import com.top_logic.layout.form.control.FieldsetDecorator;
import com.top_logic.layout.form.control.FormGroupControl;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplate;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.form.template.FormTemplateControl;
import com.top_logic.layout.messagebox.AbstractFormDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBoxContentView;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Example for {@link AbstractFormDialog}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class TestDynamicDialog extends AbstractFormDialog {
	private static final String ADD = "add";

	private static final String INPUT = "input";

	private static final String ID_PREFIX = "e";

	protected static final Property<Integer> NEXT_ID = TypedAnnotatable.property(Integer.class, "nextId", Integer.valueOf(0));
	
	private final ControlProvider CP = new DefaultFormFieldControlProvider() {
		
		private FormTemplate template;

		@Override
		public Control visitCommandField(CommandField member, Void arg) {
			return new ButtonControl(member, DefaultButtonRenderer.INSTANCE);
		}
		
		@Override
		public Control visitFormGroup(FormGroup group, Void arg) {
			final com.top_logic.layout.form.template.FormGroupControl innerView = new com.top_logic.layout.form.template.FormGroupControl(group, getGroupTemplate());
			FormGroupControl result = new FormGroupControl(group);
			result.setCollapsible(true);
			result.setRenderer(
				new DecoratedControlRenderer<>(HTMLConstants.DIV, FieldsetDecorator.INSTANCE, new Renderer<Object>() {

				@Override
				public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
					innerView.write(context, out);
				}
				
			}));
			return result;
		};
		
		private FormTemplate getGroupTemplate() {
			if (template == null) {
				template = new FormTemplate(ResPrefix.NONE, this, true, DOMUtil.parse(""
						+ "<div" 
						+ 	" xmlns='" + HTMLConstants.XHTML_NS + "'"
						+ 	" xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'" 
						+ 	" xmlns:p='" + FormPatternConstants.PATTERN_NS + "'" 
						+ ">"
						+	"<div>"
					+ "<p:field name='" + INPUT + "'/>"
					+ "<p:field name='" + ADD + "'/>"
						+	"</div>"
						+	"<div>" 
						+		"<p:field name='list'>" 
						+			"<t:list>" 
						+				"<ul>"
						+					"<t:items>" 
						+						"<li>" 
						+							"<p:self />" 
						+						"</li>" 
						+					"</t:items>" 
						+				"</ul>" 
						+			"</t:list>" 
						+		"</p:field>" 
						+	"</div>" 
						+ "</div>"
				));
				
			}
			return template;
		}

	};

	TestDynamicDialog() {
		super(ResKey.text("Test Dynamic Dialog"), DisplayDimension.dim(600, DisplayUnit.PIXEL),
			DisplayDimension.dim(500, DisplayUnit.PIXEL));
	}

	@Override
	protected void fillFormContext(FormContext context) {
		context.addMember(new FormGroup("groups", context.getResources()));
		addGroup();
	}
	
	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(ResKey.text("addGroup"), new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				addGroup();
				return HandlerResult.DEFAULT_RESULT;
			}
		}));
		addCancel(buttons);
	}
	
	protected void addGroup() {
		FormContainer container = getFormContext().getContainer("groups");
		
		final FormGroup group = new FormGroup("group-" + nextId(container), container.getResources());
		{
			StringField input = FormFactory.newStringField(INPUT);
			input.setLabel("Input");
			group.addMember(input);
			CommandField add = FormFactory.newCommandField(ADD, new Command() {
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					FormContainer listRef = group.getContainer("list");
					listRef.addMember(FormFactory.newSelectField("entry-" + nextId(listRef), Arrays.asList("a", "b", "c")));
					return HandlerResult.DEFAULT_RESULT;
				}
			});
			add.setLabel("Add");
			group.addMember(add);
			group.addMember(new FormGroup("list", group.getResources()));
			if (container.get(NEXT_ID).intValue() % 3 == 0) {
				group.setCollapsed(true);
			}
			
		}
		container.addMember(group);
	}

	String nextId(FormMember member) {
		Integer id = member.get(NEXT_ID);
		int nextId = id + 1;
		member.set(NEXT_ID, nextId);
		return ID_PREFIX + id;
	}
	
	@Override
	protected FormTemplate getTemplate() {
		return defaultTemplate(DOMUtil.parse(""
			+ "<div"
            +	" xmlns='" + HTMLConstants.XHTML_NS + "'"
            +	" xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
            +	" xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
			+ ">"
			+	"<p:field name='groups'>"
			+	"<t:list>"
			+		"<div>"
			+		"<t:items>"
			+			"<div>"
			+				"<p:self>"
			+				"</p:self>"
			+			"</div>"
			+		"</t:items>"
			+		"</div>"
			+	"</t:list>"
			+	"</p:field>"
			+ "</div>"
		), true, ResPrefix.NONE);
	}
	
	@Override
	protected HTMLFragment createView() {
		return new MessageBoxContentView(new FormTemplateControl(getFormContext(), getTemplate()));
	}
	
	@Override
	protected ControlProvider getControlProvider() {
		return CP;
	}
}
/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.ListBuilder;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.IdentityAccessor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.SimpleAccessor;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.UINonBlocking;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.layout.form.constraints.CollectionSizeConstraint;
import com.top_logic.layout.form.constraints.IntRangeConstraint;
import com.top_logic.layout.form.constraints.NumbersOnlyConstraint;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.control.AbstractFormFieldControlBase;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ConstantField;
import com.top_logic.layout.form.model.ConstraintCheckingVetoListener;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.form.model.IntField;
import com.top_logic.layout.form.model.NodeGroupInitializer;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.model.ValueVetoListener;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.form.template.SimpleListControlProvider;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.component.AbstractTableFilterProvider;
import com.top_logic.layout.table.component.TableFilterProvider;
import com.top_logic.layout.table.filter.ConfiguredFilter;
import com.top_logic.layout.table.filter.StaticFilterWrapper;
import com.top_logic.layout.table.filter.StaticFilterWrapperViewBuilder;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.SelectionTableModel;
import com.top_logic.layout.table.model.TableConfig;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.layout.tree.NodeContext;
import com.top_logic.layout.tree.model.AbstractTLTreeNodeBuilder;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;
import com.top_logic.layout.tree.model.DefaultStructureTreeUIModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.renderer.ConfigurableTreeContentRenderer;
import com.top_logic.layout.tree.renderer.ConfigurableTreeRenderer;
import com.top_logic.layout.tree.renderer.TreeContentRenderer;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.I18NResourceProvider;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.filter.ConstantForwardFilter;

/**
 * The class {@link TestLayoutProblems} is a demo {@link FormComponent} to test
 * layout problems.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestLayoutProblems extends FormComponent {

	public TestLayoutProblems(InstantiationContext context, Config attr) throws ConfigurationException {
		super(context, attr);
	}

	@Override
	public FormContext createFormContext() {
		FormContext result = new FormContext(this);
		result.addMember(ticket_2437());
		result.addMember(ticket_2372());
		result.addMember(ticket_2470());
		result.addMember(ticket_2781());
		result.addMember(ticket_2850());
		result.addMember(ticket_3109());
		result.addMember(ticket_3132());
		result.addMember(ticket_3438());
		result.addMember(ticket_3687());
		result.addMember(ticket_3725());
		result.addMember(ticket_2602());
		result.addMember(ticket_4213());
		result.addMember(ticket_4175());
		result.addMember(ticket_4594());
		result.addMember(ticket_2823());
		result.addMember(ticket_5310());
		result.addMember(ticket_4829());
		result.addMember(ticket_6828());
		result.addMember(ticket_12066());
		result.addMember(ticket_16921());
		result.addMember(ticket_17893());
		result.addMember(ticket_18991());
		result.addMember(ticket_18271());
		result.addMember(ticket_18938());
		result.addMember(ticket_19467());
		result.addMember(ticket_24173());
		result.addMember(ticket_25739());
		return result;
	}

	private FormMember ticket_24173() {
		ResPrefix i18nPrefix = getResPrefix().append("ticket24173");
		FormGroup containerGroup = new FormGroup("Ticket_24173", i18nPrefix);

		CommandField reNewMainLayoutLayoutControl = FormFactory.newCommandField("renewControlField", new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				MainLayout mainLayout = getMainLayout();
				reNewLayoutControl(mainLayout);
				return HandlerResult.DEFAULT_RESULT;
			}

			private void reNewLayoutControl(MainLayout mainLayout) {
				try {
					Method method = MainLayout.class.getDeclaredMethod("createLayoutControl");
					method.setAccessible(true);
					method.invoke(mainLayout);
				} catch (Exception ex) {
					InfoService.showError(ResKey.text(ex.getMessage()));
				}
			}
		});

		containerGroup.addMember(reNewMainLayoutLayoutControl);

		return containerGroup;
	}

	private FormMember ticket_19467() {
		ResPrefix i18nPrefix = getResPrefix().append("ticket19467");
		FormGroup containerGroup = new FormGroup("Ticket_19467", i18nPrefix);

		class OrinaryListener implements ValueListener {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				DisplayContext context = DefaultDisplayContext.getDisplayContext();
				FrameScope frameScope = context.getLayoutContext().getMainLayout().getEnclosingFrameScope();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ex) {
					// ignore. go ahead.
				}
				StringBuilder message = new StringBuilder();
				message.append(field.getLabel());
				message.append(": Value changed: oldValue: '");
				message.append(oldValue);
				message.append("', newValue: '");
				message.append(newValue);
				message.append("'");
				frameScope.addClientAction(JSSnipplet.createAlert(message.toString()));
			}

		}

		@UINonBlocking
		class NonBlockingListener extends OrinaryListener {
			// overridden to add annotation.
		}

		StringField nonBlockingField = FormFactory.newStringField("nonBlockingField");
		nonBlockingField.addValueListener(new NonBlockingListener());
		StringField blockingField = FormFactory.newStringField("blockingField");
		blockingField.addValueListener(new OrinaryListener());
		containerGroup.addMember(blockingField);
		containerGroup.addMember(nonBlockingField);
		return containerGroup;
	}

	private FormMember ticket_18938() {
		ResPrefix i18nPrefix = getResPrefix().append("ticket18938");
		FormGroup containerGroup = new FormGroup("Ticket_18938", i18nPrefix);

		final String unselectableOption = "unselectable";
		String selectableOption = "selectable";
		List<String> options = Arrays.asList(selectableOption, unselectableOption);
		SelectField vetoField = FormFactory.newSelectField("vetoField", options);
		vetoField.setAsSingleSelection(selectableOption);
		vetoField.addValueVetoListener(new ValueVetoListener() {
			
			@Override
			public void checkVeto(FormField field, Object newValue) throws VetoException {
				if (Collections.singletonList(unselectableOption).equals(newValue)) {
					throw new VetoException() {

						@Override
						public void process(WindowScope window) {
							String message = "Option '" + unselectableOption + "' is not selectable by veto.";
							MessageBox.confirm(window, MessageType.INFO, message, MessageBox.button(ButtonType.OK));
						}
						
					};
				}
			}
		});
		
		containerGroup.addMember(vetoField);

		return containerGroup;
	}

	private FormMember ticket_18271() {
		ResPrefix i18nPrefix = getResPrefix().append("ticket18271");
		FormGroup containerGroup = new FormGroup("Ticket_18271", i18nPrefix);

		final StringField tooltipField = FormFactory.newStringField("valueAsTooltip");

		final TooltipFieldControl tooltipFieldControl = new TooltipFieldControl(tooltipField);
		tooltipField.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				tooltipFieldControl.requestRepaint();
			}
		});
		ConstantField constantField = new ConstantField("tooltipField", true) {

			@Override
			public Object visit(FormMemberVisitor v, Object arg) {
				return tooltipFieldControl;
			}
		};
		containerGroup.addMember(tooltipField);
		containerGroup.addMember(constantField);

		final StringField field1 = FormFactory.newStringField("valueAsError");
		field1.addConstraint(new AbstractConstraint() {
			@Override
			public boolean check(Object value) throws CheckException {
				if (StringServices.isEmpty(value)) {
					return true;
				}

				throw new CheckException("Non-empty value as constraint error: " + value);
			}
		});
		containerGroup.addMember(field1);

		final StringField field2 = FormFactory.newStringField("explicitError");
		field2.setValue("Explicit error message");
		containerGroup.addMember(field2);

		containerGroup.addMember(FormFactory.newCommandField("setValueAsError", new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				field2.check();
				field2.setError("Explicitly set error message: " + field2.getValue());
				return HandlerResult.DEFAULT_RESULT;
			}
		}));

		return containerGroup;
	}

	private FormMember ticket_18991() {
		ResPrefix i18nPrefix = getResPrefix().append("ticket18991");
		FormGroup containerGroup = new FormGroup("Ticket_18991", i18nPrefix);
		List<Object> options = new ArrayList<>();
		options.add("String 0");
		options.add("String 1");
		options.add(Integer.valueOf(0));
		options.add(Integer.valueOf(1));
		options.add(Boolean.TRUE);
		options.add(Boolean.FALSE);

		SelectField field =
			FormFactory.newSelectField("polymorphicField", options, FormFactory.MULTIPLE, !FormFactory.IMMUTABLE);
		field.setAsSelection(options);
		NoDefaultColumnAdaption tableConfig = new NoDefaultColumnAdaption() {

			// IGNORE FindBugs(UWF_NULL_FIELD): Introducing a context specific name for null
			// makes the code easier to understand.
			private final TableFilterProvider NO_TABLE_FILTER = null;

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				declareValueColumn(table);
				declareTypeColumn(table);
			}

			private void declareValueColumn(TableConfiguration table) {
				ColumnConfiguration selfColumn = table.declareColumn("value");
				selfColumn.setColumnLabel("Value");
				selfColumn.setAccessor(SimpleAccessor.INSTANCE);
				selfColumn.setFilterProvider(new AbstractTableFilterProvider() {

					@Override
					protected List<ConfiguredFilter> createFilterList(TableViewModel tableViewModel,
							String filterPosition) {
						ArrayList<ConfiguredFilter> filters = new ArrayList<>();
						filters.add(createSupportClassFilter(String.class));
						filters.add(createSupportClassFilter(Integer.class));
						filters.add(createSupportClassFilter(Boolean.class));
						return filters;
					}

					/**
					 * Filter that supports only objects of given {@link Class}.
					 */
					private ConfiguredFilter createSupportClassFilter(Class<?> acceptedClass) {
						DisplayValue filterDescription =
							new ConstantDisplayValue("Instances of " + acceptedClass.getName());
						List<Class<?>> supportedTypes = Collections.<Class<?>> singletonList(acceptedClass);
						StaticFilterWrapperViewBuilder viewBuilder = new StaticFilterWrapperViewBuilder();
						return new StaticFilterWrapper(FilterFactory.trueFilter(), filterDescription, supportedTypes,
							viewBuilder);
					}

				});
			}

			private void declareTypeColumn(TableConfiguration table) {
				ColumnConfiguration typeColumn = table.declareColumn("type");
				typeColumn.setColumnLabel("Type of value");
				typeColumn.setAccessor(javaClassOfValue());
				typeColumn.setComparator(compareClassByName());
				typeColumn.setFilterProvider(NO_TABLE_FILTER);
			}

			private ReadOnlyAccessor<Object> javaClassOfValue() {
				return new ReadOnlyAccessor<>() {

					@Override
					public Object getValue(Object object, String property) {
						if (object == null) {
							return null;
						}
						return object.getClass();
					}
				};
			}

			private Comparator<Class<?>> compareClassByName() {
				return new Comparator<>() {

					@Override
					public int compare(Class<?> o1, Class<?> o2) {
						return o1.getName().compareTo(o2.getName());
					}
				};
			}

		};
		field.setTableConfigurationProvider(tableConfig);
		containerGroup.addMember(field);
		return containerGroup;
	}

	private FormMember ticket_17893() {
		ResPrefix i18nPrefix = getResPrefix().append("ticket17893");
		FormGroup containerGroup = new FormGroup("Ticket_17893", i18nPrefix);
		StringField source = FormFactory.newStringField("source");
		final StringField target = FormFactory.newStringField("target");
		target.setDisabled(true);
		source.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				target.setValue(newValue);
			}
		});
		containerGroup.addMember(source);
		containerGroup.addMember(target);
		return containerGroup;
	}
	private FormMember ticket_16921() {
		ResPrefix i18nPrefix = getResPrefix().append("ticket16921");
		FormGroup containerGroup = new FormGroup("Ticket_16921", i18nPrefix);

		for (int i = 0; i < 3; i++) {
			FormGroup innerGroup = new FormGroup("group" + i, i18nPrefix);
			StringField input = FormFactory.newStringField("input");
			input.initializeField("value");
			innerGroup.addMember(input);
			containerGroup.addMember(innerGroup);
		}
		return containerGroup;
	}

	private FormMember ticket_12066() {
		final ResPrefix i18nPrefix = getResPrefix().append("ticket12066");
		final FormGroup ticketGroup = new FormGroup("Ticket_12066", i18nPrefix);

		final FormGroup toggleGroup = new FormGroup("toggleGroup", i18nPrefix.append(".toggleGroup"));
		final StringField input1 = FormFactory.newStringField("input");
		input1.initializeField("value");
		toggleGroup.addMember(input1);
		ticketGroup.addMember(toggleGroup);

		final FormGroup nontoggleGroup = new FormGroup("nonToggleGroup", i18nPrefix.append(".nonToggleGroup"));
		final StringField input2 = FormFactory.newStringField("input");
		input2.initializeField("value");
		nontoggleGroup.addMember(input2);
		ticketGroup.addMember(nontoggleGroup);

		ticketGroup.addMember(FormFactory.newCommandField("toggleButton", new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				toggleGroup.setVisible(!toggleGroup.isVisible());
				input2.setVisible(!input2.isVisible());
				return HandlerResult.DEFAULT_RESULT;
			}
		}));

		ticketGroup.addMember(FormFactory.newCommandField("updateButton", new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				input1.setValue(input1.getValue() + "x");
				input2.setValue(input2.getValue() + "x");
				return HandlerResult.DEFAULT_RESULT;
			}
		}));

		return ticketGroup;
	}

	private FormMember ticket_6828() {
		final ResPrefix i18nPrefix = getResPrefix().append("ticket6828");
		final FormGroup group = new FormGroup("Ticket_6828", i18nPrefix);
		List<String> options = new ListBuilder<String>().add("a").add("b").add("c").toList();

		Constraint constraint = new CollectionSizeConstraint(2);
		Constraint warning = new CollectionSizeConstraint(3);

		SelectField fieldWithSizeConstraint =
			FormFactory.newSelectField("selectWithConstraint1", options, true, Collections.emptyList(), false);
		fieldWithSizeConstraint.addConstraint(constraint);
		fieldWithSizeConstraint.addWarningConstraint(warning);
		group.addMember(fieldWithSizeConstraint);

		SelectField fieldWithSizeConstraint2 =
			FormFactory.newSelectField("selectWithConstraint2", options, true, Collections.emptyList(), false);
		fieldWithSizeConstraint2.addConstraint(constraint);
		fieldWithSizeConstraint2.addWarningConstraint(warning);
		fieldWithSizeConstraint2.addValueVetoListener(new ConstraintCheckingVetoListener());
		group.addMember(fieldWithSizeConstraint2);

		SelectField fieldWithSizeConstraint3 =
			FormFactory.newSelectField("selectWithConstraint3", options, !FormFactory.MULTIPLE,
				Collections.emptyList(), !FormFactory.IMMUTABLE);
		fieldWithSizeConstraint3.addConstraint(new CollectionSizeConstraint(1));
		group.addMember(fieldWithSizeConstraint3);

		return group;
	}

	private FormMember ticket_4829() {
		final ResPrefix i18nPrefix = getResPrefix().append("ticket4829");
		final FormGroup group = new FormGroup("Ticket_4829", i18nPrefix);
		StringField fieldWithLengthConstraint = FormFactory.newStringField("fieldWithLengthConstraint", "", false);

		fieldWithLengthConstraint.setControlProvider(new ControlProvider() {

			@Override
			public Control createControl(Object model, String style) {
				TextInputControl result = new TextInputControl((FormField) model);
				result.setMultiLine(true);
				return result;
			}
		});
		fieldWithLengthConstraint.addConstraint(new StringLengthConstraint(5, 20));

		group.addMember(fieldWithLengthConstraint);
		return group;
	}
	private FormMember ticket_5310() {
		final ResPrefix i18nPrefix = getResPrefix().append("ticket5310");
		final FormGroup group = new FormGroup("Ticket_5310", i18nPrefix);
		TLTreeModel applicationModel = new DefaultMutableTLTreeModel(INFINITE_TREE, null);
		TreeUIModel treeModel = new DefaultStructureTreeUIModel(applicationModel );
		NodeGroupInitializer nodeGroupProvider = new NodeGroupInitializer() {
			
			@Override
			public void createNodeGroup(FormTree formTree, FormGroup nodeGroup, Object node) {
				BooleanField booleanField = FormFactory.newBooleanField("booleanField", Boolean.FALSE, false);
				nodeGroup.addMember(booleanField);
				
			}
		};
		final FormTree tree = new FormTree("tree", group.getResources(), treeModel, nodeGroupProvider);
		TreeContentRenderer contentRenderer = new ConfigurableTreeContentRenderer() {

			@Override
			public void writeNodeContent(DisplayContext context, TagWriter writer, NodeContext nodeContext)
					throws IOException {
				super.writeNodeContent(context, writer, nodeContext);

				final Object currentNode = nodeContext.currentNode();
				final Object userObject = nodeContext.getTree().getData().getTreeModel().getUserObject(currentNode);
				final FormMember booleanField = ((FormGroup) userObject).getMember("booleanField");
				DefaultFormFieldControlProvider.INSTANCE.createControl(booleanField).write(context, writer);
			}
			
		};
		tree.setTreeRenderer(new ConfigurableTreeRenderer(HTMLConstants.DIV, HTMLConstants.DIV, contentRenderer));
		group.addMember(tree);


		Command executable = new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {

				// first collapse root
				tree.getTreeModel().setExpanded(tree.getTreeModel().getRoot(), false);

				// then change all values
				final Iterator<? extends FormField> allFields = tree.getDescendantFields();
				while (allFields.hasNext()) {
					final BooleanField booleanField = (BooleanField) allFields.next();
					booleanField.setValue(!booleanField.getAsBoolean());
				}
				return HandlerResult.DEFAULT_RESULT;
			}
		};
		CommandField command = FormFactory.newCommandField("command", executable);
		group.addMember(command);
		return group;
	}

	private FormMember ticket_25739() {
		final ResPrefix i18nPrefix = getResPrefix().append("ticket25739");
		final FormGroup group = new FormGroup("Ticket_25739", i18nPrefix);

		IntField waitingField = FormFactory.newIntField("waitingTime");
		waitingField.initializeField(3000);
		waitingField.addConstraint(new IntRangeConstraint(0, 10000));
		group.addMember(waitingField);

		final List<String> rows = new ArrayList<>();
		rows.add("a1");
		rows.add("a2");
		String columnName = "col";
		TableConfiguration config = TableConfigurationFactory.table();
		ColumnConfiguration column = config.declareColumn(columnName);
		column.setAccessor(IdentityAccessor.INSTANCE);
		column.setColumnLabel("Column");
		ObjectTableModel tableModel = new ObjectTableModel(Collections.singletonList(columnName),
			config, rows);
		TableField table = FormFactory.newTableField("table", tableModel);
		table.setSelectable(true);

		table.getSelectionModel().addSelectionListener(new SelectionListener() {
			
			@Override
			public void notifySelectionChanged(SelectionModel model, Set<?> oldSelection, Set<?> newSelection) {
				if (waitingField.hasValue() && !waitingField.hasError() && waitingField.hasInt()) {
					try {
						Thread.sleep(waitingField.getAsInteger());
					} catch (InterruptedException ex) {
						throw new RuntimeException(ex);
					}
				}
			}
		});
		group.addMember(table);
		return group;
	}
	private FormMember ticket_2823() {
		final ResPrefix i18nPrefix = getResPrefix().append("ticket2823");
		final FormGroup group = new FormGroup("Ticket_2823", i18nPrefix);
		int size = 30;
		final List<String> options1 = new ArrayList<>(2 * size);
		for (int i = 0; i < 2 * size; i++) {
			options1.add("a_" + i);
		}
		final List<String> options2 = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			options2.add("a_" + i);
		}
		final SelectField selectField = FormFactory.newSelectField("select", options1, true, options1, false);
		TableConfig config = TableConfigurationFactory.tableConfig();
		config.setPageSizeOptions(5);
		config.setResPrefix(I18NConstants.TICKET_2823_TABLE);
		final TableConfig tableConfig = config;
		selectField.setTableConfigurationProvider(TableConfigurationFactory.toProvider(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, tableConfig));
		group.addMember(selectField);
		final CommandField setOptions = new CommandField("setOptions") {

			boolean setLargeList = false;

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				if (setLargeList) {
					selectField.setOptions(options1);
					setLabel(ResKey.text("Set small options"));
				} else {
					selectField.setOptions(options2);
					setLabel(ResKey.text("Set large options"));
				}
				setLargeList = !setLargeList;
				return HandlerResult.DEFAULT_RESULT;
			}
		};
		setOptions.setLabel(ResKey.text("Set small options"));
		group.addMember(setOptions);
		final CommandField setValue = new CommandField("setValue") {

			boolean setLargeList = false;

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				if (setLargeList) {
					selectField.setValue(options1);
					setLabel(ResKey.text("Set small value"));
				} else {
					selectField.setValue(options2);
					setLabel(ResKey.text("Set large value"));
				}
				setLargeList = !setLargeList;
				return HandlerResult.DEFAULT_RESULT;
			}
		};
		setValue.setLabel(ResKey.text("Set small value"));
		group.addMember(setValue);
		return group;
	}

	private ResourceProvider images(final Map<?, ThemeImage> images) {
		return new DefaultResourceProvider() {
			@Override
			public ThemeImage getImage(Object object, Flavor aFlavor) {
				return images.get(object);
			}
		};
	}

	private FormMember ticket_4594() {
		final ResPrefix i18nPrefix = getResPrefix().append("ticket4594");
		final FormGroup group = new FormGroup("Ticket_4594", i18nPrefix);
		group.addMember(FormFactory.newSelectField("select", Arrays.asList("a", "b", "c"), false, Collections.emptyList(),
			false));
		group.addMember(FormFactory.newSelectField("selectPopup", Arrays.asList("abcdefg", "bcdefg", "cdefg"), false,
			Collections.emptyList(), false));
		group.addMember(FormFactory.newSelectField("selectOption", Arrays.asList("a", "b", "c"), false,
			Collections.emptyList(),
			false));
		group.addMember(FormFactory.newSelectField("choice", Arrays.asList("a", "b", "c"), false, Collections.emptyList(),
			false));
		group.addMember(FormFactory.newSelectField("beacon", Arrays.asList("a", "b", "c", "d"), false,
			Collections.emptyList(),
			false));
		ResourceProvider errorImages = images(
			new MapBuilder<String, ThemeImage>()
				.put(null, Icons.DIALOG_QUESTION)
				.put("information", Icons.DIALOG_INFORMATION)
				.put("warning", Icons.DIALOG_WARNING)
				.put("error", Icons.DIALOG_ERROR)
				.toMap()
			);
		SelectField imageSelect =
			FormFactory.newSelectField("imageSelect", Arrays.asList("information", "warning", "error"), false,
				Collections.emptyList(), false, false, null);
		imageSelect.setOptionLabelProvider(errorImages);
		group.addMember(imageSelect);

		group.addMember(FormFactory.newBooleanField("checkbox", null, false));
		group.addMember(FormFactory.newBooleanField("booleanChoice", null, false));
		final StringField member = FormFactory.newStringField("textInput");
		// add constraint to get on key up handler
		member.addConstraint(new StringLengthConstraint(0, 9));
		group.addMember(member);
		group.addMember(FormFactory.newStringField("integerInput", null, false));

		Iterator<FormField> it = group.getFields();
		WaitingListener listener = new WaitingListener(getEnclosingFrameScope());
		while (it.hasNext()) {
			it.next().addValueListener(listener);
		}
		return group;
	}

	private final class TooltipFieldControl extends AbstractControlBase {
		private final StringField _tooltipField;

		private TooltipFieldControl(StringField tooltipField) {
			_tooltipField = tooltipField;
		}

		@Override
		public Object getModel() {
			return _tooltipField;
		}

		@Override
		public boolean isVisible() {
			return true;
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(SPAN);
			writeControlAttributes(context, out);
			out.writeAttribute(STYLE_ATTR, "background-color: red");
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out,
				ResKey.text(_tooltipField.getAsString()));
			out.endBeginTag();
			out.writeText("Tooltip-Element");
			out.endTag(SPAN);
		}

		@Override
		protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		}

		@Override
		protected boolean hasUpdates() {
			return false;
		}
	}

	private static class WaitingListener implements ValueListener {

		private final FrameScope _scope;

		public WaitingListener(FrameScope scope) {
			_scope = scope;
		}

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				// ignore
			}
			_scope.addClientAction(JSSnipplet.createAlert("Value performed!"));
		}

	}

	private FormMember ticket_4175() {
		final ResPrefix i18nPrefix = getResPrefix().append("ticket4175");
		final FormGroup group = new FormGroup("Ticket_4175", i18nPrefix);
		List<String> options = new ArrayList<>();
		StringBuilder builder = new StringBuilder("long string");
		for (int index = 0; index < 30; index++) {
			options.add(builder.toString());
			builder.insert(0, "very ");
		}
		SelectField largeContent =
			FormFactory.newSelectField("largeContent", options, true, Collections.emptyList(), false);
		group.addMember(largeContent);
		return group;
	}

	private FormMember ticket_4213() {
		final ResPrefix i18nPrefix = getResPrefix().append("ticket4213");
		final FormGroup group = new FormGroup("Ticket_4213", i18nPrefix);
		final IntField time = FormFactory.newIntField("time", 0, false);
		time.setTransient(true);
		final CommandField activateFilter = FormFactory.newCommandField("activateForward", new Command() {
		
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				ConstantForwardFilter.setActive(time.getAsInt());
				return HandlerResult.DEFAULT_RESULT;
			}
		});
		group.addMember(activateFilter);
		group.addMember(time);
		return group;
	}

	private FormMember ticket_2602() {
		final ResPrefix i18nPrefix = getResPrefix().append("ticket2602");
		
		ResKey initialReason = i18nPrefix.key(".disabledReason1");
		final CommandField disabledCommand = FormFactory.newCommandField("disabledCommand", TestControlsForm.createAlertCommand("command executed"));
		disabledCommand.setImage(com.top_logic.layout.form.tag.Icons.DELETE_BUTTON);
		disabledCommand.setNotExecutable(initialReason);
		
		List<ResKey> reasonList = new ArrayList<>();
		reasonList.add(initialReason);
		reasonList.add(i18nPrefix.key(".disabledReason2"));
		reasonList.add(i18nPrefix.key(".disabledReason3"));
		SelectField reasons = FormFactory.newSelectField("reasons", reasonList, false, Collections.singletonList(initialReason), false);
		reasons.setTransient(true);
		reasons.setOptionLabelProvider(I18NResourceProvider.INSTANCE);
		reasons.addValueListener(new ValueListener() {
			
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				List<?> newReasonList = (List<?>) newValue;
				if(newReasonList.isEmpty()) {
					disabledCommand.setExecutable();
				} else {
					Object newReasonKey = newReasonList.get(0);
					disabledCommand.setNotExecutable((ResKey) newReasonKey);
				}
			}
		});
		
		final FormGroup group = new FormGroup("Ticket_2602", i18nPrefix);
		group.addMember(disabledCommand);
		group.addMember(reasons);
		return group;
	}

	private FormMember ticket_3725() {
		final ResPrefix i18nPrefix = getResPrefix().append("ticket3725");
		final FormGroup group = new FormGroup("Ticket_3725", i18nPrefix);
		
		String fieldName = "dataField";
		FormContext innerCTX1 = new FormContext("innerCTX1", i18nPrefix.append(".innerCTX1"));
		innerCTX1.addMember(FormFactory.newDataField(fieldName));
		FormContext innerCTX2 = new FormContext("innerCTX2", i18nPrefix.append(".innerCTX2"));
		innerCTX2.addMember(FormFactory.newDataField(fieldName));
		
		group.addMember(innerCTX1);
		group.addMember(innerCTX2);
		return group;
	}

	private FormMember ticket_3687() {
		final ResPrefix i18nPrefix = getResPrefix().append("ticket3687");
		final FormGroup group = new FormGroup("Ticket_3687", i18nPrefix);
		List<String> initialSelection= Collections.singletonList("3");
		List<String> options = new ArrayList<>();
		options.add("1");
		options.add("2");
		options.addAll(initialSelection);
		
		final SelectField disabledField = FormFactory.newSelectField("disabledField", options , false, initialSelection, false);
		
		SelectField changingField = FormFactory.newSelectField("changingField", options, false, initialSelection, false);
		changingField.addValueListener(new ValueListener() {
			
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				disabledField.setValue(newValue);
				disabledField.setDisabled(true);
			}
		});
		group.addMember(disabledField);
		group.addMember(changingField);
		return group;
	}

	private FormMember ticket_3132() {
		final ResPrefix i18nPrefix = getResPrefix().append("ticket3132");
		final FormGroup group = new FormGroup("Ticket_3132", i18nPrefix);
		final FormGroup changedMembers = new FormGroup("changedMembersContainer", i18nPrefix);
		final StringField nameField = FormFactory.newStringField("nameField", null, false);
		final BooleanField changedField = FormFactory.newBooleanField("changedField", Boolean.FALSE, false);
		final StringField numberChangedMembers = FormFactory.newStringField("changedMembers", null, true);
		CommandField add = FormFactory.newCommandField("addCommand", new Command() {
			
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				final String addName = nameField.getAsString();
				if (StringServices.isEmpty(addName) || changedMembers.hasMember(addName)) {
					HandlerResult res = new HandlerResult();
					res.addErrorText("Unable to add field with name '" + addName + "'");
					return res;
				}
				final StringField member = FormFactory.newStringField(addName, addName, false);
				final boolean changed = changedField.getAsBoolean();
				if (changed) {
					member.setValue(addName + "_changed");
				}
				member.addValueListener(new ValueListener() {
					
					@Override
					public void valueChanged(FormField field, Object oldValue, Object newValue) {
						updateField(numberChangedMembers, changedMembers);
					}
				});
				changedMembers.addMember(member);
				updateField(numberChangedMembers, changedMembers);
				changedMembers.getChangedMembers();
				return HandlerResult.DEFAULT_RESULT;
			}
		});
		CommandField remove = FormFactory.newCommandField("removeCommand", new Command() {
			
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				final String removeName = nameField.getAsString();
				final FormMember mem = changedMembers.removeMember(removeName);
				HandlerResult res;
				if (mem == null) {
					res = new HandlerResult();
					res.addErrorText("No such member member '" + removeName + "'");
				} else {
					res = HandlerResult.DEFAULT_RESULT;
				}
				updateField(numberChangedMembers, changedMembers);
				return res;
			}
		});
		changedMembers.setControlProvider(SimpleListControlProvider.INSTANCE);
		group.addMember(changedMembers);
		group.addMember(add);
		group.addMember(remove);
		group.addMember(changedField);
		group.addMember(numberChangedMembers);
		group.addMember(nameField);
		return group;
	}

	private FormMember ticket_3438() {
		FormGroup group = new FormGroup("Ticket_3438", getResPrefix().append("ticket3438"));
		
		StringField style;
		group.addMember(style = FormFactory.newStringField("style"));
		
		final StringField text;
		group.addMember(text = FormFactory.newStringField("text"));
		
		final SelectField select;
		group.addMember(select = FormFactory.newSelectField("select", Arrays.asList("a", "b", "c")));
		
		style.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
	            setInputStyle(text, (String) newValue);
	            setInputStyle(select, (String) newValue);
			}

			private void setInputStyle(final FormField field, String style) {
				// Quirks from SearchFieldSupport:
				AbstractFormFieldControlBase theControl = (AbstractFormFieldControlBase) TestLayoutProblems.this.getControl(field.getQualifiedName());
	            theControl.setInputStyle(style);
			}
		});
		
		return group;
		
	}
	
	private void updateField(StringField resField, FormGroup group) {
		StringBuilder result = new StringBuilder();
		result.append(group.isChanged()).append(':');
		Collection members = group.getChangedMembers();
		for (Object mem : members ) {
			result.append(((FormMember)mem).getName()).append(',');
		}
		resField.setAsString(result.toString());
	}

	private FormMember ticket_3109() {
		final ResPrefix i18nPrefix = getResPrefix().append("ticket3109");
		final FormGroup group = new FormGroup("Ticket_3109", i18nPrefix);
		final StringField stringField = FormFactory.newStringField("emptyInput", StringServices.EMPTY_STRING, false);
		stringField.setDisabled(true);
		// Definition of css class on com.top_logic/webapps/top-logic/jsp/test/layout/TestLayoutProblems.jsp
		stringField.setCssClasses("ticket3109");
		group.addMember(stringField);
		
		CommandField command = FormFactory.newCommandField("toggleImmutable", new Command() {
			
			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				stringField.setImmutable(!stringField.isImmutable());
				return HandlerResult.DEFAULT_RESULT;
			}
		});
		group.addMember(command);
		return group;
	}
	
	private FormMember ticket_2850() {
		final ResPrefix i18nPrefix = getResPrefix().append("ticket2850");
		final FormGroup group = new FormGroup("Ticket_2850", i18nPrefix);

		String thead = "thead";
		String th = "th";
		String theadField = "theadField";
		String thField = "thField";
		String tbody = "tbody";
		String tr = "tr";
		String tbodyField = "tbodyField";
		String td = "td";
		String tdField = "tdField";
		String trField = "trField";

		final FormGroup tableGroup = new FormGroup("table", i18nPrefix);
		final FormGroup theadGroup = new FormGroup(thead, i18nPrefix);
		final FormGroup thGroup = new FormGroup(th, i18nPrefix);
		thGroup.addMember(FormFactory.newStringField(thField, thField, true));
		theadGroup.addMember(thGroup);
		theadGroup.addMember(FormFactory.newStringField(theadField, theadField, true));
		tableGroup.addMember(theadGroup);
		final FormGroup tbodyGroup = new FormGroup(tbody, i18nPrefix);
		final FormGroup trGroup = new FormGroup(tr, i18nPrefix);
		final FormGroup tdGroup = new FormGroup(td, i18nPrefix);
		tdGroup.addMember(FormFactory.newStringField(tdField, tdField, true));
		trGroup.addMember(tdGroup);
		trGroup.addMember(FormFactory.newStringField(trField, trField, true));
		tbodyGroup.addMember(trGroup);
		tbodyGroup.addMember(FormFactory.newStringField(tbodyField, tbodyField, true));
		tableGroup.addMember(tbodyGroup);

		String xmlns = "xmlns='" + HTMLConstants.XHTML_NS + "' xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "' xmlns:p='"
				+ FormPatternConstants.PATTERN_NS;
		final Document tableTemplate = DOMUtil.parse("<table " + xmlns + "' class='border'><p:field name='" + thead + "'/><p:field name='" + tbody
				+ "'/></table>");
		final Document theadTemplate = DOMUtil.parse("<thead " + xmlns + "'><tr><p:field name='" + th + "'/><th><p:field name='"
				+ theadField + "'/></th></tr></thead>");
		final Document thTemplate = DOMUtil.parse("<th " + xmlns + "'><p:field name='" + thField + "'/></th>");
		final Document tbodyTemplate = DOMUtil.parse("<tbody " + xmlns + "'><p:field name='" + tr
				+ "'/><tr><td colspan='2'><p:field name='" + tbodyField + "'/></td></tr></tbody>");
		final Document trTemplate = DOMUtil.parse("<tr " + xmlns + "'><p:field name='" + td + "'/><td><p:field name='" + trField
				+ "'/></td></tr>");
		final Document tdTemplate = DOMUtil.parse("<td " + xmlns + "'><p:field name='" + tdField + "'/></td>");

		tableGroup.setControlProvider(new DefaultFormFieldControlProvider() {

			@Override
			public Control visitFormGroup(FormGroup member, Void arg) {
				final Document template;
				if (member == tableGroup) {
					template = tableTemplate;
				} else if (member == theadGroup) {
					template = theadTemplate;
				} else if (member == thGroup) {
					template = thTemplate;
				} else if (member == tbodyGroup) {
					template = tbodyTemplate;
				} else if (member == trGroup) {
					template = trTemplate;
				} else if (member == tdGroup) {
					template = tdTemplate;
				} else {
					throw new IllegalArgumentException("unknown member:" + member);
				}
				return new FormGroupControl(member, this, template, i18nPrefix);
			}

		});

		group.addMember(tableGroup);
		group.addMember(FormFactory.newCommandField("toggleTableVisibility", new ToggleVisibility(tableGroup)));
		group.addMember(FormFactory.newCommandField("toggleTbodyVisibility", new ToggleVisibility(tbodyGroup)));
		group.addMember(FormFactory.newCommandField("toggleTheadVisibility", new ToggleVisibility(theadGroup)));
		group.addMember(FormFactory.newCommandField("toggleTrVisibility", new ToggleVisibility(trGroup)));
		group.addMember(FormFactory.newCommandField("toggleThVisibility", new ToggleVisibility(thGroup)));
		group.addMember(FormFactory.newCommandField("toggleTdVisibility", new ToggleVisibility(tdGroup)));
		return group;
	}

	private FormMember ticket_2781() {
		final FormGroup group = new FormGroup("Ticket_2781", getResPrefix().append("ticket2781"));

		final StringField numberField = FormFactory.newStringField("number", String.valueOf(0), false);
		numberField.addConstraint(NumbersOnlyConstraint.INSTANCE);
		group.addMember(numberField);
		group.addMember(FormFactory.newCommandField("increase", new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				numberField.setValue(String.valueOf(Integer.parseInt(numberField.getAsString()) + 1));
				return HandlerResult.DEFAULT_RESULT;
			}
		}));

		final BooleanField noApplyField = FormFactory.newBooleanField("noApply", false, false);
		noApplyField.setTransient(true);
		group.addMember(noApplyField);

		final BooleanField applyWithErrorsField = FormFactory.newBooleanField("applyWithErrors", false, false);
		applyWithErrorsField.setTransient(true);
		group.addMember(applyWithErrorsField);

		// ensure not both apply with error and no apply are set
		applyWithErrorsField.addValueListener(new SetFalse(noApplyField));
		noApplyField.addValueListener(new SetFalse(applyWithErrorsField));

		final BooleanField applyTemporarilyField = FormFactory.newBooleanField("applyTemporarily", false, false);
		applyTemporarilyField.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				// resets time
				_time = -1;
			}
		});
		applyTemporarilyField.setTransient(true);
		group.addMember(applyTemporarilyField);

		final BooleanField noDiscardField = FormFactory.newBooleanField("noDiscard", false, false);
		noDiscardField.setTransient(true);
		group.addMember(noDiscardField);

		final BooleanField discardWithErrors = FormFactory.newBooleanField("discardWithErrors", false, false);
		discardWithErrors.setTransient(true);
		group.addMember(discardWithErrors);

		// ensure not both discard with error and no discard are set
		discardWithErrors.addValueListener(new SetFalse(noDiscardField));
		noDiscardField.addValueListener(new SetFalse(discardWithErrors));
		
		return group;
	}
	
	/**
	 * {@link ValueListener} for {@link BooleanField}. When the new value is
	 * <code>true</code> it sets the value of a given Field to false.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static final class SetFalse implements ValueListener {
		
		private final FormField depenentField;

		public SetFalse(FormField field) {
			assert field != null : "dependent field must not be null";
			this.depenentField = field;
		}

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			if (((Boolean) newValue).booleanValue()) {
				if (depenentField != null) {
					depenentField.setValue(Boolean.FALSE);
				}
			}

		}
		
	}

	private FormMember ticket_2470() {
		final FormGroup group = new FormGroup("Ticket_2470", getResPrefix().append("ticket2470"));

		// manager to ensure ResourceRenderer is used, as that can write
		// controls in cells
		TableConfiguration manager = TableConfiguration.table();
		manager.setResPrefix(I18NConstants.TICKET_2470_TABLE);

		// business objects are the form members
		final List<StringField> options = new ArrayList<>();
		options.add(FormFactory.newStringField("first", String.valueOf(0), true));
		options.add(FormFactory.newStringField("second", String.valueOf(0), true));
		group.addMembers(options);

		final SelectField selectField = FormFactory.newSelectField("select", options, true, options, false);

		// every time a cell is accessed a new Control is build.
		Accessor optionAccessor = new ReadOnlyAccessor() {

			@Override
			public Object getValue(Object object, String property) {
				return DefaultFormFieldControlProvider.INSTANCE.createControl(object);
			}
		};
		manager.getDefaultColumn().setAccessor(optionAccessor);
		final SelectionTableModel selectionModel =
			new SelectionTableModel(selectField, new String[] { "column" }, manager);
		final FormTableModel tableModel = new FormTableModel(selectionModel, group);

		final TableField table = FormFactory.newTableField("table", tableModel);
		table.setSelectable(true);
		group.addMember(table);

		group.addMember(FormFactory.newCommandField("increase", new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				StringField field = options.get(0);
				field.initializeField(String.valueOf(Integer.parseInt(field.getAsString()) + 1));
				return HandlerResult.DEFAULT_RESULT;
			}
		}));
		return group;
	}

	private FormMember ticket_2437() {
		FormGroup group = new FormGroup("Ticket_2437", getResPrefix().append("ticket2437"));

		final StringField executed = FormFactory.newStringField("executedCount", String.valueOf(0), false);
		executed.setDisabled(true);

		final LayoutComponent self = this;
		final CommandField executeCommand = FormFactory.newCommandField("increase", new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				// Force ajaxUpdate
				final Integer value = Integer.valueOf(executed.getAsString());
				executed.initializeField(String.valueOf(value + 1));

				// Force component redraw indirectly
				self.getParent().invalidate();
				return HandlerResult.DEFAULT_RESULT;
			}
		});
		group.addMember(executed);
		group.addMember(executeCommand);
		return group;
	}

	private FormMember ticket_2372() {
		FormGroup group = new FormGroup("Ticket_2372", getResPrefix().append("ticket2372"));
		final Command testCommand = new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				final String msg = "command executed";
				getEnclosingFrameScope().addClientAction(JSSnipplet.createAlert(msg));
				Logger.info(msg, this);
				return HandlerResult.DEFAULT_RESULT;
			}
		};
		final CommandField executeTwiceImage = FormFactory.newCommandField("executeTwiceImage", testCommand);
		executeTwiceImage.setImage(com.top_logic.layout.form.tag.Icons.DELETE_BUTTON);
		executeTwiceImage.setNotExecutableImage(com.top_logic.layout.form.tag.Icons.DELETE_BUTTON_DISABLED);
		group.addMember(executeTwiceImage);
		final CommandField executeTwiceLink = FormFactory.newCommandField("executeTwiceLink", testCommand);
		group.addMember(executeTwiceLink);
		final CommandField executeTwiceButtonImage = FormFactory.newCommandField("executeTwiceButtonImage", testCommand);
		executeTwiceButtonImage.setImage(com.top_logic.layout.form.tag.Icons.DELETE_BUTTON);
		executeTwiceButtonImage.setNotExecutableImage(com.top_logic.layout.form.tag.Icons.DELETE_BUTTON_DISABLED);
		group.addMember(executeTwiceButtonImage);
		final CommandField executeTwiceButtonLink = FormFactory.newCommandField("executeTwiceButtonLink", testCommand);
		group.addMember(executeTwiceButtonLink);
		CommandField toggle = FormFactory.newCommandField("toggle", new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				switchExecutability(executeTwiceImage);
				switchExecutability(executeTwiceLink);
				switchExecutability(executeTwiceButtonImage);
				switchExecutability(executeTwiceButtonLink);
				return HandlerResult.DEFAULT_RESULT;
			}

			private void switchExecutability(CommandField commandField) {
				boolean isCurrentlyExecutable = commandField.isExecutable();
				if (isCurrentlyExecutable) {
					/* Rendered as image, no reason needed. */
					commandField.setNotExecutable(null);
				} else {
					commandField.setExecutable();
				}
			}
		});
		group.addMember(toggle);
		return group;
	}

	long _time = -1;

	/**
	 * Model builder for a dummy infinite tree structure.
	 */
	public static final TreeBuilder INFINITE_TREE = new AbstractTLTreeNodeBuilder() {
		@Override
		public List<DefaultMutableTLTreeNode> createChildList(DefaultMutableTLTreeNode node) {
			int cnt;
			if (node.getParent() == null) {
				cnt = 5;
			} else {
				cnt = (int) (Math.random() * 10);
			}
	
			List<DefaultMutableTLTreeNode> result = new ArrayList<>();
			for (int n = 0; n < cnt; n++) {
				result.add(createNode(node.getModel(), node, node.getBusinessObject() + "." + n));
			}
			return result;
		}
	
		@Override
		public boolean isFinite() {
			return false;
		}
	};

	/**
	 * Returns a command which sets the values of each field in the
	 * {@link FormContext} as {@link FormField#getDefaultValue() default value}.
	 * 
	 * @see com.top_logic.layout.form.component.FormComponent#getApplyClosure()
	 */
	@Override
	public Command getApplyClosure() {
		BooleanField noApplyField = (BooleanField) getFormContext().getFirstMemberRecursively("noApply");
		if (noApplyField.getAsBoolean()) {
			return null;
		}
		final BooleanField applyWithErrorsField = (BooleanField) getFormContext().getFirstMemberRecursively("applyWithErrors");
		final BooleanField applyExistsTemporarily = (BooleanField) getFormContext().getFirstMemberRecursively("applyTemporarily");
		if (applyExistsTemporarily.getAsBoolean()) {
			long now = System.currentTimeMillis();
			if (_time == -1) {
				_time = now;
			} else {
				long diff = now - _time;
				if (diff > 5 * 1000) {
					_time = -1;
					return null;
				}
			}
		}
		return new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				if (applyWithErrorsField.getAsBoolean()) {
					HandlerResult result = new HandlerResult();
					result.addErrorText("error during apply");
					return result;
				}
				final Iterator<? extends FormField> fields = getFormContext().getDescendantFields();
				while (fields.hasNext()) {
					final FormField field = fields.next();
					field.setDefaultValue(field.getValue());
				}
				context.getWindowScope().getTopLevelFrameScope().addClientAction(JSSnipplet.createAlert("apply executed"));
				return HandlerResult.DEFAULT_RESULT;
			}
		};
	}

	/**
	 * Returns a command which resets all fields in the {@link FormContext}.
	 * 
	 * @see com.top_logic.layout.form.component.FormComponent#getDiscardClosure()
	 */
	@Override
	public Command getDiscardClosure() {
		BooleanField noDiscardField = (BooleanField) getFormContext().getFirstMemberRecursively("noDiscard");
		if (noDiscardField.getAsBoolean()) {
			return null;
		}
		final BooleanField discardWithErrors = (BooleanField) getFormContext().getFirstMemberRecursively("discardWithErrors");
		return new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				if (discardWithErrors.getAsBoolean()) {
					HandlerResult result = new HandlerResult();
					result.addErrorText("error during discard");
					return result;
				}
				final Iterator<? extends FormField> fields = getFormContext().getDescendantFields();
				while (fields.hasNext()) {
					final FormField field = fields.next();
					field.reset();
				}
				context.getWindowScope().getTopLevelFrameScope().addClientAction(JSSnipplet.createAlert("discard executed"));
				return HandlerResult.DEFAULT_RESULT;
			}
		};
	}

	private static class ToggleVisibility implements Command {

		private final FormMember member;

		public ToggleVisibility(FormMember member) {
			this.member = member;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			member.setVisible(!member.isVisible());
			return HandlerResult.DEFAULT_RESULT;
		}

	}
	
	@Override
	protected void becomingInvisible() {
		super.becomingInvisible();
		removeFormContext();
	}
	
	public static final class Ticket2725_Command extends AbstractCommandHandler {
		
		public Ticket2725_Command(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ex) {
				// ignore
			}
			return HandlerResult.DEFAULT_RESULT;
		}
		
	}
}

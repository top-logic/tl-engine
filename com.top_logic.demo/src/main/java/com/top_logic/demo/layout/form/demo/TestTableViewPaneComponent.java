/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.constraints.IntRangeConstraint;
import com.top_logic.layout.form.control.LabelControl;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.IntField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.tag.TableTag;
import com.top_logic.layout.form.template.ButtonControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.layoutRenderer.LayoutControlRenderer;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.display.ClientDisplayData;
import com.top_logic.layout.table.display.ColumnAnchor;
import com.top_logic.layout.table.display.IndexRange;
import com.top_logic.layout.table.display.RowIndexAnchor;
import com.top_logic.layout.table.display.ViewportState;
import com.top_logic.layout.table.display.VisiblePaneRequest;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * Component for testing display behaviour of tables using manual setup of {@link ClientDisplayData}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestTableViewPaneComponent extends FormComponent {

	public interface Config extends FormComponent.Config {
		@Name(XML_CONF_KEY_COLUMNS)
		String getColumns();

		@Name(XML_CONF_KEY_ROW_BUILDER)
		PolymorphicConfiguration<? extends ModelBuilder> getRowBuilder();

		@Override
		@ItemDefault(CP.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();
	}

	public static final String DEMO_TABLE_NAME = "demoTable";

	private static final String XML_CONF_KEY_COLUMNS = "columns";
	private static final String XML_CONF_KEY_ROW_BUILDER = "rowBuilder";

	private String[] columns;
	private ModelBuilder rowBuilder;

	private TableControl _tableControl;

	private IntField horizontalScrollPosition;

	private IntField verticalScrollPosition;

	private IntField paneLeftBound;

	private IntField paneRightBound;

	private IntField forcedVisibleColumn;

	private IntField paneTopBound;

	private IntField paneBottomBound;

	private IntField forcedVisibleRow;

	private TableField demoTable;

	private CommandField applyPaneField;

	private IntField horizontalScrollPositionOffset;

	private IntField verticalScrollPositionOffset;

	public TestTableViewPaneComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
		columns = getColumnNames(context, atts);
		rowBuilder = getRowBuilder(context, atts);
	}

	private String[] getColumnNames(InstantiationContext context, Config attr) {
		return ArrayUtil.checkOnNull(StringServices.toArray(StringServices.nonEmpty(attr.getColumns())));
	}

	private ModelBuilder getRowBuilder(InstantiationContext context, Config atts) throws ConfigurationException {
		return context.getInstance(atts.getRowBuilder());
	}

	@Override
	public FormContext createFormContext() {
		FormContext context = new FormContext(this);

		demoTable = createDemoTable(context);
		horizontalScrollPosition = createIntField("x: ", context, 0);
		horizontalScrollPositionOffset = createIntField("x-offset: ", context, 0);
		verticalScrollPosition = createIntField("y: ", context, 0);
		verticalScrollPositionOffset = createIntField("y-offset: ", context, 0);
		paneLeftBound = createIntField("leftBound: ", context, -1);
		paneRightBound = createIntField("rightBound: ", context, -1);
		forcedVisibleColumn = createIntField("forcedVisibleColumn: ", context, -1);
		paneTopBound = createIntField("topBound: ", context, -1);
		paneBottomBound = createIntField("bottomBound: ", context, -1);
		forcedVisibleRow = createIntField("forcedVisibleRow: ", context, -1);
		applyPaneField = createApplyPaneField(context);
		return context;
	}

	private CommandField createApplyPaneField(FormContext context) {
		CommandField commandField = FormFactory.newCommandField("applyPaneBounds", new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				FormContext formContext = TestTableViewPaneComponent.this.getFormContext();
				if (formContext.checkAll()) {
					adjustViewSettings();
					_tableControl.requestRepaint();
				}
				return HandlerResult.DEFAULT_RESULT;
			}
			
			private void adjustViewSettings() {
				adjustVisiblePane();
				adjustScrollPositions();
			}

			private void adjustScrollPositions() {
				adjustHorizontalScrollPosition();
				adjustVerticalScrollPosition();
			}

			private void adjustHorizontalScrollPosition() {
				int columnIndex = horizontalScrollPosition.getAsInt();
				TableViewModel model = _tableControl.getViewModel();
				int columnCount = model.getColumnCount();
				if (columnIndex < 0 || columnIndex >= columnCount) {
					throw new TopLogicException(I18NConstants.ILLEGAL_COLUMN_INDEX__MIN__MAX.fill(0, columnCount - 1));
				}
				String columnName = model.getColumnName(columnIndex);
				int pixelOffset = horizontalScrollPositionOffset.getAsInt();
				getViewportState().setColumnAnchor(ColumnAnchor.create(columnName, pixelOffset));
			}
			
			private void adjustVerticalScrollPosition() {
				getViewportState().setRowAnchor(RowIndexAnchor.create(verticalScrollPosition.getAsInt(),
					verticalScrollPositionOffset.getAsInt()));
			}

			private ViewportState getViewportState() {
				return getClientDisplayData().getViewportState();
			}

			private void adjustVisiblePane() {
				setColumnRange(IndexRange.multiIndex(paneLeftBound.getAsInt(),
													 paneRightBound.getAsInt(),
													 forcedVisibleColumn.getAsInt()));
				setRowRange(IndexRange.multiIndex(paneTopBound.getAsInt(),
												  paneBottomBound.getAsInt(),
												  forcedVisibleRow.getAsInt()));
			}

			private ClientDisplayData getClientDisplayData() {
				return _tableControl.getViewModel().getClientDisplayData();
			}
			
			private void setColumnRange(IndexRange columnRange) {
				getVisiblePaneRequest().setColumnRange(columnRange);
			}
			
			private void setRowRange(IndexRange columnRange) {
				getVisiblePaneRequest().setRowRange(columnRange);
			}

			private VisiblePaneRequest getVisiblePaneRequest() {
				return getClientDisplayData().getVisiblePaneRequest();
			}
		});
		commandField.setLabel("Apply view settings");
		context.addMember(commandField);
		return commandField;
	}

	private IntField createIntField(String label, FormContext context, int intialValue) {
		IntField intField = FormFactory.newIntField(label);
		intField.setAsInt(intialValue);
		intField.setMandatory(true);
		intField.addConstraint(new IntRangeConstraint(-1, null));
		intField.setLabel(label);
		context.addMember(intField);
		return intField;
	}


	TableControl getTableControl() {
		if (_tableControl == null) {
			getFormContext();
			_tableControl = createTableControl();
		}
		return _tableControl;
	}

	private TableControl createTableControl() {
		ITableRenderer tableRenderer = demoTable.getTableModel().getTableConfiguration().getTableRenderer();
		TableControl control = TableTag.createTableControl(demoTable, tableRenderer, false);
		return control;
	}

	private TableField createDemoTable(FormContext context) {
		TableConfiguration tableConfig = TableConfiguration.table();
		adaptTableConfiguration(DEMO_TABLE_NAME, tableConfig);
		List<?> tableRows = getTableRows();
		TableModel tableModel = new ObjectTableModel(columns, tableConfig, tableRows);
		TableField tableField = FormFactory.newTableField(DEMO_TABLE_NAME, tableModel);
		context.addMember(tableField);
		return tableField;
	}

	private List<?> getTableRows() {
		return (CollectionUtil.toList((Collection) rowBuilder.getModel(getModel(), this)));
	}

	@Override
	protected void becomingInvisible() {
		super.becomingInvisible();

		if (_tableControl != null) {
			_tableControl.detach();
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		_tableControl = null;
	}

	public static final class CP implements LayoutControlProvider {
		@Override
		public LayoutControl createLayoutControl(Strategy strategy, LayoutComponent component) {
			final TestTableViewPaneComponent self = (TestTableViewPaneComponent) component;

			return new LayoutControlAdapter(new HTMLFragment() {
				
				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
					out.beginBeginTag(DIV);
					writeLayoutInformation(out);
					out.endBeginTag();
					writeTableControl(context, out);
					out.endTag(DIV);
					out.beginBeginTag(DIV);
					LayoutControlRenderer.writeLayoutConstraintInformation(out, DisplayDimension.dim(30, DisplayUnit.PERCENT));
					LayoutControlRenderer.writeLayoutInformationAttribute(Orientation.HORIZONTAL, 100, out);
					out.endBeginTag();
					writeViewpaneControlFields(context, out);
					out.endTag(DIV);
				}

				private void writeLayoutInformation(TagWriter out) throws IOException {
					LayoutControlRenderer.writeLayoutConstraintInformation(out, DisplayDimension.FIFTY_PERCENT);
					LayoutControlRenderer.writeLayoutInformationAttribute(Orientation.HORIZONTAL, 100, out);
				}

				private void writeTableControl(DisplayContext context, TagWriter out)
						throws IOException {
					TableControl tableControl = self.getTableControl();
					tableControl.write(context, out);
				}

				private void writeViewpaneControlFields(DisplayContext context, TagWriter out)
						throws IOException {
					writeScrollPositionFields(context, out);
					writeHorizontalBoundFields(context, out);
					writeVerticalBoundFields(context, out);
					writeApplyPaneField(context, out);
				}

				private void writeHorizontalBoundFields(DisplayContext context, TagWriter out)
						throws IOException {
					writeIntFields(context, out, "HorizontalPane", self.paneLeftBound, self.paneRightBound,
						self.forcedVisibleColumn);
				}

				private void writeVerticalBoundFields(DisplayContext context, TagWriter out)
						throws IOException {
					writeIntFields(context, out, "VerticalPane", self.paneTopBound, self.paneBottomBound,
						self.forcedVisibleRow);
				}

				private void writeScrollPositionFields(DisplayContext context, TagWriter out)
						throws IOException {
					writeIntFields(context, out, "ScrollPositions", self.horizontalScrollPosition,
						self.horizontalScrollPositionOffset, self.verticalScrollPosition,
						self.verticalScrollPositionOffset);
				}

				private void writeIntFields(DisplayContext context, TagWriter out, String fieldGroupLabel,
						IntField... fields)
						throws IOException {
					out.beginTag(FIELDSET);
					out.beginTag(LEGEND);
					out.writeText(fieldGroupLabel);
					out.endTag(LEGEND);
					for (int i = 0; i < fields.length; i++) {
						writeIntField(context, out, fields[i]);
					}
					out.endTag(FIELDSET);
				}

				private void writeApplyPaneField(DisplayContext context, TagWriter out)
						throws IOException {
					ButtonControlProvider.INSTANCE.createControl(self.applyPaneField).write(context, out);
				}

				private void writeIntField(DisplayContext context, TagWriter out, FormField field) throws IOException {
					out.beginTag(DIV);
					new LabelControl(field).write(context, out);
					DefaultFormFieldControlProvider.INSTANCE.createControl(field).write(context, out);
					out.endTag(DIV);
				}
			});
		}
	
	}

}

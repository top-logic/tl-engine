/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormModeModelAdapter;
import com.top_logic.layout.form.decorator.DecorateService;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectionTableField;
import com.top_logic.layout.security.SecurityAccessor;
import com.top_logic.layout.security.SecurityProvider;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableRenderer;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.EditableTableControl;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContext;

/**
 * Displays a {@link SelectionTableField} as a table with the currently selected
 * options.
 *
 * <p>
 * The currently selected options of the selection can be modified by controls
 * inlined into the table.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectionTableTag extends AbstractFormFieldControlTag implements BodyTag, ControlBodyTag {

	public static final String MANDATORY_CSS_CLASS = "tblMandatory";
	
	private ITableRenderer _tableRenderer;
	
	private Boolean _rowMove;

	private boolean _selectable;

    /** 
     * Columns of the table to display.
     *  
     * Set via {@link #setColumnNames(String)} (using space as separator) 
     *      or {@link #setColumns(String[])}.
     */ 
	private String[] _columns;

	private Set<String> _excludeColumns = Collections.emptySet();

	private BodyContent _bodycontent;

	private Accessor _accessor;

	private SecurityProvider _securityProvider;

	private int _initialSortColumn = -1;

	private boolean _ascending = true;

	/**
	 * JSP tag attribute setter.
	 * 
	 * @param tableRenderer
	 *     The renderer that should be used by this tag to render the
	 *     selection.
	 */
	public void setRenderer(ITableRenderer tableRenderer) {
		_tableRenderer = tableRenderer;
	}

	/**
	 * See
	 * {@link EditableTableControl#EditableTableControl(TableData, Map, ITableRenderer, com.top_logic.layout.form.model.ModeModel, boolean)}
	 */
	public void setRowMove(boolean rowMove) {
		_rowMove = rowMove;
	}
	
	/**
	 * See {@link TableControl#setSelectable(boolean)}.
	 */
	public void setSelectable(boolean selectable) {
		_selectable = selectable;
	}

	/**
	 * Space-separated list of displayed column names.
	 */
	public void setColumnNames(String columns) {
		_columns = StringServices.toArray(columns, ' ');
	}

    public void setAccessor(Accessor accessor) {
		_accessor = accessor;
    }
    
    /**
     * @param    aSecurityProvider    The securityProvider to set.
     */
    public void setSecurityProvider(SecurityProvider aSecurityProvider) {
		_securityProvider = aSecurityProvider;
    }

    /**
     * Array of displayed column names.
     */
    public void setColumns(String[] columnsArray) {
		_columns = columnsArray;
    
    }

    public void setAscending(boolean ascending) {
		_ascending = ascending;
    }
    
    public void setInitialSortColumn(int anInitialSortColumn) {
		_initialSortColumn = anInitialSortColumn;
    }

	@Override
	public String addControl(HTMLFragment childControl) {
		((TableControl) getControl()).addTitleBarControl(childControl);
	    // Additional controls are displayed in a flow layout without any other
		// user-defined mark-up.
	    return null;
	}

	@Override
	protected int startFormMember() throws IOException, JspException {
		boolean newlyCreated = existingControl() == null;
		
		return newlyCreated ? EVAL_BODY_BUFFERED : SKIP_BODY;
	}
	
	@Override
	public Control createControl(FormMember member, String displayStyle) {
		return createTableControl((SelectField) member);
	}

	private Control createTableControl(final SelectField selectField) {
		TableConfigurationProvider fieldSettingsProvider = new NoDefaultColumnAdaption() {
			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				table.setResPrefix(selectField.getResources());
				table.setContextMenu(selectField.getContextMenu());
			}
		};

		TableConfigurationProvider jspSettingsApply = new NoDefaultColumnAdaption() {
			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				wrapAccessors(table);
				
				for (String columnName : _excludeColumns) {
					table.removeColumn(columnName);
				}
			}
		};

		List<String> columns =
			_columns == null || _columns.length == 0 ? Collections.emptyList() : Arrays.asList(_columns);

		TableConfigurationProvider finalProvider;
		TableConfigurationProvider customProvider = selectField.getTableConfigurationProvider();
		if (columns.isEmpty()) {
			finalProvider =
				TableConfigurationFactory.combine(fieldSettingsProvider, customProvider, jspSettingsApply);
		} else {
			TableConfigurationProvider visibleColmnsProvider = TableConfigurationFactory.setDefaultColumns(columns);
			finalProvider =
				TableConfigurationFactory.combine(fieldSettingsProvider, customProvider, visibleColmnsProvider,
				jspSettingsApply);
		}
		selectField.setTableConfigurationProvider(finalProvider);

		// Create a table model based on the current selection of this tag's select field member.
		TableData tableData = DecorateService.prepareTableData(selectField);

		TableModel tableModel = tableData.getTableModel();
		TableConfiguration tableDescription = tableModel.getTableConfiguration();

		// Revert the modification. The field has used the tag-provided data to initialize its table
		// model.
		selectField.setTableConfigurationProvider(customProvider);

		ITableRenderer editTableRenderer = _tableRenderer;
		if (editTableRenderer == null) {
			editTableRenderer = tableDescription.getTableRenderer();
			if (editTableRenderer == null) {
				editTableRenderer = DefaultTableRenderer.newInstance();
			}
		}

		if (selectField.isMandatory() && selectField.isActive() && editTableRenderer instanceof TableRenderer<?>) {
			TableRenderer<?> copy = ((TableRenderer<?>) editTableRenderer).cloneRenderer();
			copy.setCustomCssClass(MANDATORY_CSS_CLASS);
			editTableRenderer = copy;
		}

		boolean rowMove = _rowMove == null ? selectField.hasCustomOrder() : _rowMove.booleanValue();
		EditableTableControl control =
			new EditableTableControl(tableData, editTableRenderer, new FormModeModelAdapter(selectField), rowMove);
		control.setSelectable(_selectable || rowMove);
		
		// Only enable sorting the view, if the table rows cannot be moved by the user. Both is not
		// possible, because moving rows affects the application model, but sorting only affects the
		// current view.
		TableViewModel viewModel = control.getViewModel();
		viewModel.setSortedApplicationModelColumn(_initialSortColumn, _ascending);

		boolean canSort = !rowMove;
		if (!canSort) {
			// Disable sorting.
			for (int cnt = viewModel.getColumnCount(), n = 0; n < cnt; n++) {
				viewModel.setColumnComparator(n, null);
			}
		}
		
		return control;
	}

	private void wrapAccessors(TableConfiguration tableConfiguration) {
		wrapAccessor(tableConfiguration.getDefaultColumn());
		for (ColumnConfiguration column : tableConfiguration.getDeclaredColumns()) {
			wrapAccessor(column);
		}
	}

	private void wrapAccessor(ColumnConfiguration column) {
		Accessor<?> columnAccessor = column.getAccessor();
		if (columnAccessor == null) {
			columnAccessor = _accessor;
		}
		if (_securityProvider != null) {
			Group currentGroup = TLContext.getContext().getCurrentPersonWrapper().getRepresentativeGroup();
			columnAccessor = new SecurityAccessor(currentGroup, _securityProvider, columnAccessor);
		}
		column.setAccessor(columnAccessor);
	}
    
    @Override
	public void doInitBody() throws JspException {
		ControlBodyTagSupport.doInitBody();
		installCorrectTagWriter();
	}
	
	@Override
	public void setBodyContent(BodyContent bodycontent) {
		// The body content is silently ignored. This tag only executes its body
		// to execute children tags that register additional views with this
		// tag's control.
		
		// Remember the given body content to check for abuse of this tag later
		// on.
		_bodycontent = bodycontent;
	}
	
	@Override
	public int doAfterBody() throws JspException {
		return ControlBodyTagSupport.doAfterBody(_bodycontent);
	}
	
	@Override
	protected int endFormMember() throws IOException, JspException {
		installCorrectTagWriter();
		// Initially write the complete table.
		writeFormField();
		return super.endFormMember();
	}
	
	@Override
	protected void teardown() {
		super.teardown();
		
		_bodycontent = null;
		_rowMove = null;
		_initialSortColumn = -1;
		_ascending = true;
		_securityProvider = null;
	}

}

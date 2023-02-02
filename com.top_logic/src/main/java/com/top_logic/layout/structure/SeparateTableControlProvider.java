/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.TableFilterOverviewControl;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.tag.TableTag;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.mig.html.layout.Layout.LayoutResizeMode;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * ControlProvider for takes a given {@link TableField} from the {@link FormContext} of a
 * {@link FormComponent} and renders it separate.
 * 
 * <p>
 * This enables to render a table of a component as frozen table.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
@IntroducesToolbar
public class SeparateTableControlProvider extends ConfiguredLayoutControlProvider<SeparateTableControlProvider.Config> {

	/**
	 * Configuration of a {@link SeparateTableControlProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<LayoutControlProvider>, ToolbarOptions, ExpandableConfig {

		/**
		 * Name of the configuration property {@link #getTableName()}.
		 */
		static final String TABLE_NAME_ATTR = "tableName";

		/**
		 * Name of the configuration property {@link #hasToolbar()}.
		 */
		static final String TOOLBAR_ATTR = "toolbar";

		/**
		 * Name of the configuration property {@link #hasSidebar()}.
		 */
		static final String SIDEBAR_ATTR = "sidebar";

		/**
		 * Name of the configuration property {@link #getSidebarWidth()}.
		 */
		static final String SIDEBAR_WIDTH_ATTR = "sidebarWidth";

		/**
		 * Name of the configuration property {@link #isSidebarMinimized()}.
		 */
		static final String SIDEBAR_MINIMIZED_ATTR = "sidebarMinimized";

		/**
		 * Default value of {@link #getSidebarWidth()}.
		 */
		String FILTER_WIDTH_DEFAULT = "200px";

		/**
		 * the name of the table field in the FormContext of the displayed component.
		 */
		@Name(TABLE_NAME_ATTR)
		@Mandatory
		String getTableName();

		/**
		 * Whether an explicit toolbar should be allocated for this table.
		 * 
		 * <p>
		 * This should only be set to <code>false</code>, to prevent duplicate toolbars, if a
		 * toolbar is allocated in the context by other means.
		 * </p>
		 */
		@Name(TOOLBAR_ATTR)
		@BooleanDefault(true)
		boolean hasToolbar();

		/**
		 * Whether a filter sidebar for this table shall be shown, or not.
		 */
		@Name(SIDEBAR_ATTR)
		@BooleanDefault(false)
		boolean hasSidebar();

		/**
		 * Whether sidebar shall be displayed minimized initially, false otherwise.
		 */
		@Name(SIDEBAR_MINIMIZED_ATTR)
		@BooleanDefault(false)
		boolean isSidebarMinimized();

		/**
		 * Width of the sidebar.
		 */
		@FormattedDefault(FILTER_WIDTH_DEFAULT)
		@Name(SIDEBAR_WIDTH_ATTR)
		DisplayDimension getSidebarWidth();
	}


	final String _tableFieldName;

	private final boolean _toolbar;

	private final Decision _showMaximize;

	private final Decision _showMinimize;

	private final boolean _initiallyMinimized;

	private boolean _hasSidebar;

	private DisplayDimension _sidebarWidth;

	private boolean _isSidebarMinimized;

	/**
	 * Creates a new {@link SeparateTableControlProvider} from the given {@link Config
	 * configuration}.
	 */
	public SeparateTableControlProvider(InstantiationContext context, Config config) {
		super(context, config);
		_tableFieldName = config.getTableName();
		_hasSidebar = config.hasSidebar();
		_toolbar = config.hasToolbar() || _hasSidebar;
		_isSidebarMinimized = config.isSidebarMinimized();
		_sidebarWidth = config.getSidebarWidth();
		_showMaximize = config.getShowMaximize();
		_showMinimize = config.getShowMinimize();
		_initiallyMinimized = config.isInitiallyMinimized();
	}

	@Override
	public Control createLayoutControl(Strategy strategy, LayoutComponent component) {
		final FormComponent formComponent = (FormComponent) component;
		Expandable model =
				new PersonalizingExpandable(component.getName() + "." + _tableFieldName + ".layoutExpansionState",
					_initiallyMinimized);
		if (_hasSidebar) {
			MaximizableControl container = new MaximizableControl(model);
			FlexibleFlowLayoutControl layout = new FlexibleFlowLayoutControl(LayoutResizeMode.INSTANT, Orientation.HORIZONTAL);
			container.setChildControl(layout);

			layout.addChild(createSidebar(formComponent));
			layout.addChild(createTable(strategy, formComponent, model));

			return container;
		} else {
			return createTable(strategy, formComponent, model);
		}
	}

	private LayoutControl createSidebar(FormComponent component) {
		Expandable model =
			new PersonalizingExpandable(component.getName() + "." + _tableFieldName + ".sidebarCollapsedState",
				_isSidebarMinimized);
		final CollapsibleControl collapsibleControl =
			new CollapsibleControl(I18NConstants.TABLE_FILTER_TITLE, model, false, Decision.FALSE, Decision.TRUE);

		TableFilterOverviewControl tableFilterOverviewControl =
			new TableFilterOverviewControl(collapsibleControl.getToolbar(),
				() -> getTableField(component, _tableFieldName));

		LayoutControlAdapter layoutControlAdapter = new LayoutControlAdapter(tableFilterOverviewControl) {

			@Override
			protected void internalAttach() {
				super.internalAttach();
				component.addInvalidationListener(this);
			}

			@Override
			protected void internalDetach() {
				component.removeInvalidationListener(this);
				super.internalDetach();
			}
		};
		collapsibleControl.setChildControl(layoutControlAdapter);
		collapsibleControl.setConstraint(
			new DefaultLayoutData(
				_sidebarWidth, 100, DisplayDimension.HUNDERED_PERCENT, 100, Scrolling.AUTO));
		collapsibleControl.getToolbar().setShowMaximizeDefault(false);
		return collapsibleControl;
	}

	private LayoutControl createTable(Strategy strategy, final FormComponent component, Expandable model) {
		TableFragment table = new TableFragment(component, _tableFieldName);
		final LayoutControlAdapter layoutAdapter = new LayoutControlAdapter(table) {

			@Override
			protected void internalAttach() {
				super.internalAttach();
				component.addInvalidationListener(this);
			}

			@Override
			protected void internalDetach() {
				component.removeInvalidationListener(this);
				super.internalDetach();
			}
		};
		if (_toolbar) {
			ResKey titleKey = component.getTitleKey(_tableFieldName);
			CollapsibleControl collapsibleControl =
				new CollapsibleControl(titleKey, model, true, _showMaximize, _showMinimize);
			component.setToolbar(_tableFieldName, collapsibleControl.getToolbar());
			collapsibleControl.getToolbar().setShowMinimizeDefault(false);
			collapsibleControl.setChildControl(layoutAdapter);
			return collapsibleControl;
		} else {
			/* Install component toolbar as toolbar for table field within the decoration callback,
			 * because the component toolbar is installed within the default decoration. */
			return LayoutControlAdapter.wrap(strategy.decorate(component, installToolbarLayouting(layoutAdapter)));
		}
	}

	private Layouting installToolbarLayouting(final LayoutControl result) {
		return new Layouting() {

			@Override
			public Control mkLayout(Strategy strategy, LayoutComponent component) {
				ToolBar contextToolbar = component.getToolBar();
				if (contextToolbar != null) {
					((FormComponent) component).setToolbar(_tableFieldName, contextToolbar);
				}
				return result;
			}
		};
	}
	
	private static final class TableFragment implements HTMLFragment {

		private final FormComponent _component;

		private final String _tableName;

		TableFragment(FormComponent component, String tableName) {
			_component = component;
			_tableName = tableName;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			TableControl control = TableTag.createTableControl(getTableField(_component, _tableName));
			control.write(context, out);
		}
	}

	static TableField getTableField(FormComponent component, String tableName) {
		return (TableField) component.getFormContext().getMember(tableName);
	}

}

/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import java.io.IOException;
import java.util.Collections;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.IdentityAccessor;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.DisplayValueChain;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.component.model.SingleSelectionListener;
import com.top_logic.layout.form.control.SelectionPartControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.table.model.ColumnConfig;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.NoDefaultColumnAdaption;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link RevisionSelectComponent} that additionally holds a compare revision.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RevisionCompareComponent extends RevisionSelectComponent implements SelectionModelOwner, SingleSelectionListener {

	private static final String COMPARE_COLUMN = "__compareColumn";

	private final DefaultSingleSelectionModel _compareRevision = new DefaultSingleSelectionModel(this);

	/** Whether compare mode is currently active. */
	private boolean _compareActive = false;

	/**
	 * Creates a new {@link RevisionCompareComponent}.
	 */
	public RevisionCompareComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public FormContext createFormContext() {
		FormContext formContext = super.createFormContext();
		return formContext;
	}

	@Override
	protected TableConfigurationProvider getTableConfigurationProvider() {
		return new NoDefaultColumnAdaption() {

			@Override
			public void adaptConfigurationTo(TableConfiguration table) {
				ColumnConfiguration column = table.declareColumn(COMPARE_COLUMN);
				column.setColumnLabelKey(I18NConstants.COMPARE_COLUMN_LABEL);
				column.setDefaultColumnWidth("30px");
				column.setRenderer(new Renderer<>() {

					@Override
					public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
						new SelectionPartControl(getSelectionModel(), value).write(context, out);
					}

				});
				column.setAccessor(IdentityAccessor.INSTANCE);
				column.setMandatory(true);
				column.setFullTextProvider(null);
				column.setClassifiers(Collections.singletonList(ColumnConfig.CLASSIFIER_NO_EXPORT));
				column.setSortable(false);
				column.setFilterProvider(null);
			}
		};
	}

	@Override
	public final SelectionModel getSelectionModel() {
		return _compareRevision;
	}

	final SingleSelectionModel compareSelection() {
		return _compareRevision;
	}

	@Override
	protected DisplayValue createTitle(Revision selectedRevision) {
		DisplayValue superTitle = super.createTitle(selectedRevision);
		if (!_compareActive) {
			return superTitle;
		}
		Revision compareRevision = (Revision) compareSelection().getSingleSelection();
		if (compareRevision == null) {
			return superTitle;
		}
		DisplayValue compareDisplayValue = super.createTitle(compareRevision);
		return DisplayValueChain.newChain(superTitle, ConstantDisplayValue.SPACE,
			new ResourceText(I18NConstants.COMPARE_TITLE_SEPARATOR), ConstantDisplayValue.SPACE, compareDisplayValue);
	}

	/**
	 * Toggles the compare mode.
	 */
	public boolean toggleCompareMode() {
		if (_compareActive) {
			compareSelection().removeSingleSelectionListener(this);
			dispatchCompareAlgorithm(null);
			_compareActive = false;
		} else {
			compareSelection().addSingleSelectionListener(this);
			handleCompareVersionChanged(null, (Revision) compareSelection().getSingleSelection());
			_compareActive = true;
		}
		if (hasFormContext()) {
			updateTitle(getSelectedRevision());
		}
		return _compareActive;
	}

	@Override
	public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject,
			Object selectedObject) {
		handleCompareVersionChanged((Revision) formerlySelectedObject, (Revision) selectedObject);
		updateTitle(getSelectedRevision());
	}
	
	void handleCompareVersionChanged(Revision oldValue, final Revision newValue) {
		if (newValue == null) {
			dispatchCompareAlgorithm(null);
		} else {
			if (oldValue != null) {
				if (oldValue.equals(newValue)) {
					/* The date changed, but the revision remains the same. No change necessary. */
					return;
				}
			}
			dispatchCompareAlgorithm(new RevisionCompareAlgorithm(newValue));
		}
	}

	private void dispatchCompareAlgorithm(final CompareAlgorithm compareAlgorithm) {
		getParent().acceptVisitorRecursively(new DefaultDescendingLayoutVisitor() {

			@Override
			public boolean visitLayoutComponent(LayoutComponent aComponent) {
				if (aComponent instanceof CompareAlgorithmHolder) {
					((CompareAlgorithmHolder) aComponent).setCompareAlgorithm(compareAlgorithm);
				}
				return super.visitLayoutComponent(aComponent);
			}
		});
	}


}

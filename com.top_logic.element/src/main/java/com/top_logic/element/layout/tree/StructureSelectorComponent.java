/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.Log;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.ComponentChannel.ChannelValueFilter;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLModule;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;

/**
 * This class provides a selector for structures. The names of the structures presented 
 * are configured in the layout xml.
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class StructureSelectorComponent extends FormComponent implements Selectable {
    
	public interface Config extends FormComponent.Config {
		@Name(XML_ATTRIBUTE_STRUCTURES)
		String getStructures();
	}

	public static final String FORM_FIELD_STRUCTURE = "structure";

    public static final String XML_ATTRIBUTE_STRUCTURES = "structures";

	private static final ChannelListener ON_SELECTION_CHANGE = new ChannelListener() {
		@Override
		public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
			StructureSelectorComponent self = (StructureSelectorComponent) sender.getComponent();
			((SelectField) self.getFormContext().getField(FORM_FIELD_STRUCTURE)).setAsSingleSelection(newValue);
		}
	};

	private static final ChannelValueFilter SELECTION_FILTER = new ChannelValueFilter() {
		@Override
		public boolean accept(ComponentChannel sender, Object oldValue, Object newValue) {
			StructureSelectorComponent self = (StructureSelectorComponent) sender.getComponent();
			return self._structureRoots.contains(newValue);
		}
	};
    
	List<TLModule> _structureRoots;
    
    public StructureSelectorComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
        String theStructureNames = StringServices.nonEmpty(atts.getStructures());
		Collection<String> structureNames;
        if (StringServices.isEmpty(theStructureNames)) {
			structureNames = AccessManager.getInstance().getStructureNames();
        } else {
			structureNames = StringServices.toList(theStructureNames, ',');
        }
        
        // TODO Ticket #1262, #3630: remove null check if ElementAccessManager is a service
		if (structureNames != null) {
			_structureRoots = new ArrayList<>(structureNames.size());
			for (Iterator<String> theIt = structureNames.iterator(); theIt.hasNext();) {
				_structureRoots.add(TLModelUtil.findModule(theIt.next()));
            }
        }
    }

    @Override
	public FormContext createFormContext() {
		FormContext result = new FormContext("form", getResPrefix());
        
		Object selection = getSelected();
        
		SelectField selectField = FormFactory.newSelectField(FORM_FIELD_STRUCTURE, _structureRoots, false,
			selection == null ? Collections.emptyList() : Collections.singletonList(selection), false);
		selectField.setMandatory(true);
		selectField.setOptionComparator(LabelComparator.newCachingInstance(MetaLabelProvider.INSTANCE));
		selectField.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				List<?> theSelectionList = (List<?>) newValue;

				Object newSelection;
				if (theSelectionList != null && !theSelectionList.isEmpty()) {
					newSelection = theSelectionList.iterator().next();
				} else {
					newSelection = null;
				}

				setSelected(newSelection);
			}
		});

		result.addMember(selectField);

		return result;
    }

    @Override
	protected boolean isChangeHandlingDefault() {
		return false;
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);

		ComponentChannel selectionChannel = selectionChannel();
		selectionChannel.set(initialSelection());
		selectionChannel.addListener(ON_SELECTION_CHANGE);
		selectionChannel.addVetoListener(SELECTION_FILTER);
	}

	private Object initialSelection() {
		if (_structureRoots != null) {
			return _structureRoots.get(0);
		} else {
			return null;
		}
	}
}

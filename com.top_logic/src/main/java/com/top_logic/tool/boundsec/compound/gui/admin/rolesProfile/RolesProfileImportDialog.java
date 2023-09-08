/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.compound.gui.admin.rolesProfile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.CommandModelUtilities;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.constraints.FilenameEndConstraint;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.SimpleFormDialog;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConfigTree;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.gui.profile.EditRolesProfileComponent;
import com.top_logic.tool.boundsec.gui.profile.EditSecurityProfileComponent;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * A {@link SimpleFormDialog} for importing a roles-profiles xml file.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class RolesProfileImportDialog extends SimpleFormDialog {

	private final LayoutComponent _rolesProfileComponent;

	/**
	 * Creates a {@link RolesProfileImportDialog}.
	 * 
	 * @param rolesProfileComponent
	 *        See: {@link #getRolesProfileComponent()}
	 */
	public RolesProfileImportDialog(LayoutComponent rolesProfileComponent) {
		super(I18NConstants.IMPORT_ROLES_PROFILES_DIALOG,
			DisplayDimension.dim(330, DisplayUnit.PIXEL),
			DisplayDimension.dim(150, DisplayUnit.PIXEL));
		_rolesProfileComponent = rolesProfileComponent;
	}

	@Override
	protected void fillFormContext(FormContext context) {
		DataField uploadField = FormFactory.newDataField(INPUT_FIELD);
		uploadField.addConstraint(new FilenameEndConstraint(".xml", I18NConstants.ONLY_XML_FILES));
		context.addMember(uploadField);
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(ButtonType.CANCEL, getDialogModel().getCloseAction()));
		CommandModel updateCommand = CommandModelFactory.commandModel(new RolesProfileImportCommand(this));
		updateCommand.setLabel(Resources.getInstance().getString(I18NConstants.IMPORT_ROLES_PROFILE));
		CommandModelUtilities.setNonExecutable(updateCommand, I18NConstants.IMPORT_ROLES_PROFILE_NOTHING_SELECTED);
		getUploadField().addValueListener(new RolesProfileUploadFieldListener(updateCommand));
		buttons.add(updateCommand);
	}

	/**
	 * The component displaying the roles-profiles or a part of them.
	 * <p>
	 * After the import this component is invalidated and (if it's a {@link FormComponent}) its
	 * {@link FormContext} is removed.
	 * </p>
	 * 
	 * @return Is null, if there is no component that should to be invalidated.
	 */
	protected LayoutComponent getRolesProfileComponent() {
		return _rolesProfileComponent;
	}

	/**
	 * The field into which the roles-profile file is uploaded.
	 */
	protected DataField getUploadField() {
		return (DataField) getFormContext().getField(INPUT_FIELD);
	}

	/**
	 * Import the given uploaded file.
	 */
	protected boolean importRolesProfile(DisplayContext context, BinaryContent uploadedItem) {
		Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		try {
			boolean importOk = importRolesProfileInternal(context, uploadedItem);
			if (importOk) {
				transaction.commit();
				updateRolesProfileComponent();
			} // Otherwise, the transaction is rolled back by the finally-block.
			return importOk;
		} catch (TopLogicException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException("Importing the roles-profiles failed. Cause: " + ex.getMessage(), ex);
		} finally {
			transaction.rollback();
		}
	}

	private boolean importRolesProfileInternal(DisplayContext context, BinaryContent uploadedItem)
			throws FactoryConfigurationError, ParserConfigurationException, SAXException, IOException,
			ConfigurationException {
		if (getRolesProfileComponent() instanceof EditRolesProfileComponent) {
			// old rolesprofile component based on the current mainlayout.
			MainLayout mainLayout = context.getLayoutContext().getMainLayout();
			return new RolesProfileHandler().importProfiles(mainLayout, uploadedItem.getStream());
		}
		Collection<LayoutConfigTree> layoutTrees =
			((EditSecurityProfileComponent) getRolesProfileComponent()).getAvailableLayoutTrees();
		new RolesProfileHandler().importProfiles(layoutTrees, uploadedItem);
		return true;
	}

	private void updateRolesProfileComponent() {
		if (getRolesProfileComponent() == null) {
			return;
		}
		if (getRolesProfileComponent() instanceof FormComponent) {
			((FormComponent) getRolesProfileComponent()).removeFormContext();
		}
		getRolesProfileComponent().invalidate();
	}

}

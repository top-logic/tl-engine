/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.comment.layout;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.comment.layout.CommentDialogComponent.OwnerExecutabilityRule;
import com.top_logic.element.comment.wrap.Comment;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.table.renderer.DefaultRowClassProvider;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.mig.html.layout.CommandDispatcher;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.SimpleOpenDialog;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * Very special renderer for comments assigned to a wrapper.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CommentTableRenderer extends DefaultTableRenderer {

	public CommentTableRenderer(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

    @Override
	public void writeColumns(DisplayContext context, TagWriter anOut, RenderState state, boolean aIsSelected, int aDisplayedRow, int aRow) throws IOException {
		{
            Comment    theComment = (Comment) state.getModel().getValueAt(aRow, 0);
            TagWriter theWriter  = (TagWriter) anOut;
            
            // Empty start row for one comment.
            theWriter.beginBeginTag(HTMLConstants.TD);
            theWriter.writeAttribute(HTMLConstants.HEIGHT_ATTR, "10px");
            theWriter.endBeginTag();
            theWriter.endTag(HTMLConstants.TD);
            theWriter.endTag(HTMLConstants.TR);
    
            // Next the row for the date, the comment was created.
            theWriter.beginBeginTag(TR);
			theWriter.writeAttribute(CLASS_ATTR, DefaultTableRenderer.TABLE_ROW_CSS_CLASS);
            theWriter.endBeginTag();
                theWriter.beginTag(HTMLConstants.TD);
                    this.writeCommentDate(theWriter, theComment);
                theWriter.endTag(HTMLConstants.TD);
            theWriter.endTag(HTMLConstants.TR);

            // The conent will apear in a new row.
            theWriter.beginBeginTag(TR);
			theWriter.writeAttribute(CLASS_ATTR, DefaultRowClassProvider.TR_SELECTED_CSS_CLASS);
            theWriter.endBeginTag();
                theWriter.beginTag(HTMLConstants.TD);
                    theWriter.beginBeginTag(TABLE);
                    theWriter.writeAttribute(CELLSPACING_ATTR, 0);
                    theWriter.writeAttribute(WIDTH_ATTR, "100%");
                    theWriter.endBeginTag();
                        theWriter.beginTag(HTMLConstants.TR);
                        // Write the name of the creator.
                        theWriter.beginBeginTag(HTMLConstants.TD);
                                theWriter.writeAttribute(HTMLConstants.VALIGN_ATTR, "top");
                                theWriter.writeAttribute(HTMLConstants.WIDTH_ATTR, "100px");
                            theWriter.endBeginTag();
                            this.writeCommentCreator(theWriter, theComment);
                        theWriter.endTag(HTMLConstants.TD);

                        theWriter.beginBeginTag(HTMLConstants.TD);
                                theWriter.writeAttribute(HTMLConstants.VALIGN_ATTR, "top");
                            theWriter.endBeginTag();
                            this.writeCommentContent(context, theWriter, theComment);
                        theWriter.endTag(HTMLConstants.TD);
                        theWriter.beginBeginTag(HTMLConstants.TD);
                                theWriter.writeAttribute(HTMLConstants.VALIGN_ATTR, "bottom");
                                theWriter.writeAttribute(HTMLConstants.ALIGN_ATTR, "right");
                            theWriter.endBeginTag();
                            this.writeCommentAdorner(context, theWriter, theComment);
                        theWriter.endTag(HTMLConstants.TD);
                    theWriter.endTag(HTMLConstants.TR);
                theWriter.endTag(HTMLConstants.TABLE);
            theWriter.endTag(HTMLConstants.TD);
        }
    }

	protected void writeCommentCreator(TagWriter aWriter, Comment aComment) throws IOException {
        Person theCreator = aComment.getCreator();
        if (theCreator != null) {
            aWriter.writeContent(theCreator.getFullName());
        }
    }
    
	protected void writeCommentDate(TagWriter aWriter, Comment aComment) throws IOException {
        aWriter.writeContent(HTMLFormatter.getInstance().formatDateTime(aComment.getCreated()));
    }
    
	protected void writeCommentContent(DisplayContext aContext, TagWriter aWriter, Comment aComment)
			throws IOException {
        BulletinBoardRenderer.INSTANCE.write(aContext, aWriter, aComment.getContent());
    }

	protected void writeCommentAdorner(DisplayContext aContext, TagWriter aWriter, Comment aComment)
			throws IOException {
		CommentTableComponent theComp = (CommentTableComponent) MainLayout.getComponent(aContext);

		this.writeCommandButton(aContext, aWriter, theComp, aComment, theComp.getEditComponentOpener(), "edit",
			theComp.getEditComponentName(), Icons.EDIT, Icons.EDIT_DISABLED);
		this.writeCommandButton(aContext, aWriter, theComp, aComment, theComp.getQuoteComponentOpener(), "quote",
			theComp.getQuoteComponentName(), Icons.QUOTE, Icons.QUOTE_DISABLED);
    }

	protected void writeCommandButton(DisplayContext context, TagWriter out, BoundComponent aComp, Comment aComment,
			CommandHandler command, String aName, ComponentName componentName, ThemeImage enabledImage, ThemeImage disabledImage)
			throws IOException {
		if (componentName == null) {
			// No component.
			return;
		}
		Map<String, Object> arguments =
			new MapBuilder<String, Object>().
				put(SimpleOpenDialog.MODEL, aComment).
				put(SimpleOpenDialog.COMPONENT_NAME, componentName).
				put(OwnerExecutabilityRule.COMMAND_ATTRIBUTE, command.getID()).
				toMap();
		ExecutableState theState = CommandDispatcher.resolveExecutableState(command, aComp, arguments);

		if (!theState.isHidden()) {
			ResKey labelKey = command.getResourceKey(aComp);
			Resources resources = context.getResources();
			String label = resources.getString(labelKey);
			CommandModel commandModel = CommandModelFactory.commandModel(command, aComp, arguments);
			if (theState.isDisabled()) {
				commandModel.setNotExecutable(theState.getI18NReasonKey());
				commandModel.setAltText(resources.getString(theState.getI18NReasonKey()));
			} else {
				commandModel.setExecutable();
				commandModel.setAltText(label);
			}
			commandModel.setLabel(label);
			commandModel.setImage(enabledImage);
			commandModel.setNotExecutableImage(disabledImage);

			new ButtonControl(commandModel).write(context, out);

			out.writeContent(HTMLConstants.NBSP);
        }
	}

}

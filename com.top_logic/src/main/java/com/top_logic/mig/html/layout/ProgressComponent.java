/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.messagebox.ProgressDialog;
import com.top_logic.layout.progress.ProgressInfo;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.util.Resources;

/**
 * A generic component to show the progress of a long lasting process provided by an implementation
 * of the {@link com.top_logic.layout.progress.ProgressInfo} interface. (Normally used as part of an
 * assistant that wants to give a user response for some ongoing task).
 * 
 * <p>
 * The progress is shown as a progress bar. Above the progress bar is a description about what the
 * whole task is about(called the progress info). Below the progress bar is a description of the
 * current (sub)task (called the progress message). This information is provided by the
 * {@link com.top_logic.layout.progress.ProgressInfo#getMessage()} method.
 * </p>
 * 
 * <p>
 * After the progress is finished a message is shown to indicate this.
 * </p>
 * 
 * <h3>Example</h3>
 * <p>
 * The progress bar at a progress of 80%:
 * <p>
 * 
 * <h3>the header(progress header)</h3>
 * 
 * An overview of the task(progress info)
 * <table style="table-layout:fixed;border:thin solid black;width:80%;height:20px;">
 * <td style="width:80%;border:1;background-color: #000080;"/>
 * <td style="width:20%;border:1;background-color: lightgrey;"/>
 * </table>
 * A short description of the current task(progress message)
 * 
 * </p>
 * </p>
 * 
 * <p>
 * <em>Note</em>: The component assumes, that the model implements the {@link ProgressInfo}
 * interface, but this can be overriden.
 * </p>
 * 
 * @author <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 * 
 * @deprecated Use {@link ProgressDialog}.
 */
@Deprecated
public abstract class ProgressComponent extends BoundComponent {

	/**
	 * Create a new ProgressComponent from XML.
	 */
	public ProgressComponent(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	/**
	 * Check if model implements ProgressInfo, if it does the model is returned.
	 * 
	 * Override this method if you can derive a ProgressInfo in some other way.
	 * 
	 * @return the model if the model implements ProgressInfo, otherwise null
	 */
	protected ProgressInfo getProgressInfo() {
		Object theModel = getModel();
		if (theModel instanceof ProgressInfo) {
			return (ProgressInfo) theModel;
		}
		return null;
	}

	/**
	 * Hook for subclasses so they are called when the import is finished.
	 * 
	 * This allows dependend components to be reloaded after this call.
	 */
	protected void progressFinished(String aContextPath, TagWriter aOut, HttpServletRequest aReq) throws IOException {
		// hook only, nothing happens here
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return false;
	}

	/**
	 * Create some nice HTML showing the Progress.
	 */
	@Override
	public void writeBody(ServletContext aContext, HttpServletRequest aReq, HttpServletResponse aResp, TagWriter aOut)
			throws IOException, ServletException {

		super.writeBody(aContext, aReq, aResp, aOut);

		Resources res = Resources.getInstance();
		ProgressInfo theProgress = getProgressInfo();
		writeProgressHeader   (theProgress, aOut, res);
		writeProgressInfo     (theProgress, aOut, res);
		writeProgressBar      (theProgress, aOut, res);
		writeProgressMessage  (theProgress, aOut, res);
		
		if (theProgress != null && ! theProgress.isFinished()) {
		} else {
			progressFinished(aReq.getContextPath(), aOut, aReq);
		}
	}

	/**
	 * Write a header giving a description about the task.
	 * 
	 * @param aProgress
	 *            information about the progress.
	 * @param aOut
	 *            render output here.
	 * @param res
	 *            use for translation
	 */
	protected void writeProgressHeader(ProgressInfo aProgress, TagWriter aOut, Resources res) throws IOException {
		aOut.beginBeginTag("h1");
		aOut.writeContent("class=\"progress\"");
		aOut.endBeginTag();
		aOut.writeContent(res.getString(getResPrefix().key("progressHeader")));
		aOut.endTag("h1");
	}

	/**
	 * Write the progress Info.
	 * 
	 * @param aProgress
	 *            information about the progress. If null a standard message is displayed.
	 * @param aOut
	 *            render output here.
	 * @param res
	 *            use for translation
	 */
	protected void writeProgressInfo(ProgressInfo aProgress, TagWriter aOut, Resources res) throws IOException {
		aOut.beginBeginTag("span");
		aOut.writeContent("style=\"margin:2pt\"");
		aOut.endBeginTag();
	    if (aProgress == null) {
			aOut.writeContent(res.getString(getResPrefix().key("progessNotAvailable")));
		}
        else {
            float progress = aProgress.getProgress();

            if (aProgress.isFinished()) {
				aOut.writeContent(res.getString(getResPrefix().key("progressFinished")));
            }
            else {
				Object params[] = new Object[] { Float.valueOf(progress), Long.valueOf(aProgress.getCurrent()),
					Long.valueOf(aProgress.getExpected()), };
				aOut.writeContent(res.getMessage(getResPrefix().key("progressInfo"), params));
            }
        }
	    aOut.endTag("span");
	}
	
	/**
	 * Writes the progress bar.
	 * 
	 * @param aProgress
	 *            information about the progress. If null nothing is done.
	 * @param aOut
	 *            render output here.
	 * @param res
	 *            use for translation
	 */
	protected void writeProgressBar(ProgressInfo aProgress, TagWriter aOut, Resources res) throws IOException {
		
        if (aProgress != null && !aProgress.isFinished()) {
			Writer out = aOut.contentWriter();
		
			float progress = aProgress.getProgress();
			
			out.write("\n\t<table class=\"progressBarTable\" style=\"margin:2pt;\">");
			out.write("\n\t<tr>");
			if (progress == 0.0) { // we only need one empty cell
				out.write("\n\t\t<td class=\"progressCellEmpty\" style=\"width:100%;\" >");
	//			out.write((int)progress +"%");
				out.write("</td>");
			} else if (progress == 100.0) { // we only need one filled cell showing
											// the 100% progress
				out.write("\n\t\t<td class=\"progressCellFilled\" style=\"width:100%;\" />");
			} else {
				out.write("\n\t\t<td class=\"progressCellFilled\" style=\"width:" + (int) progress + "%;\" />");
				out.write("\n\t\t<td class=\"progressCellEmpty\" style=\"width:" + (int) (100.0 - progress) + "%;\" >");
				out.write("</td>");
	//			out.write("<div style=\"position:absolute;left:40%;top:4px;color:green\">" + (int)progress +"% </div>");
				
			}
			out.write("\n\t</tr>");
			out.write("\n\t</table>");
        }
	}
	

	/**
	 * Writes the progress message.
	 * 
	 * @param aProgress
	 *            information about the progress. If null nothing happens.
	 * @param aOut
	 *            render output here.
	 * @param res
	 *            use for translation
	 */
	protected void writeProgressMessage(ProgressInfo aProgress, TagWriter aOut, Resources res) throws IOException {
		
		String theMessage;
		if (aProgress == null) {
			return;
		}
		else {
			theMessage = aProgress.getMessage();
		}
		if (theMessage != null) {
			aOut.beginBeginTag("pre");
			aOut.writeContent("class=\"progressMessage\"");
			aOut.endBeginTag();
			PrintWriter out = aOut.getPrinter();
            out.print(HTMLFormatter.getInstance().formatDateTime(new Date()) + ": ");
			out.println(res.getString(getResPrefix().key("progressMessage")));
            out.println();
			out.write(theMessage);
			aOut.endTag("pre");
		}
	}
}

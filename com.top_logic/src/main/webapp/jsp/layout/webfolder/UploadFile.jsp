<%@page import="com.top_logic.layout.basic.DefaultDisplayContext"
%><%@page import="com.top_logic.layout.basic.ThemeImage"
%><%@page language="java" session="true" extends="com.top_logic.knowledge.gui.layout.webfolder.WebFolderJsp"
%><%@page import="java.util.*,
com.top_logic.mig.html.UserAgent,
com.top_logic.gui.Theme,
com.top_logic.gui.ThemeFactory,
com.top_logic.mig.html.layout.FileUploadComponent"
%><%@taglib uri="util"     prefix="tl"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><layout:html>
	<%
	
	int	       theRows      		= 3;
	String     theContext   		= request.getContextPath();
	boolean    update       		= this.doingFileUpdate(application, request);
	Theme      theTheme            = ThemeFactory.getTheme();
	String	   theIcon      	   = "theme:DIALOGS_UPLOAD_DOCUMENT";
	String     theUpdateFileName 	= "";
	FileUploadComponent theComp    = (FileUploadComponent)MainLayout.getComponent(pageContext);
	boolean    showDateField       = theComp.showUploadDateField();
	if (showDateField) {
		theRows = theRows + 1;
	}
	
	if (update) {
		theRows           = theRows + 1;
		theUpdateFileName = this.getUpdateFileName(application, request);
	}
	%>
	<layout:head/>
	<layout:body>
		<form:form>
			<table id="visiblePart"
				align="center"
				border="0"
				cellpadding="5"
				cellspacing="5"
				width="100%"
			>
				<tr>
					<td
						colspan="2"
						height="5"
					>
					</td>
				</tr>
				<tr>
					<td
						align="center"
						rowspan="<%=theRows %>"
						valign="top"
					>
						<%
						ThemeImage.icon(theIcon).write(DefaultDisplayContext.getDisplayContext(pageContext), MainLayout.getTagWriter(pageContext));
						%>
					</td>
					<td class="dialogTitle"
						width="100%"
					>
						<tl:label name="tl.dialog.folder.upload.title"/>
					</td>
				</tr>
				<tr>
					<td>
						<tl:label name="tl.dialog.folder.upload.message"/>
					</td>
				</tr>
				<%
				if (update) {
					%>
					<tr>
						<td>
							<tl:label name="tl.dialog.folder.upload.file.replace"/>
							:
							<b>
								<%=theUpdateFileName %>
							</b>
						</td>
					</tr>
					<%
				}
				%>
				<tr>
					<td class="content">
						<form:custom name="<%= FileUploadComponent.UPLOAD_FIELD_NAME %>"/>
					</td>
				</tr>
				<%
				
				if (showDateField) {
					%>
					<tr>
						<td>
							<form:label name="<%=FileUploadComponent.DATE_FIELD %>"/>
							<form:date name="<%=FileUploadComponent.DATE_FIELD %>"/>
						</td>
					</tr>
					<%
				}
				%><%@include file="customForm.inc" %>
			</table>
		</form:form>
	</layout:body>
</layout:html>
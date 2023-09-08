<%@page language="java" session="true" extends="com.top_logic.knowledge.gui.layout.webfolder.WebFolderJsp"
%><%@page import="com.top_logic.importer.dispatching.DispatchingAssistentFileUploadComponent"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:formPage
			image="theme:ICONS_UPLOAD60"
			titleKeySuffix="title"
		>
			<p>
				<form:resource key="message"/>
			</p>
			<table id="visiblePart"
				style="width:100%; border:0; align:center"
				summary="Upload table"
			>
				<tr>
					<td class="label"
						style="width:0%"
					>
						<form:label name="<%=DispatchingAssistentFileUploadComponent.FIELD_IMPORTER %>"
							colon="true"
						/>
					</td>
					<td class="content"
						style="width:100%"
					>
						<form:custom name="<%=DispatchingAssistentFileUploadComponent.FIELD_IMPORTER %>"/>
					</td>
				</tr>
				<tr>
					<td class="label">
						<form:label name="<%=DispatchingAssistentFileUploadComponent.FIELD_EXTENSIONS %>"
							colon="true"
						/>
					</td>
					<td class="content">
						<form:custom name="<%=DispatchingAssistentFileUploadComponent.FIELD_EXTENSIONS %>"/>
					</td>
				</tr>
				<tr>
					<td class="label">
						<form:label name="<%=DispatchingAssistentFileUploadComponent.UPLOAD_FIELD_NAME %>"
							colon="true"
						/>
					</td>
					<td class="content"
						valign="middle"
					>
						<form:custom name="<%=DispatchingAssistentFileUploadComponent.UPLOAD_FIELD_NAME %>"/>
					</td>
				</tr>
			</table>
		</form:formPage>
	</layout:body>
</layout:html>
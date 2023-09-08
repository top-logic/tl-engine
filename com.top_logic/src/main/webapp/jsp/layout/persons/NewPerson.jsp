<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.base.security.attributes.PersonAttributes,
java.util.*,
com.top_logic.base.security.device.TLSecurityDeviceManager"
%><%@taglib uri="util" prefix="tl"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form>
			<p>
				<tl:label name="admin.person.edit.new.title"/>
			</p>
			<br/>
			<form:descriptionCell>
				<form:description>
					<form:label name="<%= PersonAttributes.USER_NAME %>"/>
					<form:error name="<%= PersonAttributes.USER_NAME %>"
						icon="true"
					/>
				</form:description>
				<form:input name="<%= PersonAttributes.USER_NAME %>"/>
			</form:descriptionCell>

			<form:descriptionCell>
				<form:description>
					<form:label name="<%= PersonAttributes.GIVEN_NAME %>"/>
					<form:error name="<%= PersonAttributes.GIVEN_NAME %>"
						icon="true"
					/>
				</form:description>
				<form:input name="<%= PersonAttributes.GIVEN_NAME %>"/>
			</form:descriptionCell>

			<form:descriptionCell>
				<form:description>
					<form:label name="<%= PersonAttributes.SUR_NAME %>"/>
					<form:error name="<%= PersonAttributes.SUR_NAME %>"
						icon="true"
					/>
				</form:description>
				<form:input name="<%= PersonAttributes.SUR_NAME %>"/>
			</form:descriptionCell>

			<form:descriptionCell>
				<form:description>
					<form:label name="<%= PersonAttributes.TITLE %>"/>
					<form:error name="<%= PersonAttributes.TITLE %>"
						icon="true"
					/>
				</form:description>
				<form:input name="<%= PersonAttributes.TITLE %>"/>
			</form:descriptionCell>

			<form:descriptionCell>
				<form:description>
					<form:label name="<%= PersonAttributes.DATA_ACCESS_DEVICE_ID %>"/>
					<form:error name="<%= PersonAttributes.DATA_ACCESS_DEVICE_ID %>"
						icon="true"
					/>
				</form:description>
				<form:select name="<%= PersonAttributes.DATA_ACCESS_DEVICE_ID %>"/>
			</form:descriptionCell>
		</form:form>
	</layout:body>
</layout:html>
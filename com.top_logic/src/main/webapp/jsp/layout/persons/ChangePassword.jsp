<%@page import="com.top_logic.knowledge.gui.layout.person.ChangePasswordComponent"
%><%@page import="com.top_logic.layout.form.control.IconErrorRenderer"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>

	<layout:body>
		<form:form>
			<!-- class="changePasswordForm" -->
			<form:ifExists name="<%=ChangePasswordComponent.OLD_PASSWORD%>">
				<form:inputCell name="<%=ChangePasswordComponent.OLD_PASSWORD%>"
					colon="true"
				/>
			</form:ifExists>

			<form:inputCell name="<%=ChangePasswordComponent.NEW_PASSWORD_1%>"
				colon="true"
			/>
			<form:inputCell name="<%=ChangePasswordComponent.NEW_PASSWORD_2%>"
				colon="true"
			/>
			<form:inputCell name="<%=ChangePasswordComponent.REQUIRE_CHANGE%>"
				colon="true"
			/>
		</form:form>
	</layout:body>
</layout:html>
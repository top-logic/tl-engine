<%@page language="java" session="true"  extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.layout.progress.AbstractProgressComponent"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form>
			<div class="progressDialog">
				<div class="head">
					<h2 style="margin-top:6px; margin-bottom:6px;">
						<form:resource key="headline"/>
					</h2>
				</div>
				<div class="message">
					<form:custom name="<%=AbstractProgressComponent.MESSAGE_FIELD %>"/>
				</div>
				<div class="progress">
					<form:custom name="<%=AbstractProgressComponent.PROGRESS_FIELD %>"/>
				</div>
			</div>
		</form:form>
	</layout:body>
</layout:html>
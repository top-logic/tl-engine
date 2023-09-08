<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.knowledge.wrap.AbstractWrapper"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="meta"     prefix="meta"
%><layout:html>
	<layout:head/>
	<layout:body>
		<meta:formPage titleAttribute="<%=AbstractWrapper.NAME_ATTRIBUTE %>">
			<meta:group>
				<meta:attributes legend="additional"/>
			</meta:group>
		</meta:formPage>
	</layout:body>
</layout:html>
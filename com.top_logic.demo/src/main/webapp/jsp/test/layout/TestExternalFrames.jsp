<%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		<h3>
			Test for embedded frames with external content. Switches to and from this view must not result in an error.
		</h3>

		<iframe
			height="480"
			src="http://www.top-logic.de"
			width="640"
		>
		</iframe>
	</layout:body>
</layout:html>
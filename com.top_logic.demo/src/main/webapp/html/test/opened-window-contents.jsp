<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="basic" prefix="basic"
%>
<html>
	<head>
	</head>
	<body id="c1">
		<basic:script>
			var h1 = document.createElement("h1");
			h1.appendChild(document.createTextNode("Window contents"));
			document.getElementById("c1").appendChild(h1);
			
			var p = document.createElement("p");
			p.appendChild(document.createTextNode(
					"Please stand by, while the contents is loaded, or close the window " +
					"immediately, before loading completes. The first action simulates a " +
					"regular window close of a completely initialized window. The second " +
			"simulates a close operation of an incompletely initialized window."));
			document.getElementById("c1").appendChild(p);
		</basic:script>
		<%
		out.flush();
		
		// Simulate slowly loading window contents.
		Thread.sleep(5000);
		%>
		<p>
			Loading completed.
		</p>
		<p>
			Close window to trigger an onunload.
		</p>
	</body>
</html>
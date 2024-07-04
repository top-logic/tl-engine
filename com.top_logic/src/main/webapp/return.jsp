<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.layout.structure.embedd.EmbeddedView.OnClose"
%><!DOCTYPE html>
<html>
<head>
<script type="text/javascript">
const iframe = window.frameElement;
if (iframe != null) {
	const controlId = iframe.getAttribute("id");
	
	/* AJAX engine of the parent window.*/
	const services = window.parent.services;
	
	const queryString = window.location.search;
	const searchParams = new URLSearchParams(queryString);
	
	var cmd = {};
	/* Pass query params from embedded application to embedding application.*/
	for (const key of searchParams.keys()) {
		cmd[key] = searchParams.get(key)
	}
	cmd["controlCommand"] = "<%= OnClose.ON_CLOSE %>";
	cmd["controlID"] = controlId;
	
	services.ajax.execute("dispatchControlCommand", cmd, services.ajax.USE_WAIT_PANE_IN_FORMULA);
}
</script>
<style type="text/css">
.tlCentered {
    margin: 0;
    position: absolute;
    top: 50%;
    left: 50%;
    margin-right: -50%;
    transform: translate(-50%, -50%);	
}
</style>
</head>
<body>
<div class="tlCentered">
	Der Vorgang wird abgeschlossen...
</div>
</body>
</html>
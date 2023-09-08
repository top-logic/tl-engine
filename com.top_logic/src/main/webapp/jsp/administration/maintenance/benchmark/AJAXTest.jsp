<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="basic" prefix="basic"
%><!DOCTYPE html>
<html>
	<head>
		<meta
			content="text/html; charset=ISO-8859-1"
			http-equiv="Content-Type"
		/>

		<title>
			AJAX Test Site
		</title>
		<link
			href="mainStyle.css"
			rel="stylesheet"
			type="text/css"
		/>

		<basic:script>
var ajaxSite = "ajax2.jsp";
var Loops = 100;
var counter = 0; 
var startTime = new Array();
var endTime = new Array();

var stopRunning = true;

function startAjax(){
    var theDate = new Date();
    startTime[counter] = theDate.getTime();
    //log("Starttime is " + startTime +"at counter " + counter);
    var http;
    if (window.XMLHttpRequest) {
        http = new XMLHttpRequest();
    } else if (window.ActiveXObject) {
        http = new ActiveXObject("Microsoft.XMLHTTP");
    }
    if (http != null) {
        http.onreadystatechange = function () {
            if (http.readyState == 4) {
                writeOut();
            }
        };
        http.open("GET", ajaxSite, /*async*/ true);
        http.send(null);
    }
}

function writeOut() {
    var theDate = new Date();
    endTime[counter] = theDate.getTime();
    counter ++;
    if (counter < Loops){
        startAjax();
    } else if(stopRunning) {
       calculate();
       stopRunning = false;
   }
   // log("Endtime is " + endTime +"at counter " + counter);
}

function calculate(){
     var resulttime = 0;
            
     for(var i = 0;i < Loops ;i++){
         resulttime += endTime[i] - startTime[i];  
     }
     resulttime = resulttime/Loops;
     log("Es wurden " + Loops +" XMLHTTPRequests durchgeführt, einer benötigte " + resulttime + "ms");
}
function log(message) {
    document.getElementById("console").appendChild(document.createTextNode(new Date() + ": " + message + "\r\n"));
}
        
function clearConsole() {
    var c = document.getElementById("console");
    while (c.lastChild != null) {
        c.removeChild(c.lastChild);
    }
    //log("Console cleared.");
    return false;
}

function reset(){
	counter = 0; 
	startTime = new Array();
	endTime = new Array();;
    clearConsole();
	stopRunning = true;


}       
</basic:script>
	</head>

	<body>
		<iframe id="AjaxID"
			height="350px"
			name="window"
			src="blank.jsp"
			width="100%"
		>
		</iframe>
		<button onclick="startAjax();">
			Ajax Test
		</button>
		<button onclick="reset();">
			Reset
		</button>
		<br/>
		<br/>
		<button onclick="location.href='ServerTime.html'">
			Back
		</button>

		<h2>
			Console output
		</h2>
		<pre id="console"
			style="border-style: solid; border-width: 1px; border-color: red;"
		>
		</pre>

		<div id="Ausgabe">
		</div>
	</body>
</html>
<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="basic" prefix="basic"
%><!DOCTYPE html>
<html>
	<head>
		<basic:script>
var TEST_LOOPS = 10;

var dontRuntoFar = true;
var counter = 0;
var serverTime = 0;
var requestClient = 0;
var responseClient = 0;
var deltaT = 0;
var deltaT2 = 0;
var responseTimeList = new Array();
var requestTimeList = new Array();
var serverTimeList = new Array();

var resolveRequest = new Array();

	
function connectionTest(){	
	var ping = document.getElementById("PingID"); 
	requestTimeList[counter] = getClientTime();
	//log("Request Time from Client: " + requestTimeList[counter] + " loop is at position"+ counter);
	ping.contentWindow.location.href="Ping.jsp";
}

/** Callback from Ping.jsp when page is loaded */
function setServerTime(aTime, save){
	if(save){
		responseTimeList[counter] = getClientTime();
		serverTime = aTime;
		serverTimeList[counter]= serverTime;
		//log("Server Time :" + serverTime +" loop is at position"+ counter );
		//log("Response Time from Client: " + responseTimeList[counter]);
	}
	
	if(counter < TEST_LOOPS){
	counter ++;
	connectionTest();
	
	} 

	if(counter == TEST_LOOPS && dontRuntoFar){
		dontRuntoFar = false;
		calculate();
	}
	
}

function getClientTime(){
	var d = new Date();
	return d.getTime();
}

function calculate(){

	//log ("Request Time :"  + requestTimeList.toString());
	//log ("Response Time :" + responseTimeList.toString());
	//log ("Server Time :"   + serverTimeList.toString());
	
	
	//Response Time from Client. We only calculate the time amount in the  pilotages.
	//Formula 
	//Trequest = (responseTime - requestTime) / 2 <- response has the higher value, so it stands in front.
	for(var i = 0 ; i < TEST_LOOPS; i++){
		resolveRequest[i] =(responseTimeList[i] - requestTimeList[i]) / 2;
	}
	
	//Add all resolveRequests and get a middeld value
	//log ("Resolve Request : " + resolveRequest.toString());
	for(var i = 0 ; i < TEST_LOOPS; i++){
		requestClient += resolveRequest[i];
	}
	 requestClient =  requestClient/ resolveRequest.length;
	 log("Durchschnittlicher Zeitverbrauch zum Server von Top Logic: " + requestClient + "ms");
	// When clock differnce is estimated to be null
	// Formula :
	// request =   serverTime -clientRequestTime 
	// response =  clientResponseTime - serverTime 
	var request = 0;
	var deltatLis = new Array();
	var deltatLis2 = new Array();
	for(var i = 0 ; i < TEST_LOOPS; i++){
		var extension = serverTimeList[i]-requestTimeList[i];
		deltatLis[i] = extension;
	}	
	for(var i = 0 ; i < TEST_LOOPS; i++){
		request += deltatLis[i]; 
	}
	request = request / deltatLis.length;
	
	for(var i = 0 ; i < TEST_LOOPS; i++){
		var extension = responseTimeList[i]-serverTimeList[i];
		deltatLis2[i] = extension;
	}	
		
	var response = 0;
	for(var i = 0 ; i < TEST_LOOPS; i++){
		response += deltatLis2[i]; 
	}
	response = response / deltatLis2.length;
	log ("Berechnung der Zeiten bei angenommenen synchronen Uhren :");
	log ("Durchschnittliche Anfragezeit: "  + request + "ms");
	log ("Durchschnittliche Antwortzeit: "  + response + "ms");
	
	// When clock differnce is estimated to be greater than null, we only can say something about the time difference.
	// Formula :
	// request =   serverTime - clientRequestTime - resolveTime
	
	var request = 0;
	var deltatLis = new Array();
	var deltatLis2 = new Array();
	for(var i = 0 ; i < TEST_LOOPS; i++){
		var extension = serverTimeList[i]-requestTimeList[i] -  requestClient;
		deltatLis[i] = extension;
	}	
	for(var i = 0 ; i < TEST_LOOPS; i++){
		request += deltatLis[i]; 
	}
	request = request / deltatLis.length;
	
	log ("Berechnung von Delta T bei angenommenen unsynchronen Uhren :");
	log ("Delta T beträgt : "  + request + "ms");

	//timeout();
}

function timeOut(){
	var t=setTimeout(function(){
	document.write("Es wurden 1000 abfragen gemacht."	);
	document.write("Der gemittelte Zeitverlust für die Leitungen beträgt :" 		+ requestClient +"<"+ "br" +">");
	document.write("Der Unterschied der beiden Uhren beträgt ungefähr :" 			+ deltaT +"<"+ "br" +">");
	}, 10000);
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
	dontRuntoFar = true;

	counter = 0;
	serverTime = 0;
	requestClient = 0;
	responseClient = 0;
	deltaT = 0;
	responseTimeList = new Array();
	requestTimeList = new Array();
	serverTimeList = new Array();
	deltatLis = new Array();
	resolveRequest = new Array();
	clearConsole();
	document.getElementById("PingID").contentWindow.location.href="blank.jsp";
	document.getElementById("PingID").window.location.reload;
}		
		</basic:script>
		<title>
			Server Response Site
		</title>
		<link
			href="mainStyle.css"
			rel="stylesheet"
			type="text/css"
		/>
	</head>
	<body>
		<br/>
		<iframe id="PingID"
			height="150px"
			name="window"
			src="blank.jsp"
			width="100%"
		>
		</iframe>
		<button onclick="connectionTest();">
			Connection Test
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
	</body>
</html>
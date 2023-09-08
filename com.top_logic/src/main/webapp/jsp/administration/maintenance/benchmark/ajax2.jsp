<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="basic" prefix="basic"
%><!DOCTYPE html>
<html>
	<head>
		<basic:script>
			function handleonloaded(){
				/*
				function foo(p){
					var x = 2* p;
					log(x);
					function bar(q){
						return x+q;
					}
					function bar2(f){
						return f*2;
					}
					
					return bar;
				}
				
				var bar1 = foo(4);
				var bar2 = foo(7);
				log(bar1.call());
				//log(bar2(2));
				*/
				var x = 13;
				var obj = {
					x: 42,
					y: [1,2,3],
					foo: function(p){
						return p + this.x;
					},
					z: 7
				};
				var myvar = obj.
				
				log(obj.foo(3));
				var foo1 = obj.foo;
				log(foo1(3));
				var foo1=obj.foo;
				
				var obj2 = {
					x:3,
					bar: obj.foo
				}
				log(obj2.bar(3));
				function log(message) {
					document.getElementById("console").appendChild(document.createTextNode(new Date() + ": " + message + "\r\n"));
				}
				
				function clearConsole() {
					var c = document.getElementById("console");
					while (c.lastChild != null) {
						c.removeChild(c.lastChild);
					}
				}
				
			}
		</basic:script>
		<title>
			Ajax Connection Test Site
		</title>
	</head>
	<body onload="handleonloaded()">
		<h2>
			Console output
		</h2>
		<pre id="console"
			style="border-style: solid; border-width: 1px; border-color: red;"
		>
		</pre>
	</body>
</html>
ECHO OFF

set java142="C:\Development\Java\jdk1.4.2\bin\java"

set clPath=.\WEB-INF\lib\izmcomjni.jar;
set clPath=%clPath%.\WEB-INF\lib\tl_reporting.jar;
set clPath=%clPath%.\WEB-INF\lib\tl_basic.jar;
set clPath=%clPath%.\WEB-INF\lib\log4j.jar;
set clPath=%clPath%.\WEB-INF\lib\j2ee.jar;
set clPath=%clPath%.\WEB-INF\lib\top-logic.jar;
set clPath=%clPath%.\WEB-INF\lib\ms-excel.jar;
set clPath=%clPath%.\WEB-INF\lib\ms-excel9.jar;
set clPath=%clPath%.\WEB-INF\lib\ms-excel10.jar;
set clPath=%clPath%.\WEB-INF\lib\ms-excel11.jar;
set clPath=%clPath%.\WEB-INF\lib\ms-excel12.jar;
set clPath=%clPath%.\WEB-INF\lib\ms-ppt.jar;
set clPath=%clPath%.\WEB-INF\lib\ms-ppt9.jar;
set clPath=%clPath%.\WEB-INF\lib\ms-ppt10.jar;
set clPath=%clPath%.\WEB-INF\lib\ms-ppt11.jar;
set clPath=%clPath%.\WEB-INF\lib\ms-ppt12.jar;
set clPath=%clPath%.\WEB-INF\lib\ms-word.jar;
set clPath=%clPath%.\WEB-INF\lib\ms-word9.jar;
set clPath=%clPath%.\WEB-INF\lib\ms-word10.jar;
set clPath=%clPath%.\WEB-INF\lib\ms-word11.jar;
set clPath=%clPath%.\WEB-INF\lib\ms-word12.jar

set mainClass=com.top_logic.reporting.remote.server.rmi.ServerRmiReporter

ECHO ON

java -cp %clPath% %mainClass% .

pause
/********************************************************************************************************************
 * $Id: GoogleMapsExample.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2008, Wolfgang Fahl, BITPlan GmbH (http://www.bitplan.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.DOMSerializer;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.meterware.httpunit.*;
/**
 * get a Route from Google Maps and convert it as a GPX file
 * useable by Garmin tracking devices - can be imported in MapSource software then ... 
 *
 */
public class GoogleMapsExample {
	
	/**
	 * shall we show debug information? 
	 */
	private boolean DEBUG=true;
	
	/**
	 * get a Route description as a Garmin compatible GPX file
	 * @param startPoint
	 * @param destPoint
	 */
	public void getRouteAsGPX(String startPoint,String destPoint, String filename,boolean withDisplay)  throws Exception {
		String kml=getRouteFromGoogleMaps(startPoint,destPoint,withDisplay,false);
		if (DEBUG) {
			System.out.println(kml);
		}
		// http://wiki.openstreetmap.org/index.php/JOSM
		// here is a so called open source kml to gpx converter
		// http://www.fish-track.com/?page_id=3
		Document gpxdoc=convertKMLtoGPX(kml);
		// output the result
		xmlSerialize(gpxdoc,filename);		
	}
	
	/**
	 * create the xml output for the given document 
	 * @param document
	 * @param filename
	 * @throws Exception
	 */
	public  void xmlSerialize(Document document,String filename) throws Exception {
			OutputFormat outputOptions = new OutputFormat();
			// outputOptions.setOmitXMLDeclaration(true);
			outputOptions.setIndent( 4 );
			outputOptions.setMethod( "xml" );
			//if (System.getProperty("os.name").startsWith("Windows")) {
			outputOptions.setEncoding("ISO-8859-1");
			Writer writer = new BufferedWriter(new FileWriter(new File(filename)));
			DOMSerializer serializer = new XMLSerializer( writer,outputOptions );
			serializer.serialize( document );
			writer.close();
	}		

	/**
	 * get the subnode with the given tagname
	 * @param parent
	 * @param tagName
	 * @return
	 */
	public Element getSubNode(Element parent,String tagName, boolean throwException) {
  	NodeList subNodes=parent.getElementsByTagName(tagName);
	  if (subNodes.getLength()!=1) {
	  	if (throwException)
	  		throw new RuntimeException("getSubNode failed for "+parent+" expected 1 child with tag name '"+tagName+"' but got "+subNodes.getLength());
	  	else
	  		return null;
	  }
	  return (Element)subNodes.item(0);
	}
	
	/**
	 * get the latitude and longitude from a route point
	 * @param kmlRoutePoint
	 * @return
	 */
	public String[] extractCoordinates(Element kmlRoutePoint) {
		String[] result=new String[2];
		Element point=getSubNode(kmlRoutePoint,"Point",false);
		if (point==null)
			return null;
		Element coordNode=getSubNode(point,"coordinates",true);
		String coords=coordNode.getTextContent();
		StringTokenizer coordSplitter=new StringTokenizer(coords,",",false);
		if (coordSplitter.countTokens()<=2) {
	  	throw new RuntimeException("extract coordinates failed for "+kmlRoutePoint+" expected at least two coordinates but got "+coordSplitter.countTokens()+" '"+coords+"'");			
		}
		result[0]=coordSplitter.nextToken();
		result[1]=coordSplitter.nextToken();
		return result;
	}
	
	/**
	 * convert the given kml file to gpx format
	 * @param kml - the kml version of the file
	 * @return
	 */
	public Document convertKMLtoGPX(String kml) throws Exception {
		DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
    DocumentBuilder        builder= factory.newDocumentBuilder();
    Document               kmldoc = builder.parse(new InputSource(new StringReader(kml)) );
    Document               gpxdoc = builder.newDocument();
    String comment="Converted by httpunit GoogleMapsExample";
    gpxdoc.appendChild(gpxdoc.createComment(comment));

    org.w3c.dom.Element root = gpxdoc.createElement("gpx");
  	root.setAttribute("xmlns", "http://www.topografix.com/GPX/1/1");
  	root.setAttribute("creator","KML2GPX Example by BITPlan");
  	root.setAttribute("version","1.1");
  	gpxdoc.appendChild(root);
  	
  	org.w3c.dom.Element metadata = gpxdoc.createElement("metadata");
  	org.w3c.dom.Element metadatalink = gpxdoc.createElement("link");
  	metadatalink.setAttribute("href","http://www.bitplan.com");
  	metadata.appendChild(metadatalink);
  	org.w3c.dom.Element metadatatext = gpxdoc.createElement("text");
  	metadatatext.setTextContent("BITPlan GmbH, Willich, Germany");
  	metadatalink.appendChild(metadatatext);
  	root.appendChild(metadata);
  	
  	org.w3c.dom.Element route = gpxdoc.createElement("rte");
  	root.appendChild(route);
  	NodeList routePoints=kmldoc.getElementsByTagName("Placemark");
  	for (int i=0;i<routePoints.getLength();i++) {
  		Element kmlRoutePoint=(Element)routePoints.item(i);
  		String name=getSubNode(kmlRoutePoint,"name",true).getTextContent();
  		if (DEBUG)
  			System.out.println("found route point "+i+": "+name);
  		
    	String coords[]=extractCoordinates(kmlRoutePoint);
    	if (coords!=null) {
	    	org.w3c.dom.Element routePoint = gpxdoc.createElement("rtept");
	    	routePoint.setAttribute("lon", coords[0] );
	    	routePoint.setAttribute("lat", coords[1] );    	
	    	org.w3c.dom.Element routePointName=gpxdoc.createElement("name");
	    	routePointName.setTextContent(name);
	    	routePoint.appendChild(routePointName);
	    	route.appendChild(routePoint);
    	}	
  	}
    return gpxdoc;
	}
	/**
	 * the url to use
	 */
  public static String url="http://maps.google.com/maps";
  /**
   * the directions string to look for
   */
  public static String directions="directions";
 
  /**
    This is how to use the example in germany:
    
  	GoogleMapsExample.url="http://maps.google.de/maps";
  	GoogleMapsExample.directions="Route berechnen";
  */	


	/**
	 * get a route from google maps with the given start and destination points
	 * @param startPoint
	 * @param destPoint
	 * @param withDisplay
	 * @param asKML
	 */
	public String getRouteFromGoogleMaps(String startPoint,String destPoint, boolean withDisplay, boolean asKML) throws Exception {
    // and now indirectly
    // create the conversation object which will maintain state for us
    WebConversation wc = new WebConversation();

    // Obtain the main page on the meterware web site
    WebRequest request = new GetMethodWebRequest(url);
    request.setParameter("output", "html");
    WebResponse response = wc.getResponse( request );
    if (withDisplay && DEBUG)
    	BrowserDisplayer.showResponseInBrowser(response);
    
    // find the link which contains the string for directions and click it
    WebLink directionsLink = response.getFirstMatchingLink( WebLink.MATCH_CONTAINED_TEXT, directions);
    if (directionsLink==null) {
    	System.err.println("could not find "+directions+" in response");
      if (DEBUG)
      	BrowserDisplayer.showResponseInBrowser(response);
    	System.exit(1);
    }
    response = directionsLink.click();
    if (withDisplay && DEBUG)
    	BrowserDisplayer.showResponseInBrowser(response);  
    WebForm lookupForm = response.getFormWithID("d_form"); 
    request = lookupForm.getRequest();
    request.setParameter( "saddr", startPoint );
    request.setParameter( "daddr", destPoint );
    response = wc.getResponse( request );
    if (withDisplay)
    	BrowserDisplayer.showResponseInBrowser(response);
    if (asKML) {
	  	// request.setParameter("output", "kml");
	    FormParameter parameter = lookupForm.getParameter( "output" );
	    FormControl outputControl=parameter.getControl();
	    outputControl.setAttribute("value", "kml");
	   	response = wc.getResponse( request );
	    if (withDisplay && DEBUG)
	    	BrowserDisplayer.showResponseInBrowser(response);
    }  
    return (response.getText());
	}
	
	/**
	 * get the distance between to given Zip codes
	 * @param 
	 */
	public String getDistance(String startPoint, String endPoint, boolean withDisplay) throws Exception {
		String route=getRouteFromGoogleMaps(startPoint,endPoint,withDisplay,false);
		String [] driveLocale={"Drive:","Fahrt:"};
		String [] unitLocale ={"mi","km"};
		for (int i=0;i<driveLocale.length;i++) {
			int drivePos=route.indexOf(driveLocale[i]);
			if (drivePos>0) {
				String driveString=route.substring(drivePos);
				int unitPos=driveString.indexOf(unitLocale[i]);
				if (unitPos>0) {
					String distanceString=driveString.substring(1, unitPos+unitLocale[i].length());
					int divPos;
					while ((divPos=distanceString.indexOf("<div>"))>0) {
						distanceString=distanceString.substring(divPos+5);
					}
					distanceString=distanceString.replace("&#160;"," ");
					return distanceString;
				}
			}
		}	
		return "?";
	}
	
  /**
   * Start the Route as GPX converter with the given command line parameters
   * display usage with and example if no parameters are given
   * @param params
   */
  public static void main( String[] params ) {
    try {
        if (params.length < 3) {
            System.out.println( "Usage: java RouteAsGPX [from] [to] ([filename]|'distance') [nodisplay]" );
            System.out.println( "      e.g. java RouteAsGPX sfo 94526 sfoexample.gpx");
            System.out.println( "         to get the route as a Garmin compatible GPX file");
            System.out.println( "      e.g. java RouteAsGPX sfo 94526 distance");
            System.out.println( "         to calculate the distance");
            String[] defaultParams={"sfo","94526","distance"};
            // defaultParams={"sfo","94526","sfoexample.gpx"};
            params=defaultParams;
            System.out.println( "will demonstrate usage with the route "+defaultParams[0]+" - "+defaultParams[1]+" and store to "+defaultParams[2]);
        }
        GoogleMapsExample routeAsGPX=new GoogleMapsExample();
        boolean withDisplay=true;
        if (params.length>=4)
        	withDisplay=false;
        String startPoint=params[0];
        String endPoint  =params[1];
        String filename  =params[2];
        if (filename.equals("distance")) {
        	String distanceS=routeAsGPX.getDistance(startPoint, endPoint, withDisplay);
        	// String distanceS=NumberFormat.getInstance().format(distance);
        	System.out.println("The distance between "+startPoint+" and "+endPoint+" is "+distanceS);
        } else {
        	routeAsGPX.getRouteAsGPX(startPoint, endPoint, filename,withDisplay);
        }	        
    } catch (Exception e) {
      System.err.println( "Exception: " + e );
      e.printStackTrace();
    }            
   }
}

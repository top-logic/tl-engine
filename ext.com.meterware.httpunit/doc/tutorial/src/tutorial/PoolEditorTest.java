package tutorial;

import com.meterware.httpunit.*;
import com.meterware.servletunit.*;

import java.util.Arrays;

import junit.framework.*;
import tutorial.persistence.BettingPool;

public class PoolEditorTest extends TestCase {

    public static void main( String args[] ) {
        junit.textui.TestRunner.run( suite() );
    }

    public static TestSuite suite() {
        return new TestSuite( PoolEditorTest.class );
    }

    public PoolEditorTest( String s ) {
        super( s );
    }


    public void setUp() throws Exception {
        BettingPool.reset();
    }


    public void testGetForm() throws Exception {
        ServletRunner sr = new ServletRunner( "web.xml" );
        ServletUnitClient client = sr.newClient();

        try {
            client.getResponse( "http://localhost/PoolEditor" );
            fail( "PoolEditor is not protected" );
        } catch (AuthorizationRequiredException e) {
        }

        client.setAuthorization( "aUser", "pool-admin" );
        client.getResponse( "http://localhost/PoolEditor" );
    }


    public void testFormAction() throws Exception {
        ServletRunner sr = new ServletRunner( "web.xml" );

        ServletUnitClient client = sr.newClient();
        client.setAuthorization( "aUser", "pool-admin" );
        WebResponse response = client.getResponse( "http://localhost/PoolEditor" );
        WebForm form = response.getFormWithID( "pool" );
        assertNotNull( "No form found with ID 'pool'", form );
        assertEquals( "Form method", "POST", form.getMethod() );
        assertEquals( "Form action", "", form.getAction() );
    }


    public void testFormContents() throws Exception {
        ServletRunner sr = new ServletRunner( "web.xml" );

        ServletUnitClient client = sr.newClient();
        client.setAuthorization( "aUser", "pool-admin" );
        WebResponse response = client.getResponse( "http://localhost/PoolEditor" );
        WebForm form = response.getFormWithID( "pool" );
        assertNotNull( "No form found with ID 'pool'", form );

        for (int i = 0; i < 10; i++) {
            assertTrue( "Missing home team " + i, form.isTextParameter( "home" + i ) );
            assertTrue( "Missing away team " + i, form.isTextParameter( "away" + i ) );
        }
        assertEquals( "Tie breaker values",
                      Arrays.asList( new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" } ),
                      Arrays.asList( form.getOptionValues( "tiebreaker" ) ) );
    }


    public void testSubmitButtons() throws Exception {
        ServletRunner sr = new ServletRunner( "web.xml" );
        ServletUnitClient client = sr.newClient();
        client.setAuthorization( "aUser", "pool-admin" );
        WebResponse response = client.getResponse( "http://localhost/PoolEditor" );

        WebForm form = response.getFormWithID( "pool" );
        assertNotNull( "No form found with ID 'pool'", form );

        assertEquals( "Number of submit buttons", 2, form.getSubmitButtons().length );
        assertNotNull( "Save button not found", form.getSubmitButton( "save", "Save" ) );
        assertNotNull( "Open Pool button not found", form.getSubmitButton( "save", "Open Pool" ) );
    }


    public void testPoolDisplay() throws Exception {
        BettingPool.getGames()[0].setAwayTeam( "New York Jets" );
        BettingPool.getGames()[0].setHomeTeam( "Philadelphia Eagles" );
        BettingPool.getGames()[2].setAwayTeam( "St. Louis Rams" );
        BettingPool.getGames()[2].setHomeTeam( "Chicago Bears" );
        BettingPool.setTieBreakerIndex(2);

        ServletRunner sr = new ServletRunner( "web.xml" );
        ServletUnitClient client = sr.newClient();
        client.setAuthorization( "aUser", "pool-admin" );
        WebResponse response = client.getResponse( "http://localhost/PoolEditor" );

        WebForm form = response.getFormWithID( "pool" );
        assertNotNull( "No form found with ID 'pool'", form );

        assertEquals( "Away team 0", "New York Jets", form.getParameterValue( "away0" ) );
        assertEquals( "Home team 0", "Philadelphia Eagles", form.getParameterValue( "home0" ) );
        assertEquals( "Away team 1", "", form.getParameterValue( "away1" ) );
        assertEquals( "Home team 1", "", form.getParameterValue( "home1" ) );
        assertEquals( "Away team 2", "St. Louis Rams", form.getParameterValue( "away2" ) );
        assertEquals( "Home team 2", "Chicago Bears", form.getParameterValue( "home2" ) );

        assertEquals( "Tie breaker game", "2", form.getParameterValue( "tiebreaker" ) );
    }


    public void testPoolEntry() throws Exception {
        ServletRunner sr = new ServletRunner( "web.xml" );
        ServletUnitClient client = sr.newClient();
        client.setAuthorization( "aUser", "pool-admin" );
        WebResponse response = client.getResponse( "http://localhost/PoolEditor" );

        WebForm form = response.getFormWithID( "pool" );
        assertNotNull( "No form found with ID 'pool'", form );
        WebRequest request = form.getRequest( "save", "Save" );

        request.setParameter( "away1", "Detroit Lions" );
        request.setParameter( "home1", "Denver Broncos" );
        request.setParameter( "tiebreaker", "1" );
        response = client.getResponse( request );
        form = response.getFormWithID( "pool" );

        assertEquals( "Away team 0", "", form.getParameterValue( "away0" ) );
        assertEquals( "Home team 0", "", form.getParameterValue( "home0" ) );
        assertEquals( "Away team 1", "Detroit Lions", form.getParameterValue( "away1" ) );
        assertEquals( "Home team 1", "Denver Broncos", form.getParameterValue( "home1" ) );

        assertEquals( "Tie breaker game", "1", form.getParameterValue( "tiebreaker" ) );
    }


    public void testPoolValidation() throws Exception {
        ServletRunner sr = new ServletRunner( "web.xml" );
        ServletUnitClient client = sr.newClient();
        client.setAuthorization( "aUser", "pool-admin" );
        WebResponse response = client.getResponse( "http://localhost/PoolEditor" );
        WebForm form = response.getFormWithID( "pool" );
        WebRequest request = form.getRequest( "save", "Open Pool" );

        request.setParameter( "away1", "Detroit Lions" );
        request.setParameter( "home1", "Denver Broncos" );
        request.setParameter( "home2", "Baltimore Ravens" );
        request.setParameter( "tiebreaker", "3" );
        InvocationContext context = client.newInvocation( request );

        PoolEditorServlet servlet = (PoolEditorServlet) context.getServlet();
        servlet.updateBettingPool( context.getRequest() );
        String[] errors = servlet.getValidationErrors();
        assertEquals( "Number of errors reported", 2, errors.length );
        assertEquals( "First error", "Tiebreaker is not a valid game", errors[0] );
        assertEquals( "Second error", "Game 2 has no away team", errors[1] );
    }


    public void testPoolOpenErrorDetection() throws Exception {
        ServletRunner sr = new ServletRunner( "web.xml" );
        ServletUnitClient client = sr.newClient();
        client.setAuthorization( "aUser", "pool-admin" );
        WebResponse response = client.getResponse( "http://localhost/PoolEditor" );
        WebForm form = response.getFormWithID( "pool" );
        WebRequest request = form.getRequest( "save", "Open Pool" );

        request.setParameter( "away1", "Detroit Lions" );
        request.setParameter( "home1", "Denver Broncos" );
        request.setParameter( "home2", "Baltimore Ravens" );
        request.setParameter( "tiebreaker", "3" );
        response = client.getResponse( request );

        WebTable errorTable = response.getTableStartingWith( "Cannot open pool for betting:" );
        assertNotNull( "No errors reported", errorTable );
        String[][] cells = errorTable.asText();
        assertEquals( "Number of error messages provided", 2, cells.length - 1 );
        assertEquals( "Error message", "Tiebreaker is not a valid game", cells[1][0] );
        assertEquals( "Error message", "Game 2 has no away team", cells[2][0] );
    }


    public void testGoodPoolOpen() throws Exception {
        ServletRunner sr = new ServletRunner( "web.xml" );
        ServletUnitClient client = sr.newClient();
        client.setAuthorization( "aUser", "pool-admin" );
        WebResponse response = client.getResponse( "http://localhost/PoolEditor" );
        WebForm form = response.getFormWithID( "pool" );
        WebRequest request = form.getRequest( "save", "Open Pool" );

        request.setParameter( "away1", "Detroit Lions" );
        request.setParameter( "home1", "Denver Broncos" );
        request.setParameter( "away3", "Indianapolis Colts" );
        request.setParameter( "home3", "Baltimore Ravens" );
        request.setParameter( "tiebreaker", "3" );
        client.getResponse( request );                                                // (1) ignore the response

        response = client.getResponse( "http://localhost/PoolEditor" );               // (2) retrieve the page separately
        form = response.getFormWithID( "pool" );
        assertNull( "Could still update the pool", form.getSubmitButton( "save" ) );  // (3) look for the buttons

        try {
            request = form.getRequest();
            request.setParameter( "home3", "Philadelphia Eagles" );                   // (4) try to change an entry
            fail( "Could still edit the pool" );
        } catch (IllegalRequestParameterException e) {}
    }

}

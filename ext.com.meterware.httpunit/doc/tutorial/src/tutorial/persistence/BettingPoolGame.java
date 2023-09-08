package tutorial.persistence;


public class BettingPoolGame {

    BettingPoolGame() {
        _homeTeam = _awayTeam = "";
    }


    public String getHomeTeam() {
        return _homeTeam;
    }


    public void setHomeTeam( String homeTeam ) {
        if (!BettingPool.isEditable()) throw new IllegalStateException( "The pool is not editable" );
        _homeTeam = homeTeam;
    }


    public String getAwayTeam() {
        return _awayTeam;
    }


    public void setAwayTeam( String awayTeam ) {
        if (!BettingPool.isEditable()) throw new IllegalStateException( "The pool is not editable" );
        _awayTeam = awayTeam;
    }


    private String _homeTeam = "";
    private String _awayTeam = "";
}

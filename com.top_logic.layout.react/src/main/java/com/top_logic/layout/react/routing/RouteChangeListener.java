package com.top_logic.layout.react.routing;

/**
 * Listener for route-forming navigation events within a {@link RoutingParticipant}.
 */
@FunctionalInterface
public interface RouteChangeListener {

    /**
     * Called when a route-forming navigation happened.
     */
    void onRouteChange(RoutingParticipant participant, RouteSegment newSegment);
}

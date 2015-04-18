package com.ws;

import com.ws.impl.BowlingScoring;

import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@Path("/")
public class BowlingScoringService extends Application {
    
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(BowlingScoring.class);
        return classes;

    }

}

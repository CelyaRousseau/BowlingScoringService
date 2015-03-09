package com.ws;

import java.util.EventListener;

/**
 * Created by Akronys on 22/02/2015.
 */

public class RollListener implements EventListener {

    public RollListener() {
        new Thread(new RollSubscriber()).start();
    }
}

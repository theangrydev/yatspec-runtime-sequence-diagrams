package io.github.theangrydev.yatspecruntimesequencediagrams.tracing;

import acceptance.realworld.userinterface.Application;
import acceptance.realworld.wiring.Wiring;
import org.junit.Test;

public class TracingTest {

    @Test
    public void example() {
        new Application(new Wiring()).start();
        System.out.println("messages = " + TracingAspect.getMessages());
    }

}

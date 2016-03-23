package io.github.theangrydev.yatspecruntimesequencediagrams;

import acceptance.realworld.userinterface.Application;
import acceptance.realworld.wiring.Wiring;
import com.googlecode.yatspec.junit.SpecResultListener;
import com.googlecode.yatspec.junit.SpecRunner;
import com.googlecode.yatspec.junit.WithCustomResultListeners;
import com.googlecode.yatspec.plugin.sequencediagram.ByNamingConventionMessageProducer;
import com.googlecode.yatspec.plugin.sequencediagram.SequenceDiagramGenerator;
import com.googlecode.yatspec.plugin.sequencediagram.SequenceDiagramMessage;
import com.googlecode.yatspec.plugin.sequencediagram.SvgWrapper;
import com.googlecode.yatspec.rendering.html.DontHighlightRenderer;
import com.googlecode.yatspec.rendering.html.HtmlResultRenderer;
import com.googlecode.yatspec.state.givenwhenthen.TestState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tracing.TraceType;
import tracing.TracingAspect;
import tracing.TracingEvent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.groupingBy;

@RunWith(SpecRunner.class)
public class TracingTest extends TestState implements WithCustomResultListeners {

    private SequenceDiagramGenerator sequenceDiagramGenerator;

    @Before
    public void collectSequenceDiagram() {
        sequenceDiagramGenerator = new SequenceDiagramGenerator();
    }

    @After
    public void generateSequenceDiagram() {
        Map<TraceType, List<TracingEvent>> collect = TracingAspect.getMessages().stream().collect(groupingBy(TracingEvent::getTraceType));
//        List<TracingEvent>

//        String.format("%s from % to %");
        Iterable<SequenceDiagramMessage> messages = new ByNamingConventionMessageProducer().messages(capturedInputAndOutputs);
        capturedInputAndOutputs.add("Sequence Diagram", sequenceDiagramGenerator.generateSequenceDiagram(messages));
    }

    @Test
    public void example() {
        new Application(new Wiring()).start();
        System.out.println("messages = " + TracingAspect.getMessages());
    }

    @Override
    public Iterable<SpecResultListener> getResultListeners() throws Exception {
        return singletonList(new HtmlResultRenderer()
                .withCustomHeaderContent(SequenceDiagramGenerator.getHeaderContentForModalWindows())
                .withCustomRenderer(SvgWrapper.class, new DontHighlightRenderer<>()));
    }
}

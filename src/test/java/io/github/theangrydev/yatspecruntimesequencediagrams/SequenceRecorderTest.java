package io.github.theangrydev.yatspecruntimesequencediagrams;

import com.googlecode.yatspec.junit.SpecResultListener;
import com.googlecode.yatspec.junit.SpecRunner;
import com.googlecode.yatspec.junit.WithCustomResultListeners;
import com.googlecode.yatspec.plugin.sequencediagram.ByNamingConventionMessageProducer;
import com.googlecode.yatspec.plugin.sequencediagram.SequenceDiagramGenerator;
import com.googlecode.yatspec.plugin.sequencediagram.SequenceDiagramMessage;
import com.googlecode.yatspec.plugin.sequencediagram.SvgWrapper;
import com.googlecode.yatspec.rendering.html.DontHighlightRenderer;
import com.googlecode.yatspec.rendering.html.HtmlResultRenderer;
import com.googlecode.yatspec.state.givenwhenthen.CapturedInputAndOutputs;
import com.googlecode.yatspec.state.givenwhenthen.TestState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import static com.googlecode.yatspec.plugin.sequencediagram.SequenceDiagramGenerator.getHeaderContentForModalWindows;

@RunWith(SpecRunner.class)
public class SequenceRecorderTest extends TestState implements WithCustomResultListeners {

    private static final boolean TRACE_METHOD_CALLS = true;

    private final SequenceRecorder sequenceRecorder = new SequenceRecorder("io.github.theangrydev", capturedInputAndOutputs);

    @Before
    public void setUp() {
        if (!TRACE_METHOD_CALLS) {
            return;
        }
        sequenceRecorder.traceMethodCalls();
    }

    @After
    public void generateSequenceDiagram() {
        if (!TRACE_METHOD_CALLS) {
            addSequenceDiagram(capturedInputAndOutputs, "Sequence Diagram");
            return;
        }
        CapturedInputAndOutputs traces = sequenceRecorder.callTraces();
        CapturedInputAndOutputs notCallTraces = sequenceRecorder.notCallTraces();
        addSequenceDiagram(notCallTraces, "Sequence Diagram");
        addSequenceDiagram(traces, "Call Trace");
        sequenceRecorder.stopTracingMethodCalls();
    }

    private void addSequenceDiagram(CapturedInputAndOutputs traces, String name) {
        Iterable<SequenceDiagramMessage> messages = new ByNamingConventionMessageProducer().messages(traces);
        SequenceDiagramGenerator sequenceDiagramGenerator = new SequenceDiagramGenerator();
        capturedInputAndOutputs.add(name, sequenceDiagramGenerator.generateSequenceDiagram(messages));
    }

    @Test
    public void records() {
        capturedInputAndOutputs.add("Call from A to B", "message");
        capturedInputAndOutputs.add("Call from B to C", "message");
        new TopLevel().test();
    }

    @Override
    public Iterable<SpecResultListener> getResultListeners() throws Exception {
        return Collections.singleton(new HtmlResultRenderer()
                .withCustomHeaderContent(getHeaderContentForModalWindows())
                .withCustomRenderer(SvgWrapper.class, new DontHighlightRenderer<>()));
    }
}

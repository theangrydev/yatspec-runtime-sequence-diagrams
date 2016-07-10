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
    private static final String TRACE_PREFIX = "Trace";

    private final SequenceRecorder sequenceRecorder = new SequenceRecorder(TRACE_PREFIX, "io.github.theangrydev", capturedInputAndOutputs);

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
            return;
        }
        CapturedInputAndOutputs traces = callTraces();
        CapturedInputAndOutputs withoutCallTraces = withoutCallTraces();
        removeTracePrefixes();
        addSequenceDiagram(traces, "Call Trace");
        addSequenceDiagram(withoutCallTraces, "Sequence Diagram");
        sequenceRecorder.stopTracingMethodCalls();
    }

    private void removeTracePrefixes() {
        for (String key : capturedInputAndOutputs.getTypes().keySet()) {
            if (!key.startsWith(TRACE_PREFIX)) {
                continue;
            }
            String value = capturedInputAndOutputs.getType(key, String.class);
            capturedInputAndOutputs.remove(key);
            capturedInputAndOutputs.add(key.substring(TRACE_PREFIX.length()), value);
        }
    }

    private CapturedInputAndOutputs withoutCallTraces() {
        CapturedInputAndOutputs withoutCallTraces = new CapturedInputAndOutputs();
        capturedInputAndOutputs.getTypes().entrySet().stream()
                .filter(entry -> !entry.getKey().startsWith(TRACE_PREFIX))
                .forEach(entry -> withoutCallTraces.add(entry.getKey(), entry.getValue()));
        return withoutCallTraces;
    }

    private CapturedInputAndOutputs callTraces() {
        CapturedInputAndOutputs traces = new CapturedInputAndOutputs();
        capturedInputAndOutputs.getTypes().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(TRACE_PREFIX))
                .forEach(entry -> traces.add(entry.getKey().substring(TRACE_PREFIX.length()), entry.getValue()));
        return traces;
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

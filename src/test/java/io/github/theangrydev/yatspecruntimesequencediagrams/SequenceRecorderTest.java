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
import com.googlecode.yatspec.state.givenwhenthen.TestState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import static com.googlecode.yatspec.plugin.sequencediagram.SequenceDiagramGenerator.getHeaderContentForModalWindows;

@RunWith(SpecRunner.class)
public class SequenceRecorderTest extends TestState implements WithCustomResultListeners {

    private static final boolean TRACE_METHOD_CALLS = false;

    private final SequenceRecorder sequenceRecorder = new SequenceRecorder(capturedInputAndOutputs);

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
        Iterable<SequenceDiagramMessage> messages = new ByNamingConventionMessageProducer().messages(capturedInputAndOutputs);
        SequenceDiagramGenerator sequenceDiagramGenerator = new SequenceDiagramGenerator();
        capturedInputAndOutputs.add("Sequence Diagram", sequenceDiagramGenerator.generateSequenceDiagram(messages));

        sequenceRecorder.stopTracingMethodCalls();
    }

    @Test
    public void records() {
        new TopLevel().test();
    }

    @Override
    public Iterable<SpecResultListener> getResultListeners() throws Exception {
        return Collections.singleton(new HtmlResultRenderer()
                .withCustomHeaderContent(getHeaderContentForModalWindows())
                .withCustomRenderer(SvgWrapper.class, new DontHighlightRenderer<>()));
    }
}

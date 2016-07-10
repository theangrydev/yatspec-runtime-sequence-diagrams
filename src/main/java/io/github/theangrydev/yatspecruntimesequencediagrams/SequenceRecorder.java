package io.github.theangrydev.yatspecruntimesequencediagrams;

import com.googlecode.yatspec.state.givenwhenthen.CapturedInputAndOutputs;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.ModifierReviewable;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;


public class SequenceRecorder extends SecurityManager {

    private static final String TRACE_PREFIX = "Trace";

    private final String targetPackage;
    private final CapturedInputAndOutputs capturedInputAndOutputs;

    private Instrumentation instrumentation;
    private ClassFileTransformer classFileTransformer;

    public SequenceRecorder(String targetPackage, CapturedInputAndOutputs capturedInputAndOutputs) {
        this.targetPackage = targetPackage;
        this.capturedInputAndOutputs = capturedInputAndOutputs;
    }

    public void traceMethodCalls() {
        instrumentation = ByteBuddyAgent.install();
        classFileTransformer = new AgentBuilder.Default()
                .type(nameStartsWith(targetPackage))
                .transform(interceptor())
                .installOnByteBuddyAgent();
    }

    public CapturedInputAndOutputs notCallTraces() {
        CapturedInputAndOutputs withoutCallTraces = new CapturedInputAndOutputs();
        capturedInputAndOutputs.getTypes().entrySet().stream()
                .filter(entry -> !entry.getKey().startsWith(TRACE_PREFIX))
                .forEach(entry -> withoutCallTraces.add(entry.getKey(), entry.getValue()));
        return withoutCallTraces;
    }

    public CapturedInputAndOutputs callTraces() {
        CapturedInputAndOutputs traces = new CapturedInputAndOutputs();
        capturedInputAndOutputs.getTypes().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(TRACE_PREFIX))
                .forEach(entry -> traces.add(entry.getKey().substring(TRACE_PREFIX.length()), entry.getValue()));
        return traces;
    }

    public void stopTracingMethodCalls() {
        instrumentation.removeTransformer(classFileTransformer);
        removeTracePrefixes();
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

    private AgentBuilder.Transformer interceptor() {
        return (builder, typeDescription, classLoader) -> builder
                .method(ModifierReviewable::isPublic)
                .intercept(MethodDelegation.to(this));
    }

    @SuppressWarnings("unused") // Invoked by ByteBuddy
    @RuntimeType
    public Object intercept(@This Object object, @Origin Method method, @AllArguments Object[] allArguments, @SuperCall Callable<?> actualMethodCall) throws Exception {
        Class<?> callingClass = callingClass();
        Class<?> declaringClass = method.getDeclaringClass();
        if (!callingClass.getName().startsWith(targetPackage) || !declaringClass.getName().startsWith(targetPackage)) {
            return actualMethodCall.call();
        }
        String from = callingClass.getSimpleName();
        String to = declaringClass.getSimpleName();
        String incomingMethodCall = TRACE_PREFIX + method.getName() + " from " + from + " to " + to;
        capturedInputAndOutputs.add(incomingMethodCall, Arrays.toString(allArguments));

        Object result = actualMethodCall.call();
        String resultOfMethodCall = TRACE_PREFIX + method.getName() + " result from " + to + " to " + from;
        capturedInputAndOutputs.add(resultOfMethodCall, result);
        return result;
    }

    private Class<?> callingClass() {
        return getClassContext()[3];
    }
}

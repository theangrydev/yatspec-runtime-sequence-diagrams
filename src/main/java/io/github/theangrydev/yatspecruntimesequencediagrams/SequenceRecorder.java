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

    private static final String TARGET_PACKAGE = "io";

    private final CapturedInputAndOutputs capturedInputAndOutputs;

    private Instrumentation instrumentation;
    private ClassFileTransformer classFileTransformer;

    public SequenceRecorder(CapturedInputAndOutputs capturedInputAndOutputs) {
        this.capturedInputAndOutputs = capturedInputAndOutputs;
    }

    public void traceMethodCalls() {
        instrumentation = ByteBuddyAgent.install();
        classFileTransformer = new AgentBuilder.Default()
                .type(nameStartsWith(TARGET_PACKAGE))
                .transform(interceptor())
                .installOnByteBuddyAgent();
    }

    public void stopTracingMethodCalls() {
        instrumentation.removeTransformer(classFileTransformer);
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
        if (!callingClass.getName().startsWith(TARGET_PACKAGE) || !declaringClass.getName().startsWith(TARGET_PACKAGE)) {
            return actualMethodCall.call();
        }
        String from = callingClass.getSimpleName();
        String to = declaringClass.getSimpleName();
        String incomingMethodCall = method.getName() + " from " + from + " to " + to;
        capturedInputAndOutputs.add(incomingMethodCall, Arrays.toString(allArguments));

        Object result = actualMethodCall.call();
        String resultOfMethodCall = method.getName() + " result from " + to + " to " + from;
        capturedInputAndOutputs.add(resultOfMethodCall, result);
        return result;
    }

    public Class<?> callingClass() {
        return getClassContext()[3];
    }
}

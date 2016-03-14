package com.github.rmannibucau.ohmyjs.service;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.Optional.ofNullable;

public abstract class BaseService implements AutoCloseable {
    private Future<Invocable> invocable;
    private Runnable destroyTask;

    protected void init(final String wrapperCode) {
        // can be long to parse the main js so let's do it async
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final ExecutorService es = Executors.newSingleThreadExecutor();
        invocable = es.submit(() -> {
            try (final InputStream is = loader.getResourceAsStream("ohmyjs/" + jsName())) {
                final ScriptEngineManager mgr = new ScriptEngineManager();
                final ScriptEngine engine = mgr.getEngineByExtension("js");

                // load babel
                engine.eval(new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8), estimatedJsSize()));
                ofNullable(wrapperCode).ifPresent(w -> {
                    try {
                        engine.eval(w);
                    } catch (final ScriptException e) {
                        throw new IllegalStateException(e);
                    }
                });


                Logger.getLogger(getClass().getName()).info(jsName().replace(".min.js","") + " ready!");

                // capture the invocable for future use
                return Invocable.class.cast(engine);
            } catch (final IOException | ScriptException e) {
                throw new IllegalStateException(e);
            }
        });
        es.shutdown();
        destroyTask = () -> {
            try {
                if (!es.awaitTermination(1, TimeUnit.MINUTES)) {
                    Logger.getLogger(getClass().getName()).warning("task in progress in " + jsName() + " but giving up after 1mn");
                }
            } catch (final InterruptedException e) {
                Thread.interrupted();
            }
        };
    }

    protected abstract int estimatedJsSize();

    protected abstract String jsName();

    protected abstract String getWrapper();

    public String transform(final String fileInput) {
        synchronized (invocable) { // this is not thread safe, normally used in a single thread of with cache so ok
            try {
                return String.valueOf(invocable.get().invokeFunction(getWrapper(), fileInput));
            } catch (final ExecutionException | ScriptException | NoSuchMethodException e) {
                throw new IllegalStateException(e);
            } catch (final InterruptedException e) {
                Thread.interrupted();
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public void close() {
        ofNullable(destroyTask).ifPresent(Runnable::run);
    }
}

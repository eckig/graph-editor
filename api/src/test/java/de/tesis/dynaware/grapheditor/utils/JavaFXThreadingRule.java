/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.utils;

import java.util.concurrent.CountDownLatch;

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javafx.application.Platform;

/**
 * A JUnit {@link Rule} for running tests on the JavaFX thread and performing JavaFX initialization. To include in your
 * test case, add the following code:
 *
 * <pre>
 * {@literal @}ClassRule
 * public static JavaFXThreadingRule jfxRule = new JavaFXThreadingRule();
 * </pre>
 *
 * This is a slightly modified version from
 * http://andrewtill.blogspot.com/2012/10/junit-rule-for-javafx-controller-testing.html
 *
 * @author Andy Till
 *
 */
public class JavaFXThreadingRule implements TestRule {

    /**
     * Flag for setting up the JavaFX, we only need to do this once for all tests.
     */
    private static boolean jfxIsSetup;

    @Override
    public Statement apply(final Statement statement, final Description description) {
        return new OnJFXThreadStatement(statement);
    }

    private static class OnJFXThreadStatement extends Statement {
        private final Statement statement;

        public OnJFXThreadStatement(final Statement aStatement) {
            statement = aStatement;
        }

        private Throwable rethrownException = null;

        @Override
        public void evaluate() throws Throwable {
            if (!jfxIsSetup) {
                setupJavaFX();
                jfxIsSetup = true;
            }
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        statement.evaluate();
                    } catch (final Throwable e) {
                        rethrownException = e;
                    }
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();

            // if an exception was thrown by the statement during evaluation,
            // then re-throw it to fail the test
            if (rethrownException != null) {
                throw rethrownException;
            }
        }

        protected void setupJavaFX() throws InterruptedException {
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await();
        }
    }
}

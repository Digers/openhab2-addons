/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.timer.internal.timer;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

import static org.junit.Assert.*;

public class TimerRunnerTest {
    Logger log = LoggerFactory.getLogger(TimerRunnerTest.class);

    TimerRunner runner;

    @Before
    public void setUp() throws Exception {
        runner = new TimerRunner();
    }

    @Test
    public void start() {
        runner.start();
    }

    @Test
    public void calculateNearest() {
        runner.getNewTimerInstance().updateWithString("1s");
        runner.getNewTimerInstance().startWithInstant(Instant.now().plus(Duration.ofSeconds(2)));
        runner.getNewTimerInstance().startWithInstant(Instant.now().plus(Duration.ofSeconds(10)));
        long time = runner.calculateNearestTime();
        log.info("Delay time is: {}", time);
        assertTrue("Timeout is less than a second", time <= 1000);
        assertTrue("Timeout is more than one ms", time > 100);
    }

    @Test
    public void calculateNearestWithSomeOld() {
        runner.getNewTimerInstance().startWithInstant(Instant.now().minus(Duration.ofSeconds(1)));
        runner.getNewTimerInstance().startWithInstant(Instant.now().plus(Duration.ofSeconds(10)));
        runner.getNewTimerInstance().startWithInstant(Instant.now().minus(Duration.ofSeconds(2)));
        runner.getNewTimerInstance().startWithInstant(Instant.now().minus(Duration.ofSeconds(2)));
        long time = runner.calculateNearestTime();
        log.info("Delay time is: {}", time);
        assertTrue("Timeout is one ms", time == 1);
    }

    @Test
    public void calculateNearestWithOnlyOld() {
        runner.getNewTimerInstance().startWithInstant(Instant.now().minus(Duration.ofSeconds(1)));
        runner.getNewTimerInstance().startWithInstant(Instant.now().minus(Duration.ofSeconds(10)));
        runner.getNewTimerInstance().startWithInstant(Instant.now().minus(Duration.ofSeconds(2)));
        runner.getNewTimerInstance().startWithInstant(Instant.now().minus(Duration.ofSeconds(2)));
        long time = runner.calculateNearestTime();
        log.info("Delay time is: {}", time);
        assertTrue("Timeout is one ms", time == 1);
    }

    @Test
    public void calculateNearestWithNoItems() {
        long time = runner.calculateNearestTime();
        log.info("Delay time is: {}", time);
        assertTrue("Timeout is max time", time == runner.MAX_WAIT_TIME);
    }

    @After
    public void tareDown() {
        runner.stop();
    }
}
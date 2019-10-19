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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Daniel Digerås
 */
public class TimerRunner {
    Logger log = LoggerFactory.getLogger(TimerRunner.class);

    public final long MAX_WAIT_TIME = 1000 * 60L;

    private Thread timeThread;
    private final Object lockObject;
    private final List<TimerInstance> timerList;

    public TimerRunner() {
        log.info("Starting");
        timerList = new ArrayList<>();
        lockObject = new Object();
        timeThread = new Thread(this::timerThreadMain);
        timeThread.setDaemon(true);
    }

    private void timerThreadMain() {
        while (!Thread.currentThread().isInterrupted()) {
            sleepUntilEvent();
            try {
                runTimers();
            } catch (Exception e) {
                log.error("Got unhandled exception in timer thread", e);
            }
        }
    }

    private void sleepUntilEvent() {
        long nextTimeout = calculateNearestTime();
        synchronized (lockObject) {
            try {
                log.debug("About to sleep {} ms", nextTimeout);
                lockObject.wait(nextTimeout);
            } catch (InterruptedException e) {
                log.info("Stopped thread");
                Thread.currentThread().interrupt();
            }
        }
    }

    private void runTimers() {
        final List<TimerInstance> timers;
        Instant now = Instant.now();
        synchronized (timerList) {
            timers = timerList.stream()
                    .filter(timer -> timer.shouldUpdate(now))
                    .peek(timer -> log.debug("Found timer to trigger: {}", timer))
                    .collect(Collectors.toList());
        }

        for (TimerInstance timer : timers) {
            try {
                log.debug("Trigger timer {}", timer);
                timer.trigger(now);
            } catch (Exception e) {
                log.error("Got exception when processing timer", e);
            }
        }
    }

    public long calculateNearestTime() {
        synchronized (timerList) {
            Instant now = Instant.now();
            return timerList.stream()
                    .filter(TimerInstance::isActive)
                    .map(TimerInstance::nextTime)
                    .reduce((a, b) -> a.isBefore(b) ? a : b)
                    .map(nextTime -> Duration.between(now, nextTime))
                    .map(Duration::toMillis)
                    .map(tim -> tim < 1 ? 1 : tim)
                    .orElse(MAX_WAIT_TIME);
        }
    }

    public TimerInstance getNewTimerInstance() {
        TimerInstance timerInstance = new TimerInstance(this);
        synchronized (timerList) {
            timerList.add(timerInstance);
        }
        return timerInstance;
    }

    void removeTimerInstance(TimerInstance timerInstance) {
        synchronized (timerList) {
            timerList.remove(timerInstance);
        }
    }

    public void reload() {
        log.debug("Timer runner reloaded");
        synchronized (lockObject) {
            lockObject.notifyAll();
        }
    }

    public void start() {
        timeThread.start();
    }

    public void stop() {
        timeThread.interrupt();
    }

}

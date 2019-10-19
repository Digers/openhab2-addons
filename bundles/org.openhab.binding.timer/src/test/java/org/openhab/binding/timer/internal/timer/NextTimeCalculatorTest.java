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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openhab.binding.timer.internal.timer.NextTimeCalculator;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class NextTimeCalculatorTest {
    private NextTimeCalculator nextTimeCalculator;
    private Instant from;
    private Duration duration;
    private String changeString;
    private boolean absolute;


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Collection<Object[]> params = new ArrayList<>();
        params.add(new Object[]{Duration.ofSeconds(1), "+1s", false});
        params.add(new Object[]{Duration.ofSeconds(10), "+10s", false});
        params.add(new Object[]{Duration.ofMinutes(1), "+1m", false});
        params.add(new Object[]{Duration.ofMinutes(1), "+60s", false});
        params.add(new Object[]{Duration.ofHours(2), "+1h58m120s", false});
        params.add(new Object[]{Duration.ofSeconds(-1), "-1s", false});
        params.add(new Object[]{Duration.ofMinutes(2), "1m60s", true});
        params.add(new Object[]{Duration.ofMinutes(3), "180s", true});
        params.add(new Object[]{Duration.ZERO, "", false});

        return params;
    }

    public NextTimeCalculatorTest(Duration duration, String changeString, boolean absolute) {
        this.from = Instant.now();
        this.duration = duration;
        this.changeString = changeString;
        this.absolute = absolute;
        nextTimeCalculator = new NextTimeCalculator();
    }

    @Test
    public void calculateNow() {
        assertEquals(from.plus(duration), nextTimeCalculator.calculate(from, from, changeString));
    }

    @Test
    public void calculateWithPositiveOffset() {
        if (absolute) {
            assertEquals(from.plus(duration).plusSeconds(30), nextTimeCalculator.calculate(from.plusSeconds(30), from, changeString));
        } else {
            assertEquals(from.plus(duration), nextTimeCalculator.calculate(from.plusSeconds(30), from, changeString));
        }
    }
}
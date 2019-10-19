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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Daniel Digerås
 */
public class NextTimeCalculator {
    private final Logger log = LoggerFactory.getLogger(NextTimeCalculator.class);
    private final Pattern pattern;
    enum Direction {
        PLUS,
        MINUS,
        ABSOLUTE
    }

    public Instant calculate(Instant now, Instant from, String command) {
        log.trace("Calculate from command {}", command);
        if (command.length() == 0) {
            log.error("Could not parse command, expects non zero length");
            return from;
        }

        final Direction direction = getDirectionFromString(command);
        final Matcher stringMatcher = pattern.matcher(command);
        Duration durationChange = Duration.ZERO;

        while (stringMatcher.find()) {
            durationChange = getDuration(stringMatcher, durationChange);
        }

        if (direction == Direction.MINUS) {
            durationChange = durationChange.negated();
        }

        if (direction == Direction.ABSOLUTE) {
            return now.plus(durationChange);
        } else {
            return from.plus(durationChange);
        }
    }

    private Direction getDirectionFromString(String command) {
        Direction direction;
        switch (command.charAt(0)) {
            case '+':
                direction = Direction.PLUS;
                break;
            case '-':
                direction = Direction.MINUS;
                break;
            default:
                direction = Direction.ABSOLUTE;
        }
        return direction;
    }

    private Duration getDuration(Matcher m, Duration d) {
        int number = Integer.parseInt(m.group(1));
        char unit = m.group(2).toLowerCase().charAt(0);

        switch (unit) {
            case 's':
                d = d.plusSeconds(number);
                break;
            case 'm':
                d = d.plusMinutes(number);
                break;
            case 'h':
                d = d.plusHours(number);
                break;
            case 'd':
                d = d.plusDays(number);
                break;
            default:
                log.error("Could not find type");
        }
        return d;
    }

    public NextTimeCalculator() {
        pattern = Pattern.compile("(\\d+)([sSmMhHdD]{1})");
    }

}

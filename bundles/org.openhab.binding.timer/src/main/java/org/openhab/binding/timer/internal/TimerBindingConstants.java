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
package org.openhab.binding.timer.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The {@link TimerBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Daniel Digerås - Initial contribution
 */
@NonNullByDefault
public class TimerBindingConstants {

    private static final String BINDING_ID = "timer";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_TIMER = new ThingTypeUID(BINDING_ID, "timer");

    // List of all Channel ids
    public static final String ENDTIME = "endtime";
    public static final String TIMELEFT = "timeleft";
    public static final String DURATION = "duration";
    public static final String AUTOMATIC = "automatic";

    // List of all custom channel types
    public static final String SWITCH_TYPE = "switch";
    public static final String DIMMER_TYPE = "dimmer";
    public static final String NUMBER_TYPE = "number";

    public static Set<String> getCustomTypes() {
        Set<String> customTypes = new HashSet<>();
        customTypes.add(SWITCH_TYPE);
        customTypes.add(DIMMER_TYPE);
        customTypes.add(NUMBER_TYPE);
        return customTypes;
    }
}

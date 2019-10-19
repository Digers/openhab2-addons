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

import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link TimerConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Daniel Digerås - Initial contribution
 */
public class TimerConfiguration {

    /**
     * Sample configuration parameter. Replace with your own.
     */
    public boolean persistent;
    public String defaultTime;
    public @Nullable String maxTime;
}

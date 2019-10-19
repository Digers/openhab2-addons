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

import static org.openhab.binding.timer.internal.TimerBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.timer.internal.timer.TimerInstance;
import org.openhab.binding.timer.internal.timer.TimerRunner;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TimerHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Daniel Digerås - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.timer", service = ThingHandlerFactory.class)
public class TimerHandlerFactory extends BaseThingHandlerFactory {
    Logger log = LoggerFactory.getLogger(TimerHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_TIMER);

    private @Nullable TimerRunner timerRunner;
    private Map<String, TimerInstance> timers;

    public TimerHandlerFactory() {
        super();
        timers = new HashMap<>();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        boolean supported = SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
        return supported;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        ThingUID thingUID = thing.getUID();

        log.debug("create thing: {}", thing.getUID());
        if (THING_TYPE_TIMER.equals(thingTypeUID)) {
            if (!timers.containsKey(thingUID.getId())) {
                timers.put(thingUID.getId(), timerRunner.getNewTimerInstance());
            }

            return new TimerHandler(thing, timers.get(thingUID.getId()));
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        super.removeHandler(thingHandler);
        log.debug("remove handler");
        if (thingHandler instanceof TimerHandler) {
            log.debug("Free timer handler resources");
            TimerInstance timerInstance = timers.remove(thingHandler.getThing().getUID().getId());
            if (timerInstance != null) {
                timerInstance.dispose();
            }
        }
    }

    @Override
    public void removeThing(ThingUID thingUID) {
        super.removeThing(thingUID);
        log.debug("remove thing: {}", thingUID);
        TimerInstance timerInstance = timers.remove(thingUID.getId());
        if (timerInstance != null) {
            timerInstance.dispose();
        }
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        log.debug("Activate timer handler factory");
        timerRunner = new TimerRunner();
        timerRunner.start();
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
        log.debug("Deactivate timer handler factory");
        timerRunner.stop();
        timerRunner = null;
    }
}

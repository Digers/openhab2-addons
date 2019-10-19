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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.timer.internal.channelconfigurations.NumericTimerChannelConfiguration;
import org.openhab.binding.timer.internal.channelconfigurations.SwitchTimerChannelConfiguration;
import org.openhab.binding.timer.internal.channelconfigurations.TimerChannelConfiguration;
import org.openhab.binding.timer.internal.timer.TimerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The {@link TimerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Digerås - Initial contribution
 */
@NonNullByDefault
public class TimerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TimerHandler.class);

    private static final String TIMER_VALUE_PROP = "timerValue";

    private @Nullable TimerConfiguration config;
    private @Nullable Map<String, TimerChannelConfiguration> channelConfig;
    private TimerInstance timerInstance;
    private boolean automatic;
    private String duration;

    public TimerHandler(Thing thing, TimerInstance timerInstance) {
        super(thing);
        logger.debug("Create thing class for {}", thing.getUID());
        this.timerInstance = timerInstance;
        automatic = true;
        duration = "";
        timerInstance.setUpdaterCallback(timer -> updateState(TIMELEFT, StringType.valueOf(timer.toFuzzyRemainingString())));
        timerInstance.setTriggerCallback(() -> {
            deactivateChannels();
            updateProperty(TIMER_VALUE_PROP, timerInstance.toString());
        });
        logger.debug("Properties for {} is: {}", thing.getUID(), thing.getProperties());
    }

    private void deactivateChannels() {
        if (channelConfig != null) {
            channelConfig.forEach((channelID, config1) -> {
                postCommand(channelID, config1.getCommandFor(false));
                updateState(channelID, config1.getStateFor(false));
            });
        }
    }

    private void activateConfiguredChannels() {
        channelConfig.forEach((channelID, config1) -> {
            //postCommand(channelID, config1.getCommandFor(true));
            updateState(channelID, config1.getStateFor(true));
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("Timer handler channel:{} received command update: {}", channelUID, command);
        logger.info("ChannelTypeId: {} Kind: {}", thing.getChannel(channelUID).getChannelTypeUID(), thing.getChannel(channelUID).getKind());

        String channelID = channelUID.getId();

        if (command instanceof RefreshType) {
            channelRefresh(channelID);
            return;
        }

        if (ENDTIME.equals(channelID)) {
            if (command instanceof DateTimeType) {
                ZonedDateTime newTime = ((DateTimeType) command).getZonedDateTime();
                Instant f = Instant.from(newTime);
                if (!f.isBefore(Instant.now())) {
                    timerInstance.startWithInstant(f);
                }
                channelRefresh(TIMELEFT);
                channelRefresh(ENDTIME);
            }
        }

        else if (TIMELEFT.equals(channelID)) {
            if (command instanceof StringType) {
                String timeChangeString = command.toFullString();
                timerInstance.updateWithString(timeChangeString);
                if (timerInstance.isActive()) {
                    activateConfiguredChannels();
                }
                ZonedDateTime endTime = timerInstance.getEndTime().atZone(ZoneId.systemDefault());
                updateState(ENDTIME, new DateTimeType(endTime));
                logger.debug("Updated timer with: {}", timeChangeString);
                updateProperty(TIMER_VALUE_PROP, timerInstance.toString());
                channelRefresh(TIMELEFT);
                channelRefresh(ENDTIME);
            }
        }

        else if (DURATION.equals(channelID)) {
            duration = command.toFullString().trim();
            if (duration.isEmpty()) {
                duration = config.defaultTime;
            }
            channelRefresh(DURATION);
        }

        else if (AUTOMATIC.equals(channelID)) {
            if (command instanceof OnOffType) {
                automatic = command == OnOffType.ON;
                logger.debug("Changed automatic to: {}", automatic);
            }
        } else {
            TimerChannelConfiguration config1 = channelConfig.get(channelID);
            if (config1 != null && automatic) {
                if (command.equals(config1.getCommandFor(false)) && timerInstance.isActive()) {
                    logger.debug("Disable timer from channel {}", channelID);
                    timerInstance.cancel();
                    updateProperty(TIMER_VALUE_PROP, timerInstance.toString());
                    channelRefresh(ENDTIME);
                    channelRefresh(TIMELEFT);
                    deactivateChannels();
                } else if (command.equals(config1.getCommandFor(true)) && !timerInstance.isActive()) {
                    logger.debug("Enable timer with {} from channel {}", config.defaultTime, channelID);
                    timerInstance.updateWithString(getAutoTime());
                    updateProperty(TIMER_VALUE_PROP, timerInstance.toString());
                    channelRefresh(ENDTIME);
                    channelRefresh(TIMELEFT);
                    channelConfig.forEach((channelx, config2) -> {
                        if(!channelx.equals(channelID)) {
                            updateState(channelx, config2.getStateFor(true));
                        }
                    });
                    activateConfiguredChannels();
                }
                thing.getChannel(channelUID).getKind();
                logger.debug("Handle other channel");
            }
        }
    }

    private String getAutoTime() {
        if (duration != null && !duration.isEmpty()) {
            return duration;
        } else {
            return config.defaultTime;
        }
    }

    private void handlePredefinedCommands(ChannelUID channelUID, Command command) {

    }

    private void channelRefresh(String channel) {
        if (ENDTIME.equals(channel)) {
            updateState(channel, new DateTimeType(timerInstance.getEndTime().atZone(ZoneId.systemDefault())));
        } else if (TIMELEFT.equals(channel)) {
            updateState(TIMELEFT, StringType.valueOf(timerInstance.toFuzzyRemainingString()));
        } else if (DURATION.equals(channel)) {
            updateState(DURATION, StringType.valueOf(duration));
        } else if (AUTOMATIC.equals(channel)) {
            updateState(AUTOMATIC, OnOffType.from(automatic));
        } else if (channelConfig.containsKey(channel)) {
            logger.debug("Set channel setting");
            TimerChannelConfiguration config1 = channelConfig.get(channel);
            updateState(channel, config1.getStateFor(timerInstance.isActive()));
        }
    }

    private void updateAllStates() {
        ZonedDateTime endTime = timerInstance.getEndTime().atZone(ZoneId.systemDefault());
        updateState(ENDTIME, new DateTimeType(endTime));
        updateState(TIMELEFT, StringType.valueOf(timerInstance.toFuzzyRemainingString()));
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        logger.info("Timer handler channel:{} received state update: {}", channelUID, state);
        super.updateState(channelUID, state);
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        config = getConfigAs(TimerConfiguration.class);

        channelConfig = thing.getChannels().stream()
                .filter(this::channelIsCustom)
                .peek(channel -> logger.debug("channel config: {}", channel.getConfiguration()))
                .collect(Collectors.toMap(
                        x -> x.getUID().getId(),
                        channel -> {
                            switch (channel.getChannelTypeUID().getId()) {
                                case SWITCH_TYPE:
                                    return channel.getConfiguration().as(SwitchTimerChannelConfiguration.class);
                                case NUMBER_TYPE:
                                case DIMMER_TYPE:
                                    return channel.getConfiguration().as(NumericTimerChannelConfiguration.class);
                                default:
                                    return channel.getConfiguration().as(SwitchTimerChannelConfiguration.class);
                            }
                        }));

        logger.debug("Mapped channels: {}", channelConfig.keySet());

        updateStatus(ThingStatus.ONLINE);

        String lastTimerValue = thing.getProperties().get(TIMER_VALUE_PROP);
        if (lastTimerValue != null) {
            logger.debug("Recalling with value {}", lastTimerValue);
            timerInstance.recall(lastTimerValue);
        }

        if (duration.isEmpty()) {
            duration = config.defaultTime;
        }

        logger.debug("Finished initializing!");
    }

    private boolean channelIsCustom(Channel channel) {
        String id = channel.getChannelTypeUID().getId();
        return getCustomTypes().contains(id);
    }
}

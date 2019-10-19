package org.openhab.binding.timer.internal.channelconfigurations;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

public class SwitchTimerChannelConfiguration extends TimerChannelConfiguration {
    public boolean restCondition;
    public boolean turnOnState;

    @Override
    public State getStateFor(boolean active) {
        return OnOffType.from(active ? turnOnState : restCondition);
    }

    @Override
    public Command getCommandFor(boolean active) {
        return OnOffType.from(active ? turnOnState : restCondition);
    }
}

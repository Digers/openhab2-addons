package org.openhab.binding.timer.internal.channelconfigurations;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

public class NumericTimerChannelConfiguration extends TimerChannelConfiguration {
    @Override
    public State getStateFor(boolean active) {
        return DecimalType.valueOf(active ? "100" : "0"); // TODO actually have real values
    }

    @Override
    public Command getCommandFor(boolean active) {
        return DecimalType.valueOf(active ? "100" : "0"); // TODO actually have real values;
    }
}

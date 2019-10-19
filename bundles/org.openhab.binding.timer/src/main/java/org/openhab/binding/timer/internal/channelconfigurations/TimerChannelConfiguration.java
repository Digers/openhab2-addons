package org.openhab.binding.timer.internal.channelconfigurations;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

public abstract class TimerChannelConfiguration {
    public boolean managed;
    public boolean activateOnRestChange;

    public abstract State getStateFor(boolean active);
    public abstract Command getCommandFor(boolean active);
}

package org.openhab.binding.timer.internal.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.function.Consumer;

public class TimerInstance {
    private static final Logger log = LoggerFactory.getLogger(TimerInstance.class);

    public Instant getEndTime() {
        return endTime;
    }

    private Instant endTime;
    private Instant nextUpdateTime;

    private boolean isActive;
    private TimerRunner timerRunner;
    private Runnable triggerCallback;
    private Consumer<TimerInstance> updaterCallbackConsumer;

    TimerInstance(TimerRunner timerRunner) {
        endTime = Instant.now();
        this.timerRunner = timerRunner;
        isActive = false;
    }

    public void startWithInstant(Instant instant) {
        endTime = instant;
        isActive = true;
        recalculateTimes();
        timerRunner.reload();
    }

    public void updateWithString(String command) {
        Instant now = Instant.now();
        Instant calculatedEndTime;
        if (isActive) {
            calculatedEndTime = new NextTimeCalculator().calculate(now, endTime, command);
            if (calculatedEndTime.isBefore(now)) {
                endTime = now;
            } else {
                endTime = calculatedEndTime;
            }
        } else {
            calculatedEndTime = new NextTimeCalculator().calculate(now, now, command);
            if (calculatedEndTime.isAfter(now)) {
                endTime = calculatedEndTime;
                isActive = true;
            }
        }
        recalculateTimes();
        timerRunner.reload();
    }

    private void recalculateTimes() {
        Duration timeToEnd = Duration.between(Instant.now(), endTime);
        Instant xTime;
        if (timeToEnd.toMinutes() > 5) {
            xTime = Instant.now().plus(1, ChronoUnit.MINUTES);
        } else {
            xTime = Instant.now().plus(1, ChronoUnit.SECONDS);
        }

        if (xTime.isAfter(endTime)) {
            xTime = endTime;
        }

        nextUpdateTime = xTime;
    }

    public Instant nextTime() {
        if (nextUpdateTime == null) {
            recalculateTimes();
        }
        return nextUpdateTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean shouldUpdate(Instant now) {
        if (nextUpdateTime == null) {
            recalculateTimes();
        }
        return nextUpdateTime.isBefore(now) && isActive;
    }

    private boolean shouldTrigger(Instant now) {
        return endTime.isBefore(now) && isActive;
    }

    public void trigger(Instant now) {
        if (shouldTrigger(now)) {
            isActive = false;
            if (triggerCallback != null) {
                triggerCallback.run();
            }
        }

        if (updaterCallbackConsumer != null) {
            updaterCallbackConsumer.accept(this);
        }
        recalculateTimes();
    }

    public String toFuzzyRemainingString() {
        StringBuilder builder = new StringBuilder();
        Duration timeLeft = Duration.between(Instant.now(), endTime);
        if (timeLeft.isNegative() || timeLeft.getSeconds() == 0) {
            builder.append("stopped");
        } else if (timeLeft.toHours() > 0) {
            long hours = timeLeft.toHours();
            builder.append(hours);
            builder.append("h ");
            timeLeft = timeLeft.minus(hours, ChronoUnit.HOURS);
            builder.append(timeLeft.toMinutes());
            builder.append("m");
        } else if (timeLeft.toMinutes() > 5) {
            builder.append(timeLeft.toMinutes());
            builder.append("m");
        }  else if (timeLeft.toMinutes() > 0) {
            builder.append(timeLeft.toMinutes());
            builder.append("m ");
            timeLeft = timeLeft.minus(timeLeft.toMinutes(), ChronoUnit.MINUTES);
            builder.append(timeLeft.getSeconds() + 1);
            builder.append("s");
        } else {
            builder.append(timeLeft.getSeconds() + 1);
            builder.append("s");
        }
        return builder.toString();
    }

    public void cancel() {
        isActive = false;
        endTime = Instant.now();
    }

    public void dispose() {
        isActive = false;
        timerRunner.removeTimerInstance(this);
    }

    public void setTriggerCallback(Runnable triggerCallback) {
        this.triggerCallback = triggerCallback;
    }

    public void setUpdaterCallback(Consumer<TimerInstance> updaterCallbackConsumer) {
        this.updaterCallbackConsumer = updaterCallbackConsumer;
    }

    public String toString() {
        return endTime.toString() + "$" + isActive;
    }

    public void recall(String value) {
        String[] decomposed = value.split("\\$");
        if (decomposed.length != 2) {
            log.error("Malformed string, cant recall timer");
        }

        Instant newTime = Instant.parse(decomposed[0]);
        boolean newIsActive = decomposed[1].equals("true");

        endTime = newTime;
        recalculateTimes();
        isActive = newIsActive;
        timerRunner.reload();
    }
}

#Timer function

### Channels:
* ActiveHigh:Switch - Is set to high when timer is running
* ActiveLow:Switch  - Is set to low when timer is running
* TriggerOn:Switch - Is pulsed when the timer starts
* TriggerOff:Switch - Is pulsed when the timer stops
* CalendarTime:Text - The time when the timer expires, the time the timer previously expired.
* TimeLeft:Text - The time left in human readable format, Write to set another time
* Duration:Text - The absolute duration of the timer, overwrites DefaultTime.
* Automatic:Switch - If the timer will be run or not. Not applicable for when the timer is started
                   manually.


### Thing properties:
* Persistent - The timer survives OpenHAB restarts or not
* ReactToStateChange - If timer should be enabled when receiving state update
* ReactToCommandChange - If timer should be enabled when receiving commands
* ResetTimeOnTrigger - If true, an on trigger will also reset the on time effectively extending the time
* DefaultTime - The preconfigured default time
* MaxTime - Optional maximum time the timer might be on, more specifically the maximum value time left can take.

### Channel properties
* restCondition - What the timer should consider off or expired
* turnOnState - The state the channel should take when the timer starts
* activateOnRestChange - 
* timerMode - 
* managed - 

## Operation:

### Time string composition:
* Begins with "+": will add to the time
* Begins with "-": will subtract from the time
* Begins with "": will set the absolute time
* "s" - seconds
* "m" - minutes
* "h" - hours

All values are additive: "1h60m" is two hours, "1h120m" is three hours.


### Example add more timeout: 
* TimeLeft -> "+10m" - add 10 minutes to the timer
* TimeLeft -> "+1h5s" - add 1 hour 5 seconds to the timer
* TimeLeft -> "1h" - Set the timer to 1 hour. This will be resetted next run
* TimeLeft -> "0" - No time left - the timer will be reset

If the timer is inactive and TimeLeft is set to something, the timer will activate.

Duration = 1 hour
Duration -> "+10m" - Duration is 1 hour and 10 minutes
Duration -> "-10m" - Duration is 50 minutes
Duration -> "2h30m" - Duration is 2 hours and 30 minutes


Duration and TimeLeft can never be negative. If a subtraction happens where the
result would be negative, tha resulting value will be zero.

If the duration is set to zero, the timer will when activated directly deactvate again.

Due to the nature of IoT devices the timer resolution will be in seconds

Use cases:
1. Towel heater
* Towel heater is off.
* Towel heater is turned on.
* Timer begins to run
* CalendarTime and duration begins to tick.
* Time is extended
* Timer reaches zero
* Towel heater is turned off

2. Turn on light in the nights
* Wake up.
* Click turn on night lights
* Timer starts
* after x minutes timer reaches zero
* lights are turned off again



# Pitch
Hello everybody!

Lets talk timeouts and rules with timers.

At my home I have two towel heaters, as well as some lights that should turn off after a set time.
What I have done previously is to use timers in rules. I have however found them a bit cumbersome
and "rigid".

Let me take the towel heater as an example:
* After power on, I want it on four hours. (Quite simple with rules)
* I want to see the time when it turns off (Quite simple with rules)
* I would really like to add or substract time from the timer. Ex. if I shower at the end of the
  four hours and want to extend the time two additional hours. (Possible but more inolved code)
* Don't mess up the timer if I rewrite the rule file. (Is this possible?)

Now expand this to 3-5 or more things and the rules are becoming hard to follow.
To simplify the rules and at the same time get all features I want, I have created a timer binding. Think of it as
expire from OpenHAB1 on steroids. The timer/timeout can either be activated on channel change,
or manually by setting a timeout. For each timer "thing" channels can be added in the
configuration. They can either be linked to actual items with the follow profile or setup to
be fully managed for use in rules. There are four additional channels for the timer thing:
* `EndTime` - The time when the timer expires and a configured "off" value
  is set for the configured output channels.
* `TimeLeft` - The time left to timer expiry in a somewhat human readable format.
  ex: "30m", "4h 10m" or "15s".
* `Duration` - The default time the timer will run.
* `Automatic` - If the timer will run automatically or not.

Further the channels have the following features:
* If a datetime command is sent to the `EndTime` and that datetime is in the future, the timer
  will be started with that datetime as the expire time.
* Formatted strings could be sent to  `TimeLeft` to change the time. Ex: "12m": set the timeout
  to 12 minutes, "+80m": add 1 hour and 20 minutes to the timeout. "-10m": remove 10 minutes from
  the timeout, "+1d3s": add one day and 3 seconds to the timeout.
* Depending on circumstances, `Duration` can be changed so that automatic timeout can be dynamically
  configured. Ex. switch between 10 minutes timeout and an hour timeout.
* The `Automatic` channel can be set to false in order to disable the timer auto start.
  Ex: Front light should light until you turn it off manually.
  
What I would want to know/discuss is the feasibility of such binding. There are no physical thing
behind the binding, I have
however rationalised it to that for ex. KNX there are logic modules that could do this for OpenHAB, this is just cutting the
middle hands. One worry is that to many features are bolted on or baked in, so that it becomes a behemoth
of a binding that people starts to dislike.

The benefits are that it's simple to connect a timer/timeout to a thing, it's simple to manipulate the timer
and the timer will be able to survive restarts with guaranteed only one visible timer instance.

What do the community think of my idea?

I am not sold on my own naming, could the plugin be renamed to something better?

At present there is no built jar to speak of, and the source is really hacky with bad structure.
If dear reader want to take a peek, the source is at: https://github.com/Digers/openhab2-addons/tree/feature/timer/bundles/org.openhab.binding.timer

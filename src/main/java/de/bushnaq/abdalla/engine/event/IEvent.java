package de.bushnaq.abdalla.engine.event;

public interface IEvent {
    de.bushnaq.abdalla.engine.event.EventLevel getLevel();

    StackTraceElement[] getStackTrace();

    String getWhat();

    long getWhen();

    Object getWho();

    String getWhosName();
}

package de.bushnaq.abdalla.engine.event;

import java.io.PrintStream;
import java.util.List;

public interface IEventManager {
    void add(EventLevel level, long when, Object who, String what);

    List<IEvent> filter(Object objectFilter);

    String formatEventForObject(IEvent event);

    String getWhoName(IEvent event);

    boolean isEnabled();

    void print(final PrintStream out);

    void setEnablePrintEvent(boolean enablePrintEvent);

    void setObjectFilter(Object objectFilter);

    void shutdown();

    void writeEventToFile(IEvent event);
}

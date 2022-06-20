package ee.buerokratt.ruuter.util;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.time.Instant;


public class TimestampConverter extends ClassicConverter {

    @Override
    public String convert(ILoggingEvent event) {
        return String.valueOf(Instant.now().getEpochSecond());
    }
}

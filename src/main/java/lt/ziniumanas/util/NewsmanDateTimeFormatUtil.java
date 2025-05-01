package lt.ziniumanas.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NewsmanDateTimeFormatUtil {
        public static final String DATE_FORMAT = "yyyy-MM-dd";
        public static final String TIME_FORMAT = "HH:mm:ss";
        public static final String DATE_TIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;

        public static String getFormatterDateTime(LocalDateTime localDateTime) {
            return DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).format(localDateTime);
        }
}


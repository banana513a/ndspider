package com.ndcf.spider.crawler.common.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    /**
     * 获取当前时间时间戳（long）
     *
     * @return
     */
    public static long currentTimeMillis() {

//        System.out.println(Instant.now().toEpochMilli());
//        System.out.println(System.currentTimeMillis());
        return Instant.now().toEpochMilli();
    }

    /**
     * 获取当前日期(yyyy-MM-dd)
     *
     * @return
     */
    public static LocalDate currentLocalDate() {
        return LocalDate.now();
    }

    /**
     * 获取当前时间(HH:mm:ss.SSS)
     *
     * @return
     */
    public static LocalTime currentLocalTime() {
        return LocalTime.now();
    }

    /**
     * 获取当前日期时间(yyyy-MM-dd'T'HH:mm:ss.SSS)
     *
     * @return
     */
    public static LocalDateTime currentLocalDateTime() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前日期字符串(yyyy-MM-dd)
     *
     * @return
     */
    public static String getCurrentDateStr() {
        return DateTimeFormatter.ofPattern(TimeFormatter.DATE_FORMATTER).format(currentLocalDateTime());
    }


    /**
     * 获取当前时间字符串(HH:mm:ss)
     *
     * @return
     */
    public static String getCurrentTimeStr() {
        return DateTimeFormatter.ofPattern(TimeFormatter.TIME_FORMATTER).format(currentLocalDateTime());
    }

    /**
     * 获取当前日期时间字符串(yyyy-MM-dd HH:mm:ss)
     *
     * @return
     */
    public static String getCurrentDateTimeStr() {
        return DateTimeFormatter.ofPattern(TimeFormatter.DATETIME_FORMATTER).format(currentLocalDateTime());
    }

    /**
     * 获取当前日期字符串(yyyy-MM-dd HH:mm:ss.SSS)
     *
     * @return
     */
    public static String getCurrentDateTimeMsStr() {
        return DateTimeFormatter.ofPattern(TimeFormatter.DATETIME_FORMATTER_SSS).format(currentLocalDateTime());
    }

    /**
     * 将时间字符串转为自定义时间格式的LocalDateTime
     *
     * @param time   需要转化的时间字符串
     * @param format 自定义的时间格式
     * @return
     */
    public static LocalDateTime convertStringToLocalDateTime(String time, String format) {
        return LocalDateTime.parse(time, DateTimeFormatter.ofPattern(format));
    }

    /**
     * 将LocalDateTime转为自定义的时间格式的字符串
     *
     * @param localDateTime 需要转化的LocalDateTime
     * @param format        自定义的时间格式
     * @return
     */
    public static String convertLocalDateTimeToString(LocalDateTime localDateTime, String format) {
        return localDateTime.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 将long类型的timestamp转为LocalDateTime
     *
     * @param timestamp
     * @return
     */
    public static LocalDateTime convertTimestampToLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    /**
     * 将LocalDateTime转为long类型的timestamp
     *
     * @param localDateTime
     * @return
     */
    public static long convertLocalDateTimeToTimestamp(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 获取LocalDateTime的最大时间的字符串格式(yyyy-MM-dd HH:mm:ss)
     *
     * @param localDateTime
     * @return
     */
    public static String getMaxDateTime(LocalDateTime localDateTime) {
        return convertLocalDateTimeToString(localDateTime.with(LocalTime.MAX), TimeFormatter.DATETIME_FORMATTER);
    }

    /**
     * 获取LocalDateTime的最小时间的字符串格式(yyyy-MM-dd HH:mm:ss)
     *
     * @param localDateTime
     * @return
     */
    public static String getMinDateTime(LocalDateTime localDateTime) {
        return convertLocalDateTimeToString(localDateTime.with(LocalTime.MIN), TimeFormatter.DATETIME_FORMATTER);
    }

    /**
     * 获取LocalDate的最大时间的字符串格式(yyyy-MM-dd HH:mm:ss)
     *
     * @param localDate
     * @return
     */
    public static String getMaxDateTime(LocalDate localDate) {
        return convertLocalDateTimeToString(localDate.atTime(LocalTime.MAX), TimeFormatter.DATETIME_FORMATTER);

    }

    /**
     * 获取LocalDate的最小时间的字符串格式(yyyy-MM-dd HH:mm:ss)
     *
     * @param localDate
     * @return
     */
    public static String getMinDateTime(LocalDate localDate) {
        return convertLocalDateTimeToString(localDate.atTime(LocalTime.MIN), TimeFormatter.DATETIME_FORMATTER);
    }
}
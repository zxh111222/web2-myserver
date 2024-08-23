package Secondhand;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyDateConversionUtil {
    public static Date parseDate(String dateString) {
        try {
            SimpleDateFormat dateFormat;
            if (dateString.length() == 10) {
                dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            } else if (dateString.length() == 16) {
                dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            } else {
                dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            }
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        try {
            SimpleDateFormat dateFormat;
            String formattedDate;

            // 检查日期对象的时间部分是否为零，以决定使用哪种格式
            SimpleDateFormat checkTimeFormat = new SimpleDateFormat("HH:mm:ss");
            String timeString = checkTimeFormat.format(date);

            if ("00:00:00".equals(timeString)) {
                // 如果时间部分为零，则只返回日期部分
                dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                formattedDate = dateFormat.format(date);
            } else {
                // 如果时间部分不为零，决定是否包含秒
                SimpleDateFormat checkSecondsFormat = new SimpleDateFormat("ss");
                String secondsString = checkSecondsFormat.format(date);

                if ("00".equals(secondsString)) {
                    // 如果秒部分为零，则返回不包含秒的格式
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                } else {
                    // 否则返回包含秒的格式
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                }
                formattedDate = dateFormat.format(date);
            }

            return formattedDate;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

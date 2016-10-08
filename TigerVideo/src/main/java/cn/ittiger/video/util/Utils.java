package cn.ittiger.video.util;

/**
 * Created by baina on 16-9-14.
 */
public class Utils {

    /**
     * 转换视频时长(s)为时分秒的展示格式
     * @param seconds   视频总时长，单位秒
     * @return
     */
    public static String formatTimeLength(long seconds) {

        String formatLength = "";
        if(seconds == 0) {
            formatLength = "00:00";
        } else if(seconds < 60) {//小于1分钟
            formatLength = "00:" + (seconds < 10 ? "0" + seconds : seconds);
        } else if(seconds < 60 * 60) {//小于1小时
            long sec = seconds % 60;
            long min = seconds / 60;
            formatLength = (min < 10 ? "0" + min : String.valueOf(min)) + ":" +
                    (sec < 10 ? "0" + sec : String.valueOf(sec));
        } else {
            long hour = seconds / 3600;
            long min = seconds % 3600 / 60;
            long sec = seconds % 3600 % 60;
            formatLength = (hour < 10 ? "0" + hour : String.valueOf(hour)) + ":" +
                    (min < 10 ? "0" + min : String.valueOf(min)) + ":" +
                    (sec < 10 ? "0" + sec : String.valueOf(sec));
        }
        return formatLength;
    }
}

package coriolis;

/**
 * Created by maxvl on 05.06.2017.
 */
public class ValuePrinterUtil {
    public static String printMetricValue(double value) {
        if (value>3000) {
            return Math.round(value/1000) + " км";
        } else if (value>3) {
            return Math.round(value) + " м";
        } else if (value>0.03) {
            return Math.round(value*100) + " см";
        } else if (value>0.003) {
            return Math.round(value*1000) + " мм";
        } else {
            return Math.round(value*1E6) + " мкм";
        }
    }
}

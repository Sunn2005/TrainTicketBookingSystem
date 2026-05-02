package iuh.fit.gui.statistics;

import java.text.NumberFormat;
import java.util.Locale;

public class MoneyUtils {

    public static String formatVND(double value) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("en", "US"));
        return nf.format(value) + " VND";
    }
}
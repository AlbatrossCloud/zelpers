package pe.albatross.zelpers.miscelanea;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberFormat {

    public String dec(Object value) {
        if (value == null) {
            return null;
        }

        DecimalFormat myFormatter = new DecimalFormat("#####0.######", new DecimalFormatSymbols(new Locale("pe", "PE")));
        return myFormatter.format(value);
    }

    public String precio(Object value) {
        if (value == null) {
            return null;
        }

        DecimalFormat myFormatter = new DecimalFormat("###,##0.00", new DecimalFormatSymbols(new Locale("pe", "PE")));
        return myFormatter.format(value);
    }

    public String medida(Object value) {
        if (value == null) {
            return null;
        }

        DecimalFormat myFormatter = new DecimalFormat("###,##0.########", new DecimalFormatSymbols(new Locale("pe", "PE")));
        return myFormatter.format(value);
    }

    public String codigo(Object value, int ancho) {
        if (value == null) {
            return null;
        }
        StringBuilder cod = new StringBuilder();
        for (int i = 0; i < ancho; i++) {
            cod.append('0');
        }
        DecimalFormat myFormatter = new DecimalFormat(cod.toString(), new DecimalFormatSymbols(new Locale("pe", "PE")));
        return myFormatter.format(value);

    }

}

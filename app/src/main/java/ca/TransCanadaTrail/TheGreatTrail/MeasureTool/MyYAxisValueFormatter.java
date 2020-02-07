package ca.TransCanadaTrail.TheGreatTrail.MeasureTool;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

public class MyYAxisValueFormatter implements IAxisValueFormatter {

    private DecimalFormat mFormat;
    private boolean x = true;

    public MyYAxisValueFormatter(boolean x) {

        // format values to 1 decimal digit

        this.x = x ;


        if (x) {
            mFormat = new DecimalFormat("###,###,##0.00");
        }
        else {
            mFormat = new DecimalFormat("###,###,##0.00");
        }
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // "value" represents the position of the label on the axis (x or y)
        String unit = " km";
        if (x){
            unit = " km";
        }
        else {
            unit = " m";
        }
        return mFormat.format(value) + unit;
    }

    /** this is only needed if numbers are returned, else return 0 */
     public int getDecimalDigits() { return 1; }
}

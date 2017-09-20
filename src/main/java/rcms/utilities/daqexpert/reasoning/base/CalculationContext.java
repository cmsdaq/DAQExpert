package rcms.utilities.daqexpert.reasoning.base;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * @implNote Note that the only reason why the Serializable interface is implemented is that the Context needs to be cloneable, @see ConditionProducer for implementation details.
 */
public class CalculationContext implements Serializable {

    private Float min;
    private Float max;
    private Float avg;
    private Float lastReportedAvg;
    private Float sum;
    private Float current;
    private int count;
    private boolean report;

    private DecimalFormat df;

    public CalculationContext(){
        df = new DecimalFormat();
        df.setMaximumFractionDigits(1);
    }

    public void update(Float n) {
        count++;
        current = n;
        report = false;
        if (min == null || min > n) {
            min = n;
        }

        if (max == null || max < n) {
            max = n;
        }

        if (sum == null) {
            sum = 0f;
        }

        sum += n;

        avg = sum / count;

        if (lastReportedAvg == null) {
            lastReportedAvg = avg;
        } else {

            float change = Math.abs(lastReportedAvg - avg) / lastReportedAvg;

            // if change is more than 10% report. this may be parametrized
            if (change > 0.10f) {
                lastReportedAvg = avg;
                report = true;
            }
        }
    }

    public void reset() {
        min = null;
        max = null;
        avg = null;
        sum = null;
        report = false;
    }

    public boolean isReport() {
        return report;
    }

    public Float getMin() {
        return min;
    }

    public void setMin(Float min) {
        this.min = min;
    }

    public Float getMax() {
        return max;
    }

    public void setMax(Float max) {
        this.max = max;
    }

    public Float getAvg() {
        return avg;
    }

    public void setAvg(Float avg) {
        this.avg = avg;
    }

    public String getValueWithPrecision(Float value){
        if(value == null){
            return "-";
        }
        return df.format(value);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append("**").append("curr").append(":").append("**").append(getValueWithPrecision(current)).append(",");
        sb.append("**").append("avg").append(":").append("**").append(getValueWithPrecision(avg)).append(",");
        sb.append("**").append("min").append(":").append("**").append(getValueWithPrecision(min)).append(",");
        sb.append("**").append("max").append(":").append("**").append(getValueWithPrecision(max));
        sb.append(")");
        return sb.toString();
    }
}

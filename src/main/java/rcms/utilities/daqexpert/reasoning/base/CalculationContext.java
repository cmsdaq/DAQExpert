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
    private boolean allTheSame;

    private String unit;
    private DecimalFormat df;

    public CalculationContext(String unit, int precision) {

        if (unit == null) {
            this.unit = "";
        } else {
            this.unit = unit;
        }
        this.df = new DecimalFormat();
        this.df.setMaximumFractionDigits(precision);
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

        allTheSame = allTheSame();
    }

    private boolean allTheSame() {
        String base = getValueWithPrecision(current);
        if (base.equals(getValueWithPrecision(avg)) && base.equals(getValueWithPrecision(max)) && base.equals(getValueWithPrecision(min))) {
            return true;
        }
        return false;
    }

    public void reset() {
        min = null;
        max = null;
        avg = null;
        sum = null;
        report = false;
        allTheSame = false;
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

    public String getValueWithPrecision(Float value) {
        if (value == null) {
            return "-";
        }
        return df.format(value);
    }

    @Override
    public String toString() {

        if (allTheSame) {
            return new StringBuilder().append(getValueWithPrecision(current)).append(unit).toString();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append("**").append("curr").append(":").append("**").append(getValueWithPrecision(current)).append("**").append(unit).append("**").append(", ");
        sb.append("**").append("avg").append(":").append("**").append(getValueWithPrecision(avg)).append("**").append(unit).append("**").append(", ");
        sb.append("**").append("min").append(":").append("**").append(getValueWithPrecision(min)).append("**").append(unit).append("**").append(", ");
        sb.append("**").append("max").append(":").append("**").append(getValueWithPrecision(max)).append("**").append(unit).append("**");
        sb.append(")");
        return sb.toString();
    }
}

package rcms.utilities.daqexpert.processing.context;

import java.io.Serializable;
import java.text.DecimalFormat;
import rcms.utilities.daqexpert.processing.context.ContextEntry;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @implNote Note that the only reason why the Serializable interface is implemented is that the ContextHandler needs to be cloneable, @see ConditionProducer for implementation details.
 */
@Entity
@Table(name="condition_context_statistic")
public class StatisticContextEntry extends ContextEntry {

    private Float min;
    private Float max;
    private Float avg;

    @Transient
    private Float lastReportedAvg;
    @Transient
    private Float sum;


    @Transient
    private Float current;
    @Transient
    private int count;
    @Transient
    private boolean report;
    @Transient
    private boolean allTheSame;

    public void setUnit(String unit) {
        this.unit = unit;
    }

    private String unit;
    @Transient
    private DecimalFormat df;

    public StatisticContextEntry(){

    }

    public StatisticContextEntry(String unit, int precision) {

        if (unit == null) {
            this.unit = "";
        } else {
            this.unit = unit;
        }
        this.df = new DecimalFormat();
        this.df.setMaximumFractionDigits(precision);
        this.type = "S";
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


    public Float getCurrent() {
        return current;
    }

    public void setCurrent(Float current) {
        this.current = current;
    }


    @Override
    public String getTextRepresentation() {
        boolean highlightMakrup = ContextHandler.highlightMarkup;

        if (allTheSame) {
            return new StringBuilder().append(getValueWithPrecision(current)).append(unit).toString();
        }

        StringBuilder sb = new StringBuilder();

        sb.append("(");
        if (highlightMakrup) sb.append("<sub><sup>");
        sb.append(" ").append("last").append(": ");
        if (highlightMakrup) sb.append("</sup></sub>");
        sb.append(getValueWithPrecision(current)).append(unit).append(", ");

        if (highlightMakrup) sb.append("<sub><sup>");
        sb.append(" ").append("avg").append(": ");
        if (highlightMakrup) sb.append("</sup></sub>");
        sb.append(getValueWithPrecision(avg)).append(unit).append(", ");

        if (highlightMakrup) sb.append("<sub><sup>");
        sb.append(" ").append("min").append(": ");
        if (highlightMakrup) sb.append("</sup></sub>");
        sb.append(getValueWithPrecision(min)).append(unit).append(", ");
        if (highlightMakrup) sb.append("<sub><sup>");
        sb.append(" ").append("max").append(": ");
        if (highlightMakrup) sb.append("</sup></sub>");
        sb.append(getValueWithPrecision(max)).append(unit);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Object getValue() {
        return this;
    }

    public String getUnit() {
        return unit;
    }
}

package fi.ubigu.gsdig.unitdata;

import fi.ubigu.gsdig.joins.AggregateFunction;

public class SensitivitySetting {

    private AggregateFunction aggregate;
    private String property;
    private double minValue;

    public AggregateFunction getAggregate() {
        return aggregate;
    }

    public void setAggregate(AggregateFunction aggregate) {
        this.aggregate = aggregate;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

}

package fi.ubigu.gsdig.joins;

import java.util.List;

public class JoinAttribute {

    private String property;
    private List<AggregateFunction> aggregate;

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public List<AggregateFunction> getAggregate() {
        return aggregate;
    }

    public void setAggregate(List<AggregateFunction> aggregate) {
        this.aggregate = aggregate;
    }
    
}

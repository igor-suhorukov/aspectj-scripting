package org.aspectj.configuration.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Date;

@XmlAccessorType(XmlAccessType.FIELD)
public class TimerTask {
    private Date firstTime;
    private Long delay;
    private Long period;
    private String jobExpression;

    public TimerTask() {
    }

    public TimerTask(Date firstTime, Long delay, Long period, String jobExpression) {
        this.firstTime = firstTime;
        this.delay = delay;
        this.period = period;
        this.jobExpression = jobExpression;
    }

    public Date getFirstTime() {
        return firstTime;
    }

    public Long getDelay() {
        return delay;
    }

    public Long getPeriod() {
        return period;
    }

    public String getJobExpression() {
        return jobExpression;
    }
}

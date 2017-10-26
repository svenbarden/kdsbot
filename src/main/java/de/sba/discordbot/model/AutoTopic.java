package de.sba.discordbot.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class AutoTopic implements Serializable {
    private Long id;
    private int dayOfMonth;
    private int month;
    private String topic;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    @Column(length = 2000)
    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}

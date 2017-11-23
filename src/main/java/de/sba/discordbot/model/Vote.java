package de.sba.discordbot.model;

import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
import java.sql.Timestamp;

@Table(uniqueConstraints = @UniqueConstraint(name = "UK_VOTE", columnNames = {"poll_id", "user"}))
public class Vote implements Serializable {
    private Poll poll;
    private Timestamp voted;
    private String user;
    private int option;

    @OneToMany
    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public Timestamp getVoted() {
        return voted;
    }

    public void setVoted(Timestamp voted) {
        this.voted = voted;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getOption() {
        return option;
    }

    public void setOption(int option) {
        this.option = option;
    }
}

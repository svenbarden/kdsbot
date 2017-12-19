package de.sba.discordbot.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@NamedQueries(
        {
                @NamedQuery(name = "vote.findByPollAndUser", query = "SELECT v FROM Vote v WHERE v.poll = :poll AND v.user = :user"),
                @NamedQuery(name = "vote.findByPoll", query = "SELECT v FROM Vote v WHERE v.poll = :poll")
        }
)
@Table(uniqueConstraints = @UniqueConstraint(name = "UK_VOTE", columnNames = {"user", "poll_id"}))
@Entity
public class Vote implements Serializable {
    private Long id;
    private int pollOption;
    private Timestamp voted;
    private String user;
    private Poll poll;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getPollOption() {
        return pollOption;
    }

    public void setPollOption(int pollOption) {
        this.pollOption = pollOption;
    }

    @ManyToOne(optional = false)
    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }
}

package de.sba.discordbot.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OrderColumn;

@NamedQueries(
        {
            @NamedQuery(name = "poll.findOpenByChannel", query = "SELECT p FROM Poll p WHERE p.channel = :channel AND closed IS NULL ORDER BY p.created DESC")
        }
)
@Entity
public class Poll implements Serializable {
    private Long id;
    private String title;
    private Set<String> options = new LinkedHashSet<>();
    private String createdBy;
    private Timestamp created;
    private String channel;
    private Timestamp closed;

    @Column(length = 1000)
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    @Column(length = 2000)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    public Set<String> getOptions() {
        return options;
    }

    public void setOptions(Set<String> options) {
        this.options = options;
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(length = 2000)
    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Timestamp getClosed() {
        return closed;
    }

    public void setClosed(Timestamp closed) {
        this.closed = closed;
    }
}

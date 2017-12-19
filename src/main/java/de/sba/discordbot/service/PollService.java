package de.sba.discordbot.service;

import de.sba.discordbot.exception.PollNotFoundException;
import de.sba.discordbot.model.Poll;
import de.sba.discordbot.model.Vote;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.NoResultException;

@Service
public class PollService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PollService.class);
    @Autowired
    private PersistenceService persistenceService;

    @Transactional(readOnly = true)
    public Map<Integer, List<Vote>> findVotes(Poll poll) {
        List<Vote> votes = persistenceService.getEntityManager().createNamedQuery("vote.findByPoll", Vote.class)
                .setParameter("poll", poll).getResultList();
        return votes.stream().collect(Collectors.groupingBy(Vote::getPollOption));
    }

    @Transactional(readOnly = true)
    public Poll findOpenByChannel(String channel) {
        try {
            return persistenceService.getEntityManager().createNamedQuery("poll.findOpenByChannel", Poll.class)
                    .setParameter("channel", channel)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public void close(String channel) {
        Poll openPoll = findOpenByChannel(channel);
        if(openPoll != null) {
            openPoll.setClosed(new Timestamp(System.currentTimeMillis()));
            persistenceService.persist(openPoll);
        }
    }

    @Transactional
    public Poll create(String title, String author, String channel, String[] options) {
        LOGGER.debug("create new poll {} in channel {} for user {}", title, channel, author);
        Poll openPoll = findOpenByChannel(channel);
        Poll poll;
        if(openPoll == null) {
            poll = new Poll();
            poll.setCreated(new Timestamp(System.currentTimeMillis()));
            poll.setTitle(title);
            poll.setCreatedBy(author);
            Arrays.stream(options).filter(StringUtils::isNotBlank).forEach(option -> poll.getOptions().add(option));
            poll.setChannel(channel);
            persistenceService.persist(poll);
        } else {
            poll = null;
            LOGGER.debug("there is already an open poll");
        }

        return poll;
    }

    @Transactional
    public void vote(String user, String channel, int index) throws IndexOutOfBoundsException, PollNotFoundException {
        LOGGER.debug("user {} voted for {} in channel {}", user, index, channel);
        Poll poll = findOpenByChannel(channel);
        if(poll == null) {
            LOGGER.debug("no open poll in channel found");
            throw new PollNotFoundException("no open poll found in channel " + channel);
        } else {
            if(poll.getOptions().size() <= index) {
                throw new IndexOutOfBoundsException("poll index " + index + " not in " + poll.getOptions().size());
            }
            Vote vote;
            try {
                vote = persistenceService.getEntityManager().createNamedQuery("vote.findByPollAndUser", Vote.class)
                        .setParameter("poll", poll)
                        .setParameter("user", user)
                        .setMaxResults(1)
                        .getSingleResult();
            } catch (NoResultException e) {
                vote = null;
            }
            if(vote == null) {
                LOGGER.trace("create new vote entry");
                vote = new Vote();
                vote.setPoll(poll);
                vote.setUser(user);
            }
            vote.setVoted(new Timestamp(System.currentTimeMillis()));
            vote.setPollOption(index);
            persistenceService.persist(vote);
        }
    }
}

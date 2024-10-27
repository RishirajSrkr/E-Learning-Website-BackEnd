package com.rishiraj.bitbybit.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class VoteScheduler {

    private static final Logger log = LoggerFactory.getLogger(VoteScheduler.class);
    private final VotingServiceImpl votingService;


    public VoteScheduler(VotingServiceImpl votingService) {
        this.votingService = votingService;
    }

//    @Scheduled(cron = "0 * * * * *")
    public void saveToDatabase(){

        log.info("Scheduler being called");
        votingService.persistVotesToDatabase();
    }
}

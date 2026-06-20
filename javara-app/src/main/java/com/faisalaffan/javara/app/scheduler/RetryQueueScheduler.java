package com.faisalaffan.javara.app.scheduler;

import com.faisalaffan.javara.app.repository.RetryQueueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RetryQueueScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetryQueueScheduler.class);
    private final RetryQueueRepository repository;

    public RetryQueueScheduler(RetryQueueRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedDelay = 30_000)
    public void processRetryQueue() {
        var pending = repository.findPendingRetries();
        log.debug("Processing {} pending retries", pending.size());
        for (var job : pending) {
            // Replay through the adapter
            // Increment attempts
            // If exhausted, alert
        }
    }
}

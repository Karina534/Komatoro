package org.example.komatoro.security.jwt.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CleanDeactivatedTokens {
    private final JdbcTemplate jdbcTemplate;

    public CleanDeactivatedTokens(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Раз в сутки в 03:00
    @Scheduled(cron = "0 0 3 * * *")
    public void clean(){
        int deleted = jdbcTemplate.update("""
            delete from deactivated_tokens where keep_until < now()
            """);

        log.info("Deleted {} deactivated tokens", deleted);
    }
}

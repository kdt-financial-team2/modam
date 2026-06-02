package com.intelliJ_JO.modam.global.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class SchemaUpdater implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE spend_record ALTER COLUMN img_url SET DATA TYPE CLOB");
            log.info("[SchemaUpdater] spend_record.img_url → CLOB 마이그레이션 완료");
        } catch (Exception e) {
            // 이미 CLOB이거나 테이블이 없는 경우 무시
        }
    }
}

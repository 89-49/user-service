package org.pgsg.user_service.user.infrastructure.loader;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ChatTimeCsvLoader extends AbstractCsvLoader {

	private static final String CHECK_SQL = "SELECT CASE WHEN COUNT(*) >= 10000 THEN 1 ELSE 0 END FROM p_chat_time_range";
	private static final String CSV_PATH = "csv/p_chat_time_range.csv";
	private static final String INSERT_SQL = "INSERT INTO p_chat_time_range (user_id, day_of_week, start_time, end_time) " +
			"VALUES (CAST(? AS UUID), ?, CAST(? AS TIME), CAST(? AS TIME)) ON CONFLICT DO NOTHING";

	public ChatTimeCsvLoader(JdbcTemplate jdbcTemplate) {
		super(jdbcTemplate, CHECK_SQL, CSV_PATH, INSERT_SQL);
	}

	@Override
	@Transactional
	public void load() throws Exception {
		super.load();
	}

	@Override
	public String getName() {
		return "Chat Time Range Table (p_chat_time_range)";
	}
}

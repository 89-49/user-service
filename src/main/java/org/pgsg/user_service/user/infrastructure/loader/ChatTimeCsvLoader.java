package org.pgsg.user_service.user.infrastructure.loader;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatTimeCsvLoader extends AbstractCsvLoader {

	private static final String CHECK_SQL = "SELECT COUNT(*) FROM p_chat_time_range " +
			"WHERE user_id = CAST('9107aa7c-a733-4514-8033-89e601a450ab' AS UUID) " +
			"AND day_of_week = 'THURSDAY' AND start_time = CAST('14:00:00' AS TIME)";
	private static final String CSV_PATH = "csv/p_chat_time_range.csv";
	private static final String INSERT_SQL = "INSERT INTO p_chat_time_range (user_id, day_of_week, start_time, end_time) VALUES (CAST(? AS UUID), ?, CAST(? AS TIME), CAST(? AS TIME))";

	public ChatTimeCsvLoader(JdbcTemplate jdbcTemplate) {
		super(jdbcTemplate, CHECK_SQL, CSV_PATH, INSERT_SQL);
	}

	@Override
	public String getName() {
		return "Chat Time Range Table (p_chat_time_range)";
	}
}

package org.pgsg.user_service.user.infrastructure.loader;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserCsvLoader extends AbstractCsvLoader {

	private static final String CHECK_SQL = "SELECT COUNT(*) FROM p_user LIMIT 1";
	private static final String CSV_PATH = "csv/p_user.csv";
	private static final String INSERT_SQL = "INSERT INTO p_user (user_id, username, password, user_role, name, nickname, created_by, created_at, modified_by, modified_at, deleted_by, deleted_at) " +
			"VALUES (CAST(? AS UUID), ?, ?, ?, ?, ?, CAST(? AS UUID), CAST(? AS TIMESTAMP), CAST(? AS UUID), CAST(? AS TIMESTAMP), CAST(? AS UUID), CAST(? AS TIMESTAMP)) " +
			"ON CONFLICT DO NOTHING";

	public UserCsvLoader(JdbcTemplate jdbcTemplate) {
		super(jdbcTemplate, CHECK_SQL, CSV_PATH, INSERT_SQL);
	}

	@Override
	@Transactional
	public void load() throws Exception {
		super.load();
	}

	@Override
	public String getName() {
		return "User Table (p_user)";
	}
}

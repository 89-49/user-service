package org.pgsg.user_service.user.infrastructure.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractCsvLoader implements CsvLoader {

	protected final JdbcTemplate jdbcTemplate;
	private final String checkSql;
	private final String csvPath;
	private final String insertSql;

	@Override
	public boolean isLoaded() {
		try {
			Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
			return count != null && count > 0;
		} catch (Exception e) {
			log.warn("[{}] Could not check table existence or data.", getClass().getSimpleName());
			return false;
		}
	}

	@Override
	public void load() throws Exception {
		log.info("[{}] Loading data from {}...", getClass().getSimpleName(), csvPath);
		ClassPathResource resource = new ClassPathResource(csvPath);
		if (!resource.exists()) {
			log.warn("[{}] CSV file not found: {}", getClass().getSimpleName(), csvPath);
			return;
		}

		try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			String header = br.readLine();
			if (header == null) return;
			int columnCount = header.split(",", -1).length;

			List<Object[]> batchArgs = new ArrayList<>();

			while ((line = br.readLine()) != null) {
				String[] data = line.split(",", -1);

				if (data.length != columnCount) {
					log.warn("[{}] Malformed row skipped (expected {} columns, got {}): {}",
							getClass().getSimpleName(), columnCount, data.length, line);
					continue;
				}

				Object[] row = new Object[columnCount];
				for (int i = 0; i < columnCount; i++) {
					String val = data[i].trim();
					row[i] = val.isEmpty() ? null : val;
				}
				batchArgs.add(row);

				if (batchArgs.size() >= 1000) {
					jdbcTemplate.batchUpdate(insertSql, batchArgs);
					batchArgs.clear();
				}
			}
			if (!batchArgs.isEmpty()) {
				jdbcTemplate.batchUpdate(insertSql, batchArgs);
			}
		}
		log.info("[{}] Successfully loaded data from {}.", getClass().getSimpleName(), csvPath);
	}
}

package org.pgsg.user_service.user.infrastructure.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("dev")
public class UserServiceCsvDataLoader implements CommandLineRunner {

	private final UserCsvLoader userCsvLoader;
	private final ChatTimeCsvLoader chatTimeCsvLoader;

	@Override
	public void run(String... args) throws Exception {
		log.info("[UserServiceCsvDataLoader] Starting orchestrated CSV data load...");

		// 리스트에 추가하는 순서대로 실행 (명시적 순서 보장)
		List<CsvLoader> loaders = List.of(userCsvLoader, chatTimeCsvLoader);

		for (CsvLoader loader : loaders) {
			executeLoader(loader);
		}

		log.info("[UserServiceCsvDataLoader] Orchestrated CSV data load completed.");
	}

	private void executeLoader(CsvLoader loader) throws Exception {
		if (!loader.isLoaded()) {
			log.info("[UserServiceCsvDataLoader] Delegating load task to: {}", loader.getName());
			loader.load();
		} else {
			log.info("[UserServiceCsvDataLoader] Data already exists for: {}. Skipping.", loader.getName());
		}
	}
}


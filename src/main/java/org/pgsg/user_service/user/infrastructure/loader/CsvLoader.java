package org.pgsg.user_service.user.infrastructure.loader;

public interface CsvLoader {
	/**
	 * 데이터가 이미 로드되었는지 확인합니다.
	 */
	boolean isLoaded();

	/**
	 * CSV 파일에서 데이터를 읽어 DB에 로드합니다.
	 */
	void load() throws Exception;

	/**
	 * 로더의 이름을 반환합니다 (로그용).
	 */
	String getName();
}

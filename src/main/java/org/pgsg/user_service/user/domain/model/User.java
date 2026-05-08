package org.pgsg.user_service.user.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import org.pgsg.common.domain.BaseEntity;
import org.pgsg.user_service.user.domain.exception.UserErrorCode;
import org.pgsg.user_service.user.domain.exception.UserServiceException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Getter
@Table(
		name = "p_user",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_user_username", columnNames = {"username"})
		}
)
@Entity
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "username", length = 12, nullable = false)
	private String username;

	@Column(name = "password", nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(name = "user_role", nullable = false)
	private UserRole userRole;

	@Column(name = "name", length = 20, nullable = false)
	private String name;

	@Column(name = "nickname", length = 20, nullable = false)
	private String nickname;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(
			name = "p_chat_time_range",
			joinColumns = @JoinColumn(name = "user_id"),
			uniqueConstraints = @UniqueConstraint(
					name = "idx_chat_time_range",
					columnNames = {"user_id", "day_of_week", "start_time"}
			)
	)
	@OrderBy("dayOfWeek ASC, startTime ASC, endTime ASC")
	private final List<ChatTimeRange> chatTimeRanges = new ArrayList<>();

	@Builder(access = AccessLevel.PRIVATE)
	private User(String username, String password, UserRole userRole, String name, String nickname) {
		this.username = username;
		this.password = password;
		this.userRole = userRole;
		this.name = name;
		this.nickname = nickname;
	}

	public static User create(String username, String encryptedPassword, UserRole userRole, String name, String nickname) {
		return User.builder()
				.username(username)
				.password(encryptedPassword)
				.userRole(userRole)
				.name(name)
				.nickname(nickname)
				.build();
	}

	public void updateProfile(String name, String nickname) {
		if (name != null) {
			this.name = name;
		}
		if (nickname != null) {
			this.nickname = nickname;
		}
	}

	public void updateRole(UserRole userRole) {
		if (userRole != null) {
			this.userRole = userRole;
		}
	}

	public void updatePassword(String password) {
		if (password != null) {
			this.password = password;
		}
	}

	public void delete(UUID actorId) {
		// 이미 엔티티 외부에서 삭제 권한에 관한 검증이 완료되었다고 가정
		super.delete(actorId);
	}

	public boolean isEnabled() {
		return this.deletedAt == null;
	}


	// 회원이 설정한 채팅 가능 시간대 관련
	public void addChatTimeRangeList(List<ChatTimeRange> chatTimeRanges) {
		if (chatTimeRanges != null && !chatTimeRanges.isEmpty()) {
			List<ChatTimeRange> combined = new ArrayList<>(this.chatTimeRanges);
			combined.addAll(chatTimeRanges);
			validateRanges(combined);
			this.chatTimeRanges.addAll(chatTimeRanges);
		}
	}

	public List<ChatTimeRange> findAvailableChatTime(LocalDate date, LocalTime time) {
		return chatTimeRanges.stream()
				.filter(chatTimeRange -> date != null && chatTimeRange.getDayOfWeek() == date.getDayOfWeek())
				.filter(chatTimeRange -> time != null
						&& !time.isBefore(chatTimeRange.getStartTime())
						&& time.isBefore(chatTimeRange.getEndTime()))
				.toList();
	}

	public void updateChatTimeRanges(List<ChatTimeRange> chatTimeRanges) {
		if (chatTimeRanges != null) {
			validateRanges(chatTimeRanges);
			this.chatTimeRanges.clear();
			this.chatTimeRanges.addAll(chatTimeRanges);
		}
	}

	public void clearChatTimeRanges() {
		this.chatTimeRanges.clear();
	}

	private void validateRanges(List<ChatTimeRange> ranges) {
		if (ranges == null || ranges.isEmpty()) {
			return;
		}

		List<ChatTimeRange> sorted = ranges.stream()
				.sorted(Comparator.comparing(ChatTimeRange::getDayOfWeek)
						.thenComparing(ChatTimeRange::getStartTime))
				.toList();

		for (int i = 0; i < sorted.size() - 1; i++) {
			ChatTimeRange current = sorted.get(i);
			ChatTimeRange next = sorted.get(i + 1);
			if (current.overlapsWith(next)) {
				throw new UserServiceException(UserErrorCode.DUPLICATE_CHAT_TIME_RANGE);
			}
		}
	}
}


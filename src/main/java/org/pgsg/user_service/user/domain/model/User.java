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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
	@CollectionTable(name = "p_chat_time_range", joinColumns = @JoinColumn(name = "user_id"))
	@OrderBy("dayOfWeek ASC, startTime ASC")
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

	public void delete(UUID userId) {
		if (this.userId != userId) {
			throw new UserServiceException(UserErrorCode.UNAUTHORIZED);
		}
		super.delete(userId);
	}

	public boolean isEnabled() {
		return this.deletedAt == null;
	}


	public void addChatTimeRangeList(List<ChatTimeRange> chatTimeRanges) {
		this.chatTimeRanges.addAll(chatTimeRanges);
	}

	public void updateChatTimeRanges(List<ChatTimeRange> chatTimeRanges) {
		if (chatTimeRanges != null) {
			this.chatTimeRanges.clear();
			this.chatTimeRanges.addAll(chatTimeRanges);
		}
	}

	public void clearChatTimeRanges() {
		this.chatTimeRanges.clear();
	}
}


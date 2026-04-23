package org.pgsg.user_service.user.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// TODO: 공통모듈 배포 시 BaseEntity 상속
@Getter
@Table(
		name = "p_user",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_user_user_info",
						columnNames = {"username", "name", "nickname"}
				)
		}
)
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

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

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "nickname", nullable = false)
	private String nickname;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "p_chat_time_range", joinColumns = @JoinColumn(name = "user_id"))
	private List<ChatTimeRange> chatTimeRange = new ArrayList<>();

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

	// TODO: 채팅가능시간 관련 세부 로직 추가(인증 로직 구현 이후 회원 관련 기능 구현 시)
}

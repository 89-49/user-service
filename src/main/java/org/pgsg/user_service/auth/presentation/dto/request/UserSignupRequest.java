package org.pgsg.user_service.auth.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.pgsg.user_service.auth.application.dto.command.SignupUserCommand;
import org.pgsg.user_service.user.application.dto.command.CreateChatTimeCommand;
import org.pgsg.user_service.user.domain.model.UserRole;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

public record UserSignupRequest(

        @NotBlank(message = "아이디는 필수 입력 값입니다.")
        @Pattern(regexp = "^[a-zA-Z0-9]{8,12}$", message = "아이디는 최소 8글자, 최대 12글자의 영문 대/소문자, 숫자만 허용합니다.")
        String username,

        @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
        @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{8,20}$", message = "비밀번호는 최소 8글자, 최대 20글자의 영문 대/소문자, 숫자, 특수문자만 허용합니다.")
        String password,

        @NotBlank(message = "권한 정보는 필수 입력 값입니다.")
        @Pattern(regexp = "^(?i)(USER|MANAGER|MASTER|ROLE_USER|ROLE_MANAGER|ROLE_MASTER|일반 사용자|관리자|총관리자)$", message = "올바른 권한 형식이 아닙니다.")
        String userRole,

        @NotBlank(message = "이름은 필수 입력 값입니다.")
        @Pattern(regexp = "^[a-zA-Z가-힣]{1,20}$", message = "이름은 최대 20자까지, 한글과 영문만 허용합니다.")
        String name,

        @NotBlank(message = "닉네임은 필수 입력 값입니다.")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣]{1,20}$", message = "닉네임은 최대 20자까지, 한글, 영문, 숫자만 허용합니다.")
        String nickname,

        @Valid
        @NotNull
        @NotEmpty(message = "채팅 가능 시간은 최소 하나 이상 등록해야 합니다.")
        List<ChatTimeRequest> chatTimeRanges
) {
    // 채팅 가능 시간 정보만을 담당하는 내부 중첩 record
    public record ChatTimeRequest(
            @NotNull(message = "요일 정보는 필수입니다.")
            DayOfWeek dayOfWeek,

            @NotNull(message = "시작 시간은 필수입니다.")
            LocalTime startTime,

            @NotNull(message = "종료 시간은 필수입니다.")
            LocalTime endTime
    ) {
        public CreateChatTimeCommand toCommand() {
            return new CreateChatTimeCommand(dayOfWeek, startTime, endTime);
        }
    }

    // Request DTO를 서비스 계층에서 사용할 Command 객체로 변환
    public SignupUserCommand toCommand() {
        return new SignupUserCommand(
                username,
                password,
                UserRole.find(userRole),
                name,
                nickname,
                chatTimeRanges.stream()
                        .filter(Objects::nonNull)
                        .map(ChatTimeRequest::toCommand)
                        .toList()
        );
    }
}

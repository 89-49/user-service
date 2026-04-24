package org.pgsg.user_service.auth.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.pgsg.user_service.auth.application.dto.command.SignupUserCommand;
import org.pgsg.user_service.user.application.dto.command.CreateChatTimeCommand;
import org.pgsg.user_service.user.domain.model.UserRole;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;


public record UserSignupRequest(
        @NotBlank(message = "아이디는 필수 입력 값입니다.")
        @Size(max = 12, message = "아이디는 최대 12자까지 가능합니다.")
        String username,

        @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
        String password,

        @NotBlank(message = "권한 정보는 필수 입력 값입니다.")
        String userRole,

        @NotBlank(message = "이름은 필수 입력 값입니다.")
        String name,

        @NotBlank(message = "닉네임은 필수 입력 값입니다.")
        String nickname,

		@Valid
        @NotEmpty(message = "채팅 가능 시간은 최소 하나 이상 등록해야 합니다.")
        List<ChatTimeRequest> chatTimeRanges
) {
    //채팅 가능 시간 정보만을 담당하는 내부 중첩 record
    public record ChatTimeRequest(
            @NotNull(message = "요일 정보는 필수입니다.")
            DayOfWeek dayOfWeek,

            @NotNull(message = "시작 시간은 필수입니다.")
            LocalTime startTime,

            @NotNull(message = "종료 시간은 필수입니다.")
            LocalTime endTime
    ) {
        public CreateChatTimeCommand toCreateCommand() {
            return new CreateChatTimeCommand(dayOfWeek, startTime, endTime);
        }
    }

    // Request DTO를 서비스 계층에서 사용할 Command 객체로 변환
    public SignupUserCommand toCommand() {
        return new SignupUserCommand(
                this.username,
                this.password,
                UserRole.find(this.userRole.toUpperCase()),
                this.name,
                this.nickname,
                this.chatTimeRanges.stream()
                        .map(ChatTimeRequest::toCreateCommand)
                        .toList()
        );
    }
}

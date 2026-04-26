package org.pgsg.user_service.auth.infrastructure;

import lombok.RequiredArgsConstructor;
import org.pgsg.user_service.user.domain.exception.UserException;
import org.pgsg.user_service.user.application.UserService;
import org.pgsg.user_service.user.application.dto.info.LoginUserDetailInfo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 이 클래스는 AuthService 내부에서 직접 호출해서 사용하지 않아도 됨
// AuthenticationManager에의 인증로직 통과 시, 커스텀한 UserDetailsImpl를 제공하기 위한 작업을 수행하는 클래스
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;	// 추후 FeignClient로 교체될 지점

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			LoginUserDetailInfo loginDetails = userService.getUserForAuth(username);

            // 공통모듈에서 제공하는 커스텀 UserDetailsImpl로 반환
            // 커스텀 UserDetailsImpl을 사용하기 위해 AuthenticationManager를 사용한 방식으로 전환함
            return loginDetails.toUserDetails();
		} catch (UserException e) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
		}
    }
}
package org.pgsg.user_service.auth.domain;

import org.pgsg.config.security.UserDetailsImpl;

public interface UserAuthenticator {

	UserDetailsImpl verify(String username, String rawPassword);
}

package com.ec2.user.service;

import com.ec2.config.JwtTokenProvider;
import com.ec2.user.dto.UserRequestDto;
import com.ec2.user.entity.User;
import com.ec2.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Long signup(UserRequestDto request) {
        // ⬇️⬇️⬇️ 이 부분을 수정 ⬇️⬇️⬇️
        if (userRepository.findByName(request.name()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이름입니다.");
        }
        // ⬇️⬇️⬇️ 이 부분을 수정 ⬇️⬇️⬇️
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.builder()
                .name(request.name()) // ◀◀◀ 여기도 수정
                .password(encodedPassword)
                .build();

        return userRepository.save(user).getId();
    }

    @Transactional(readOnly = true)
    public String login(UserRequestDto request) {
        // ⬇️⬇️⬇️ 이 부분을 수정 ⬇️⬇️⬇️
        User user = userRepository.findByName(request.name())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이름입니다."));

        // ⬇️⬇️⬇️ 이 부분을 수정 ⬇️⬇️⬇️
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        return jwtTokenProvider.createToken(user.getName());
    }
}
package com.ec2.user.controller;

import com.ec2.config.JwtTokenProvider;
import com.ec2.seat.dto.SeatResponseDto;
import com.ec2.seat.service.SeatService;
import com.ec2.user.dto.UserRequestDto;
import com.ec2.user.dto.UserResponseDto;
import com.ec2.user.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final SeatService seatService;

    @GetMapping("/healthcheck")
    public ResponseEntity<String> healthcheck(){
        return ResponseEntity.ok("백엔드 서버 실행 중");
    }

    // 회원가입 (공개)
    @PostMapping("/users/signup")
    public ResponseEntity<String> signup(@RequestBody UserRequestDto request) {
        userService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    // 로그인 (공개)
    @PostMapping("/users/login")
    public ResponseEntity<UserResponseDto> login(@RequestBody UserRequestDto request) {
        String token = userService.login(request);
        return ResponseEntity.ok(new UserResponseDto(token));
    }

    // 내 정보 보기 (보호됨 - 토큰 필요)
    // WebConfig에 등록된 "/api/protected/**" 패턴에 해당
    @GetMapping("/protected/me")
    public ResponseEntity<String> getMyInfo(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        String userName = jwtTokenProvider.getUserNameFromToken(token);
        return ResponseEntity.ok("당신의 이름은 " + userName + " 입니다.");
    }

    @GetMapping("/users/{memberId}/ticket")
    public ResponseEntity<List<SeatResponseDto>> getUserTickets(@PathVariable Long memberId) {
        List<SeatResponseDto> tickets = seatService.findSeatsByMember(memberId);
        return ResponseEntity.ok(tickets);
    }
}
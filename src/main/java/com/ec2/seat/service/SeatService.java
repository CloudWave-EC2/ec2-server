package com.ec2.seat.service;

import com.ec2.seat.dto.HoldRequestDto;
import com.ec2.seat.dto.ReservationRequestDto;
import com.ec2.seat.dto.SeatResponseDto;
import com.ec2.seat.repository.SeatRepository;
import com.ec2.user.entity.User;
import com.ec2.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {
    private static final Logger log = LoggerFactory.getLogger(SeatService.class);


    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate; // ◀◀◀ RedisTemplate 주입


    @Transactional(readOnly = true)
    public List<SeatResponseDto> findAllSeats() {
        return seatRepository.findAll().stream()
                .map(SeatResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void reserve(Long memberId, ReservationRequestDto request) {
        String key = "seat:hold:" + request.seatId();

        // 1. Redis에서 선점 정보 확인
        String holderId = redisTemplate.opsForValue().get(key);

        if (holderId == null) {
            throw new IllegalStateException("선점 정보가 없습니다. 먼저 좌석을 선점해주세요.");
        }

        if (!holderId.equals(memberId.toString())) {
            throw new IllegalStateException("다른 사용자가 선점한 좌석입니다.");
        }

        // 2. 사용자 조회 (기존 로직)
        User user = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. ID: " + memberId));

        // 3. DB에 좌석 예약 처리 (기존 로직)
        int updatedCount = seatRepository.reserveSeat(user, request.seatId());

        if (updatedCount != 1) {
            throw new IllegalStateException("이미 예약되었거나 존재하지 않는 좌석입니다.");
        }

        // 4. 예약 성공 시 Redis의 선점 정보 삭제
        redisTemplate.delete(key);
    }

    @Transactional(readOnly = true)
    public List<SeatResponseDto> findSeatsByMember(Long memberId) {
        return seatRepository.findByUserId(memberId).stream()
                .map(SeatResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void hold(Long memberId, HoldRequestDto request) {
        String key = "seat:hold:" + request.seatId();
        String value = memberId.toString();

        log.info("===== Redis 좌석 선점 시도 시작 =====");
        log.info("Key: [{}], Value: [{}], TTL: 10분", key, value);

        try {
            // Redis에 값을 쓰는 핵심 로직
            Boolean isSuccess = redisTemplate.opsForValue()
                    .setIfAbsent(key, value, 10, TimeUnit.MINUTES);

            // "isSuccess" 변수에 어떤 값이 담기는지 로그로 확인
            log.info("redisTemplate.opsForValue().setIfAbsent() 실행 결과: {}", isSuccess);

            if (isSuccess == null) {
                // 이 경우는 Redis 연결 자체에 문제가 있거나, 트랜잭션 관련 설정 문제일 수 있습니다.
                log.error("Redis 작업 결과가 null입니다! Redis 연결 상태 또는 설정을 확인해야 합니다.");
                throw new IllegalStateException("Redis 작업에 실패했습니다. (결과: null)");
            }

            if (!isSuccess) {
                log.warn("좌석 선점 실패: 이미 다른 사용자가 선점한 좌석입니다. Key: {}", key);
                throw new IllegalStateException("이미 다른 사용자가 선점한 좌석입니다.");
            }

            log.info("Redis에 좌석 선점 성공! Key: {}", key);

        } catch (Exception e) {
            // Redis 작업 중 예상치 못한 다른 예외가 발생했는지 확인
            log.error("Redis 작업 중 심각한 예외가 발생했습니다.", e);
            throw new RuntimeException("Redis 작업 중 오류 발생", e);
        } finally {
            log.info("===== Redis 좌석 선점 시도 종료 =====");
        }
    }
}
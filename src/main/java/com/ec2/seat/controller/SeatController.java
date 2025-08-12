package com.ec2.seat.controller;

import com.ec2.seat.dto.HoldRequestDto;
import com.ec2.seat.dto.ReservationRequestDto;
import com.ec2.seat.dto.SeatResponseDto;
import com.ec2.seat.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seats")
public class SeatController {

    private final SeatService seatService;

    // 좌석 목록 조회 (공개)
    @GetMapping
    public ResponseEntity<List<SeatResponseDto>> getSeats() {
        List<SeatResponseDto> seats = seatService.findAllSeats();
        return ResponseEntity.ok(seats);
    }

    @PostMapping("/{memberId}/reservations")
    public ResponseEntity<String> reserveSeats(
            @PathVariable Long memberId,
            @RequestBody ReservationRequestDto request) {

        seatService.reserve(memberId, request);
        return ResponseEntity.ok("좌석 예약이 완료되었습니다.");
    }

    @PostMapping("/{memberId}/hold")
    public ResponseEntity<String> holdSeat(
            @PathVariable Long memberId,
            @RequestBody HoldRequestDto request) {
        seatService.hold(memberId, request);
        return ResponseEntity.ok(request.seatId() + "번 좌석 선점에 성공했습니다.");
    }
}
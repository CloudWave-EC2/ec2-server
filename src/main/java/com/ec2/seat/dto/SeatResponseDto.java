package com.ec2.seat.dto;

import com.ec2.seat.entity.Seat;
import com.ec2.seat.entity.SeatStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

// record는 그 자체로 불변 데이터 객체입니다.
// 필드를 선언하면 private final 필드, 모든 필드를 받는 생성자, getter, equals(), hashCode(), toString()이 자동으로 생성됩니다.
public record SeatResponseDto(
        Long id,
        String code,
        SeatStatus status,
        @JsonInclude(JsonInclude.Include.NON_NULL) // userId가 null이면 json 응답에 포함하지 않음
        Long userId
) {
    /**
     * Seat 엔티티를 SeatResponseDto 레코드로 변환하기 위한 추가 생성자입니다.
     * 이 생성자를 통해 Service 로직을 수정할 필요 없이 DTO 변환을 깔끔하게 처리할 수 있습니다.
     * @param seat 변환할 Seat 엔티티
     */
    public SeatResponseDto(Seat seat) {
        // this()를 통해 레코드의 기본 생성자를 호출합니다.
        this(
                seat.getId(),
                seat.getCode(),
                seat.getStatus(),
                (seat.getUser() != null) ? seat.getUser().getId() : null // user가 null일 경우를 처리
        );
    }
}
package com.ec2.seat.repository;

import com.ec2.seat.entity.Seat;
import com.ec2.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat,Long> {
    @Modifying
    @Query("UPDATE Seat s SET s.user = :user, s.status = 'CONFIRMED' WHERE s.id = :seatId AND s.status = 'EMPTY'")
    int reserveSeat(@Param("user") User user, @Param("seatId") Long seatId);
    List<Seat> findByUserId(Long userId);

}

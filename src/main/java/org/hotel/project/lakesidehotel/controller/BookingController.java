package org.hotel.project.lakesidehotel.controller;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.hotel.project.lakesidehotel.exception.InvalidBookingRequestException;
import org.hotel.project.lakesidehotel.exception.ResourceNotFoundException;
import org.hotel.project.lakesidehotel.model.BookedRoom;
import org.hotel.project.lakesidehotel.model.Room;
import org.hotel.project.lakesidehotel.response.BookingResponse;
import org.hotel.project.lakesidehotel.response.RoomResponse;
import org.hotel.project.lakesidehotel.service.IBookingService;
import org.hotel.project.lakesidehotel.service.IRoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final IBookingService bookingService;
    private final IRoomService roomService;

    @GetMapping("/all-bookings")
    public ResponseEntity<List<BookingResponse>> getALlBookings() {
        List<BookedRoom> bookings = bookingService.getALlBookings();
        List<BookingResponse> bookingResponses = new ArrayList<>();
        IntStream
                .range(0, bookings.size())
                .forEach(index -> bookingResponses
                        .add(getBookingResponse(bookings.get(index))));
        return ResponseEntity.ok(bookingResponses);
    }

    @GetMapping("/confirmation/{confirmationCode}")
    public ResponseEntity<?> getBookingByConfirmationCode(@PathVariable String confirmationCode) {
        try {
            BookedRoom booking = bookingService.findByBookingConfirmationCode(confirmationCode);
            BookingResponse bookingResponse = getBookingResponse(booking);
            return ResponseEntity.ok(bookingResponse);
        } catch (ResourceNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    @PostMapping("/room/{roomId}/booking")
    public ResponseEntity<?> saveBooking(@PathVariable Long roomId,
                                         @RequestBody BookedRoom bookingRequest) {
        try {
            String confirmationCode = bookingService.saveBooking(roomId, bookingRequest);
            return ResponseEntity.ok("Room booked successfully, Your booking confirmation code is :" + confirmationCode);
        } catch (InvalidBookingRequestException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @DeleteMapping("/booking/{bookingId}/delete")
    public void cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
    }

    private BookingResponse getBookingResponse(BookedRoom booking) {
        Room room = roomService.getRoomById(booking.getRoom().getId()).get();
        RoomResponse roomResponse = RoomResponse
                .builder()
                .id(room.getId())
                .roomPrice(room.getRoomPrice())
                .roomType(room.getRoomType())
                .build();
        return BookingResponse
                .builder()
                .id(booking.getBookingId())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .guestFullName(booking.getGuestFullName())
                .guestEmail(booking.getGuestEmail())
                .numOfAdults(booking.getNumOfAdults())
                .numOfChildren(booking.getNumOfChildren())
                .totalNumOfGuest(booking.getTotalNumOfGuest())
                .bookingConfirmationCode(booking.getBookingConfirmationCode())
                .room(roomResponse)
                .build();
    }
}

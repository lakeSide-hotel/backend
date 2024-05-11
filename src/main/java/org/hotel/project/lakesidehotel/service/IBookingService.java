package org.hotel.project.lakesidehotel.service;

import org.hotel.project.lakesidehotel.model.BookedRoom;

import java.util.List;

public interface IBookingService {
    List<BookedRoom> getAllBookingByRoomId(Long roomId);

    List<BookedRoom> getALlBookings();

    void cancelBooking(Long bookingId);

    String saveBooking(Long roomId, BookedRoom bookingRequest);

    BookedRoom findByBookingConfirmationCode(String confirmationCode);
}

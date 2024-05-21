package org.hotel.project.lakesidehotel.service.implement;

import lombok.RequiredArgsConstructor;
import org.hotel.project.lakesidehotel.exception.InvalidBookingRequestException;
import org.hotel.project.lakesidehotel.exception.ResourceNotFoundException;
import org.hotel.project.lakesidehotel.model.BookedRoom;
import org.hotel.project.lakesidehotel.model.Room;
import org.hotel.project.lakesidehotel.repository.BookingRepository;
import org.hotel.project.lakesidehotel.service.IBookingService;
import org.hotel.project.lakesidehotel.service.IRoomService;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService implements IBookingService {
    private final BookingRepository bookingRepository;
    private final IRoomService roomService;

    @Override
    public List<BookedRoom> getAllBookingByRoomId(Long roomId) {
        return bookingRepository.findByRoomId(roomId);
    }

    @Override
    public List<BookedRoom> getALlBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public void cancelBooking(Long bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    @Override
    public String saveBooking(Long roomId, BookedRoom bookingRequest) {
        if (bookingRequest
                .getCheckOutDate()
                .isBefore(bookingRequest.getCheckInDate())) {
            throw new InvalidBookingRequestException("Check-in date must come before check-out date");
        }
        Room room = roomService.getRoomById(roomId).get();
        List<BookedRoom> existingBookings = room.getBookings();
        boolean roomIsAvailable = roomIsAvailable(bookingRequest, existingBookings);
        if (roomIsAvailable) {
            room.addBooking(bookingRequest);
            bookingRepository.save(bookingRequest);
        } else {
            throw new InvalidBookingRequestException("Sorry, this room is not available for the selected dates");
        }
        return bookingRequest.getBookingConfirmationCode();
    }

    @Override
    public BookedRoom findByBookingConfirmationCode(String confirmationCode) {
        return bookingRepository.findByBookingConfirmationCode(confirmationCode)
                .orElseThrow(
                        () -> new ResourceNotFoundException("No Booking found with booking code: " + confirmationCode)
                );
    }

    private boolean roomIsAvailable(BookedRoom bookingRequest,
                                    List<BookedRoom> existingBookings) {
        return existingBookings.stream()
                .noneMatch(existingBooking ->
                        bookingRequest.getCheckInDate().equals(existingBooking.getCheckInDate()) ||
                                bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckOutDate()) ||

                                (bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckInDate()) &&
                                        bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckOutDate())) ||

                                (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate()) &&
                                        bookingRequest.getCheckOutDate().equals(existingBooking.getCheckOutDate())) ||

                                (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate()) &&
                                        bookingRequest.getCheckOutDate().isAfter(existingBooking.getCheckOutDate())) ||

                                (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate()) &&
                                        bookingRequest.getCheckOutDate().equals(existingBooking.getCheckInDate())) ||

                                (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate()) &&
                                        bookingRequest.getCheckOutDate().equals(bookingRequest.getCheckInDate()))
                );
    }
}

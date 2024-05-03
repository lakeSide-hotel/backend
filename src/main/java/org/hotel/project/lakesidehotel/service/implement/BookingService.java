package org.hotel.project.lakesidehotel.service.implement;

import org.hotel.project.lakesidehotel.model.BookedRoom;
import org.hotel.project.lakesidehotel.service.IBookingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService implements IBookingService {
    @Override
    public List<BookedRoom> getAllBookingByRoomId(Long roomId) {
        return null;
    }
}

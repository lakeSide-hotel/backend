package org.hotel.project.lakesidehotel.controller;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.hotel.project.lakesidehotel.exception.PhotoRetrievalException;
import org.hotel.project.lakesidehotel.model.BookedRoom;
import org.hotel.project.lakesidehotel.model.Room;
import org.hotel.project.lakesidehotel.response.BookingResponse;
import org.hotel.project.lakesidehotel.response.RoomResponse;
import org.hotel.project.lakesidehotel.service.IBookingService;
import org.hotel.project.lakesidehotel.service.IRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoomController {
    private final IRoomService roomService;
    private final IBookingService bookingService;

    @PostMapping("/add/new-room")
    public ResponseEntity<RoomResponse> addNewRoom(@RequestParam("photo") MultipartFile photo,
                                                   @RequestParam("roomType") String roomType,
                                                   @RequestParam("roomPrice") BigDecimal roomPrice) throws SQLException, IOException {
        Room room = roomService.addNewRoom(photo, roomType, roomPrice);
        RoomResponse response = new RoomResponse(room.getId(), room.getRoomType(), room.getRoomPrice());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/room/types")
    public List<String> getRoomTypes() {
        return roomService.getAllRoomTypes();
    }

    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() throws SQLException {
        List<Room> rooms = roomService.getAllRooms();
        List<RoomResponse> roomResponses = new ArrayList<>();
        for (Room room : rooms) {
            byte[] photoBytes = roomService.getRoomPhotoById(room.getId());
            if (photoBytes != null && photoBytes.length > 0) {
                String base64Photo = Base64.encodeBase64String(photoBytes);
                RoomResponse roomResponse = getRoomResponse(room);
                roomResponse.setPhoto(base64Photo);
                roomResponses.add(roomResponse);
            }
        }
        return ResponseEntity.ok(roomResponses);
    }

    private RoomResponse getRoomResponse(Room room) {
        List<BookedRoom> bookings = getAllBookingByRoomId(room.getId());
//        List<BookingResponse> bookingInfo = bookings.stream().map(
//                booking -> BookingResponse.builder()
//                        .id(booking.getBookingId())
//                        .checkInDate(booking.getCheckInDate())
//                        .checkOutDate(booking.getCheckOutDate())
//                        .bookingConfirmationCode(booking.getBookingConfirmationCode())
//                        .build()).toList();
        byte[] photoBytes = null;
        Blob photoBlob = room.getPhoto();
        if(photoBlob != null) {
            try {
                photoBytes = photoBlob.getBytes(1, (int) photoBlob.length());
            } catch (SQLException e) {
                throw  new PhotoRetrievalException("Error retrieving photo");
            }
        }
        return RoomResponse.builder()
                .id(room.getId())
                .roomType(room.getRoomType())
                .roomPrice(room.getRoomPrice())
                .isBooked(room.getIsBooked())
//                .bookings(bookingInfo)
                .photo(Base64.encodeBase64String(photoBytes))
                .build();
    }

    @DeleteMapping("/delete/room/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable("roomId") Long roomId) {
        roomService.deleteRoom(roomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private List<BookedRoom> getAllBookingByRoomId(Long roomId) {
        return bookingService.getAllBookingByRoomId(roomId);
    }

}

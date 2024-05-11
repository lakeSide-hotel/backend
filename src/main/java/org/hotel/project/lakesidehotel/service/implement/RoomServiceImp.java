package org.hotel.project.lakesidehotel.service.implement;

import lombok.RequiredArgsConstructor;
import org.hotel.project.lakesidehotel.exception.InternalServerException;
import org.hotel.project.lakesidehotel.exception.ResourceNotFoundException;
import org.hotel.project.lakesidehotel.model.Room;
import org.hotel.project.lakesidehotel.repository.RoomRepository;
import org.hotel.project.lakesidehotel.service.IRoomService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomServiceImp implements IRoomService {

    private final RoomRepository roomRepository;

    @Override
    public Room addNewRoom(MultipartFile photo, String roomType, BigDecimal roomPrice) throws IOException, SQLException {
        Room room = Room.builder()
                .roomType(roomType)
                .roomPrice(roomPrice)
                .isBooked(false)
                .build();
        if (!photo.isEmpty()) {
            byte[] photoBytes = photo.getBytes();
            Blob photoBlob = new SerialBlob(photoBytes);
            room.setPhoto(photoBlob);
        }
        return roomRepository.save(room);
    }

    @Override
    public List<String> getAllRoomTypes() {
        return roomRepository.findDistinctRoomTypes();
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public byte[] getRoomPhotoById(Long roomId) throws SQLException {
        Optional<Room> room = roomRepository.findById(roomId);
        if (room.isEmpty()) {
            throw new ResourceNotFoundException("Sorry, room not found");
        }
        Blob photoBlob = room.get().getPhoto();
        if (photoBlob != null) {
            return photoBlob.getBytes(1, (int) photoBlob.length());
        }
        return null;
    }

    @Override
    public void deleteRoom(Long roomId) {
        Optional<Room> roomOptional = roomRepository.findById(roomId);
        if (roomOptional.isPresent()) {
            roomRepository.deleteById(roomId);
        }

    }

    @Override
    public Room updateRoom(Long roomId, String roomType, BigDecimal roomPrice, byte[] photoBytes) {
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        if (roomType != null)
            room.setRoomType(roomType);
        if (roomPrice != null)
            room.setRoomPrice(roomPrice);
        if (photoBytes != null && photoBytes.length > 0) {
            try {
                 room.setPhoto(new SerialBlob(photoBytes));
            } catch (SQLException sqlException) {
                throw new InternalServerException("Error updating room");
            }
        }
        return roomRepository.save(room);
    }

    @Override
    public Optional<Room> getRoomById(Long roomId) {
        return roomRepository.findById(roomId);
    }
}

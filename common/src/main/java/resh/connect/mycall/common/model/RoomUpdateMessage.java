package resh.connect.mycall.common.model;

import java.util.List;

public class RoomUpdateMessage {
    private String type = "room_update";
    private List<Participant> participants;

    public RoomUpdateMessage(List<Participant> participants) {
        this.participants = participants;
    }

    public String getType() { return type; }
    public List<Participant> getParticipants() { return participants; }
}

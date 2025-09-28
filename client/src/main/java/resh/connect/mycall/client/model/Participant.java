package resh.connect.mycall.client.model;

public class Participant {
    private final String nickname;
    private boolean micOn;

    public Participant(String nickname, boolean micOn) {
        this.nickname = nickname;
        this.micOn = micOn;
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isMicOn() {
        return micOn;
    }

    public void setMicOn(boolean micOn) {
        this.micOn = micOn;
    }

    @Override
    public String toString() {
        return nickname + (micOn ? " (микрофон Вкл)" : " (микрофон Выкл)");
    }
}


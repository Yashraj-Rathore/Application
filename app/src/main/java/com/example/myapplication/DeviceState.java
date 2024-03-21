package com.example.myapplication;

public enum DeviceState {

    DEFAULT_STATE(0),
    STATE_ONE(1),
    STATE_TWO(2),// ... add more states as necessary

    STATE_THREE(3),
    STATE_FOUR(4),// ... add more states as necessary
    STATE_FIVE(5),
    STATE_SIX(6),// ... add more states as necessary
    STATE_SEVEN(7),

    STATE_EIGHT(8);



    private final int stateCode;

    DeviceState(int stateCode) {
        this.stateCode = stateCode;
    }

    public int getStateCode() {
        return stateCode;
    }

    public static DeviceState fromStateCode(int stateCode) {
        for (DeviceState state : DeviceState.values()) {
            if (state.getStateCode() == stateCode) {
                return state;
            }
        }
        return DEFAULT_STATE;
    }
}


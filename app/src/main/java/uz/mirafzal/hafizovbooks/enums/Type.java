package uz.mirafzal.hafizovbooks.enums;

import uz.mirafzal.hafizovbooks.R;

public enum Type {
    TEXT (R.string.text),
    AUDIO (R.string.audio),
    VIDEO (R.string.video);

    private int titleId;

    Type(int title) {
        this.titleId = title;
    }

    public int getTitleId() {
        return titleId;
    }
}

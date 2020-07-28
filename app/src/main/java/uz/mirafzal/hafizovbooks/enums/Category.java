package uz.mirafzal.hafizovbooks.enums;

import uz.mirafzal.hafizovbooks.R;

public enum Category {
    MY_BOOKS(R.string.my_books),
    MY_PURCHASES(R.string.my_purchases),
    SHOP(R.string.shop),
    DISCOUNTS(R.string.discounts);

    private int titleId;

    Category(int title) {
        this.titleId = title;
    }

    public int getTitleId() {
        return titleId;
    }
}

public class Card {
    private String cardID;

    public Card(String cardID) {
        this.cardID = cardID;
    }

    public String getCardID() {
        return cardID;
    }

    public boolean isNumber() {
        return getNumberValue() >= 0;
    }

    public int getNumberValue() {
        if (cardID.length() == 0) {
            return -1;
        }
        for (int i = 0; i < cardID.length(); i++) {
            if (!Character.isDigit(cardID.charAt(i))) {
                return -1;
            }
        }
        return Integer.parseInt(cardID);
    }

    public boolean isBonus() {
        return cardID.startsWith("plus_");
    }

    public boolean isDoubler() {
        return cardID.equals("times_2");
    }

    public boolean isAction() {
        return cardID.equals("flip_3") || cardID.equals("freeze") || cardID.equals("second_chance");
    }

    public int getBonusValue() {
        if (!isBonus()) {
            return 0;
        }
        return Integer.parseInt(cardID.substring(5));
    }

    public String getLabel() {
        if (isNumber()) {
            return cardID;
        }
        if (isBonus()) {
            return "+" + getBonusValue();
        }
        if (isDoubler()) {
            return "x2";
        }
        if (cardID.equals("flip_3")) {
            return "Flip 3";
        }
        if (cardID.equals("freeze")) {
            return "Freeze";
        }
        if (cardID.equals("second_chance")) {
            return "Second Chance";
        }
        return cardID;
    }

    public String toString() {
        return cardID;
    }
}

import java.util.*;

public class Player {
    public static final String ACTIVE = "active";
    public static final String STAYED = "stayed";
    public static final String FROZEN = "frozen";
    public static final String BUSTED = "busted";

    public static final int FLIP_SEVEN = 7;

    private String name;
    private ArrayList<Card> hand = new ArrayList<Card>();
    private String state = ACTIVE;
    private int score = 0;
    private int lastRoundPoints = 0;

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    public String getState() {
        return state;
    }

    public int getScore() {
        return score;
    }

    public int getLastRoundPoints() {
        return lastRoundPoints;
    }

    public void bankRound(int points) {
        lastRoundPoints = points;
        score += points;
    }

    public void addToHand(Card card) {
        hand.add(card);
    }

    public Card removeFromHand(String cardID) {
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getCardID().equals(cardID)) {
                return hand.remove(i);
            }
        }
        return null;
    }

    public void removeCard(Card card) {
        hand.remove(card);
    }

    public boolean hasCard(String cardID) {
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getCardID().equals(cardID)) {
                return true;
            }
        }
        return false;
    }

    public void updateState(String newState) {
        state = newState;
    }

    public boolean isActive() {
        return state.equals(ACTIVE);
    }

    public boolean scoresThisRound() {
        return !state.equals(BUSTED);
    }

    public String checkState() {
        if (!isActive()) {
            return state;
        }
        Set<Integer> numbers = new HashSet<Integer>();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.isNumber() && !numbers.add(card.getNumberValue())) {
                state = BUSTED;
                return state;
            }
        }
        return state;
    }

    public int uniqueNumberCount() {
        Set<Integer> numbers = new HashSet<Integer>();
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).isNumber()) {
                numbers.add(hand.get(i).getNumberValue());
            }
        }
        return numbers.size();
    }

    public boolean hasFlippedSeven() {
        return uniqueNumberCount() >= FLIP_SEVEN;
    }

    public int checkTotal() {
        int numberTotal = 0;
        int bonus = 0;
        boolean doubled = false;

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.isNumber()) {
                numberTotal += card.getNumberValue();
            } else if (card.isDoubler()) {
                doubled = true;
            } else if (card.isBonus()) {
                bonus += card.getBonusValue();
            }
        }

        if (doubled) {
            numberTotal = numberTotal * 2;
        }
        return numberTotal + bonus;
    }

    public int roundScore(int flipSevenBonus) {
        if (!scoresThisRound()) {
            return 0;
        }
        if (hasFlippedSeven()) {
            return checkTotal() + flipSevenBonus;
        }
        return checkTotal();
    }

    public void resetHand() {
        hand.clear();
        state = ACTIVE;
    }
}

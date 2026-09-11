import java.util.*;

public class Game {
    public static final String MODE_WAITING_FOR_START = "waiting_for_start";
    public static final String MODE_WAITING_FOR_ACTION = "waiting_for_action";
    public static final String MODE_FLIP3_CHOICE = "waiting_for_flip3_choice";
    public static final String MODE_FREEZE_CHOICE = "waiting_for_freeze_choice";
    public static final String MODE_SECOND_CHANCE_CHOICE = "waiting_for_second_chance_choice";
    public static final String MODE_GAME_OVER = "game_over";

    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 5;
    public static final int TARGET_SCORE = 200;
    public static final int FLIP_SEVEN_BONUS = 15;
    public static final int FLIP_3_CARDS = 3;

    public static final int KEY_HIT = 1;
    public static final int KEY_STAY = 2;

    private ArrayList<Player> players = new ArrayList<Player>();
    private ArrayList<Card> deck = new ArrayList<Card>();
    private ArrayList<Card> discard = new ArrayList<Card>();
    private ArrayList<Card> roundDiscard = new ArrayList<Card>();

    private ArrayList<Card> pendingCards = new ArrayList<Card>();
    private ArrayList<Player> pendingOwners = new ArrayList<Player>();

    private int roundCount = 0;
    private int dealerIndex = 0;
    private int currentPlayerIndex = 0;

    private boolean gameInitialized = false;
    private boolean isPlaying = false;
    private boolean roundEnded = false;
    private String gameMode = MODE_WAITING_FOR_START;
    private String lastCardDrawn = "";
    private String message = "";

    private Card pendingCard = null;
    private Player pendingActor = null;

    public Game(int cnt) {
        int playerCount = Math.max(MIN_PLAYERS, Math.min(cnt, MAX_PLAYERS));
        for (int i = 0; i < playerCount; i++) {
            players.add(new Player("Player " + (i + 1)));
        }
        resetDeck();
    }

    public ArrayList<Player> getState() {
        return players;
    }

    public Player getCurrentPlayer() {
        if (players.isEmpty()) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public int getDealerIndex() {
        return dealerIndex;
    }

    public String getGameMode() {
        return gameMode;
    }

    public String getLastCardDrawn() {
        return lastCardDrawn;
    }

    public int getRoundCount() {
        return roundCount;
    }

    public boolean isGamePlaying() {
        return isPlaying;
    }

    public boolean isGameOver() {
        return gameMode.equals(MODE_GAME_OVER);
    }

    public boolean isRoundFinished() {
        return gameMode.equals(MODE_WAITING_FOR_START) || gameMode.equals(MODE_GAME_OVER);
    }

    public boolean isChoosingTarget() {
        return gameMode.equals(MODE_FLIP3_CHOICE)
                || gameMode.equals(MODE_FREEZE_CHOICE)
                || gameMode.equals(MODE_SECOND_CHANCE_CHOICE);
    }

    public Player getPendingActor() {
        return pendingActor;
    }

    public Card getPendingCard() {
        return pendingCard;
    }

    public int getCardsLeft() {
        return deck.size();
    }

    public String getMessage() {
        return message;
    }

    public boolean canTarget(Player player) {
        if (!isChoosingTarget() || pendingCard == null) {
            return false;
        }
        return eligibleTargets(pendingCard).contains(player);
    }

    private void resetDeck() {
        deck.clear();
        discard.clear();
        roundDiscard.clear();
        deck.add(new Card("0"));
        for (int i = 1; i <= 12; i++) {
            for (int j = 1; j <= i; j++) {
                deck.add(new Card(Integer.toString(i)));
            }
        }
        for (int i = 0; i < 3; i++) {
            deck.add(new Card("flip_3"));
            deck.add(new Card("freeze"));
            deck.add(new Card("second_chance"));
        }
        deck.add(new Card("plus_2"));
        deck.add(new Card("plus_4"));
        deck.add(new Card("plus_6"));
        deck.add(new Card("plus_8"));
        deck.add(new Card("plus_10"));
        deck.add(new Card("times_2"));
        Collections.shuffle(deck);
    }

    public void init() {
        if (!gameInitialized) {
            gameInitialized = true;
            startNewRound();
        }
    }

    public void processInput(int key) {
        if (!gameInitialized) {
            return;
        }
        if (gameMode.equals(MODE_WAITING_FOR_START)) {
            dealRound();
        } else if (gameMode.equals(MODE_WAITING_FOR_ACTION)) {
            if (key == KEY_HIT) {
                hit();
            } else if (key == KEY_STAY) {
                stay();
            }
        } else if (isChoosingTarget()) {
            chooseTarget(key - 1);
        }
    }

    public void hit() {
        if (!gameMode.equals(MODE_WAITING_FOR_ACTION)) {
            return;
        }
        Player player = getCurrentPlayer();
        if (player == null || !player.isActive()) {
            return;
        }

        Card card = drawCard(player);
        if (card == null) {
            endRound();
            return;
        }
        resolveCard(player, card);
        processPendingActions();
        if (isChoosingTarget() || roundEnded) {
            return;
        }
        endTurn();
    }

    public void stay() {
        if (!gameMode.equals(MODE_WAITING_FOR_ACTION)) {
            return;
        }
        Player player = getCurrentPlayer();
        if (player == null || !player.isActive()) {
            return;
        }
        player.updateState(Player.STAYED);
        message = player.getName() + " stays on " + player.checkTotal() + ".";
        endTurn();
    }

    public void chooseTarget(int playerIndex) {
        if (!isChoosingTarget() || playerIndex < 0 || playerIndex >= players.size()) {
            return;
        }
        Player target = players.get(playerIndex);
        if (!canTarget(target)) {
            return;
        }

        Card card = pendingCard;
        Player owner = pendingActor;
        pendingCard = null;
        pendingActor = null;
        gameMode = MODE_WAITING_FOR_ACTION;

        applyAction(owner, card, target);
        processPendingActions();
        if (isChoosingTarget() || roundEnded) {
            return;
        }
        endTurn();
    }

    public void dealRound() {
        if (gameMode.equals(MODE_WAITING_FOR_START)) {
            startNewRound();
        }
    }

    private void startNewRound() {
        roundCount++;
        roundEnded = false;
        for (int i = 0; i < players.size(); i++) {
            roundDiscard.addAll(players.get(i).getHand());
            players.get(i).resetHand();
        }
        if (roundCount > 1) {
            dealerIndex = (dealerIndex + 1) % players.size();
        }
        currentPlayerIndex = (dealerIndex + 1) % players.size();
        pendingCard = null;
        pendingActor = null;
        pendingCards.clear();
        pendingOwners.clear();
        lastCardDrawn = "";
        gameMode = MODE_WAITING_FOR_ACTION;
        isPlaying = true;
        message = "Round " + roundCount + ". " + players.get(dealerIndex).getName()
                + " deals, " + getCurrentPlayer().getName() + " goes first.";
    }

    private void endTurn() {
        if (someoneFlippedSeven() || !hasActivePlayer()) {
            endRound();
            return;
        }
        advanceToNextPlayer();
        gameMode = MODE_WAITING_FOR_ACTION;
    }

    public void advanceToNextPlayer() {
        if (players.isEmpty() || !hasActivePlayer()) {
            return;
        }
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (!players.get(currentPlayerIndex).isActive());
    }

    private boolean hasActivePlayer() {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).isActive()) {
                return true;
            }
        }
        return false;
    }

    private boolean someoneFlippedSeven() {
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (player.hasFlippedSeven() && player.scoresThisRound()) {
                return true;
            }
        }
        return false;
    }

    private void endRound() {
        if (roundEnded) {
            return;
        }
        roundEnded = true;
        isPlaying = false;

        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).isActive()) {
                players.get(i).updateState(Player.STAYED);
            }
        }

        String summary = "Round " + roundCount + ":";
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            int points = player.roundScore(FLIP_SEVEN_BONUS);
            player.bankRound(points);
            if (i > 0) {
                summary = summary + ",";
            }
            summary = summary + " " + player.getName() + " +" + points;
        }

        discardPendingActions();
        roundDiscard.addAll(discard);
        discard.clear();

        Player winner = getWinner();
        if (winner != null) {
            gameMode = MODE_GAME_OVER;
            message = winner.getName() + " wins with " + winner.getScore() + " points!";
        } else if (isTieAtTarget()) {
            gameMode = MODE_WAITING_FOR_START;
            message = summary + ". Tie on " + getTopScore() + " - another round is played.";
        } else {
            gameMode = MODE_WAITING_FOR_START;
            message = summary;
        }
    }

    public int getTopScore() {
        int top = 0;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getScore() > top) {
                top = players.get(i).getScore();
            }
        }
        return top;
    }

    private int countAtScore(int score) {
        int count = 0;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getScore() == score) {
                count++;
            }
        }
        return count;
    }

    public boolean isTieAtTarget() {
        int top = getTopScore();
        return top >= TARGET_SCORE && countAtScore(top) > 1;
    }

    public Player getWinner() {
        int top = getTopScore();
        if (top < TARGET_SCORE || countAtScore(top) > 1) {
            return null;
        }
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getScore() == top) {
                return players.get(i);
            }
        }
        return null;
    }

    public Player getLeader() {
        Player leader = null;
        for (int i = 0; i < players.size(); i++) {
            if (leader == null || players.get(i).getScore() > leader.getScore()) {
                leader = players.get(i);
            }
        }
        return leader;
    }

    private Card drawCard(Player player) {
        if (deck.isEmpty()) {
            if (roundDiscard.isEmpty()) {
                return null;
            }
            deck.addAll(roundDiscard);
            roundDiscard.clear();
            Collections.shuffle(deck);
        }
        Card card = deck.remove(deck.size() - 1);
        lastCardDrawn = card.getCardID();
        player.addToHand(card);
        return card;
    }

    private void resolveCard(Player player, Card card) {
        if (card.isNumber()) {
            if (countNumber(player, card.getNumberValue()) > 1 && player.hasCard("second_chance")) {
                moveIdToDiscard(player, "second_chance");
                moveCardToDiscard(player, card);
                message = player.getName() + " uses Second Chance on the second " + card.getCardID() + ".";
                return;
            }
            if (player.checkState().equals(Player.BUSTED)) {
                message = player.getName() + " busts on a second " + card.getCardID() + ".";
            } else if (player.hasFlippedSeven()) {
                message = player.getName() + " flips 7! " + FLIP_SEVEN_BONUS + " point bonus.";
            }
            return;
        }
        if (card.isAction()) {
            pendingCards.add(card);
            pendingOwners.add(player);
        }
    }

    private void processPendingActions() {
        while (!pendingCards.isEmpty()) {
            if (someoneFlippedSeven()) {
                return;
            }
            Card card = pendingCards.remove(0);
            Player owner = pendingOwners.remove(0);
            if (!owner.getHand().contains(card)) {
                continue;
            }
            if (!owner.isActive()) {
                moveCardToDiscard(owner, card);
                continue;
            }

            ArrayList<Player> targets = eligibleTargets(card);
            if (targets.isEmpty()) {
                moveCardToDiscard(owner, card);
                continue;
            }
            if (targets.size() == 1) {
                applyAction(owner, card, targets.get(0));
                continue;
            }

            pendingCard = card;
            pendingActor = owner;
            gameMode = modeFor(card);
            message = owner.getName() + " drew " + card.getLabel() + ". Choose a player.";
            return;
        }
    }

    private String modeFor(Card card) {
        if (card.getCardID().equals("freeze")) {
            return MODE_FREEZE_CHOICE;
        }
        if (card.getCardID().equals("flip_3")) {
            return MODE_FLIP3_CHOICE;
        }
        return MODE_SECOND_CHANCE_CHOICE;
    }

    private ArrayList<Player> eligibleTargets(Card card) {
        ArrayList<Player> targets = new ArrayList<Player>();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (!player.isActive()) {
                continue;
            }
            if (card.getCardID().equals("second_chance") && hasOtherSecondChance(player, card)) {
                continue;
            }
            targets.add(player);
        }
        return targets;
    }

    private boolean hasOtherSecondChance(Player player, Card card) {
        ArrayList<Card> hand = player.getHand();
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i) != card && hand.get(i).getCardID().equals("second_chance")) {
                return true;
            }
        }
        return false;
    }

    private void applyAction(Player owner, Card card, Player target) {
        String cardID = card.getCardID();
        if (cardID.equals("freeze")) {
            moveCardToDiscard(owner, card);
            target.updateState(Player.FROZEN);
            message = owner.getName() + " freezes " + target.getName() + " on " + target.checkTotal() + ".";
        } else if (cardID.equals("flip_3")) {
            moveCardToDiscard(owner, card);
            message = owner.getName() + " gives Flip 3 to " + target.getName() + ".";
            flipThree(target);
        } else {
            if (target != owner) {
                owner.removeCard(card);
                target.addToHand(card);
                message = owner.getName() + " gives Second Chance to " + target.getName() + ".";
            } else {
                message = owner.getName() + " keeps the Second Chance.";
            }
        }
    }

    private void flipThree(Player target) {
        for (int i = 0; i < FLIP_3_CARDS; i++) {
            if (!target.isActive()) {
                return;
            }
            Card card = drawCard(target);
            if (card == null) {
                return;
            }
            resolveCard(target, card);
            if (target.hasFlippedSeven() && target.scoresThisRound()) {
                return;
            }
        }
    }

    private int countNumber(Player player, int value) {
        int count = 0;
        ArrayList<Card> hand = player.getHand();
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).isNumber() && hand.get(i).getNumberValue() == value) {
                count++;
            }
        }
        return count;
    }

    private void discardPendingActions() {
        if (pendingCard != null && pendingActor != null && pendingActor.getHand().contains(pendingCard)) {
            moveCardToDiscard(pendingActor, pendingCard);
        }
        pendingCard = null;
        pendingActor = null;

        for (int i = 0; i < pendingCards.size(); i++) {
            Card card = pendingCards.get(i);
            Player owner = pendingOwners.get(i);
            if (owner.getHand().contains(card)) {
                moveCardToDiscard(owner, card);
            }
        }
        pendingCards.clear();
        pendingOwners.clear();
    }

    private void moveCardToDiscard(Player player, Card card) {
        player.removeCard(card);
        discard.add(card);
    }

    private void moveIdToDiscard(Player player, String cardID) {
        Card removed = player.removeFromHand(cardID);
        if (removed != null) {
            discard.add(removed);
        }
    }
}

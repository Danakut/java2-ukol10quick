package cz.czechitas.webapp.logic;

import java.util.*;
import cz.czechitas.webapp.entity.*;
import cz.czechitas.webapp.persistence.*;

public class PexesoService {
    

    public final int CARD_PAIR_SUM = 4;

    private PexesoRepository gameProvider;

    public PexesoService(PexesoRepository gameProvider) {
        this.gameProvider = gameProvider;
    }

    public GameBoard createBoard() {
        GameBoard board = new GameBoard(createCardset(), GameStatus.PLAYER1_CHOOSE_1ST_CARD);
        gameProvider.save(board);
        return board;
    }

    public GameBoard findBoard(Long id) {
        return gameProvider.findOne(id);
    }

    public void makeMove(Long boardId, int clickedCardNumber) {
        GameBoard board = gameProvider.findOne(boardId);
        Card chosenCard = board.findCard(clickedCardNumber);

        if (board.getStatus() == GameStatus.PLAYER1_CHOOSE_1ST_CARD) {
            if (chosenCard.getStatus() == CardStatus.BACK) {
                chosenCard.setStatus(CardStatus.FACE);
                board.setStatus(GameStatus.PLAYER1_CHOOSE_2ND_CARD);
            }

        } else if (board.getStatus() == GameStatus.PLAYER1_CHOOSE_2ND_CARD)  {
            if (chosenCard.getStatus() == CardStatus.BACK) {
                chosenCard.setStatus(CardStatus.FACE);
                board.setStatus(GameStatus.PLAYER1_EVALUATION);
            }

        } else if (board.getStatus() == GameStatus.PLAYER1_EVALUATION) {
            ArrayList<Card> turnedCards = new ArrayList<>();
            int cardsTaken = 0;
            for (Card card : board.getCardset()) {
                if (card.getStatus() == CardStatus.FACE) {
                    turnedCards.add(card);
                }
                if (card.getStatus() == CardStatus.TAKEN) {
                    cardsTaken +=1;
                }
            }

            Card card1 = turnedCards.get(0);
            Card card2 = turnedCards.get(1);
            if (card1.getFilepath().equals(card2.getFilepath())) {
                card1.setStatus(CardStatus.TAKEN);
                card2.setStatus(CardStatus.TAKEN);
                cardsTaken +=2;
            } else {
                card1.setStatus(CardStatus.BACK);
                card2.setStatus(CardStatus.BACK);
            }

            if (cardsTaken/2 == CARD_PAIR_SUM) {
                board.setStatus(GameStatus.GAME_FINISHED);
            } else {
                board.setStatus(GameStatus.PLAYER1_CHOOSE_1ST_CARD);
            }
        }

        gameProvider.save(board);
    }

    private List<Card> createCardset() {
        List<Card> cardset = new ArrayList<>();
        for (int cardNumber = 0; cardNumber < CARD_PAIR_SUM * 2; cardNumber++) {
            Card card = new Card(cardNumber, CardStatus.BACK);
            cardset.add(card);
        }
        Collections.shuffle(cardset);
        return cardset;
    }
}

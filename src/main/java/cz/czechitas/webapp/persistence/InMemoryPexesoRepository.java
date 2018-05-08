package cz.czechitas.webapp.persistence;

import java.util.*;
import cz.czechitas.webapp.entity.*;


public class InMemoryPexesoRepository implements PexesoRepository {

    private Random random;
    private Map<Long, GameBoard> gameBoardMap;

    public InMemoryPexesoRepository() {
        random = new Random();
        gameBoardMap = new HashMap<>();
    }

    public GameBoard findOne(Long id) {
        GameBoard board = gameBoardMap.get(id);
        if (board == null) {
            throw new GameNotFoundException();
        }
        return board;
    }

    public GameBoard save(GameBoard board) {
        if (board.getId() == null) {
            setupNewBoard(board);
        }
        gameBoardMap.put(board.getId(), board);
        return board;
    }

    private Long vygenerujNahodneId() {
        return (long)Math.abs(random.nextInt());
    }

    private GameBoard setupNewBoard(GameBoard board) {
        board.setId(vygenerujNahodneId());        //co se stane, kdyz se nahodne vygeneruje id hry, ktera uz je v "databazi" (gameBoardMap)?
        for (Card card : board.getCardset()) {
            card.setId(vygenerujNahodneId());
        }
        return board;
    }
}

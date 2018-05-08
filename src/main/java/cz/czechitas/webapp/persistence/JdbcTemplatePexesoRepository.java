package cz.czechitas.webapp.persistence;

import java.sql.*;
import java.time.*;
import java.util.*;
import org.mariadb.jdbc.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.*;
import cz.czechitas.webapp.entity.*;

public class JdbcTemplatePexesoRepository implements PexesoRepository {

    private MariaDbDataSource dataSource;
    private JdbcTemplate querySender;
    private RowMapper<GameBoard> boardConverter;
    private RowMapper<Card> cardConverter;

    public JdbcTemplatePexesoRepository() {
        try {
            dataSource = new MariaDbDataSource();
            dataSource.setUserName("student");
            dataSource.setPassword("password");
            dataSource.setUrl("jdbc:mysql://localhost:3306/Pexeso");
            querySender = new JdbcTemplate(dataSource);
            boardConverter = BeanPropertyRowMapper.newInstance(GameBoard.class);
            cardConverter = BeanPropertyRowMapper.newInstance(Card.class);
        } catch (SQLException e) {
            throw new RuntimeException("Nepodarilo se pripojit do databaze: " + e.getMessage(), e);
        }
    }

    public GameBoard findOne(Long id) {
        GameBoard board = querySender.queryForObject("SELECT ID, Status FROM Gameboards WHERE ID = ?", boardConverter, id);
        List<Card> cardset = querySender.query("SELECT ID, CardNumber, Status FROM Cards WHERE GameBoardID = ?",cardConverter, id);
        board.setCardset(cardset);
        return board;
    }

    public GameBoard save(GameBoard board) {
        if (board.getId() == null) {
            setupNewBoard(board);
        }
        updateBoard(board);
        return board;
    }
    
    private GameBoard setupNewBoard(GameBoard board) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO GameBoards (Status, LastMove) VALUES (?, ?)";
        querySender.update((Connection con) -> {
                    PreparedStatement statement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    statement.setString(1, board.getStatus().name());              // proč je tu getStatus().name() místo jen getStatus()?
                    statement.setObject(2, Instant.now());
                    return statement;
                },
                keyHolder);
        board.setId(keyHolder.getKey().longValue());
        
        List<Card> cardset = board.getCardset();
        for (int i = 0; i < cardset.size(); i++) {
            Card card = cardset.get(i);
            addCard(card, board.getId(), i);
        }
        return board;
    }

    private void addCard(Card card, Long boardId, int cardArrayIndex) {
        GeneratedKeyHolder cardKeyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO Cards (CardNumber, Status, GameBoardID, CardOrder) " +
                "VALUES (?, ?, ?, ?)";
        querySender.update((Connection con) -> {
                    PreparedStatement statement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    statement.setInt(1, card.getCardNumber());
                    statement.setString(2, card.getStatus().name());
                    statement.setLong(3, boardId);
                    statement.setInt(4, cardArrayIndex);
                    return statement;
                },
                cardKeyHolder);
        card.setId(cardKeyHolder.getKey().longValue());
    }

    private GameBoard updateBoard(GameBoard board) {
        querySender.update("UPDATE GameBoards SET Status = ?,LastMove = ? WHERE ID = ?",
                board.getStatus().name(),     // proč je tu getStatus().name() místo jen getStatus()?
                Instant.now(),
                board.getId());

        List<Card> cardset = board.getCardset();
        for (int i = 0; i < cardset.size(); i++) {
            Card card = cardset.get(i);
            querySender.update("UPDATE Cards SET Status = ?, CardOrder = ? WHERE ID = ?",
                    card.getStatus().name(),
                    i,
                    card.getId());
        }
        return board;
    }
}


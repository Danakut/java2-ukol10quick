package cz.czechitas.webapp.persistence;

import cz.czechitas.webapp.entity.*;

public interface PexesoRepository {

    public GameBoard findOne(Long id);
    public GameBoard save(GameBoard board);

}

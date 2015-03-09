package com.ws.impl;

import com.bowling.dao.GameDAO;
import com.bowling.dao.LaneDAO;
import com.bowling.dao.ReservationDAO;
import com.bowling.entity.Game;
import com.bowling.entity.Lane;
import com.bowling.entity.Player;
import com.bowling.entity.Reservation;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Akronys on 23/02/2015.
 */
public class ModuleTest {

    @Test
    public void insertScores() {

     /*   Lane lane1 = new Lane("piste_1", true);
        Game game = new Game(10, lane1);

        Player player1 = new Player();
        player1.setId(1);

        Reservation res = new Reservation();
        res.setPlayers(Arrays.asList(new Player[]{player1}));
        res.setGames(Arrays.asList(new Game[]{game}));

        //Score score1 = new Score(1, player1, game);

        new LaneDAO().save(lane1);
        new GameDAO().save(game);
        new ReservationDAO().save(res);
        //new ScoreDAO().save(score1); */

    }
}

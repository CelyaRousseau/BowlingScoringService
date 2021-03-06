package com.ws.impl;

import com.bowling.dao.GameDAO;
import com.bowling.dao.ScoreDAO;
import com.bowling.dao.UserDAO;
import com.bowling.entity.Game;
import com.bowling.entity.Player;
import com.bowling.entity.Score;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ws.ScoringPublisher;
import com.ws.itf.IBowlingScoring;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

public class BowlingScoring implements IBowlingScoring {

    private static  final String SPARE             = "spare";
    private static  final String STRIKE            = "strike";
    private static  final String GENERIC           = "generic";
    private static  final String LASTTURNEXCEPTION = "lastTurnException";


    @Override
    public Response getScoresRanking(int limit) {
        return Response.status(200).entity("2").build();
    }

    @Override
    public Response getScoresByUser(int user_id) {
        return null;
    }

    @Override
    public Response getScoresByGame(int game_id) throws JsonProcessingException {

        ObjectMapper mapper  = new ObjectMapper();
        List<Score> scores   = new ScoreDAO().getScoresByGame(game_id);

        return Response.status(200).entity(mapper.writeValueAsString(scores)).build();
    }

    @Override
    public Response getScoresByGameAndUser(int game_id, int user_id) throws JsonProcessingException {
        return null;
    }

    @Override
    public Response getScoresByGameRoundAndUser(int game_id, int round_id, int user_id) {
        return null;
    }

    @Override
    public Response postScores(String json) throws IOException {

        Scores(json);
        return Response.status(200).entity("2").build();
    }

    public void Scores(String json) throws IOException{
        ObjectMapper mapper = new ObjectMapper();

        int game_id = mapper.readTree(json).get("game_id").asInt();
        int round_id = mapper.readTree(json).get("round_id").asInt();
        int user_id = mapper.readTree(json).get("user_id").asInt();

        int[] scores = new int[3];
        scores[0] = mapper.readTree(json).get("scores").get(0).asInt();
        scores[1] = mapper.readTree(json).get("scores").get(1).asInt();
        scores[2] = isLastTurn(round_id) ?  mapper.readTree(json).get("scores").get(2).asInt() : 0;

        /** TODO
         IN : Scores d'un Round

         1. Mise en base des scores du [Round spécifié -> à modifier via nombre de joueurs]
         2. Vérification qu'il y a des points au round d'avant si il y a un round avant
         2. BIS Vérifier si score du tour précédent est un spare/strike
         3. Calcul points du round d'avant si besoin
         4. Vérification Strike/Spare tour actuel
         5. Calcul des points du tour actuel en fonction de spare/strike ou rien

         OUT : Nouvelle ligne "Score" en bdd avec les points du Round et/ou round précédent

         TODO
         - Exception des Strikes à la suite
         */

        Score currentScores = insertScoresInBase(game_id, round_id, user_id, scores);
        if(havePreviousRound(round_id)){
            Score previousScore = new ScoreDAO().getScoresByGameRoundAndUser(currentScores.getGame().getId(), currentScores.getRound() - 1, currentScores.getPlayer().getId());
            if(!hasPreviousPoints(previousScore)){
                calculateRoundPoints(previousScore, currentScores);
            }
        }

        calculateRoundPoints(currentScores, round_id);

        // Publish message "Scoring have been update" according to lane
        try {
            Game game= new GameDAO().get(Game.class, game_id);
            int lane_id = game.getLane().getId();
            String routing_key = "piste_" + lane_id;
            new ScoringPublisher().publishMessage("Scoring have been update", routing_key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Score insertScoresInBase(int game_id, int round_id, int user_id, int [] scores){
        /** TODO : Appel vers un autre service qui donnerait la partie en cours et le user concerné */
        Player player       = new UserDAO().get(Player.class, user_id);
        Game game           = new GameDAO().get(Game.class, game_id);
        Score currentScores = new Score(round_id, player, game);

        currentScores.setFirstRoll(scores[0]);
        currentScores.setSecondRoll(scores[1]);

        if(isLastTurn(round_id)){
            currentScores.setThirdRoll(scores[2]);
        }

        new ScoreDAO().save(currentScores);

        return currentScores;
    }

    public boolean isLastTurn(int round_id){
        return round_id == 10;
    }

    public  boolean havePreviousRound(int currentRound){
        int previousRound = currentRound - 1;

        return previousRound > 0;
    }

    public boolean hasPreviousPoints(Score currentScore){
        int previousRoundPoint = new ScoreDAO().get(Score.class, currentScore.getId()).getRoundPoints();
        return previousRoundPoint != -1;

    }

    public void calculateRoundPoints(Score currentScore, int round_id){
        int[] scoreByRoll = convertScoresToInt(currentScore);
        int roundPoints;

        if(isSpare(scoreByRoll) || isStrike(scoreByRoll[1])){
            roundPoints = -1;
            if(isLastTurn(round_id)){
                roundPoints = calculateRoundPoints(currentScore, LASTTURNEXCEPTION);
                System.out.println("LastTurn : " + roundPoints);
            }
            System.out.println("roundpoint : " + roundPoints);
        } else {
            roundPoints = calculateRoundPoints(currentScore, GENERIC);
        }

        currentScore.setRoundPoints(roundPoints);
        new ScoreDAO().update(currentScore);

    }

    private int[] convertScoresToInt(Score scores) {
        int[] scoreByRoll = new int[3];
        scoreByRoll[0]    = scores.getFirstRoll();
        scoreByRoll[1]    = scores.getSecondRoll();
        scoreByRoll[2]    = scores.getThirdRoll();

        return scoreByRoll;
    }

    public void calculateRoundPoints(Score previousScores, Score currentScores){
        int[] scoreByRoll = convertScoresToInt(previousScores);
        int roundPoints = 0;

        if(isStrike(scoreByRoll[1])|| isStrike(scoreByRoll[0])){
            roundPoints = calculateRoundPoints(currentScores, STRIKE);
        } else if(isSpare(scoreByRoll)){
            roundPoints = calculateRoundPoints(currentScores, SPARE);
        }

        previousScores.setRoundPoints(roundPoints);
        new ScoreDAO().update(previousScores);
    }

    public int calculateRoundPoints(Score currentScore, String rule){
        int roundPoints   = 0;
        int[] scoreByRoll = convertScoresToInt(currentScore);

        switch (rule){
            case GENERIC:
                roundPoints = scoreByRoll[0] + scoreByRoll[1];
                break;
            case SPARE:
                roundPoints = 10 + scoreByRoll[0];
                break;
            case STRIKE:
                roundPoints = 10 + scoreByRoll[0] + scoreByRoll[1];
                break;
            case LASTTURNEXCEPTION:
                roundPoints = scoreByRoll[0] + scoreByRoll[1] + scoreByRoll[2];
                break;
        }
        return roundPoints;
    }

    /**
     * @param fallenKeels int
     * @return isStrike Boolean
     */
    public  boolean isStrike(int fallenKeels) {
        return fallenKeels == 10;
    }

    /**
     * @param fallenKeels int
     * @return isSpare Boolean
     */
    public  boolean isSpare(int [] fallenKeels) {
      return fallenKeels[0] + fallenKeels[1] == 10;
    }
}

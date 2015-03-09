package com.ws.itf;

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/Scores")
public interface IModule {

    /** ROUTES EN GET **/

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScoresRanking(@QueryParam("limit") int limit);

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScoresByUser(@QueryParam("user_id") int user_id);

    @GET
    @Path("/{game_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScoresByGame(@PathParam("game_id") int game_id) throws JsonProcessingException;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScoresByGameAndUser(@PathParam("game_id") int game_id, @PathParam("user_id") int user_id);

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScoresByGameRoundAndUser(@PathParam("game_id") int game_id, @PathParam("round_id") int round_id, @PathParam("user_id") int user_id);

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postScores(String json) throws IOException;

}

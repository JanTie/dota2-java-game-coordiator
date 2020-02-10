package org.tomass.dota.gc.handlers.callbacks.match;

import java.util.ArrayList;
import java.util.List;

import org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CMsgGCToClientFindTopSourceTVGamesResponse.Builder;
import org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CSourceTVGameSmall.Player;

import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

public class TopSourceTvGamesCallback extends CallbackMsg {

    private List<Game> games = new ArrayList<>();

    public TopSourceTvGamesCallback(Builder builder) {
        for (org.tomass.protobuf.dota.DotaGcmessagesClientWatch.CSourceTVGameSmall.Builder sourceGame : builder
                .getGameListBuilderList()) {
            Game game = new Game();
            game.setAverageMmr(sourceGame.getAverageMmr());
            game.setDireScore(sourceGame.getDireScore());
            game.setRadianScore(sourceGame.getRadiantScore());
            game.setRadiantLead(sourceGame.getRadiantLead());
            game.setLobbyId(sourceGame.getLobbyId());
            game.setMatchId(sourceGame.getMatchId());
            game.setPlayers(sourceGame.getPlayersList());
            games.add(game);
        }
    }

    public List<Game> getGames() {
        return games;
    }

    public void setGames(List<Game> games) {
        this.games = games;
    }

    @Override
    public String toString() {
        return "TopSourceTvGamesCallback [games=" + games + "]";
    }

    public class Game extends CallbackMsg {

        private Integer averageMmr;
        private Long lobbyId;
        private Integer radiantLead;
        private Integer radianScore;
        private Integer direScore;
        private Long matchId;
        private List<Player> players;

        public Integer getAverageMmr() {
            return averageMmr;
        }

        public void setAverageMmr(Integer averageMmr) {
            this.averageMmr = averageMmr;
        }

        public Long getLobbyId() {
            return lobbyId;
        }

        public void setLobbyId(Long lobbyId) {
            this.lobbyId = lobbyId;
        }

        public Integer getRadiantLead() {
            return radiantLead;
        }

        public void setRadiantLead(Integer radiantLead) {
            this.radiantLead = radiantLead;
        }

        public Integer getRadianScore() {
            return radianScore;
        }

        public void setRadianScore(Integer radianScore) {
            this.radianScore = radianScore;
        }

        public Integer getDireScore() {
            return direScore;
        }

        public void setDireScore(Integer direScore) {
            this.direScore = direScore;
        }

        public Long getMatchId() {
            return matchId;
        }

        public void setMatchId(Long matchId) {
            this.matchId = matchId;
        }

        public List<Player> getPlayers() {
            return players;
        }

        public void setPlayers(List<Player> players) {
            this.players = players;
        }

        @Override
        public String toString() {
            return "Game [averageMmr=" + averageMmr + ", lobbyId=" + lobbyId + ", radiantLead=" + radiantLead
                    + ", radianScore=" + radianScore + ", direScore=" + direScore + ", matchId=" + matchId
                    + ", players=" + players + "]";
        }

    }
}

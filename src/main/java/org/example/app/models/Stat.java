package org.example.app.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class Stat {
    private int elo = 100;
    private int wins;
    private int draws;

    @JsonIgnore
    User user;
    @JsonIgnore
    private int total;

    // Only used at server start as db helper
    @JsonIgnore
    String fkUserId;

    public Stat(String fkUserId, int elo, int wins, int draws, int total) {
        this.fkUserId = fkUserId;
        this.elo = elo;
        this.wins = wins;
        this.draws = draws;
        this.total = total;
    }

    public Stat(User user) {
        this.user = user;
    }

    @JsonProperty("name")
    private String getName() {
        return user.getName();
    }

    @JsonProperty("losses")
    public int getDefeats() {
        return total - wins - draws;
    }

    @JsonProperty("winRate")
    private String getWinRate() {
        int perc = (int) Math.round((double) wins / total * 100);
        return String.format("%d%%", perc);
    }

    public void won() {
        total++;
        wins++;
        elo += 3;
    }

    public void lost() {
        total++;
        elo -= 5;
    }

    public void draw() {
        total++;
        draws++;
    }

    public Stat assignUser(User user) {
        this.user = user;
        fkUserId = null;
        return this;
    }
}

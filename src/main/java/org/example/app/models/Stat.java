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

    public Stat(User user) {
        this.user = user;
    }

    @JsonProperty("name")
    private String getName() {
        return user.getName();
    }
    @JsonProperty("losses")
    private int getDefeats() {
        return total - wins - draws;
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
}

package com.gathin.tvhome;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * One upcoming or live match for a team, from TheSportsDB's free API (thesportsdb.com). Parsing is plain
 * JSON-in, object-out so it is tested on the JVM; the network call that supplies the JSON lives in
 * MainActivity's background fetch (see league_team, config.xml).
 */
final class LeagueMatch {
    final String homeTeam, awayTeam, venue, league;
    /** "" before kickoff, else the API's short code: 1H, HT, 2H, FT, AET, PEN, ... */
    final String status;
    /** null before kickoff. */
    final Integer homeScore, awayScore;
    final Instant kickoff;

    LeagueMatch(String homeTeam, String awayTeam, String venue, String league, String status,
                Integer homeScore, Integer awayScore, Instant kickoff) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.venue = venue;
        this.league = league;
        this.status = status;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.kickoff = kickoff;
    }

    boolean finished() {
        return status.equals("FT") || status.equals("AET") || status.equals("PEN")
                || status.equalsIgnoreCase("Match Finished");
    }

    boolean live() {
        return !status.isEmpty() && !finished() && !status.equals("NS");
    }

    /** The first not-yet-finished event in an eventsnext.php {"events":[...]} response, or null. */
    static LeagueMatch parseNext(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONArray events = root.optJSONArray("events");
            if (events == null) return null;
            for (int i = 0; i < events.length(); i++) {
                JSONObject e = events.optJSONObject(i);
                if (e == null) continue;
                LeagueMatch m = parseEvent(e);
                if (m != null && !m.finished()) return m;
            }
        } catch (Exception ignored) {
            // malformed or unreachable: the panel just stays off
        }
        return null;
    }

    /** The first team id in a searchteams.php {"teams":[...]} response, or null. */
    static String parseTeamId(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONArray teams = root.optJSONArray("teams");
            if (teams == null || teams.length() == 0) return null;
            JSONObject team = teams.optJSONObject(0);
            if (team == null) return null;
            String id = team.optString("idTeam", "").trim();
            return id.isEmpty() ? null : id;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static LeagueMatch parseEvent(JSONObject e) {
        String home = e.optString("strHomeTeam", "").trim();
        String away = e.optString("strAwayTeam", "").trim();
        if (home.isEmpty() || away.isEmpty()) return null;
        String timestamp = e.optString("strTimestamp", "").trim();
        Instant kickoff;
        try {
            // "2026-10-22T08:30:00", UTC with no zone suffix.
            kickoff = Instant.parse(timestamp + "Z");
        } catch (DateTimeParseException ex) {
            return null;
        }
        return new LeagueMatch(home, away,
                e.optString("strVenue", "").trim(), e.optString("strLeague", "").trim(),
                e.optString("strStatus", "").trim(),
                intOrNull(e.optString("intHomeScore", "")), intOrNull(e.optString("intAwayScore", "")),
                kickoff);
    }

    private static Integer intOrNull(String s) {
        try {
            return s.isEmpty() ? null : Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

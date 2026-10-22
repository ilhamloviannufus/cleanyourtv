package com.gathin.tvhome;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A league standings table from TheSportsDB's free API (lookuptable.php): plain JSON-in, object-out,
 * tested on the JVM. The network call that supplies the JSON lives in MainActivity's background fetch.
 */
final class LeagueTable {
    static final class Row {
        final int rank, points;
        final String team;

        Row(int rank, String team, int points) {
            this.rank = rank;
            this.team = team;
            this.points = points;
        }
    }

    private LeagueTable() {
    }

    /** The table from a lookuptable.php {"table":[...]} response, ranked order; empty when unreadable. */
    static List<Row> parse(String json) {
        List<Row> rows = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(json);
            JSONArray table = root.optJSONArray("table");
            if (table == null) return rows;
            for (int i = 0; i < table.length(); i++) {
                JSONObject r = table.optJSONObject(i);
                if (r == null) continue;
                String team = r.optString("strTeam", "").trim();
                if (team.isEmpty()) continue;
                rows.add(new Row(r.optInt("intRank", 0), team, r.optInt("intPoints", 0)));
            }
        } catch (Exception ignored) {
            // malformed or unreachable: the panel just shows no table
        }
        return rows;
    }

    /** The row for this team, by a case-insensitive exact match, or null. */
    static Row find(List<Row> table, String team) {
        for (Row r : table) if (r.team.equalsIgnoreCase(team)) return r;
        return null;
    }

    /** How many rows {@link #around} returns: enough to place the team without crowding the small panel. */
    static final int ROWS = 3;

    /**
     * Up to {@link #ROWS} rows centred on the team's own rank (one above, one below); the top rows when
     * the team is not found. Always in rank order.
     */
    static List<Row> around(List<Row> table, String team) {
        if (table.isEmpty()) return Collections.emptyList();
        Row self = find(table, team);
        int center = self == null ? 0 : table.indexOf(self);
        int start = Math.max(0, Math.min(center - 1, table.size() - ROWS));
        int end = Math.min(table.size(), start + ROWS);
        return table.subList(start, end);
    }
}

package edu.eci.arsw.blueprints.persistence.impl;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistence;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Primary
@Repository
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    private final JdbcTemplate jdbcTemplate;

    public PostgresBlueprintPersistence(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {

        String checkSql = "SELECT COUNT(*) FROM blueprints WHERE author = ? AND name = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, bp.getAuthor(), bp.getName());

        if (count != null && count > 0) {
            throw new BlueprintPersistenceException("Blueprint already exists: " + bp.getAuthor() + "/" + bp.getName());
        }

        String insertBpSql = "INSERT INTO blueprints (author, name) VALUES (?, ?)";
        jdbcTemplate.update(insertBpSql, bp.getAuthor(), bp.getName());

        savePoints(bp);
    }

    private void savePoints(Blueprint bp) {
        String insertPointSql = "INSERT INTO points (blueprint_author, blueprint_name, x, y) VALUES (?, ?, ?, ?)";
        for (Point p : bp.getPoints()) {
            jdbcTemplate.update(insertPointSql, bp.getAuthor(), bp.getName(), p.x(), p.y());
        }
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        String sql = "SELECT * FROM blueprints WHERE author = ? AND name = ?";
        List<Blueprint> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Blueprint bp = new Blueprint(rs.getString("author"), rs.getString("name"), new ArrayList<>());
            return bp;
        }, author, name);

        if (result.isEmpty()) {
            throw new BlueprintNotFoundException("Blueprint not found: " + author + "/" + name);
        }

        Blueprint bp = result.get(0);
        loadPoints(bp);
        return bp;
    }

    private void loadPoints(Blueprint bp) {
        String sql = "SELECT x, y FROM points WHERE blueprint_author = ? AND blueprint_name = ? ORDER BY id";
        List<Point> points = jdbcTemplate.query(sql, (rs, rowNum) -> new Point(rs.getInt("x"), rs.getInt("y")),
                bp.getAuthor(), bp.getName());
        for (Point p : points) {
            bp.addPoint(p);
        }
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        String sql = "SELECT * FROM blueprints WHERE author = ?";
        List<Blueprint> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
            return new Blueprint(rs.getString("author"), rs.getString("name"), new ArrayList<>());
        }, author);

        if (result.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author: " + author);
        }

        Set<Blueprint> blueprints = new HashSet<>();
        for (Blueprint bp : result) {
            loadPoints(bp);
            blueprints.add(bp);
        }
        return blueprints;
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        String sql = "SELECT * FROM blueprints";
        List<Blueprint> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
            return new Blueprint(rs.getString("author"), rs.getString("name"), new ArrayList<>());
        }, (Object[]) null);

        Set<Blueprint> blueprints = new HashSet<>();
        for (Blueprint bp : result) {
            loadPoints(bp);
            blueprints.add(bp);
        }
        return blueprints;
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        getBlueprint(author, name);

        String sql = "INSERT INTO points (blueprint_author, blueprint_name, x, y) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, author, name, x, y);
    }
}

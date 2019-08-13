package com.popokis.popok.sql_db;

import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;

import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public final class Database {

  private final DataSource dataSource;

  private Database(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public static Database create(DataSource dataSource) {
    return new Database(dataSource);
  }

  public long executeInsert(Query query) {
    try (Connection connection = dataSource.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(query.query(), Statement.RETURN_GENERATED_KEYS)) {
      query.parameters(preparedStatement);

      if (preparedStatement.executeUpdate() == 0) throw new RuntimeException("No rows affected");

      try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
        return generateId(resultSet);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public int executeDML(Query query) {
    try (Connection connection = dataSource.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(query.query())) {
      query.parameters(preparedStatement);
      return preparedStatement.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> Optional<T> executeQuery(Query query, JdbcMapper<T> mapper) {
    CachedRowSet cachedRowSet;
    ResultSetWrappingSqlRowSet rowSet;

    try (Connection connection = dataSource.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(query.query())) {
      query.parameters(preparedStatement);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.isBeforeFirst()) {
          cachedRowSet = RowSetProvider.newFactory().createCachedRowSet();
          cachedRowSet.populate(resultSet);
        } else {
          return Optional.empty();
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    // In order to use aliases, ResultSetWrappingSqlRowSet is necessary. This class comes from spring-jdbc dependency.
    rowSet = new ResultSetWrappingSqlRowSet(cachedRowSet);
    // here we move the cursor one step
    rowSet.next();
    return mapper.map(rowSet);
  }

  private long generateId(ResultSet generatedKeys) throws SQLException {
    if (generatedKeys.next()) {
      return generatedKeys.getLong(1);
    } else {
      throw new RuntimeException("DML failed, no ID obtained.");
    }
  }
}

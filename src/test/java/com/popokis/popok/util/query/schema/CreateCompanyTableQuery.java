package com.popokis.popok.util.query.schema;

import com.popokis.popok.data.Query;

import java.sql.PreparedStatement;

public final class CreateCompanyTableQuery implements Query {

  @Override
  public String query() {
    return "CREATE TABLE IF NOT EXISTS company (" +
        "c_id INT UNSIGNED NOT NULL AUTO_INCREMENT, " +
        "c_name VARCHAR(255) NOT NULL, " +
        "PRIMARY KEY (c_id))";
  }

  @Override
  public void parameters(PreparedStatement stm) {

  }
}

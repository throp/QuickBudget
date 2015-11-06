package com.bennorthrop.quickbudget;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class CategoryMonth {

	BigDecimal total = BigDecimal.ZERO;

	String categoryCode;

	String month;

	public static class CategoryMonthMapper implements ResultSetMapper<CategoryMonth> {

		@Override
		public CategoryMonth map(int index, ResultSet r, StatementContext ctx) throws SQLException {
			CategoryMonth cm = new CategoryMonth();
			cm.total = r.getBigDecimal("total");
			cm.categoryCode = r.getString("category_code");
			cm.month = r.getString("month");
			return cm;
		}

	}

}

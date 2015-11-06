package com.bennorthrop.quickbudget;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class Transaction {

	String id;
	String name;
	BigDecimal amount;
	Date postedDate;
	Date userDate;
	String categoryCode;
	TransactionType transactionType;

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Date getPostedDate() {
		return postedDate;
	}

	public Date getUserDate() {
		return userDate;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public TransactionType getTransactionType() {
		return transactionType;
	}

	public static class TransactionMapper implements ResultSetMapper<Transaction> {

		@Override
		public Transaction map(int index, ResultSet r, StatementContext ctx) throws SQLException {
			Transaction tx = new Transaction();
			tx.id = r.getString("id");
			tx.amount = r.getBigDecimal("amount");
			tx.categoryCode = r.getString("category_code");
			tx.name = r.getString("name");
			tx.postedDate = r.getDate("posted_dt");
			tx.userDate = r.getDate("user_dt");
			tx.transactionType = TransactionType.valueOf(r.getString("transaction_type"));
			return tx;
		}
	}

	@Override
	public String toString() {
		return "Transaction [id=" + id + ", name=" + name + ", amount=" + amount + ", postedDate=" + postedDate
				+ ", userDate=" + userDate + ", category=" + categoryCode + ", transactionType=" + transactionType
				+ "]";
	}

}

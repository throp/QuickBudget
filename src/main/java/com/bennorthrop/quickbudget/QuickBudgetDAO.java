package com.bennorthrop.quickbudget;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import com.bennorthrop.quickbudget.CategoryMonth.CategoryMonthMapper;
import com.bennorthrop.quickbudget.Transaction.TransactionMapper;

@RegisterMapper(TransactionMapper.class)
public interface QuickBudgetDAO {

	@SqlUpdate("create table if not exists transaction (id varchar(100) primary key, name varchar(100), amount decimal, posted_dt date, user_dt date, category_code varchar(3), transaction_type varchar(10))")
	void createTransactionTable();

	@SqlUpdate("insert into transaction (id, name, amount, posted_dt, user_dt, category_code, transaction_type) values (:id, :name, :amount, :postedDate, :userDate, :categoryCode, :transactionType)")
	void insertTransaction(@BindBean Transaction transaction);

	@SqlQuery("select id, name, amount, posted_dt, user_dt, category_code, transaction_type from transaction where id = :id")
	Transaction findById(@Bind("id") String id);

	@SqlQuery("select id, name, amount, posted_dt, user_dt, category_code, transaction_type from transaction")
	List<Transaction> findAllTransactions();

	@SqlQuery("select category_code, sum(amount) as total, monthname(posted_dt) as month from transaction group by category_code, monthname(posted_dt)")
	@Mapper(CategoryMonthMapper.class)
	List<CategoryMonth> findCategoryMonths();

	@SqlQuery("select id, name, amount, posted_dt, user_dt, category_code, transaction_type from transaction where monthname(posted_dt) = :month and category_code = :categoryCode order by posted_dt")
	List<Transaction> findByMonthAndCategory(@Bind("month") String month, @Bind("categoryCode") String categoryCode);

	@SqlQuery("select id, name, amount, posted_dt, user_dt, category_code, transaction_type from transaction where category_code = 'UN' and monthname(posted_dt) = :month")
	List<Transaction> findUncategorized(@Bind("month") String month);

	@SqlUpdate("update transaction set category_code = :categoryCode where id = :id")
	void updateCategory(@Bind("id") String id, @Bind("categoryCode") String categoryCode);
}

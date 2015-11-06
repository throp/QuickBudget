package com.bennorthrop.quickbudget;

import static com.bennorthrop.quickbudget.CategoryType.Expense;
import static com.bennorthrop.quickbudget.CategoryType.Ignore;
import static com.bennorthrop.quickbudget.CategoryType.Income;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;

import org.h2.jdbcx.JdbcConnectionPool;
import org.skife.jdbi.v2.DBI;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class QuickBudget {

	final ObjectMapper mapper = new ObjectMapper();

	final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd YYYY");

	final QuickBudgetDAO dao;

	final TransactionParser txParser = new TransactionParser();

	final int IMPORT_TRANSACTIONS = 1;

	final int CATEGORIZE_TRANSACTIONS = 2;

	final int VIEW_TRANSACTIONS = 3;

	final int VIEW_BUDGET = 4;

	final int EXIT = 5;

	Map<String, Category> categories;

	Map<Integer, String> months = new HashMap<>();

	List<Rule> rules;

	public static void main(String args[]) throws Exception {
		new QuickBudget().start();
	}

	protected QuickBudget() {
		DataSource ds = JdbcConnectionPool.create("jdbc:h2:~/Code/Personal/QuickBudget/quickbudget", "sa", "");
		DBI dbi = new DBI(ds);
		dao = dbi.open(QuickBudgetDAO.class);
	}

	void start() throws Exception {
		System.out.println("Welcome to Quick Budget");

		init();
		loop();
	}

	void loop() throws Exception {
		switch (promptMainMenu()) {
		case IMPORT_TRANSACTIONS:
			importTransactions();
			break;
		case CATEGORIZE_TRANSACTIONS:
			categorizeTransactions();
			break;
		case VIEW_TRANSACTIONS:
			viewTransactions();
			break;
		case VIEW_BUDGET:
			viewBudget();
			break;
		case EXIT:
			System.exit(1);
			break;
		default:
			break;
		}
		loop();
	}

	void categorizeTransactions() {
		String month = promptMonth();

		List<Transaction> txs = dao.findUncategorized(month);
		if (txs.isEmpty()) {
			System.out.println("\nNo uncategorized transactions");
		}
		for (Transaction tx : txs) {
			tx.categoryCode = deriveCategory(tx);
			dao.updateCategory(tx.id, tx.categoryCode);
		}
		System.out.println("\nDone");
	}

	void viewBudget() {
		printFunction("View Budget");

		System.out.printf("  %20s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s%10s", "", "January", "February", "March",
				"April", "May", "June", "July", "August", "September", "October", "November", "December");
		printRowHeader("\n------");
		List<CategoryMonth> cms = dao.findCategoryMonths();

		printCategoryType(Income);
		printBudgetRow(Income, cms);
		printTotalRow(Income, cms);

		printCategoryType(Expense);
		printBudgetRow(Expense, cms);
		printTotalRow(Expense, cms);

		printCategoryType(Ignore);
		printBudgetRow(Ignore, cms);
		printTotalRow(Ignore, cms);

		promptToContinue();

	}

	private void printCategoryType(CategoryType type) {
		System.out.println(type.name());
	}

	private void printRowHeader(String str) {
		System.out.println(str
				+ "----------------------------------------------------------------------------------------------------------------------------------------");
	}

	void printTotalRow(CategoryType type, List<CategoryMonth> cms) {
		BigDecimal jan = totalPerType(type, "January", cms);
		BigDecimal feb = totalPerType(type, "February", cms);
		BigDecimal mar = totalPerType(type, "March", cms);
		BigDecimal apr = totalPerType(type, "April", cms);
		BigDecimal may = totalPerType(type, "May", cms);
		BigDecimal jun = totalPerType(type, "June", cms);
		BigDecimal jul = totalPerType(type, "July", cms);
		BigDecimal aug = totalPerType(type, "August", cms);
		BigDecimal sep = totalPerType(type, "September", cms);
		BigDecimal oct = totalPerType(type, "October", cms);
		BigDecimal nov = totalPerType(type, "November", cms);
		BigDecimal dec = totalPerType(type, "December", cms);
		System.out.printf("  %-20s%10.2f%10.2f%10.2f%10.2f%10.2f%10.2f%10.2f%10.2f%10.2f%10.2f%10.2f%10.2f\n", "Total",
				jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec);
	}

	BigDecimal totalPerType(final CategoryType type, String month, List<CategoryMonth> cms) {

		if (cms == null) {
			return BigDecimal.ZERO;
		}

		return cms.stream()
				.filter(cm -> categories.get(cm.categoryCode) != null && categories.get(cm.categoryCode).type == type
						&& cm.month.equals(month))
				.collect(Collectors.toList()).stream().map(cm -> cm.total).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	void printBudgetRow(CategoryType type, List<CategoryMonth> cms) {
		List<Category> cats = categories.values().stream().filter(c -> c.type == type).collect(Collectors.toList());
		for (Category category : cats.stream().sorted((c1, c2) -> Integer.compare(c1.getOrder(), c2.getOrder()))
				.collect(Collectors.toList())) {
			BigDecimal jan = total("January", category.code, cms);
			BigDecimal feb = total("February", category.code, cms);
			BigDecimal mar = total("March", category.code, cms);
			BigDecimal apr = total("April", category.code, cms);
			BigDecimal may = total("May", category.code, cms);
			BigDecimal jun = total("June", category.code, cms);
			BigDecimal jul = total("July", category.code, cms);
			BigDecimal aug = total("August", category.code, cms);
			BigDecimal sep = total("September", category.code, cms);
			BigDecimal oct = total("October", category.code, cms);
			BigDecimal nov = total("November", category.code, cms);
			BigDecimal dec = total("December", category.code, cms);
			System.out.printf("  %-20s%10.2f%10.2f%10.2f%10.2f%10.2f%10.2f%10.2f%10.2f%10.2f%10.2f%10.2f%10.2f\n",
					category.name, jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec);
		}
	}

	BigDecimal total(final String month, String categoryCode, List<CategoryMonth> cms) {
		if (cms == null) {
			return BigDecimal.ZERO;
		}

		return cms.stream()
				.filter(cm -> cm.month.equals(month) && cm.categoryCode != null && cm.categoryCode.equals(categoryCode))
				.findFirst().orElse(new CategoryMonth()).total;
	}

	void viewTransactions() {
		printFunction("View Transactions");

		Category category = promptCategory();
		String month = promptMonth();

		printEmptyLine();
		printTransactionHeader();
		dao.findByMonthAndCategory(month, category.code).stream().forEach(tx -> printTransaction(tx));
		promptToContinue();
	}

	void importTransactions() throws ParseException, XMLStreamException, IOException {
		printFunction("Import Transactions");
		Files.walk(Paths.get("./import/")).forEach(filePath -> {
			if (Files.isRegularFile(filePath)) {
				try {
					System.out.println("\nFile: " + filePath);
					txParser.readTransactions(filePath.toFile()).stream().forEach(tx -> addTransaction(tx));
				} catch (Exception e) {
					System.err.println(e);
					System.exit(1);
				}
			}
		});

	}

	void addTransaction(Transaction tx) {
		if (dao.findById(tx.id) == null) {
			tx.categoryCode = deriveCategory(tx);
			dao.insertTransaction(tx);

		}
	}

	private String deriveCategory(Transaction tx) {

		String defaultCategoryCode = null;
		if (tx.categoryCode == null) {
			for (Rule rule : rules) {
				if (tx.name.contains(rule.getSearchString())) {
					tx.categoryCode = rule.getCategoryCode();
					defaultCategoryCode = rule.getCategoryCode();
					break;
				}
			}
		}

		printEmptyLine();
		printTransaction(tx);
		printEmptyLine();

		printEmptyLine();

		Category category = promptCategory();

		if (category != null) {
			return category.code;
		}

		return defaultCategoryCode == null ? "UN" : defaultCategoryCode;
	}

	String promptMonth() {
		System.out.println("\nEnter a month ('1' for January, etc.):");
		try {
			Integer monthNum = Integer.parseInt(System.console().readLine());

			String monthName = months.get(monthNum);
			if (monthName == null) {
				System.out.println("\nInvalid entry");
				return promptMonth();
			}

			return monthName;
		} catch (NumberFormatException e) {
			System.out.println("\nInvalid entry");
			return promptMonth();
		}
	}

	void promptToContinue() {
		System.out.println("\nPress any key to continue");
		System.console().readLine();
	}

	int promptMainMenu() {
		System.out.println("\n");
		System.out.println("Main Menu:");
		System.out.println("  1: Import Transactions");
		System.out.println("  2: Categorize Transactions");
		System.out.println("  3: View Transactions");
		System.out.println("  4: View Budget");
		System.out.println("  5: Exit");
		System.out.println("\n");

		String in = System.console().readLine();
		try {
			return Integer.parseInt(in);
		} catch (NumberFormatException e) {
			System.out.println("\nInvalid entry\n");
			return promptMainMenu();
		}
	}

	Category promptCategory() {
		System.out.println("Enter Category: ");
		categories.values().stream().forEach(c -> System.out.printf("  %3s: %-30s\n", c.code, c.name));
		String categoryCode = System.console().readLine();
		if (categoryCode == null || "".equals(categoryCode.trim())) {
			return null;
		}

		Category category = categories.get(categoryCode.toUpperCase());
		if (category == null) {
			System.out.println("\nInvalid category!\n");
			return promptCategory();
		}

		return category;
	}

	void printFunction(String function) {
		System.out.println("\n" + function + "\n");
	}

	void printTransactionHeader() {
		System.out.printf("%-14s%-20s%-40s%7s\n", "POSTED", "CATEGORY", "NAME", "AMOUNT");
	}

	void printTransaction(Transaction tx) {
		System.out.printf("%-14s%-20s%-40s%10.2f\n", date(tx.postedDate), categoryName(tx.categoryCode), tx.name,
				tx.amount);
	}

	void printEmptyLine() {
		System.out.println("");
	}

	void init() throws JsonMappingException, JsonParseException, IOException {

		dao.createTransactionTable();

		Categories cs = mapper.readValue(new File("categories.json"), Categories.class);
		categories = cs.getCategories().stream().collect(Collectors.toMap(Category::getCode, Function.identity()));
		rules = categories.values().stream().map(Category::initRules).flatMap(r -> r.stream())
				.collect(Collectors.toList());

		months.put(1, "January");
		months.put(2, "February");
		months.put(3, "March");
		months.put(4, "April");
		months.put(5, "May");
		months.put(6, "June");
		months.put(7, "July");
		months.put(8, "August");
		months.put(9, "September");
		months.put(10, "October");
		months.put(11, "November");
		months.put(12, "December");
	}

	String categoryName(String categoryCode) {
		Category cat = categories.get(categoryCode);
		return cat == null ? "--" : cat.name;
	}

	String date(Date date) {
		return date == null ? "--" : DATE_FORMAT.format(date);
	}
}

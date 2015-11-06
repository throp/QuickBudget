package com.bennorthrop.quickbudget;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class TransactionParser {

	public List<Transaction> readTransactions(File file)
			throws FileNotFoundException, XMLStreamException, ParseException {
		XMLInputFactory factory = XMLInputFactory.newInstance();

		XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream(file));
		List<Transaction> transactions = new ArrayList<>();
		Transaction transaction = null;
		String text = null;
		while (reader.hasNext()) {
			int event = reader.next();
			switch (event) {
			case XMLStreamConstants.START_ELEMENT:
				if ("STMTTRN".equals(reader.getLocalName())) {
					transaction = new Transaction();
				}
				break;
			case XMLStreamConstants.CHARACTERS:
				text = reader.getText().trim();
				break;
			case XMLStreamConstants.END_ELEMENT:
				if ("STMTTRN".equals(reader.getLocalName())) {
					transactions.add(transaction);
				}

				if ("TRNAMT".equals(reader.getLocalName())) {
					transaction.amount = new BigDecimal(text);
				}

				if ("NAME".equals(reader.getLocalName())) {
					transaction.name = text;
				}

				if ("MEMO".equals(reader.getLocalName())) {
					transaction.name += " " + text;
				}

				if ("TRNTYPE".equals(reader.getLocalName())) {
					transaction.transactionType = TransactionType.valueOf(text);
				}

				if ("FITID".equals(reader.getLocalName())) {
					transaction.id = text;
				}

				if ("DTPOSTED".equals(reader.getLocalName())) {
					transaction.postedDate = parseDate(text);
				}

				if ("DTUSER".equals(reader.getLocalName())) {
					transaction.userDate = parseDate(text);
				}
				break;
			}
		}
		return transactions;
	}

	/**
	 * The shittiest way ever to parse a date from text.
	 */
	private Date parseDate(String text) {
		String year = text.substring(0, 4);
		String month = text.substring(4, 6);
		String day = text.substring(6, 8);
		Calendar cal = new GregorianCalendar();
		cal.set(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day), 12, 0, 0);
		return cal.getTime();
	}
}

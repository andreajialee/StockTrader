package assignment3;

import java.util.*;
import java.util.concurrent.*;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.lang.NumberFormatException;
import java.io.IOException;
import java.io.File;

public class PA3 {
	public static Map<String,Semaphore> sem = new HashMap<String, Semaphore>();
	public static ArrayList<String> brokers = new ArrayList<String>();
	/**
	 * Checks if the ticker from CSV file is within the 
	 * company File
	 * @param t
	 * @param data
	 * @return
	 */
	public static boolean checkTicker(String t, Stocks stocks) {
		return stocks.data.stream().anyMatch((stock) -> {
			return stock.ticker.equals(t);
		});
	}
	/**
	 * Checks if date is valid
	 * @param date
	 * @return
	 * Code from: https://mkyong.com/java/how-to-check-if-date-is-valid-in-java/
	 */
	public static boolean checkDate(String date) {
        boolean valid = false;
        try {
            LocalDate.parse(date,DateTimeFormatter.ofPattern("uuuu-M-d").withResolverStyle(ResolverStyle.STRICT));
            valid = true;
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            valid = false;
        }
        return valid;
    }
     /**
      * Read Stock Json File inputed by user using GSON
      */
	private static Stocks readStockFile(String file) throws IOException, InputMismatchException, JsonParseException {
    	File f =  new File(file);
		Scanner s = new Scanner(f);
		String input = "";
		while(s.hasNext()) {
			input += s.nextLine();
		}
		s.close();
		Gson gson = new Gson();
		Stocks stocks = gson.fromJson(input, Stocks.class);
		// Check JSON File and Stocks
		for(int i=0; i<stocks.data.size(); i++) {
			Stock stock = stocks.data.get(i);
			if(stock.getStockBrokers()  == 0) {
				brokers.add(stock.getTicker());
			}
			if (stock.getName() == null || stock.getTicker() == null || stock.getDescription() == null || stock.getStartDate() == null || stock.getExchangeCode() == null) {
				throw new InputMismatchException("The file " + file + " has empty or missing field(s).");
			}
			else if (stock.getName() == "" || stock.getTicker() == "" || stock.getDescription() == "" || stock.getStartDate() == "" || stock.getExchangeCode() == "") {
				throw new InputMismatchException("The file " + file + " has empty or missing field(s).");
			}
			else if(!checkDate(stocks.data.get(i).getStartDate())) {
				System.out.println("The date " + stocks.data.get(i).getStartDate() + " from " + stocks.data.get(i).getName() + " is not formatted properly. \n");
			}
		}
		// TODO: Check if there are duplicates
		return stocks;
    }

    /**
     * Read Stock Trades CSV File inputed by user
     * @throws IOException 
     * @throws NumberFormatException 
     */
    private static LinkedList<Trade> readScheduleFile(String file, Stocks data) throws NumberFormatException, IOException {
    	LinkedList<Trade> trades = new LinkedList<Trade>();
    	File f =  new File(file);
		Scanner s = new Scanner(f);
		String input = "";
		String regex = "[^\\d\\. ]| \\.|\\.$";
		while(s.hasNext()) {
			input = s.nextLine();
			if(input == "")
				continue;
			String[] line = input.split(",");
			// There are more or less than 3 columns
			if(line.length != 3) {
				throw new ArrayIndexOutOfBoundsException();
			}
			// Ticker doesn't exist in company file
			if(!checkTicker(line[1], data)) {
				System.out.println("The ticker " + line[1] + " does not exist in our company file.");
				throw new InputMismatchException();
			}
			int time = Integer.parseInt(line[0].replaceAll(regex,""));
			// Time is less than 0
			if(line[0].charAt(0) == '-') {
				System.out.println("The file " + file + " has a time less than 0.");
				throw new NumberFormatException();
			}
			int quantity = Integer.parseInt(line[2].replaceAll(regex,""));
			// Change quantity to negative if string is negative
			if(line[2].charAt(0) == '-') {
				quantity = -1 * quantity;
			}
			Trade trade = new Trade(time, line[1], quantity);
			trades.add(trade);
		}
		s.close();
		// Empty csv file
		if (trades == null || trades.size() == 0) {
			System.out.println("The file " + file + " is empty, please add data.\n");
			throw new IOException();
		}
		return trades;
    }


    public static void main(String[] args) throws InterruptedException {
    	Scanner sc = new Scanner(System.in);
    	Stocks stocks = new Stocks();
    	LinkedList<Trade> trades = new LinkedList<Trade>();
    	String companyFile = "";
    	String scheduleFile = "";
    	Boolean accepted = true;
    	
    	while(accepted) {
    		System.out.println("What is the name of the file containing the company information?");
    		companyFile = sc.nextLine();
			System.out.println();
			try {
				stocks = readStockFile(companyFile);
				break;
			}
			catch(InputMismatchException err){
				System.out.println(err.getMessage());
				System.out.println();
			}
			catch(IOException err) {
				System.out.println("The file " + companyFile + " could not be found.\n");
			}
			catch(JsonParseException err) {
				System.out.println("The file " + companyFile + " has mising parameters, cannot be parsed, or has incorrect formatting.\n");
			}
		}
    	accepted = true;
    	while(accepted) {
    		System.out.println("What is the name of the file containing the schedule information?");
    		scheduleFile = sc.nextLine();
			System.out.println();
			try {
				trades = readScheduleFile(scheduleFile, stocks);
				break;
			}
			catch (ArrayIndexOutOfBoundsException err) {
				System.out.println("The file " + scheduleFile + " has too many or too little columns.\n");
			}
			catch(InputMismatchException err){
				System.out.println(err.getMessage());
				System.out.println();
			}
			catch(IOException err) {
				System.out.println("The file " + scheduleFile + " could not be found.\n");
			}
			catch (NumberFormatException err) {
				System.out.println("File " + scheduleFile + " has incorrect formatting with its numeric types.\n");
			}
    	}
    	sc.close();
    	
    	// Starting execution of semaphores
    	System.out.println("Starting execution of program...");
    	// Add stocks from wrapper class and create new semaphores for each stock
    	for(int i=0; i<stocks.data.size(); i++) {
    		Stock stock = stocks.data.get(i);
    		sem.put(stock.getTicker(), new Semaphore(stock.getStockBrokers()));
    	}
    	// Add each trade and execute threads
    	ExecutorService exec = Executors.newFixedThreadPool(trades.size());
    	for(int i=0; i<trades.size(); i++) {
    		Trade trade = trades.get(i);
    		String ticker = trade.ticker;
    		// If there is a company with 0 brokers, we don't execute the trade
    		if(brokers.contains(ticker)) {
    			continue;
    		}
    		exec.execute(new Trade(trade.time, trade.ticker, trade.quantity));
    	}
    	exec.shutdown();
		while (!exec.isTerminated()) {
			Thread.yield();
		}
		System.out.println("All trades completed!");
    }
}

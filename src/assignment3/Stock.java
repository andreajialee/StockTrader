package assignment3;

public class Stock implements Comparable<Stock>{
	String name;
	String ticker;
	String startDate;
	int stockBrokers;
	String description;
	String exchangeCode;
	
	public String getName() {
		return this.name;
	}
	public void setName(String name_) {
		this.name = name_;
	}
	public String getTicker() {
		return this.ticker;
	}
	public void setTicker(String ticker_) {
		this.ticker = ticker_;
	}
	public String getStartDate() {
		return this.startDate;
	}
	public void setStartDate(String startDate_) {
		this.startDate = startDate_;
	}
	public int getStockBrokers() {
		return this.stockBrokers;
	}
	public void setStockBrokers(int stockBrokers_) {
		this.stockBrokers = stockBrokers_;
	}
	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description_) {
		this.description = description_;
	}
	public String getExchangeCode() {
		return this.exchangeCode;
	}
	public void setExchangeCode(String exchangeCode_) {
		this.exchangeCode = exchangeCode_;
	}
	
	public int compareTo(Stock stock) {
		return this.getName().toLowerCase().compareTo(stock.getName().toLowerCase());
	}
}

